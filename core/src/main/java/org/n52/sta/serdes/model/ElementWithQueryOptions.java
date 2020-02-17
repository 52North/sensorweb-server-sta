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
package org.n52.sta.serdes.model;

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;

public abstract class ElementWithQueryOptions<P extends IdEntity> {

    protected P entity;
    protected QueryOptions queryOptions;

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public P getEntity() {
        return entity;
    }

    //TODO: implement
    public static ElementWithQueryOptions from(Object entity, QueryOptions queryOptions) {
        switch (entity.getClass().getSimpleName()) {
            default:
                System.out.println(entity.getClass().getSimpleName());
                return new ThingWithQueryOptions((PlatformEntity) entity, queryOptions);
        }
    }

    public static class ThingWithQueryOptions extends ElementWithQueryOptions<PlatformEntity> {

        public ThingWithQueryOptions(PlatformEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }

    public static class SensorWithQueryOptions extends ElementWithQueryOptions<ProcedureEntity> {

        public SensorWithQueryOptions(ProcedureEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }

    public static class ObservedPropertyWithQueryOptions extends ElementWithQueryOptions<PhenomenonEntity> {

        public ObservedPropertyWithQueryOptions(PhenomenonEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }

    public static class ObservationWithQueryOptions extends ElementWithQueryOptions<DataEntity<?>> {

        public ObservationWithQueryOptions(DataEntity<?> thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }

    public static class LocationWithQueryOptions extends ElementWithQueryOptions<LocationEntity> {

        public LocationWithQueryOptions(LocationEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }

    public static class HistoricalLocationWithQueryOptions extends ElementWithQueryOptions<HistoricalLocationEntity> {

        public HistoricalLocationWithQueryOptions(HistoricalLocationEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }

    public static class FeatureOfInterestWithQueryOptions extends ElementWithQueryOptions<AbstractFeatureEntity<?>> {

        public FeatureOfInterestWithQueryOptions(AbstractFeatureEntity<?> thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }

    public static class DatastreamWithQueryOptions extends ElementWithQueryOptions<DatastreamEntity> {

        public DatastreamWithQueryOptions(DatastreamEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }

}
