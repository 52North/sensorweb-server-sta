/*
 * Copyright (C) 2018-2023 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.serdes.json;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.data.service.ServiceUtils;
import org.springframework.util.Assert;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONFeatureOfInterest extends JSONBase.JSONwithIdNameDescription<FeatureEntity>
    implements AbstractJSONEntity {

    private static final String COULD_NOT_PARSE = "Could not parse feature to GeoJSON. Error was: ";
    // JSON Properties. Matched by Annotation or variable name
    public String encodingType;

    public JsonNode feature;
    public JsonNode properties;

    @JsonManagedReference
    public JSONObservation[] Observations;

    private final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";
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
        self = new FeatureEntity();
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    public FeatureEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                Assert.notNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                Assert.notNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
                Assert.notNull(feature, INVALID_INLINE_ENTITY_MISSING + "feature");
                Assert.notNull(feature.get(TYPE), INVALID_INLINE_ENTITY_MISSING + FEATURE_TYPE);
                Assert.notNull(encodingType, INVALID_ENCODINGTYPE);
                Assert.isTrue(Objects.equals(encodingType, ENCODINGTYPE_GEOJSON), INVALID_ENCODINGTYPE);

                self.setIdentifier(identifier);
                self.setName(name);
                self.setDescription(description);

                if (properties != null) {
                    self.setParameters(convertParameters(properties, self));
                }

                if (feature != null) {
                    //TODO: check what is actually allowed here
                    GeoJsonReader reader = new GeoJsonReader(factory);
                    String geo;
                    if (FEATURE.equals(feature.get(TYPE).asText())) {
                        Assert.notNull(feature.get(GEOMETRY), INVALID_INLINE_ENTITY_MISSING + FEATURE_GEOM);
                        geo = feature.get(GEOMETRY).toString();
                    } else {
                        Assert.isTrue(POINT.equals(feature.get(TYPE).asText()),
                                      INVALID_INLINE_ENTITY_MISSING + FEATURE_TYPE);
                        Assert.isTrue(feature.has(COORDINATES), INVALID_INLINE_ENTITY_MISSING + FEATURE_COORDS);
                        geo = feature.toString();
                    }
                    try {
                        self.setGeometry(reader.read(geo));
                    } catch (ParseException e) {
                        Assert.notNull(null, COULD_NOT_PARSE + e.getMessage());
                    }
                    self.setFeatureType(ServiceUtils.createFeatureType(self.getGeometry()));
                }

                return self;

            case PATCH:
                self.setIdentifier(identifier);
                self.setStaIdentifier(identifier);
                self.setName(name);
                self.setDescription(description);

                if (encodingType != null) {
                    Assert.state(encodingType.equals(ENCODINGTYPE_GEOJSON), INVALID_ENCODINGTYPE);
                }

                if (properties != null) {
                    self.setParameters(convertParameters(properties, self));
                }

                if (feature != null) {
                    //TODO: check what is actually allowed here
                    GeoJsonReader reader = new GeoJsonReader(factory);
                    String geo;
                    if (FEATURE.equals(feature.get(TYPE).asText())) {
                        Assert.notNull(feature.get(GEOMETRY), INVALID_INLINE_ENTITY_MISSING + FEATURE_GEOM);
                        geo = feature.get(GEOMETRY).toString();
                    } else {
                        Assert.isTrue(POINT.equals(feature.get(TYPE).asText()),
                                      INVALID_INLINE_ENTITY_MISSING + FEATURE_TYPE);
                        Assert.isTrue(feature.has(COORDINATES), INVALID_INLINE_ENTITY_MISSING + FEATURE_COORDS);
                        geo = feature.toString();
                    }
                    try {
                        self.setGeometry(reader.read(geo));
                    } catch (ParseException e) {
                        Assert.notNull(null, COULD_NOT_PARSE + e.getMessage());
                    }
                    self.setFeatureType(ServiceUtils.createFeatureType(self.getGeometry()));
                }

                // TODO: handle nested observations
                // if (backReference != null) {
                // TODO: link feature to observations?
                // throw new NotImplementedException();
                // }

                return self;
            case REFERENCE:
                Assert.isNull(name, INVALID_REFERENCED_ENTITY);
                Assert.isNull(description, INVALID_REFERENCED_ENTITY);
                Assert.isNull(encodingType, INVALID_REFERENCED_ENTITY);
                Assert.isNull(feature, INVALID_REFERENCED_ENTITY);
                Assert.isNull(properties, INVALID_REFERENCED_ENTITY);
                Assert.isNull(Observations, INVALID_REFERENCED_ENTITY);

                self.setIdentifier(identifier);
                self.setStaIdentifier(identifier);
                return self;
            default:
                return null;
        }
    }
}
