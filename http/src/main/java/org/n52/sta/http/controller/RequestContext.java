package org.n52.sta.http.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.svalbard.odata.core.QueryOptionsFactory;

public class RequestContext {
    
    private final String serviceUri;

    private final QueryOptions queryOptions;

    public static RequestContext create(String serviceUri, HttpServletRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        QueryOptions queryOptions = parseQueryOptions(request);

        // TODO parse StaPath

        return new RequestContext(serviceUri, queryOptions);
    }
    
    RequestContext(String serviceUri, QueryOptions queryOptions) {
        Objects.requireNonNull(serviceUri, "serviceUri must not be null");
        Objects.requireNonNull(queryOptions, "request must not be null");
        this.serviceUri = serviceUri;
        this.queryOptions = queryOptions;
    }

    private static QueryOptions parseQueryOptions(HttpServletRequest request) {
        String queryString = request.getQueryString();
        QueryOptionsFactory factory = new QueryOptionsFactory();
        return Optional.ofNullable(queryString).map(decodeQueryString())
                .map(factory::createQueryOptions)
                .orElse(factory.createEmpty());
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

    public String getServiceUri() {
        return serviceUri;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

}
