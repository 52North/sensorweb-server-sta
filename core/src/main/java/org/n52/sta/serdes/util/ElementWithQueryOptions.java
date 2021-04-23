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

package org.n52.sta.serdes.util;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservationGroupEntity;
import org.n52.series.db.beans.sta.ObservationRelationEntity;
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ElementWithQueryOptions<P extends HibernateRelations.HasId> {

    protected P entity;
    protected QueryOptions queryOptions;
    protected Set<String> fieldsToSerialize = new HashSet<>();
    protected Map<String, QueryOptions> fieldsToExpand = new HashMap<>();
    protected boolean hasSelectOption;
    protected boolean hasExpandOption;

    ElementWithQueryOptions(P entity, QueryOptions queryOptions) {
        this.entity = entity;
        this.queryOptions = queryOptions;
    }

    public static ElementWithQueryOptions from(Object entity, QueryOptions queryOptions) {
        Object unwrapped = (entity instanceof HibernateProxy) ? Hibernate.unproxy(entity) : entity;
        switch (unwrapped.getClass().getSimpleName()) {
            case "PlatformEntity":
                return new ThingWithQueryOptions((PlatformEntity) unwrapped, queryOptions);
            case "ProcedureEntity":
                return new SensorWithQueryOptions((ProcedureEntity) unwrapped, queryOptions);
            case "PhenomenonEntity":
                return new ObservedPropertyWithQueryOptions((PhenomenonEntity) unwrapped, queryOptions);
            case "LocationEntity":
                return new LocationWithQueryOptions((LocationEntity) unwrapped, queryOptions);
            case "HistoricalLocationEntity":
                return new HistoricalLocationWithQueryOptions((HistoricalLocationEntity) unwrapped, queryOptions);
            case "StaFeatureEntity":
                return new FeatureOfInterestWithQueryOptions((StaFeatureEntity<?>) unwrapped, queryOptions);
            case "FeatureEntity":
                return new FeatureOfInterestWithQueryOptions(
                    new StaFeatureEntity<>((FeatureEntity) unwrapped), queryOptions);
            case "DatasetEntity":
            case "AbstractDatasetEntity":
            case "DatasetAggregationEntity":
                return new DatastreamWithQueryOptions((AbstractDatasetEntity) unwrapped, queryOptions);
            case "ObservationGroupEntity":
                return new ObservationGroupWithQueryOptions((ObservationGroupEntity) unwrapped, queryOptions);
            case "ObservationRelationEntity":
                return new ObservationRelationWithQueryOptions((ObservationRelationEntity) unwrapped, queryOptions);
            case "LicenseEntity":
                return new LicenseWithQueryOptions((LicenseEntity) unwrapped, queryOptions);
            case "PartyEntity":
                return new PartyWithQueryOptions((PartyEntity) unwrapped, queryOptions);
            case "ProjectEntity":
                return new ProjectWithQueryOptions((ProjectEntity) unwrapped, queryOptions);
            default:
                if (unwrapped instanceof DataEntity) {
                    // Hibernate.initialize(((DataEntity) unwrapped).getDataset());
                    return new ObservationWithQueryOptions((DataEntity<?>) unwrapped, queryOptions);
                } else {
                    throw new RuntimeException(
                        "Error wrapping object with queryOptions. Could not find Wrapper for class: " +
                            unwrapped.getClass().getSimpleName());
                }
        }
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public P getEntity() {
        return entity;
    }

    public Set<String> getFieldsToSerialize() {
        return fieldsToSerialize;
    }

    public Map<String, QueryOptions> getFieldsToExpand() {
        return fieldsToExpand;
    }

    public boolean hasSelectOption() {
        return hasSelectOption;
    }

    public boolean hasExpandOption() {
        return hasExpandOption;
    }

    public void unwrap(boolean enableImplicitSelect) {
        if (queryOptions != null) {
            if (queryOptions.hasSelectFilter()) {
                hasSelectOption = true;
                fieldsToSerialize.addAll(queryOptions.getSelectFilter().getItems());
            }
            if (queryOptions.hasExpandFilter()) {
                hasExpandOption = true;
                for (ExpandItem item : queryOptions.getExpandFilter().getItems()) {
                    fieldsToExpand.put(item.getPath(), item.getQueryOptions());
                    // Add expanded items to $select replacing implicit selection with explicit selection
                    if (hasSelectOption && enableImplicitSelect) {
                        fieldsToSerialize.add(item.getPath());
                    }
                }
            }
        }
    }

    public static class ThingWithQueryOptions extends ElementWithQueryOptions<PlatformEntity> {

        ThingWithQueryOptions(PlatformEntity entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class SensorWithQueryOptions extends ElementWithQueryOptions<ProcedureEntity> {

        SensorWithQueryOptions(ProcedureEntity entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class ObservedPropertyWithQueryOptions extends ElementWithQueryOptions<PhenomenonEntity> {

        ObservedPropertyWithQueryOptions(PhenomenonEntity entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class ObservationWithQueryOptions extends ElementWithQueryOptions<DataEntity<?>> {

        ObservationWithQueryOptions(DataEntity<?> entity,
                                    QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class LocationWithQueryOptions extends ElementWithQueryOptions<LocationEntity> {

        LocationWithQueryOptions(LocationEntity entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class HistoricalLocationWithQueryOptions
        extends ElementWithQueryOptions<HistoricalLocationEntity> {

        HistoricalLocationWithQueryOptions(HistoricalLocationEntity entity,
                                           QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class FeatureOfInterestWithQueryOptions extends ElementWithQueryOptions<StaFeatureEntity<?>> {

        FeatureOfInterestWithQueryOptions(StaFeatureEntity<?> entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class DatastreamWithQueryOptions extends ElementWithQueryOptions<AbstractDatasetEntity> {

        DatastreamWithQueryOptions(AbstractDatasetEntity entity,
                                   QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class ObservationGroupWithQueryOptions extends ElementWithQueryOptions<ObservationGroupEntity> {

        ObservationGroupWithQueryOptions(ObservationGroupEntity entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class ObservationRelationWithQueryOptions extends ElementWithQueryOptions<ObservationRelationEntity> {

        ObservationRelationWithQueryOptions(ObservationRelationEntity entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class LicenseWithQueryOptions extends ElementWithQueryOptions<LicenseEntity> {

        LicenseWithQueryOptions(LicenseEntity entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class PartyWithQueryOptions extends ElementWithQueryOptions<PartyEntity> {

        PartyWithQueryOptions(PartyEntity entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }


    public static class ProjectWithQueryOptions extends ElementWithQueryOptions<ProjectEntity> {

        ProjectWithQueryOptions(ProjectEntity entity, QueryOptions queryOptions) {
            super(entity, queryOptions);
        }
    }

}
