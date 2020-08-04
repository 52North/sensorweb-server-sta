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

import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.series.db.beans.sta.mapped.ObservationEntity;
import org.n52.series.db.beans.sta.mapped.extension.CSObservation;
import org.n52.series.db.beans.sta.mapped.extension.ObservationRelation;
import org.n52.shetland.ogc.filter.FilterConstants;
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
public class CSObservationQuerySpecifications extends EntityQuerySpecifications<CSObservation<?>> {

    public Specification<CSObservation<?>> withRelationStaIdentifier(final String relationIdentifier) {
        return (root, query, builder) -> {
            final Join<CSObservation, ObservationRelation> join =
                    root.join("relation", JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), relationIdentifier);
        };
    }

    public static Specification<CSObservation<?>> withCSDatastreamStaIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, DatasetEntity> join = datastream.join(DatastreamEntity.PROPERTY_DATASETS);
            sq.select(join.get(DatasetEntity.PROPERTY_ID)).where(
                    builder.equal(datastream.get(DatastreamEntity.PROPERTY_STA_IDENTIFIER), datastreamIdentifier));

            Subquery<ObservationEntity> subquery = query.subquery(ObservationEntity.class);
            Root<ObservationEntity> observation = subquery.from(ObservationEntity.class);

            subquery.select(observation.get(ObservationEntity.PROPERTY_ID))
                    .where(builder.in(observation.get(ObservationEntity.PROPERTY_DATASET)).value(sq));

            return builder.in(root.get("observation")).value(subquery);
        };
    }

    public static Specification<CSObservation<?>> withFOIStaIdentifier(
            final String featureIdentifier) {

        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatasetEntity> dataset = sq.from(DatasetEntity.class);
            Subquery<FeatureEntity> subquery = query.subquery(FeatureEntity.class);
            Root<FeatureEntity> feature = subquery.from(FeatureEntity.class);
            subquery.select(feature.get(FeatureEntity.PROPERTY_ID))
                    .where(builder.equal(feature.get(FeatureEntity.PROPERTY_STA_IDENTIFIER), featureIdentifier));
            sq.select(dataset.get(DatasetEntity.PROPERTY_ID))
              .where(builder.equal(dataset.get(DatasetEntity.PROPERTY_FEATURE), subquery));
            return builder.in(root.get(ObservationEntity.PROPERTY_DATASET)).value(sq);
        };
    }

    @Override protected Specification<CSObservation<?>> handleRelatedPropertyFilter(String propertyName,
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

    @Override protected Specification<CSObservation<?>> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<CSObservation<?>>) (root, query, builder) -> {
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
