/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
 * Software GmbH
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

package org.n52.sta.api.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.api.dto.impl.FeatureOfInterest;
import org.n52.sta.api.serdes.common.AbstractJSONEntity;
import org.n52.sta.api.serdes.common.JSONBase;

import java.util.Objects;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONFeatureOfInterest extends JSONBase.JSONwithIdNameDescription<FeatureOfInterestDTO>
    implements AbstractJSONEntity {

    private static final String COULD_NOT_PARSE = "Could not parse feature to GeoJSON. Error was: ";
    // JSON Properties. Matched by Annotation or variable name
    public String encodingType;

    public JsonNode feature;
    public ObjectNode properties;

    @JsonManagedReference
    public JSONObservation[] Observations;

    private final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";
    private final String ENCODINGTYPE_GEOJSON_ALT = "application/geo+json";
    private final String INVALID_ENCODINGTYPE =
        "Invalid encodingType supplied. Only GeoJSON (application/vnd.geo+json) is supported!";
    private final GeometryFactory factory =
        new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    private final String TYPE = "type";
    private final String GEOMETRY = "geometry";
    private final String FEATURE = "Feature";
    private final String COORDINATES = "coordinates";
    private final String POINT = "Point";
    private final String FEATURE_TYPE = "feature->type";
    private final String FEATURE_COORDS = "feature->coordinates";
    private final String FEATURE_GEOM = "feature->geometry";

    public JSONFeatureOfInterest() {
        self = new FeatureOfInterest();
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    public FeatureOfInterestDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                assertNotNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                assertNotNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
                assertNotNull(feature, INVALID_INLINE_ENTITY_MISSING + "feature");
                assertNotNull(feature.get(TYPE), INVALID_INLINE_ENTITY_MISSING + FEATURE_TYPE);
                assertNotNull(encodingType, INVALID_ENCODINGTYPE);
                assertState(Objects.equals(encodingType, ENCODINGTYPE_GEOJSON) ||
                            Objects.equals(encodingType, ENCODINGTYPE_GEOJSON_ALT), INVALID_ENCODINGTYPE);

                self.setId(identifier);
                self.setName(name);
                self.setDescription(description);

                if (properties != null) {
                    self.setProperties(properties);
                }

                if (feature != null) {
                    //TODO: check what is actually allowed here
                    GeoJsonReader reader = new GeoJsonReader(factory);
                    String geo;
                    if (FEATURE.equals(feature.get(TYPE).asText())) {
                        assertNotNull(feature.get(GEOMETRY), INVALID_INLINE_ENTITY_MISSING + FEATURE_GEOM);
                        geo = feature.get(GEOMETRY).toString();
                    } else {
                        assertState(POINT.equals(feature.get(TYPE).asText()),
                                     INVALID_INLINE_ENTITY_MISSING + FEATURE_TYPE);
                        assertState(feature.has(COORDINATES), INVALID_INLINE_ENTITY_MISSING + FEATURE_COORDS);
                        geo = feature.toString();
                    }
                    try {
                        self.setFeature(reader.read(geo));
                    } catch (ParseException e) {
                        assertNotNull(null, COULD_NOT_PARSE + e.getMessage());
                    }
                }

                return self;

            case PATCH:
                self.setId(identifier);
                self.setName(name);
                self.setDescription(description);

                if (encodingType != null) {
                    assertState(encodingType.equals(ENCODINGTYPE_GEOJSON) ||
                                     Objects.equals(encodingType, ENCODINGTYPE_GEOJSON_ALT), INVALID_ENCODINGTYPE);
                }

                if (properties != null) {
                    self.setProperties(properties);
                }

                if (feature != null) {
                    //TODO: check what is actually allowed here
                    GeoJsonReader reader = new GeoJsonReader(factory);
                    String geo;
                    if (FEATURE.equals(feature.get(TYPE).asText())) {
                        assertNotNull(feature.get(GEOMETRY), INVALID_INLINE_ENTITY_MISSING + FEATURE_GEOM);
                        geo = feature.get(GEOMETRY).toString();
                    } else {
                        assertState(POINT.equals(feature.get(TYPE).asText()),
                                      INVALID_INLINE_ENTITY_MISSING + FEATURE_TYPE);
                        assertState(feature.has(COORDINATES), INVALID_INLINE_ENTITY_MISSING + FEATURE_COORDS);
                        geo = feature.toString();
                    }
                    try {
                        self.setFeature(reader.read(geo));
                    } catch (ParseException e) {
                        assertNotNull(null, COULD_NOT_PARSE + e.getMessage());
                    }
                }

                // TODO: handle nested observations
                // if (backReference != null) {
                // TODO: link feature to observations?
                // throw new NotImplementedException();
                // }

                return self;
            case REFERENCE:
                assertIsNull(name, INVALID_REFERENCED_ENTITY);
                assertIsNull(description, INVALID_REFERENCED_ENTITY);
                assertIsNull(encodingType, INVALID_REFERENCED_ENTITY);
                assertIsNull(feature, INVALID_REFERENCED_ENTITY);
                assertIsNull(properties, INVALID_REFERENCED_ENTITY);
                assertIsNull(Observations, INVALID_REFERENCED_ENTITY);

                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }
}
