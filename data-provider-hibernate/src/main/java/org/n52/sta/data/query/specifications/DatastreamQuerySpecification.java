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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.Expression;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.springframework.data.jpa.domain.Specification;

public class DatastreamQuerySpecification implements BaseQuerySpecifications<AbstractDatasetEntity> {

    private final Map<String, MemberFilter<AbstractDatasetEntity>> filterByMember;

    private final Map<String, PropertyComparator<AbstractDatasetEntity, ?>> entityPathByProperty;
    
    public DatastreamQuerySpecification() {
        this.filterByMember = new HashMap<>();

        this.entityPathByProperty = new HashMap<>();

        // TODO maybe we should refactor the above maps to a super class
        //
        // interface -> abstract class
        // define and add specifications within constructor
        // ThingQuerySpecification has some implementation
        // which would move to the base class then
        // 
        // it is possible that some interface methods can
        // become private then and the subclasses are there
        // just to define property/member comparator specs 

    }

    @Override
    public Specification<AbstractDatasetEntity> compareProperty(String property, ComparisonOperator operator,
            Expression<?> rightExpr) throws SpecificationsException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Specification<AbstractDatasetEntity> compareProperty(Expression<?> leftExpr, ComparisonOperator operator,
            String property) throws SpecificationsException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Specification<AbstractDatasetEntity> applyOnMember(String member, Specification<?> memberSpec)
            throws SpecificationsException {
        // TODO Auto-generated method stub
        return null;
    }
    
}
