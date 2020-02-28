/*
 * Copyright 2015-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.n52.sta.serdes.util;

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
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
        case "PlatformEntity":
            return new ThingWithQueryOptions((PlatformEntity) entity, queryOptions);
        case "ProcedureEntity":
            if (entity instanceof SensorEntity) {
                return new SensorWithQueryOptions((SensorEntity) entity, queryOptions);
            } else {
                return new SensorWithQueryOptions(new SensorEntity((ProcedureEntity) entity), queryOptions);
            }
        case "PhenomenonEntity":
            if (entity instanceof ObservablePropertyEntity) {
                return new ObservedPropertyWithQueryOptions((ObservablePropertyEntity) entity, queryOptions);
            } else {
                return new ObservedPropertyWithQueryOptions(new ObservablePropertyEntity((PhenomenonEntity) entity),
                                                            queryOptions);
            }
        case "DataEntity":
            if (entity instanceof StaDataEntity) {
                return new ObservationWithQueryOptions((StaDataEntity<?>) entity, queryOptions);
            } else {
                return new ObservationWithQueryOptions(new StaDataEntity<>((DataEntity<?>) entity), queryOptions);
            }
        case "LocationEntity":
            return new LocationWithQueryOptions((LocationEntity) entity, queryOptions);
        case "HistoricalLocationEntity":
            return new HistoricalLocationWithQueryOptions((HistoricalLocationEntity) entity, queryOptions);
        case "AbstractFeatureEntity":
        case "FeatureEntity":
            return new FeatureOfInterestWithQueryOptions((AbstractFeatureEntity<?>) entity, queryOptions);
        case "DatastreamEntity":
            return new DatastreamWithQueryOptions((DatastreamEntity) entity, queryOptions);
        default:
            throw new RuntimeException("THIS SHOULD NOT HAPPEN!");
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


    public static class ObservationWithQueryOptions extends ElementWithQueryOptions<StaDataEntity<?>> {

        ObservationWithQueryOptions(StaDataEntity<?> thing, QueryOptions queryOptions) {
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


    public static class HistoricalLocationWithQueryOptions extends ElementWithQueryOptions<HistoricalLocationEntity> {

        HistoricalLocationWithQueryOptions(HistoricalLocationEntity thing, QueryOptions queryOptions) {
            this.entity = thing;
            this.queryOptions = queryOptions;
        }
    }


    public static class FeatureOfInterestWithQueryOptions extends ElementWithQueryOptions<AbstractFeatureEntity<?>> {

        FeatureOfInterestWithQueryOptions(AbstractFeatureEntity<?> thing, QueryOptions queryOptions) {
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
