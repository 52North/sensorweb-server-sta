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

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.data.query.specifications.util.LiteralComparator;
import org.n52.sta.data.query.specifications.util.SimplePropertyComparator;
import org.springframework.data.jpa.domain.Specification;

public class FeatureOfInterestQuerySpecification extends QuerySpecification<AbstractFeatureEntity> {

    public FeatureOfInterestQuerySpecification() {
        super();
        this.entityPathByProperty.put(StaConstants.PROP_NAME, new SimplePropertyComparator<>(
                HibernateRelations.HasName.PROPERTY_NAME));
        this.entityPathByProperty.put(StaConstants.PROP_DESCRIPTION, new SimplePropertyComparator<>(
                HibernateRelations.HasDescription.PROPERTY_DESCRIPTION));
        this.entityPathByProperty.put(StaConstants.PROP_ENCODINGTYPE, new LiteralComparator<>("application/geo+json"));

        this.filterByMember.put(StaConstants.OBSERVATIONS, createObservationFilter());

    }

    private MemberFilter<AbstractFeatureEntity> createObservationFilter() {
        return memberSpec -> (root, query, builder) -> {
            Subquery<Long> sqFeature = query.subquery(Long.class);
            Root<DatasetEntity> dataset = sqFeature.from(DatasetEntity.class);
            Subquery<DatasetEntity> sqDataset = query.subquery(DatasetEntity.class);
            Root<DataEntity> data = sqDataset.from(DataEntity.class);
            sqDataset.select(dataset)
                     .where(((Specification<DataEntity>) memberSpec).toPredicate(data,
                                                                                 query,
                                                                                 builder));

            sqFeature.select(dataset.get(DatasetEntity.PROPERTY_FEATURE))
                     .where(builder.in(dataset)
                                   .value(sqDataset));
            return builder.in(root.get(AbstractFeatureEntity.PROPERTY_ID))
                          .value(sqFeature);

        };
    }
}
