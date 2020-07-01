/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
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

package org.n52.sta.data.query;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.sta.mapped.extension.CSObservation;
import org.n52.series.db.beans.sta.mapped.extension.ObservationRelation;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationRelationQuerySpecifications extends EntityQuerySpecifications<ObservationRelation> {

    public Specification<ObservationRelation> withGroupStaIdentifier(final String groupIdentifier) {
        return (root, query, builder) -> {
            final Join<ObservationRelation, ObservationRelation> join =
                    root.join(ObservationRelation.PROPERTY_GROUP, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), groupIdentifier);
        };
    }

    public Specification<ObservationRelation> withCSObservationStaIdentifier(final String observationIdentifier) {
        return (root, query, builder) -> {
            final Join<ObservationRelation, CSObservation> join =
                    root.join(ObservationRelation.PROPERTY_OBSERVATION, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), observationIdentifier);
        };
    }

    @Override protected Specification<ObservationRelation> handleRelatedPropertyFilter(String propertyName,
                                                                                       Specification<?> propertyValue) {
        return (root, query, builder) -> {
            try {
                switch (propertyName) {
                default:
                    throw new STAInvalidFilterExpressionException("Could not find related property: " + propertyName);
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override protected Specification<ObservationRelation> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<ObservationRelation>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                default:
                    throw new STAInvalidFilterExpressionException("Error getting filter for Property: \"" + propertyName
                                                                          + "\". No such property in Entity.");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
