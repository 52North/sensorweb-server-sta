package org.n52.sta.serdes;

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.service.query.QueryOptions;

public abstract class ElementWithQueryOptions<P extends IdEntity> {

    protected P entity;
    protected QueryOptions queryOptions;

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public P getEntity() {
        return entity;
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
