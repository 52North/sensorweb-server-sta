/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.api.old.serialize.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.sta.api.old.dto.Location;
import org.n52.sta.api.old.serialize.common.AbstractJSONEntity;
import org.n52.sta.api.old.serialize.common.JSONBase;

@SuppressWarnings("VisibilityModifier")
public class JSONLocation extends JSONBase.JSONwithIdNameDescription<Location> implements AbstractJSONEntity {

    private static final String COULD_NOT_PARSE = "Could not parse location to GeoJSON. Error was: ";

    // JSON Properties. Matched by Annotation or variable name
    public String encodingType;
    public JsonNode location;
    public ObjectNode properties;

    @JsonManagedReference
    public JSONThing[] Things;

    @JsonManagedReference
    public JSONHistoricalLocation[] HistoricalLocations;

    private final String ENCODINGTYPE_GEOJSON = "application/geo+json";
    private final String ENCODINGTYPE_GEOJSON_ALT = "application/vnd.geo+json";
    private final String INVALID_ENCODINGTYPE =
        "Invalid encodingType supplied. Only GeoJSON (application/vnd.geo+json) is supported!";
    private final GeometryFactory factory =
        new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    private final String TYPE = "type";
    private final String GEOMETRY = "geometry";
    private final String COORDINATES = "coordinates";
    private final String LOCATION_TYPE = "feature->type";
    private final String LOCATION_GEOM = "location->geometry";

    public JSONLocation() {
        self = new Location();
    }

    @Override protected void parseReferencedFrom() {
        if (referencedFromType != null) {
            switch (referencedFromType) {
                case "HistoricalLocations":
                    assertIsNull(HistoricalLocations, INVALID_DUPLICATE_REFERENCE);
                    this.HistoricalLocations = new JSONHistoricalLocation[1];
                    this.HistoricalLocations[0] = new JSONHistoricalLocation();
                    this.HistoricalLocations[0].identifier = referencedFromID;
                    return;
                case "Things":
                    assertIsNull(Things, INVALID_DUPLICATE_REFERENCE);
                    this.Things = new JSONThing[1];
                    this.Things[0] = new JSONThing();
                    this.Things[0].identifier = referencedFromID;
                    return;
                default:
                    throw new IllegalArgumentException(INVALID_BACKREFERENCE);
            }
        }
    }

    @Override
    public Location parseToDTO(JSONBase.EntityType type) {
        GeoJsonReader reader;
        switch (type) {
            case FULL:
                parseReferencedFrom();
                assertNotNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                assertNotNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
                assertNotNull(encodingType, INVALID_INLINE_ENTITY_MISSING + "encodingType");
                assertNotNull(encodingType, INVALID_ENCODINGTYPE);
                assertState(Objects.equals(encodingType, ENCODINGTYPE_GEOJSON) ||
                                  Objects.equals(encodingType, ENCODINGTYPE_GEOJSON_ALT), INVALID_ENCODINGTYPE);

                assertNotNull(location, INVALID_INLINE_ENTITY_MISSING + "location");
                //TODO: check what is actually allowed here.
                if (location.has(GEOMETRY)) {
                    assertState("Feature".equals(location.get(TYPE).asText()),
                                  INVALID_INLINE_ENTITY_MISSING + LOCATION_TYPE);
                    assertNotNull(location.get(GEOMETRY), INVALID_INLINE_ENTITY_MISSING + LOCATION_GEOM);
                } else {
                    assertState("Point".equals(location.get(TYPE).asText()),
                                  INVALID_INLINE_ENTITY_MISSING + LOCATION_TYPE);
                    assertNotNull(location.get(COORDINATES), INVALID_INLINE_ENTITY_MISSING + LOCATION_GEOM);
                }
                self.setId(identifier);
                self.setName(name);
                self.setDescription(description);

                // This is already set by default
                // self.setEncodingType(encodingType);

                reader = new GeoJsonReader(factory);
                try {
                    if (location.has(GEOMETRY)) {
                        self.setGeometry(reader.read(location.get(GEOMETRY).toString()));
                    } else {
                        self.setGeometry(reader.read(location.toString()));
                    }
                } catch (ParseException e) {
                    assertNotNull(null, COULD_NOT_PARSE + e.getMessage());
                }

                if (properties != null) {
                    self.setProperties(properties);
                }

                if (Things != null) {
                    self.setThings(Arrays.stream(Things)
                                       .map(thing -> thing.parseToDTO(JSONBase.EntityType.FULL,
                                                                      JSONBase.EntityType.REFERENCE))
                                       .collect(Collectors.toSet()));
                }
                if (HistoricalLocations != null) {
                    self.setHistoricalLocations(Arrays.stream(HistoricalLocations)
                                                    .map(loc -> loc.parseToDTO(JSONBase.EntityType.FULL,
                                                                               JSONBase.EntityType.REFERENCE))
                                                    .collect(Collectors.toSet()));
                }

                if (backReference != null) {
                    if (backReference instanceof JSONThing) {
                        if (self.getThings() != null) {
                            self.getThings().add(((JSONThing) backReference).getEntity());
                        } else {
                            self.setThings(Collections.singleton(((JSONThing) backReference).getEntity()));
                        }
                    } else {
                        JSONHistoricalLocation backRef = (JSONHistoricalLocation) backReference;
                        self.addHistoricalLocation(backRef.getEntity());
                    }
                }
                return self;

            case PATCH:
                parseReferencedFrom();
                self.setId(identifier);
                self.setName(name);
                self.setDescription(description);

                // This is already set by default
                // self.setEncodingType(encodingType);

                if (encodingType != null) {
                    assertState(
                        encodingType.equals(ENCODINGTYPE_GEOJSON) || encodingType.equals(ENCODINGTYPE_GEOJSON_ALT),
                        INVALID_ENCODINGTYPE);
                }

                if (location != null) {
                    reader = new GeoJsonReader(factory);
                    try {
                        if (location.has(GEOMETRY)) {
                            self.setGeometry(reader.read(location.get(GEOMETRY).toString()));
                        } else {
                            self.setGeometry(reader.read(location.toString()));
                        }
                    } catch (ParseException e) {
                        assertNotNull(null, COULD_NOT_PARSE + e.getMessage());
                    }
                }

                if (properties != null) {
                    self.setProperties(properties);
                }

                if (Things != null) {
                    self.setThings(Arrays.stream(Things)
                                       .map(thing -> thing.parseToDTO(JSONBase.EntityType.REFERENCE))
                                       .collect(Collectors.toSet()));
                }
                if (HistoricalLocations != null) {
                    self.setHistoricalLocations(Arrays.stream(HistoricalLocations)
                                                    .map(loc -> loc.parseToDTO(JSONBase.EntityType.REFERENCE))
                                                    .collect(Collectors.toSet()));
                }
                return self;

            case REFERENCE:
                assertIsNull(name, INVALID_REFERENCED_ENTITY);
                assertIsNull(description, INVALID_REFERENCED_ENTITY);
                assertIsNull(encodingType, INVALID_REFERENCED_ENTITY);
                assertIsNull(location, INVALID_REFERENCED_ENTITY);
                assertIsNull(properties, INVALID_REFERENCED_ENTITY);
                assertIsNull(Things, INVALID_REFERENCED_ENTITY);

                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }
}
