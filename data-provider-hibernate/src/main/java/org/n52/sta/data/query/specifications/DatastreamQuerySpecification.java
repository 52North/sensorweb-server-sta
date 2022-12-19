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

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.data.query.specifications.util.SimplePropertyComparator;
import org.n52.sta.data.query.specifications.util.TimePropertyComparator;
import org.springframework.data.jpa.domain.Specification;

public class DatastreamQuerySpecification extends QuerySpecification<AbstractDatasetEntity> {

    public DatastreamQuerySpecification() {
        this.filterByMember.put(StaConstants.SENSORS, createSensorFilter());
        this.filterByMember.put(StaConstants.OBSERVED_PROPERTIES, createObservedPropertyFilter());
        this.filterByMember.put(StaConstants.THINGS, createThingfilter());
        this.filterByMember.put(StaConstants.OBSERVATIONS, createObservationFilter());
        this.filterByMember.put(StaConstants.PARTIES, createPartyfilter());
        this.filterByMember.put(StaConstants.PROJECTS, createProjectfilter());
        this.filterByMember.put(StaConstants.LICENSES, createLicensefilter());

        this.entityPathByProperty.put(StaConstants.PROP_NAME, new SimplePropertyComparator<>(HasName.PROPERTY_NAME));
        this.entityPathByProperty.put(StaConstants.PROP_DESCRIPTION,
                                      new SimplePropertyComparator<>(HasDescription.PROPERTY_DESCRIPTION));
        this.entityPathByProperty.put(StaConstants.PROP_PHENOMENON_TIME,
                                      new TimePropertyComparator<>(
                                                                   AbstractDatasetEntity.PROPERTY_FIRST_VALUE_AT,
                                                                   AbstractDatasetEntity.PROPERTY_LAST_VALUE_AT));
        this.entityPathByProperty.put(StaConstants.PROP_RESULT_TIME,
                                      new TimePropertyComparator<>(
                                                                   AbstractDatasetEntity.PROPERTY_RESULT_TIME_START,
                                                                   AbstractDatasetEntity.PROPERTY_RESULT_TIME_END));
        //@formatter:off
        SimplePropertyComparator<AbstractDatasetEntity, String> customComparator =
                new SimplePropertyComparator<>(FormatEntity.FORMAT) {
                    /**
                     * Joins FormatEntity that actually stores OMObservationType.
                     *
                     * @param root
                     *        Root DatasetRoot
                     * @return Path to specific property
                     */
                    @Override
                    protected Path<String> getPath(Root<AbstractDatasetEntity> root) {
                        Join<AbstractDatasetEntity, FormatEntity> join = joinFormatEntity(root);
                        return join.get(this.entityPath);
                    }

                    private Join<AbstractDatasetEntity, FormatEntity> joinFormatEntity(
                            Root<AbstractDatasetEntity> root) {
                        return root.join(AbstractDatasetEntity.PROPERTY_OM_OBSERVATION_TYPE);
                    }
                };
        //@formatter:on
        this.entityPathByProperty.put(StaConstants.PROP_OBSERVATION_TYPE, customComparator);

        // TODO $filter=unitOfMeasurement/symbol eq 'asdf'
    }

    @Override
    public Optional<Specification<AbstractDatasetEntity>> isStaEntity() {
        // TODO: ideally we should check for datasetType == not_initialized, but it is
        // not exposed in the abstract class
        // checking for platform == null as a workaround
        Specification<AbstractDatasetEntity> isDatastreamRoot = (root, query, builder) -> {
            Predicate hasPlatform = root.get(AbstractDatasetEntity.PROPERTY_PLATFORM)
                                        .isNotNull();
            Predicate isNotAggregate = builder.isNull(root.get(AbstractDatasetEntity.PROPERTY_AGGREGATION));
            return builder.and(hasPlatform, isNotAggregate);
        };
        return Optional.of(isDatastreamRoot);
    }

    private MemberFilter<AbstractDatasetEntity> createObservationFilter() {
        // add member specification on root specfication
        return memberSpec -> (root, query, builder) -> {

            // TODO: maybe refactor this to use BaseQuerySpecifications.createQuery similar
            // to other Filters

            Subquery<AbstractDatasetEntity> idQuery = query.subquery(AbstractDatasetEntity.class);
            Root<DataEntity> data = idQuery.from(DataEntity.class);
            idQuery.select(data.get(DataEntity.PROPERTY_DATASET_ID))
                   .where(((Specification<DataEntity>) memberSpec).toPredicate(data, query, builder));

            Subquery<AbstractDatasetEntity> aggregationQuery = query.subquery(AbstractDatasetEntity.class);
            Root<AbstractDatasetEntity> realDataset = aggregationQuery.from(AbstractDatasetEntity.class);
            aggregationQuery.select(realDataset.get(AbstractDatasetEntity.PROPERTY_AGGREGATION))
                            .where(builder.equal(realDataset.get(IdEntity.PROPERTY_ID), idQuery));

            // matches id or aggregation
            return builder.or(builder.equal(root.get(IdEntity.PROPERTY_ID), idQuery),
                              builder.equal(root.get(IdEntity.PROPERTY_ID), aggregationQuery));
        };
    }

    private MemberFilter<AbstractDatasetEntity> createSensorFilter() {
        return memberSpecification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID,
                                                  ProcedureEntity.class);
            Subquery< ? > subquery = memberQuery.create(memberSpecification, query, builder);
            // n..1
            return builder.in(subquery)
                          .value(root.get(AbstractDatasetEntity.PROPERTY_PROCEDURE));

        };
    }

    private MemberFilter<AbstractDatasetEntity> createObservedPropertyFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID,
                                                  PhenomenonEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // n..1
            return builder.in(subquery)
                          .value(root.get(AbstractDatasetEntity.PROPERTY_PHENOMENON));
        };
    }

    private MemberFilter<AbstractDatasetEntity> createThingfilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID,
                                                  PlatformEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // n..1
            return builder.in(subquery)
                          .value(root.get(AbstractDatasetEntity.PROPERTY_PLATFORM));

        };
    }

    private MemberFilter<AbstractDatasetEntity> createPartyfilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID,
                                                  PartyEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // n..1
            return builder.in(subquery)
                          .value(root.get(AbstractDatasetEntity.PROPERTY_PARTY));

        };
    }

    private MemberFilter<AbstractDatasetEntity> createProjectfilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID,
                                                  ProjectEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // n..1
            return builder.in(subquery)
                          .value(root.get(AbstractDatasetEntity.PROPERTY_PROJECT));

        };
    }

    private MemberFilter<AbstractDatasetEntity> createLicensefilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID,
                                                  LicenseEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // n..1
            return builder.in(subquery)
                          .value(root.get(AbstractDatasetEntity.PROPERTY_LICENSE));

        };
    }

}
