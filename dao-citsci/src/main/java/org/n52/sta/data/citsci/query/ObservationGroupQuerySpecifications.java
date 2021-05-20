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

package org.n52.sta.data.citsci.query;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.observationgroup.ObservationGroupParameterEntity;
import org.n52.series.db.beans.sta.ObservationGroupEntity;
import org.n52.series.db.beans.sta.ObservationRelationEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.data.vanilla.query.EntityQuerySpecifications;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationGroupQuerySpecifications extends EntityQuerySpecifications<ObservationGroupEntity> {

    public Specification<ObservationGroupEntity> withRelationStaIdentifier(final String relationIdentifier) {
        return (root, query, builder) -> {
            final Join<ObservationGroupEntity, ObservationRelationEntity> join =
                root.join(ObservationGroupEntity.PROP_ENTITIES, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), relationIdentifier);
        };
    }

    @Override protected Specification<ObservationGroupEntity> handleRelatedPropertyFilter(
        String propertyName,
        Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (StaConstants.OBSERVATION_RELATIONS.equals(propertyName)) {
                Subquery<ObservationGroupEntity> sq = query.subquery(ObservationGroupEntity.class);
                Root<ObservationRelationEntity> obsRelation = sq.from(ObservationRelationEntity.class);
                sq.select(obsRelation.get(ObservationRelationEntity.PROPERTY_GROUP))
                    .where(((Specification<ObservationRelationEntity>) propertyValue).toPredicate(obsRelation,
                                                                                                  query,
                                                                                                  builder));
                return builder.in(root.get(ObservationGroupEntity.ID)).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override protected Specification<ObservationGroupEntity> handleDirectPropertyFilter(
        String propertyName,
        Expression<?> propertyValue,
        FilterConstants.ComparisonOperator operator,
        boolean switched) {
        return (Specification<ObservationGroupEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        return handleDirectStringPropertyFilter(root.get(ObservationGroupEntity.STA_IDENTIFIER),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                false);
                    case StaConstants.PROP_NAME:
                        return handleDirectStringPropertyFilter(root.get(ObservationGroupEntity.NAME),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    case StaConstants.PROP_DESCRIPTION:
                        return handleDirectStringPropertyFilter(root.get(ObservationGroupEntity.DESCRIPTION),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    default:
                        // We are filtering on variable keys on properties
                        if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                            return handleProperties(root,
                                                    query,
                                                    builder,
                                                    propertyName,
                                                    propertyValue,
                                                    operator,
                                                    switched,
                                                    ObservationGroupParameterEntity.PROP_OBS_GROUP_ID,
                                                    ParameterFactory.EntityType.OBS_GROUP);
                        } else {
                            throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                        }
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
