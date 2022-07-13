/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */

package org.n52.sta.api.path;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sta.api.entity.Identifiable;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.n52.svalbard.odata.core.expr.MemberExpr;
import org.n52.svalbard.odata.core.expr.StringValueExpr;
import org.n52.svalbard.odata.core.expr.bool.ComparisonExpr;

public class Request {

    private final Optional<SelectPath< ? extends Identifiable>> selectPath;
    private final QueryOptions queryOptions;

    public Request(SelectPath< ? extends Identifiable> selectPath, QueryOptions queryOptions) {
        Objects.requireNonNull(selectPath, "selectPath must not be null!");
        this.selectPath = Optional.of(selectPath);
        this.queryOptions = queryOptions == null
                ? QueryOptionsFactory.createEmpty()
                : queryOptions;
    }

    private Request(QueryOptions queryOptions) {
        this.queryOptions = queryOptions;
        this.selectPath = Optional.empty();
    }

    /**
     * A Request for an instance with specified id.
     *
     * @param id
     *        the instance's id
     * @return a request to get an instance by its id
     */
    public static Request createIdRequest(String id) {
        MemberExpr leftExpr = new MemberExpr("id");
        StringValueExpr rightExpr = new StringValueExpr(id);
        ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        ComparisonExpr expr = new ComparisonExpr(operator, leftExpr, rightExpr);
        Set<FilterClause> filters = Collections.singleton(new FilterFilter(expr));
        QueryOptions query = QueryOptionsFactory.createQueryOptions(filters);
        return new Request(query);
    }

    /**
     * The request path or Optional.empty() if not present.
     *
     * @return the request path
     */
    public Optional<SelectPath< ? extends Identifiable>> getPath() {
        return selectPath;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public boolean isRefRequest() {
        return selectPath.map(SelectPath::isRef)
                         .orElse(false);
    }

}
