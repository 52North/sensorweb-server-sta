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
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.series.db.beans.sta.mapped.ObservationEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;

public abstract class ElementWithQueryOptions<P extends HibernateRelations.HasId> {

    protected P entity;
    protected QueryOptions queryOptions;

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public P getEntity() {
        return entity;
    }

    public static ElementWithQueryOptions from(Object entity, QueryOptions queryOptions) {
        Object unwrapped = (entity instanceof HibernateProxy) ? Hibernate.unproxy(entity) : entity;
        switch (unwrapped.getClass().getSimpleName()) {
        case "PlatformEntity":
            return new ThingWithQueryOptions((PlatformEntity) unwrapped, queryOptions);
        case "ProcedureEntity":
            return new SensorWithQueryOptions(new SensorEntity((ProcedureEntity) unwrapped), queryOptions);
        case "SensorEntity":
            return new SensorWithQueryOptions((SensorEntity) unwrapped, queryOptions);
        case "PhenomenonEntity":
            return new ObservedPropertyWithQueryOptions(new ObservablePropertyEntity((PhenomenonEntity) unwrapped),
                                                        queryOptions);
        case "ObservablePropertyEntity":
            return new ObservedPropertyWithQueryOptions((ObservablePropertyEntity) unwrapped, queryOptions);
        case "LocationEntity":
            return new LocationWithQueryOptions((LocationEntity) unwrapped, queryOptions);
        case "HistoricalLocationEntity":
            return new HistoricalLocationWithQueryOptions((HistoricalLocationEntity) unwrapped, queryOptions);
        case "StaFeatureEntity":
            return new FeatureOfInterestWithQueryOptions((StaFeatureEntity<?>) unwrapped, queryOptions);
        case "FeatureEntity":
            return new FeatureOfInterestWithQueryOptions(
                    new StaFeatureEntity<>((FeatureEntity) unwrapped), queryOptions);
        case "DatastreamEntity":
            return new DatastreamWithQueryOptions((DatastreamEntity) unwrapped, queryOptions);
        default:
            if (unwrapped instanceof ObservationEntity) {
                return new ObservationWithQueryOptions((ObservationEntity<?>) unwrapped, queryOptions);
            } else {
                throw new RuntimeException(
                        "Error wrapping object with queryOptions. Could not find Wrapper for class: " +
                                unwrapped.getClass().getSimpleName());
            }
        }
    }

    public static class ThingWithQueryOptions extends ElementWithQueryOptions<PlatformEntity> {

        ThingWithQueryOptions(PlatformEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }


    public static class SensorWithQueryOptions extends ElementWithQueryOptions<SensorEntity> {

        SensorWithQueryOptions(SensorEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }


    public static class ObservedPropertyWithQueryOptions extends ElementWithQueryOptions<ObservablePropertyEntity> {

        ObservedPropertyWithQueryOptions(ObservablePropertyEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }


    public static class ObservationWithQueryOptions extends ElementWithQueryOptions<ObservationEntity<?>> {

        ObservationWithQueryOptions(ObservationEntity<?> thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }


    public static class LocationWithQueryOptions extends ElementWithQueryOptions<LocationEntity> {

        LocationWithQueryOptions(LocationEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }


    public static class HistoricalLocationWithQueryOptions
            extends ElementWithQueryOptions<HistoricalLocationEntity> {

        HistoricalLocationWithQueryOptions(HistoricalLocationEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }


    public static class FeatureOfInterestWithQueryOptions extends ElementWithQueryOptions<StaFeatureEntity<?>> {

        FeatureOfInterestWithQueryOptions(StaFeatureEntity<?> thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }


    public static class DatastreamWithQueryOptions extends ElementWithQueryOptions<DatastreamEntity> {

        DatastreamWithQueryOptions(DatastreamEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }

}
