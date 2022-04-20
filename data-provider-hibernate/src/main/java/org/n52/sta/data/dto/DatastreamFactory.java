package org.n52.sta.data.dto;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.dto.DatastreamDto;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Datastream.UnitOfMeasurement;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.utils.TimeUtil;

public class DatastreamFactory extends BaseDtoFactory<DatastreamDto, DatastreamFactory> {
    
    public static Datastream create(AbstractDatasetEntity entity) {
        DatastreamFactory factory = create();
        factory.withMetadata(entity);
        factory.setObservations(entity);
        factory.setObservationType(entity);
        factory.setObservedArea(entity.getGeometry());

        // TODO includeDatastreamCategory
        factory.setProperties(entity);
        factory.setTime(entity);
        return factory.get();
    }

    public static DatastreamFactory create() {
        return new DatastreamFactory(new DatastreamDto());
    }

    public DatastreamFactory(DatastreamDto dto) {
        super(dto);
    }

    public DatastreamFactory setThing(Thing thing) {
        get().setThing(thing);
        return this;
    }

    public DatastreamFactory setSensor(Sensor sensor) {
        get().setSensor(sensor);
        return this;
    }

    public DatastreamFactory setObservedProperty(ObservedProperty observedProperty) {
        get().setObservedProperty(observedProperty);
        return this;
    }

    private DatastreamFactory setObservationType(AbstractDatasetEntity entity) {
        FormatEntity type = entity.getOMObservationType();
        Optional<UnitOfMeasurement> unit = createUom(entity.getUnit());
        return setObservationType(type.getFormat(), unit.orElse(null));
    }

    private Optional<Datastream.UnitOfMeasurement> createUom(UnitEntity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        String symbol = entity.getSymbol();
        String name = entity.getName();
        String definition = entity.getLink();
        return Optional.of(new Datastream.UnitOfMeasurement(symbol, name, definition));
    }

    public DatastreamFactory setObservationType(String type, Datastream.UnitOfMeasurement uom) {
        get().setObservationType(type);
        get().setUnitOfMeasurement(uom);
        return this;
    }

    public DatastreamFactory setObservedArea(Geometry geometry) {
        get().setObservedArea(geometry);
        return this;
    }

    private DatastreamFactory setTime(AbstractDatasetEntity entity) {
        Date samplingTimeStart = entity.getSamplingTimeStart();
        Date samplingTimeEnd = entity.getSamplingTimeEnd();
        Date resultTimeStart = entity.getResultTimeStart();
        Date resultTimeEnd = entity.getResultTimeEnd();

        Optional<DateTime> sStart = Optional.ofNullable(samplingTimeStart).map(TimeUtil::createDateTime);
        Optional<DateTime> sEnd = Optional.ofNullable(samplingTimeEnd).map(TimeUtil::createDateTime);
        Optional<DateTime> rStart = Optional.ofNullable(resultTimeStart).map(TimeUtil::createDateTime);
        Optional<DateTime> rEnd = Optional.ofNullable(resultTimeEnd).map(TimeUtil::createDateTime);

        Time phenomenonTime = sStart.map(start -> TimeUtil.createTime(start, sEnd.orElse(null))).orElse(null);
        Time resultTime = rStart.map(start -> TimeUtil.createTime(start, rEnd.orElse(null))).orElse(null);
        return setTime(phenomenonTime, resultTime);
    }

    public DatastreamFactory setTime(Time phenomenonTime, Time resultTime) {
        get().setPhenomenonTime(phenomenonTime);
        get().setResultTime(resultTime);
        return this;
    }

    public DatastreamFactory setObservations(AbstractDatasetEntity entity) {
        Set<DataEntity<?>> observations = entity.getObservations();
        Streams.stream(observations).forEach(this::addObservation);
        return this;
    }

    private DatastreamFactory addObservation(DataEntity<?> entity) {
        addObservation(ObservationFactory.create(entity));
        return this;
    }

    public DatastreamFactory addObservation(Observation<?> observation) {
        get().addObservation(observation);
        return this;
    }
}
