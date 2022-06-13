
package org.n52.sta.http.serialize.in;

import java.io.IOException;
import java.util.function.BiFunction;

import org.n52.sta.api.entity.Identifiable;
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

    private final ObjectMapper mapper;

    public StaEntityDeserializer(Class<T> entityType, ObjectMapper mapper) {
        super(entityType);
        this.mapper = mapper;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        JsonNode node = ctxt.readTree(p);
        if (Thing.class.isAssignableFrom(handledType())) {
            return wrapNode(node, ThingNode::new);
        } else {
            LOGGER.debug("Invalid input for type '{}': {}", handledType(), node.toPrettyString());
            throw new IllegalStateException("Unknown type to serialize: " + handledType());
        }

    }

    @SuppressWarnings("unchecked")
    private T wrapNode(JsonNode node, BiFunction<JsonNode, ObjectMapper, ? extends Identifiable> create) {
        return (T) create.apply(node, mapper);
    }

}
