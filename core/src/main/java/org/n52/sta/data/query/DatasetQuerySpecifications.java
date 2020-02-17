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

import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class DatasetQuerySpecifications {

    /**
     * Matches datasets having offering with given ids.
     *
     * @param id the id to match
     * @return a specification
     */
    public Specification<DatasetEntity> matchOfferings(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, OfferingEntity> join =
                    root.join(DatasetEntity.PROPERTY_OFFERING, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), id);
        };
    }

    /**
     * Matches datasets having feature with given id.
     *
     * @param id the id to match
     * @return a specification
     */
    public Specification<DatasetEntity> matchFeatures(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, FeatureEntity> join =
                    root.join(DatasetEntity.PROPERTY_FEATURE, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), id);
        };
    }

    /**
     * Matches datasets having procedures with given id.
     *
     * @param id the id to match
     * @return a specification
     */
    public Specification<DatasetEntity> matchProcedures(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, ProcedureEntity> join =
                    root.join(DatasetEntity.PROPERTY_PROCEDURE, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), id);
        };
    }

    /**
     * Matches datasets having phenomena with given id.
     *
     * @param id the id to match
     * @return a specification
     */
    public Specification<DatasetEntity> matchPhenomena(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, PhenomenonEntity> join =
                    root.join(DatasetEntity.PROPERTY_PHENOMENON, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), id);
        };
    }

    /**
     * Matches datasets having platform with given id.
     *
     * @param id the id to match
     * @return a specification
     */
    public Specification<DatasetEntity> matchPlatform(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, PlatformEntity> join =
                    root.join(DatasetEntity.PROPERTY_PLATFORM, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), id);
        };
    }

    /**
     * Matches datasets having catefory with given id.
     *
     * @param id the id to match
     * @return a specification
     */
    public Specification<DatasetEntity> matchCategory(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, CategoryEntity> join =
                    root.join(DatasetEntity.PROPERTY_CATEGORY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), id);
        };
    }
}
