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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.janmayen.stream.Streams;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class StaNode implements Identifiable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaNode.class);

    protected final ObjectMapper mapper;
    protected final String NIY = "not implemented yet!";
    private final JsonNode node;

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

    protected Geometry parseGeometry(String property) throws InvalidValueException {
        Optional<JsonNode> propertyNode = get(property);
        if (!propertyNode.isPresent()) {
            return null;
        }

        // TODO read srs from geometry?
        int srs = 4326;
        PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
        GeometryFactory geometryFactory = new GeometryFactory(precisionModel, srs);
        GeoJsonReader reader = new GeoJsonReader(geometryFactory);
        JsonNode geometry = propertyNode.get();
        try {
            // We might have the geometry embedded or as raw object
            // see 18-088 Section 8.2.2 Example 2 --> location
            // see 18-088 Section 8.2.4 Example 4 --> observedArea
            final String GEOMETRY = "geometry";
            if (geometry.has(GEOMETRY)) {
                return reader.read(geometry.get(GEOMETRY).toString());
            } else {
                return reader.read(geometry.toString());
            }
        } catch (ParseException e) {
            LOGGER.debug("Could not parse GeoJson at '{}': {}", property, geometry.toPrettyString());
            throw new InvalidValueException(String.format("Invalid GeoJSON at '%s'!", property));
        }
    }

    protected Time parseTime(String property) {
        Optional<JsonNode> propertyNode = get(property);
        if (!propertyNode.isPresent()) {
            return null;
        }
        JsonNode time = propertyNode.get();
        try {
            return TimeUtil.parseTime(time.asText());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Could not parse Time at '{}': {}", property, time.toPrettyString());
            throw new InvalidValueException(String.format("Invalid Time at '%s'!", property));
        }
    }

    protected <T> T getOrNull(String property, Function<JsonNode, T> asResult) {
        Optional<JsonNode> propertyNode = get(property);
        return propertyNode.map(asResult::apply)
                           .orElse(null);
    }

    protected Optional<JsonNode> get(String property) {
        JsonNode propertyNode = node.get(property);
        return propertyNode == null || propertyNode.isNull()
                ? Optional.empty()
                : Optional.of(propertyNode);
    }

    protected Map<String, Object> toMap(String property) {
        Optional<JsonNode> objectNode = get(property);
        return objectNode.map(this::toMap)
                         .orElse(Collections.emptyMap());
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
        return toStream(arrayNode).map(nodeMapper)
                                  .collect(Collectors.toSet());
    }

    private Stream<JsonNode> toStream(JsonNode arrayNode) {
        return arrayNode.isArray()
                ? Streams.stream(arrayNode)
                // FIXME throw exception as json is invalid
                : Stream.empty();
    }

}
