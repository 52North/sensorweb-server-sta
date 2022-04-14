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
package org.n52.sta.data;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Test;
import org.mockito.Mock;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.data.support.FilterExprVisitor;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.n52.svalbard.odata.core.expr.Expr;
import org.springframework.data.jpa.domain.Specification;

public class ODataQueryTest {

    @Mock(serializable = true)
    Root<AbstractDatasetEntity> root;
    @Mock(serializable = true)
    CriteriaQuery<?> query;
    @Mock(serializable = true)
    CriteriaBuilder builder;

    //@Test
    public void test() {

        QueryOptions options = createQueryOptions("$filter=result mod 2 eq 0");
        Specification<AbstractDatasetEntity> specification = (root, query, builder) -> {
            try  {
                Predicate jpaExpression = extracted(options, root, query, builder);
                return builder.and(jpaExpression);
            } catch (STAInvalidQueryException e) {
                return builder.isFalse(builder.literal(false));
            }
        };

        Predicate predicate = specification.toPredicate(root, query, builder);


    }

    private <T> Predicate extracted(QueryOptions options, Root<T> root, CriteriaQuery<?> query,
            CriteriaBuilder builder) throws STAInvalidQueryException {
        FilterFilter filter = options.getFilterFilter();
        Expr filterExpression = (Expr) filter.getFilter();
        FilterExprVisitor<T> visitor = createVisitor(root, query, builder);
        return (Predicate) filterExpression.accept(visitor);
    }

    private <T> FilterExprVisitor<T> createVisitor(Root<T> root, CriteriaQuery<?> query,
            CriteriaBuilder builder) throws STAInvalidFilterExpressionException {
        return new FilterExprVisitor<T>(root, query, builder);
    }

    private QueryOptions createQueryOptions(String query) {
        return new QueryOptionsFactory().createQueryOptions(query);
    }

}
