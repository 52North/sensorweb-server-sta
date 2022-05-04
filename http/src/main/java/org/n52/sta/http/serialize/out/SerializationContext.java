package org.n52.sta.http.serialize.out;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.svalbard.odata.core.QueryOptionsFactory;

public class SerializationContext<T> {

    private final ObjectMapper requestMapper;

    private final QueryOptions queryOptions;

    private final Class<T> type;

    public static <E> SerializationContext<E> create(ObjectMapper mapperConfig, HttpServletRequest request, Function<QueryOptions, StaSerializer<E>> factory) {
        Objects.requireNonNull(mapperConfig, "mapperConfig must not be null!");
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(factory, "factory must not be null");

        ObjectMapper mapper = mapperConfig.copy();
        QueryOptions queryOptions = parseQueryOptions(request);
        StaSerializer<E> serializer = factory.apply(queryOptions);
        return new SerializationContext<>(queryOptions, mapper, serializer);
    }

    public SerializationContext(QueryOptions queryOptions, ObjectMapper requestMapper, StaSerializer<T> serializer) {
        Objects.requireNonNull(queryOptions, "queryOptions must not be null!");
        Objects.requireNonNull(requestMapper, "requestMapper must not be null!");
        Objects.requireNonNull(serializer, "serializer must not be null");

        this.requestMapper = requestMapper;
        this.queryOptions = queryOptions;
        serializer.registerAt(requestMapper);
        this.type = serializer.getType();
    }

    private static QueryOptions parseQueryOptions(HttpServletRequest request) {
        String queryString = request.getQueryString();
        QueryOptionsFactory factory = new QueryOptionsFactory();
        return Optional.ofNullable(queryString).map(decodeQueryString())
                .map(factory::createQueryOptions)
                .orElse(factory.createDummy());
    }

    private static Function<String, String> decodeQueryString() {
        return query -> {
            try {
                return URLDecoder.decode(query, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Encoding not found!");
            }
        };
    }

    public ObjectWriter createWriter() {
        return requestMapper.writer();
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public Class<T> getType() {
        return type;
    }

}