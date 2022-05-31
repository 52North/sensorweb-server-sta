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
package org.n52.sta.data.query.specifications;

import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import java.util.HashMap;
import java.util.Map;

public class HistoricalLocationQuerySpecification implements BaseQuerySpecifications<HistoricalLocationEntity> {

    private final Map<String, MemberFilter<HistoricalLocationEntity>> filterByMember;

    private final Map<String, PropertyComparator<HistoricalLocationEntity, ?>> entityPathByProperty;

    public HistoricalLocationQuerySpecification() {
        this.filterByMember = new HashMap<>();

        this.entityPathByProperty = new HashMap<>();
    }

    @Override
    public Specification<HistoricalLocationEntity> compareProperty(String property, ComparisonOperator operator,
            Expression<?> rightExpr) throws SpecificationsException {
        throw new SpecificationsException("not implemented");
    }

    @Override
    public Specification<HistoricalLocationEntity> compareProperty(Expression<?> leftExpr, ComparisonOperator operator,
            String property) throws SpecificationsException {
        throw new SpecificationsException("not implemented");
    }

    @Override
    public Specification<HistoricalLocationEntity> applyOnMember(String member, Specification<?> memberSpec)
            throws SpecificationsException {
        throw new SpecificationsException("not implemented");
    }

}
