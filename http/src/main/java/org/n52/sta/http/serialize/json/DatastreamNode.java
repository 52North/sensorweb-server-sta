package org.n52.sta.http.serialize.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;

public class DatastreamNode extends StaNode implements Datastream {

    public DatastreamNode(JsonNode node, ObjectMapper mapper) {
        super(node, mapper);
    }

    @Override
    public String getName() {
        return getOrNull(StaConstants.PROP_NAME, JsonNode::asText);
    }

    @Override
    public String getDescription() {
        return getOrNull(StaConstants.PROP_DESCRIPTION, JsonNode::asText);
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(StaConstants.PROP_PROPERTIES);
    }

    @Override
    public String getObservationType() {
        return getOrNull(StaConstants.PROP_OBSERVATION_TYPE, JsonNode::asText);
    }

    @Override
    public UnitOfMeasurement getUnitOfMeasurement() {
        return getOrNull(StaConstants.PROP_UOM, n -> {
            String name = n.get("name").asText();
            String symbol = n.get("symbol").asText();
            String definition = n.get("definition").asText();
            return new UnitOfMeasurement(symbol, name, definition);
        });
    }

    @Override
    public Geometry getObservedArea() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Time getPhenomenonTime() {
        return parseTime(StaConstants.PROP_PHENOMENON_TIME);
    }

    @Override
    public Time getResultTime() {
        return parseTime(StaConstants.PROP_RESULT_TIME);
    }

    @Override
    public Thing getThing() {
        return getOrNull(StaConstants.THING, n -> new ThingNode(n, mapper));
    }

    @Override
    public Sensor getSensor() {
        return getOrNull(StaConstants.SENSOR, n -> new SensorNode(n, mapper));
    }

    @Override
    public ObservedProperty getObservedProperty() {
        return getOrNull(StaConstants.OBSERVED_PROPERTY, n -> new ObservedPropertyNode(n, mapper));
    }

    @Override
    public Set<Observation<?>> getObservations() {
        return toSet(StaConstants.OBSERVATIONS, n -> new ObservationNode(n, mapper));
    }

}
