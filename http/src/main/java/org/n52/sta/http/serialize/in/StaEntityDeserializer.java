
package org.n52.sta.http.serialize.in;

import java.io.IOException;
import java.util.function.BiFunction;

import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class StaEntityDeserializer<T extends Identifiable> extends StdDeserializer<T> {

    private static final long serialVersionUID = -3963474080926571532L;

    private static final Logger LOGGER = LoggerFactory.getLogger(StaEntityDeserializer.class);

    private final BiFunction<JsonNode, ObjectMapper, ? extends Identifiable> instanceFactory;
    
    private final ObjectMapper mapper;

    public StaEntityDeserializer(Class<T> entityType, ObjectMapper mapper) {
        super(entityType);
        this.instanceFactory = createInstanceFactory();
        this.mapper = mapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = ctxt.readTree(p);
        return (T) instanceFactory.apply(node, mapper);
    }

    private BiFunction<JsonNode, ObjectMapper, ? extends Identifiable> createInstanceFactory() {
        if (Thing.class.isAssignableFrom(handledType())) {
            return ThingNode::new;
        } else if (Datastream.class.isAssignableFrom(handledType())) {
            return DatastreamNode::new;
        } else if (Sensor.class.isAssignableFrom(handledType())) {
            return SensorNode::new;
        } else if (Location.class.isAssignableFrom(handledType())) {
            return LocationNode::new;
        } else if (HistoricalLocation.class.isAssignableFrom(handledType())) {
            return HistoricalLocationNode::new;
        } else if (ObservedProperty.class.isAssignableFrom(handledType())) {
            return ObservedPropertyNode::new;
        } else if (Observation.class.isAssignableFrom(handledType())) {
            return ObservationNode::new;
        } else if (FeatureOfInterest.class.isAssignableFrom(handledType())) {
            return FeatureOfInterestNode::new;
        } else {
            LOGGER.debug("Unknown input type '{}'", handledType());
            throw new IllegalStateException("Unknown type to serialize: " + handledType());
        }
    }

}
