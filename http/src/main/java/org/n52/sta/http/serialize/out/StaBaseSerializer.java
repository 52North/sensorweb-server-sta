package org.n52.sta.http.serialize.out;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.n52.janmayen.stream.Streams;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;

public abstract class StaBaseSerializer<T> extends StdSerializer<T> implements StaSerializer<T> {

    private final String baseUrl;

    private final transient Optional<Set<String>> selectFilter;

    private final transient Optional<Set<ExpandItem>> expandFilter;

    protected StaBaseSerializer(QueryOptions queryOptions, Class<T> class1) {
        super(class1);
        Objects.requireNonNull(queryOptions, "queryOptions must not be null!");
        this.baseUrl = queryOptions.getBaseURI();
        this.selectFilter = getSelects(queryOptions);
        this.expandFilter = getExpands(queryOptions);
    }

    @Override
    public void registerAt(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(getType(), this);
        mapper.registerModule(module);
    }

    public Class<T> getType() {
        return handledType();
    }

    protected boolean isExpanded(String member) {
        if (!expandFilter.isPresent()) {
            return false;
        }
        Set<ExpandItem> expands = expandFilter.get();
        return Streams.stream(expands).anyMatch(item -> member.equals(item.getPath()));
    }

    protected Optional<QueryOptions> getQueryOptionsForExpanded(String member) {
        if (!isExpanded(member)) {
            return Optional.empty();
        }
        Set<ExpandItem> expands = expandFilter.get();
        return Streams.stream(expands).filter(item -> member.equals(item.getPath())).findFirst()
                .map(ExpandItem::getQueryOptions);
    }

    protected void writeProperty(String name, ThrowingFieldWriter fieldWriter) throws IOException {
        if (selectFilter.isPresent()) {
            Set<String> selects = selectFilter.get();
            if (selects.isEmpty() || selects.contains(name)) {
                fieldWriter.writeIfSelected(name);
            }
        } else {
            // serialize on missing filter
            fieldWriter.writeIfSelected(name);
        }
    }

    protected void writeMemberArray(String member, ThrowingArrayWriter arrayItemWriter) throws IOException {
        if (selectFilter.isPresent()) {
            Set<String> selects = selectFilter.get();
            if (selects.isEmpty() || selects.contains(member)) {
                Optional<QueryOptions> options = getQueryOptionsForExpanded(member);
                arrayItemWriter.writeIfSelected(options);
            }
        } else {
            // serialize on missing filter
            Optional<QueryOptions> options = getQueryOptionsForExpanded(member);
            arrayItemWriter.writeIfSelected(options);
        }
    }

    protected String createSelfLink(String id) {
        return String.format("%s(%s)", baseUrl, id);
    }

    protected String createNavigationLink(String id, String member) {
        return String.format("%s/%s", createSelfLink(id), member);
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

    @FunctionalInterface
    protected interface ThrowingFieldWriter {
        void writeIfSelected(String name) throws IOException;
    }

    @FunctionalInterface
    protected interface ThrowingArrayWriter {
        void writeIfSelected(Optional<QueryOptions> options) throws IOException;
    }


}
