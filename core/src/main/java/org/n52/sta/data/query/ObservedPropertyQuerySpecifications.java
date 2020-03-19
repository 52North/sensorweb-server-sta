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
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservedPropertyQuerySpecifications extends EntityQuerySpecifications<PhenomenonEntity> {

    public Specification<PhenomenonEntity> withDatastreamIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            final Join<PhenomenonEntity, DatastreamEntity> join =
                    root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), datastreamIdentifier);
        };
    }

    @Override protected Specification<PhenomenonEntity> handleRelatedPropertyFilter(String propertyName,
                                                                                    Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (DATASTREAMS.equals(propertyName)) {
                Subquery<PhenomenonEntity> sq = query.subquery(PhenomenonEntity.class);
                Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
                final Join<DatastreamEntity, PhenomenonEntity> join =
                        datastream.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
                sq.select(join)
                  .where(((Specification<DatastreamEntity>) propertyValue).toPredicate(datastream,
                                                                                       query,
                                                                                       builder));
                return builder.in(root).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override protected Specification<PhenomenonEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<PhenomenonEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "id":
                    return handleDirectStringPropertyFilter(root.get("staIdentifier"),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            false);
                case "name":
                    return handleDirectStringPropertyFilter(root.get(DescribableEntity.PROPERTY_NAME),
                                                            propertyValue, operator, builder, switched);
                case "description":
                    return handleDirectStringPropertyFilter(
                            root.get(DescribableEntity.PROPERTY_DESCRIPTION),
                            propertyValue,
                            operator,
                            builder,
                            switched);
                case "definition":
                case "identifier":
                    return handleDirectStringPropertyFilter(
                            root.get(DescribableEntity.PROPERTY_IDENTIFIER),
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
