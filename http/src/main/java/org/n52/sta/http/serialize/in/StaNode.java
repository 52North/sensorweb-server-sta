/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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
        return propertyNode.map(asResult::apply).orElse(null);
    }

    protected Optional<JsonNode> get(String property) {
        JsonNode propertyNode = node.get(property);
        return propertyNode == null || propertyNode.isNull()
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
