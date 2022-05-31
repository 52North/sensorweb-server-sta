package org.n52.sta.api.path;

import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.n52.svalbard.odata.core.expr.MemberExpr;
import org.n52.svalbard.odata.core.expr.StringValueExpr;
import org.n52.svalbard.odata.core.expr.bool.ComparisonExpr;

import java.util.Collections;
import java.util.Optional;

public class Request {

    private final Optional<Path> path;
    private final QueryOptions queryOptions;

    public Request(String id) {
        path = Optional.empty();
        queryOptions = QueryOptionsFactory.createQueryOptions(
                Collections.singleton(new FilterFilter(
                        new ComparisonExpr(FilterConstants.ComparisonOperator.PropertyIsEqualTo,
                                new MemberExpr("id"),
                                new StringValueExpr(id))
                ))
        );
    }

    public Request(Path path, QueryOptions queryOptions) {
        this.path = Optional.ofNullable(path);
        this.queryOptions = queryOptions;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public Optional<Path> getPath() {
        return path;
    }
}
