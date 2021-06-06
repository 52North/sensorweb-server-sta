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

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DescribableEntity;
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
public class ObservationRelationQuerySpecifications extends EntityQuerySpecifications<ObservationRelationEntity> {

    public static Specification<ObservationRelationEntity> withGroupStaIdentifier(final String groupIdentifier) {
        return (root, query, builder) -> {
            final Join<ObservationGroupEntity, ObservationRelationEntity> join =
                root.join(ObservationRelationEntity.PROPERTY_GROUP, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), groupIdentifier);
        };
    }

    public static Specification<ObservationRelationEntity> withSubjectStaIdentifier(
        final String observationIdentifier) {
        return (root, query, builder) -> {
            final Join<ObservationRelationEntity, DataEntity<?>> join =
                root.join(ObservationRelationEntity.PROPERTY_SUBJECT, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), observationIdentifier);
        };
    }

    public static Specification<ObservationRelationEntity> withObjectStaIdentifier(
        final String observationIdentifier) {
        return (root, query, builder) -> {
            final Join<ObservationRelationEntity, DataEntity<?>> join =
                root.join(ObservationRelationEntity.PROPERTY_OBJECT, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), observationIdentifier);
        };
    }

    @Override protected Specification<ObservationRelationEntity> handleRelatedPropertyFilter(
        String propertyName,
        Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (StaConstants.OBSERVATION_GROUP.equals(propertyName)) {
                Subquery<ObservationRelationEntity> sq = query.subquery(ObservationRelationEntity.class);
                Root<ObservationGroupEntity> obsGroup = sq.from(ObservationGroupEntity.class);
                sq.select(obsGroup.get(ObservationGroupEntity.ID))
                    .where(((Specification<ObservationGroupEntity>) propertyValue).toPredicate(obsGroup,
                                                                                               query,
                                                                                               builder));
                return builder.in(root.get(ObservationRelationEntity.PROPERTY_GROUP)).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override protected Specification<ObservationRelationEntity> handleDirectPropertyFilter(
        String propertyName,
        Expression<?> propertyValue,
        FilterConstants.ComparisonOperator operator,
        boolean switched) {
        return (Specification<ObservationRelationEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        return handleDirectStringPropertyFilter(root.get(ObservationRelationEntity.STA_IDENTIFIER),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                false);
                    case StaConstants.PROP_ROLE:
                        return handleDirectStringPropertyFilter(root.get(ObservationRelationEntity.PROPERTY_ROLE),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    case StaConstants.PROP_DESCRIPTION:
                        return handleDirectStringPropertyFilter(
                            root.get(ObservationRelationEntity.PROPERTY_DESCRIPTION),
                            propertyValue,
                            operator,
                            builder,
                            switched);
                    case "namespace":
                        return handleDirectStringPropertyFilter(root.get(ObservationRelationEntity.PROPERTY_NAMESPACE),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    default:
                        throw new RuntimeException("Error getting filter for Property: \"" + propertyName
                                                       + "\". No such property in Entity.");

                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
