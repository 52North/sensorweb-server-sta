package org.n52.sta.api.path;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;

public class Request {

    private final Path path;
    private final QueryOptions queryOptions;

    public Request(Path path, QueryOptions queryOptions) {
        this.path = path;
        this.queryOptions = queryOptions;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public Path getPath() {
        return path;
    }
}
