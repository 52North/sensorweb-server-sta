package org.n52.sta.http.serialize.in;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.n52.janmayen.stream.Streams;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.old.utils.TimeUtil;

abstract class StaNode implements Identifiable {

    protected final JsonNode node;

    protected final ObjectMapper mapper;

    protected StaNode(JsonNode node, ObjectMapper mapper) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(mapper, "mapper must not be null");
        this.node = node;
        this.mapper = mapper;
    }

    @Override
    @JsonProperty("@iot.id")
    public String getId() {
        return getOrNull(StaConstants.AT_IOT_ID, JsonNode::asText);
    }

    protected Time parseTime(String property) {
        return get(property).map(propertyNode -> {
            return TimeUtil.parseTime(propertyNode.asText());
        }).orElseThrow(() -> new IllegalArgumentException("Could not parse Time at '" + property + "'"));
    }

    protected <T> T getOrNull(String property, Function<JsonNode, T> asResult) {
        Optional<JsonNode> propertyNode = get(property);
        return propertyNode.map(n -> asResult.apply(n)).orElse(null);
    }

    protected Optional<JsonNode> get(String property) {
        JsonNode propertyNode = node.get(property);
        return propertyNode.isNull()
                ? Optional.empty()
                : Optional.of(propertyNode);
    }

    protected Map<String, Object> toMap(String property) {
        Optional<JsonNode> objectNode = get(property);
        return objectNode.map(this::toMap).orElse(Collections.emptyMap());
    }

    private Map<String, Object> toMap(JsonNode node) {
        return mapper.convertValue(node, new TypeReference<Map<String, Object>>() {});
    }

    protected <T> Set<T> toSet(String property, Function<JsonNode, T> nodeMapper) {
        Optional<JsonNode> arrayNode = get(property);
        return arrayNode.isPresent()
                ? toSet(arrayNode.get(), nodeMapper)
                : Collections.emptySet();
    }

    protected <T> Set<T> toSet(JsonNode arrayNode, Function<JsonNode, T> nodeMapper) {
        return toStream(arrayNode).map(nodeMapper).collect(Collectors.toSet());
    }

    private Stream<JsonNode> toStream(JsonNode arrayNode) {
        return arrayNode.isArray()
                ? Streams.stream(arrayNode)
                : Stream.empty();
    }

}
