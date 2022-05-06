package org.n52.sta.http.serialize.out;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.n52.janmayen.stream.Streams;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.http.controller.RequestContext;

public class SerializationContext {

    private final String serviceUri;

    private final QueryOptions queryOptions;

    private final ObjectMapper mapper;

    private final Optional<Set<String>> selectFilter;

    private final Optional<Set<ExpandItem>> expandFilter;

    SerializationContext(String serviceUri, QueryOptions queryOptions, ObjectMapper mapper) {
        Objects.requireNonNull(serviceUri, "serviceUri must not be null!");
        Objects.requireNonNull(queryOptions, "queryOptions must not be null!");
        Objects.requireNonNull(mapper, "mapper must not be null");

        this.serviceUri = serviceUri;
        this.queryOptions = queryOptions;
        this.mapper = mapper;

        this.selectFilter = getSelects(queryOptions);
        this.expandFilter = getExpands(queryOptions);
    }

    public static SerializationContext create(RequestContext requestContext, ObjectMapper mapperConfig) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(mapperConfig, "mapperConfig must not be null!");

        ObjectMapper mapper = mapperConfig.copy();
        String serviceUri = requestContext.getServiceUri();
        QueryOptions queryOptions = requestContext.getQueryOptions();
        return new SerializationContext(serviceUri, queryOptions, mapper);
    }

    public static SerializationContext create(SerializationContext otherContext, QueryOptions queryOptions) {
        Objects.requireNonNull(otherContext, "otherContext must not be null");
        ObjectMapper mapper = otherContext.mapper;
        String serviceUri = otherContext.serviceUri;
        return new SerializationContext(serviceUri, queryOptions, mapper);
    }

    /**
     * Registers specified serializer at the context's object mapper.
     * 
     * @param serializer the serializer to register
     */
    public <T> void register(StaBaseSerializer<T> serializer) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(serializer.getType(), serializer);
        mapper.registerModule(module);
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public ObjectWriter createWriter() {
        return mapper.writer();
    }

    public boolean isSelected(String name) {
        return selectFilter.map(selects -> selects.isEmpty() || selects.contains(name)).orElse(true);
    }

    public boolean isExpanded(String member) {
        if (!expandFilter.isPresent()) {
            return false;
        }
        Set<ExpandItem> expands = expandFilter.get();
        return Streams.stream(expands).anyMatch(item -> member.equals(item.getPath()));
    }

    public Optional<QueryOptions> getQueryOptionsForExpanded(String member) {
        if (!isExpanded(member)) {
            return Optional.empty();
        }
        Set<ExpandItem> expands = expandFilter.get();
        return Streams.stream(expands).filter(item -> member.equals(item.getPath())).findFirst()
                .map(ExpandItem::getQueryOptions);
    }

    private Optional<Set<ExpandItem>> getExpands(QueryOptions queryOptions) {
        Optional<ExpandFilter> optionalFilter = getExpandFilter(queryOptions);
        return optionalFilter.map(ExpandFilter::getItems);
    }

    private Optional<ExpandFilter> getExpandFilter(QueryOptions queryOptions) {
        return queryOptions != null
                ? Optional.ofNullable(queryOptions.getExpandFilter())
                : Optional.empty();
    }

    private Optional<Set<String>> getSelects(QueryOptions queryOptions) {
        Optional<SelectFilter> optionalFilter = getSelectFilter(queryOptions);
        return optionalFilter.map(SelectFilter::getItems);
    }

    private Optional<SelectFilter> getSelectFilter(QueryOptions queryOptions) {
        return queryOptions != null
                ? Optional.ofNullable(queryOptions.getSelectFilter())
                : Optional.empty();
    }

}