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

package org.n52.sta.plus.persistence.query;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.observationgroup.ObservationGroupParameterEntity;
import org.n52.series.db.beans.sta.plus.GroupEntity;
import org.n52.series.db.beans.sta.plus.LicenseEntity;
import org.n52.series.db.beans.sta.plus.RelationEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.data.old.query.EntityQuerySpecifications;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationGroupQuerySpecifications extends EntityQuerySpecifications<GroupEntity> {

    public static Specification<GroupEntity> withRelationStaIdentifier(final String relationIdentifier) {
        return (root, query, builder) -> {
            final Join<GroupEntity, RelationEntity> join = root.join(GroupEntity.PROP_RELATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), relationIdentifier);
        };
    }

    public static Specification<GroupEntity> withLicenseStaIdentifier(final String relationIdentifier) {
        return (root, query, builder) -> {
            final Join<GroupEntity, LicenseEntity> join = root.join(GroupEntity.PROPERTY_LICENSE, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), relationIdentifier);
        };
    }

    public static Specification<GroupEntity> withObservationStaIdentifier(final String observationId) {
        return (root, query, builder) -> {
            final Join<GroupEntity, DataEntity> join = root.join("observations", JoinType.INNER);
            return builder.equal(join.get(DataEntity.PROPERTY_STA_IDENTIFIER), observationId);
        };
    }

    @Override
    protected Specification<GroupEntity> handleRelatedPropertyFilter(
            String propertyName,
            Specification< ? > propertyValue) {
        return (root, query, builder) -> {
            if (StaConstants.RELATIONS.equals(propertyName)) {
                Subquery<GroupEntity> sq = query.subquery(GroupEntity.class);
                Root<RelationEntity> obsRelation = sq.from(RelationEntity.class);
                sq.select(obsRelation.get(RelationEntity.PROPERTY_GROUPS))
                  .where(((Specification<RelationEntity>) propertyValue).toPredicate(obsRelation,
                                                                                     query,
                                                                                     builder));
                return builder.in(root.get(GroupEntity.ID))
                              .value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override
    protected Specification<GroupEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression< ? > propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<GroupEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        return handleDirectStringPropertyFilter(root.get(GroupEntity.STA_IDENTIFIER),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                false);
                    case StaConstants.PROP_NAME:
                        return handleDirectStringPropertyFilter(root.get(GroupEntity.NAME),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    case StaConstants.PROP_DESCRIPTION:
                        return handleDirectStringPropertyFilter(root.get(GroupEntity.DESCRIPTION),
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
