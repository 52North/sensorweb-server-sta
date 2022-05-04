package org.n52.sta.data.entity;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;

public class DatastreamData extends StaData<AbstractDatasetEntity> implements Datastream {

    public DatastreamData(AbstractDatasetEntity data) {
        super(data);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public String getDescription() {
        return data.getDescription();
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(data.getParameters());
    }

    @Override
    public String getObservationType() {
        FormatEntity type = data.getOMObservationType();
        return type.getFormat();
    }

    @Override
    public UnitOfMeasurement getUnitOfMeasurement() {
        return createUom(data.getUnit()).orElse(null);
    }

    @Override
    public Geometry getObservedArea() {
        return data.getGeometry();
    }

    @Override
    public Time getPhenomenonTime() {
        Date samplingTimeStart = data.getSamplingTimeStart();
        Date samplingTimeEnd = data.getSamplingTimeEnd();
        return toTimeInterval(samplingTimeStart, samplingTimeEnd);
    }

    @Override
    public Time getResultTime() {
        Date resultTimeStart = data.getResultTimeStart();
        Date resultTimeEnd = data.getResultTimeEnd();
        return toTimeInterval(resultTimeStart, resultTimeEnd);
    }

    @Override
    public Thing getThing() {
        return new ThingData(data.getPlatform());
    }

    @Override
    public Sensor getSensor() {
        return new SensorData(data.getProcedure());
    }

    @Override
    public ObservedProperty getObservedProperty() {
        return new ObservedPropertyData(data.getObservableProperty());
    }

    @Override
    public Set<Observation<?>> getObservations() {
        return toSet(data.getObservations(), ObservationData::new);
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
}
