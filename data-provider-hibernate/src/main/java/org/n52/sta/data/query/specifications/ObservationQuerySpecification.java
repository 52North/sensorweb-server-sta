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

import java.util.Optional;

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.springframework.data.jpa.domain.Specification;

public class ObservationQuerySpecification extends QuerySpecification<DataEntity<?>> {

    public ObservationQuerySpecification() {
        super();
        this.filterByMember.put(StaConstants.DATASTREAMS, createDatastreamFilter());
        this.filterByMember.put(StaConstants.FEATURES_OF_INTEREST, createFeatureOfInterestFilter());
    }

    @Override
    public Optional<Specification<DataEntity<?>>> isStaEntity() {
        return Optional.of((root, query, builder) -> builder.isNull(root.get(DataEntity.PROPERTY_PARENT)));
    }

    private MemberFilter<DataEntity<?>> createDatastreamFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(AbstractDatasetEntity.PROPERTY_ID, AbstractDatasetEntity.class);
            Subquery<?> subquery = memberQuery.create(specification, query, builder);
            // n..1
            return builder.in(subquery)
                          .value(root.get(DataEntity.PROPERTY_DATASET_ID));
        };
    }

    private MemberFilter<DataEntity<?>> createFeatureOfInterestFilter() {
        return specification -> (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatasetEntity> dataset = sq.from(DatasetEntity.class);
            Subquery<FeatureEntity> subquery = query.subquery(FeatureEntity.class);
            Root<FeatureEntity> feature = subquery.from(FeatureEntity.class);
            subquery.select(feature.get(FeatureEntity.PROPERTY_ID))
                    .where(((Specification<FeatureEntity>) specification).toPredicate(feature, query, builder));
            sq.select(dataset.get(DatasetEntity.PROPERTY_ID))
              .where(builder.equal(dataset.get(DatasetEntity.PROPERTY_FEATURE), subquery));
            return builder.in(root.get(DataEntity.PROPERTY_DATASET))
                          .value(sq);
        };
    }
}
