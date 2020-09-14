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

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ProjectQuerySpecifications extends EntityQuerySpecifications<ProjectEntity> {

    public Specification<ProjectEntity> withDatastreamStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<ProjectEntity, AbstractDatasetEntity> join =
                    root.join(ProjectEntity.PROPERTY_DATASTREAMS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), identifier);
        };
    }

    @Override
    protected Specification<ProjectEntity> handleRelatedPropertyFilter(String propertyName,
                                                                       Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (StaConstants.DATASTREAMS.equals(propertyName)) {
                Subquery<ProjectEntity> sq = query.subquery(ProjectEntity.class);
                Root<AbstractDatasetEntity> datastream = sq.from(AbstractDatasetEntity.class);
                sq.select(datastream.get(AbstractDatasetEntity.PROPERTY_PROJECT))
                  .where(((Specification<AbstractDatasetEntity>) propertyValue).toPredicate(datastream,
                                                                                            query,
                                                                                            builder));
                return builder.in(root.get(ProjectEntity.ID)).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override protected Specification<ProjectEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<ProjectEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case StaConstants.PROP_ID:
                    return handleDirectStringPropertyFilter(root.get(ProjectEntity.STA_IDENTIFIER),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(root.get(ProjectEntity.NAME),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(root.get(ProjectEntity.DESCRIPTION),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case StaConstants.PROP_URL:
                    return handleDirectStringPropertyFilter(root.get(ProjectEntity.PROPERTY_URL),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case StaConstants.PROP_RUNTIME:
                    switch (operator) {
                    case PropertyIsLessThan:
                    case PropertyIsLessThanOrEqualTo:
                        return handleDirectDateTimePropertyFilter(root.get(ProjectEntity.PROPERTY_RUNTIME_END),
                                                                  propertyValue,
                                                                  operator,
                                                                  builder);
                    case PropertyIsGreaterThan:
                    case PropertyIsGreaterThanOrEqualTo:
                        return handleDirectDateTimePropertyFilter(root.get(ProjectEntity.PROPERTY_RUNTIME_START),
                                                                  propertyValue,
                                                                  operator,
                                                                  builder);
                    case PropertyIsEqualTo:
                        Predicate eqStart =
                                handleDirectDateTimePropertyFilter(root.get(ProjectEntity.PROPERTY_RUNTIME_START),
                                                                   propertyValue,
                                                                   operator,
                                                                   builder);
                        Predicate eqEnd =
                                handleDirectDateTimePropertyFilter(root.get(ProjectEntity.PROPERTY_RUNTIME_END),
                                                                   propertyValue,
                                                                   operator,
                                                                   builder);
                        return builder.and(eqStart, eqEnd);
                    case PropertyIsNotEqualTo:
                        Predicate neStart =
                                handleDirectDateTimePropertyFilter(root.get(ProjectEntity.PROPERTY_RUNTIME_START),
                                                                   propertyValue,
                                                                   operator,
                                                                   builder);
                        Predicate neEnd =
                                handleDirectDateTimePropertyFilter(root.get(ProjectEntity.PROPERTY_RUNTIME_END),
                                                                   propertyValue,
                                                                   operator,
                                                                   builder);
                        return builder.or(neStart, neEnd);
                    default:
                        throw new STAInvalidFilterExpressionException("Unknown operator: " + operator.toString());
                    }
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
