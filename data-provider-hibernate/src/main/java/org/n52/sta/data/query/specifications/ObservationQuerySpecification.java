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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.sta.GroupEntity;
import org.n52.series.db.beans.sta.RelationEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.exception.ProviderException;
import org.n52.sta.api.path.PathSegment;
import org.n52.sta.api.path.VeiledPathSegment;
import org.springframework.data.jpa.domain.Specification;

public class ObservationQuerySpecification extends QuerySpecification<DataEntity> {

    public ObservationQuerySpecification() {
        super();
        this.filterByMember.put(StaConstants.DATASTREAMS, createDatastreamFilter());
        this.filterByMember.put(StaConstants.FEATURES_OF_INTEREST, createFeatureOfInterestFilter());
        this.filterByMember.put(StaConstants.GROUPS, createGroupsFilter());
        this.filterByMember.put(StaConstants.SUBJECT, createSubjectsFilter());
        this.filterByMember.put(StaConstants.OBJECT, createObjectsFilter());
    }

    @Override
    public Optional<Specification<DataEntity>> isStaEntity() {
        return Optional.of((root, query, builder) -> builder.isNull(root.get(DataEntity.PROPERTY_PARENT)));
    }

    @Override
    protected Specification<DataEntity> parsePath(List<PathSegment> segments) throws ProviderException {
        if (!segments.isEmpty() && ObservationVeiledPathPredicates.subject().test(segments.get(0))
                || ObservationVeiledPathPredicates.object().test(segments.get(0))) {
            return super.parsePath(segments.stream()
                    .map(s -> ObservationVeiledPathPredicates.relations().test(s)
                            ? VeiledPathSegment.of(s,
                                    ObservationVeiledPathPredicates.subject().test(segments.get(0))
                                            ? StaConstants.SUBJECT
                                            : StaConstants.OBJECT)
                            : s)
                    .collect(Collectors.toList()));
        }
        return super.parsePath(segments);
    }

    private MemberFilter<DataEntity> createDatastreamFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(AbstractDatasetEntity.PROPERTY_ID, AbstractDatasetEntity.class);
            Subquery<?> subquery = memberQuery.create(specification, query, builder);
            // n..1
            return builder.in(subquery).value(root.get(DataEntity.PROPERTY_DATASET_ID));
        };
    }

    private MemberFilter<DataEntity> createFeatureOfInterestFilter() {
        return specification -> (root, query, builder) -> {
            Subquery<DatasetEntity> datasetQuery = query.subquery(DatasetEntity.class);
            Root<DatasetEntity> dataset = datasetQuery.from(DatasetEntity.class);
            Subquery<FeatureEntity> featureQuery = query.subquery(FeatureEntity.class);
            Root<FeatureEntity> feature = featureQuery.from(FeatureEntity.class);
            featureQuery.select(feature.get(FeatureEntity.PROPERTY_ID))
                    .where(((Specification<FeatureEntity>) specification).toPredicate(feature, query, builder));
            datasetQuery.select(dataset.get(DatasetEntity.PROPERTY_ID))
                    .where(builder.equal(dataset.get(DatasetEntity.PROPERTY_FEATURE), featureQuery));
            return builder.in(root.get(DataEntity.PROPERTY_DATASET)).value(datasetQuery);
        };
    }

    private MemberFilter<DataEntity> createGroupsFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, GroupEntity.class);
            Subquery<?> subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join<?, ?> join = root.join(DataEntity.PROPERTY_GROUPS, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID)).value(subquery);
        };
    }

    private MemberFilter<DataEntity> createSubjectsFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, RelationEntity.class);
            Subquery<?> subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join<?, ?> join = root.join(DataEntity.PROPERTY_SUBJECTS, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID)).value(subquery);
        };
    }

    private MemberFilter<DataEntity> createObjectsFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, RelationEntity.class);
            Subquery<?> subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join<?, ?> join = root.join(DataEntity.PROPERTY_OBJECTS, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID)).value(subquery);
        };
    }

    private static final class ObservationVeiledPathPredicates {

        private ObservationVeiledPathPredicates() {
        }

        public static Predicate<PathSegment> relations() {
            return new Predicate<PathSegment>() {

                @Override
                public boolean test(PathSegment input) {
                    return StaConstants.RELATIONS.equals(input.getCollection());
                }
            };
        }

        public static Predicate<PathSegment> subject() {
            return new Predicate<PathSegment>() {

                @Override
                public boolean test(PathSegment input) {
                    return StaConstants.SUBJECT.equals(input.getCollection());
                }
            };
        }

        public static Predicate<PathSegment> object() {
            return new Predicate<PathSegment>() {

                @Override
                public boolean test(PathSegment input) {
                    return StaConstants.OBJECT.equals(input.getCollection());
                }
            };
        }

    }
}
