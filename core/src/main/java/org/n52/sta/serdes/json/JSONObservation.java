/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterJsonEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashSet;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONObservation extends JSONBase.JSONwithIdTime<StaDataEntity> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String phenomenonTime;
    public String resultTime;
    public String result;
    public Object resultQuality;
    public String validTime;
    public ArrayNode parameters;

    @JsonManagedReference
    public JSONFeatureOfInterest FeatureOfInterest;
    @JsonManagedReference
    public JSONDatastream Datastream;

    public JSONObservation() {
        self = new StaDataEntity();
    }

    @Override
    public StaDataEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
        case FULL:
            Assert.notNull(result, INVALID_INLINE_ENTITY + "result");
            return createPostEntity();
        case PATCH:
            return createPatchEntity();
        case REFERENCE:
            Assert.isNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(result, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultQuality, INVALID_REFERENCED_ENTITY);
            Assert.isNull(parameters, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        default:
            return null;
        }
    }

    private StaDataEntity createPatchEntity() {
        self.setIdentifier(identifier);

        // parameters
        handleParameters(parameters);

        // phenomenonTime
        if (phenomenonTime != null) {
            Time time = parseTime(phenomenonTime);
            if (time instanceof TimeInstant) {
                self.setSamplingTimeStart(((TimeInstant) time).getValue().toDate());
                self.setSamplingTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setSamplingTimeStart(((TimePeriod) time).getStart().toDate());
                self.setSamplingTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }

        // Set resultTime only when supplied
        if (resultTime != null) {
            // resultTime
            self.setResultTime(((TimeInstant) parseTime(resultTime)).getValue().toDate());
        }

        // validTime
        if (validTime != null) {
            Time time = parseTime(validTime);
            if (time instanceof TimeInstant) {
                self.setValidTimeStart(((TimeInstant) time).getValue().toDate());
                self.setValidTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setValidTimeStart(((TimePeriod) time).getStart().toDate());
                self.setValidTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }

        self.setValue(result);

        // Link to Datastream
        if (Datastream != null) {
            self.setDatastream(Datastream.toEntity(JSONBase.EntityType.REFERENCE));
        }

        // Link to FOI
        if (FeatureOfInterest != null) {
            self.setFeatureOfInterest(FeatureOfInterest.toEntity(JSONBase.EntityType.REFERENCE));
        }

        return self;
    }

    private StaDataEntity createPostEntity() {
        self.setIdentifier(identifier);

        // phenomenonTime
        if (phenomenonTime != null) {
            Time time = parseTime(phenomenonTime);
            if (time instanceof TimeInstant) {
                self.setSamplingTimeStart(((TimeInstant) time).getValue().toDate());
                self.setSamplingTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setSamplingTimeStart(((TimePeriod) time).getStart().toDate());
                self.setSamplingTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        } else {
            // Use time of POST Request as fallback
            Date date = DateTime.now().toDate();
            self.setSamplingTimeStart(date);
            self.setSamplingTimeEnd(date);
        }

        // Set resultTime only when supplied
        if (resultTime != null) {
            // resultTime
            self.setResultTime(((TimeInstant) parseTime(resultTime)).getValue().toDate());
        }

        // validTime
        if (validTime != null) {
            Time time = parseTime(validTime);
            if (time instanceof TimeInstant) {
                self.setValidTimeStart(((TimeInstant) time).getValue().toDate());
                self.setValidTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setValidTimeStart(((TimePeriod) time).getStart().toDate());
                self.setValidTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }

        // parameters
        handleParameters(parameters);

        // result
        self.setValue(result);

        // Link to Datastream
        if (Datastream != null) {
            self.setDatastream(
                    Datastream.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONDatastream) {
            self.setDatastream(((JSONDatastream) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY + "Datastream");
        }

        // Link to FOI
        if (FeatureOfInterest != null) {
            self.setFeatureOfInterest(
                    FeatureOfInterest.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONFeatureOfInterest) {
            self.setFeatureOfInterest(((JSONFeatureOfInterest) backReference).getEntity());
        }

        return self;
    }

    private void handleParameters(ArrayNode parameters) {
        // parameters
        if (parameters != null) {
            final String NAME = "name";
            final String VALUE = "value";
            HashSet<ParameterEntity<?>> parameterJsonEntities = new HashSet<>();
            for (JsonNode param : parameters) {

                // Check that structure is correct
                Assert.isTrue(param.has(NAME));
                Assert.isTrue(param.has(VALUE));
                Assert.isTrue(!param.has(2));

                if (param.get(NAME).asText().equals(Sos2Constants.HREF_PARAMETER_SPATIAL_FILTERING_PROFILE)) {
                    // Add as samplingGeometry to enable interoperability with SOS
                    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
                    JsonNode jsonNode = param.get(VALUE);
                    GeoJsonReader reader = new GeoJsonReader(factory);
                    try {
                        GeometryEntity geometryEntity = new GeometryEntity();
                        geometryEntity.setGeometry(reader.read(jsonNode.toString()));
                        self.setGeometryEntity(geometryEntity);
                    } catch (ParseException e) {
                        Assert.notNull(null, "Could not parse" + e.getMessage());
                    }

                }
                ParameterJsonEntity parameterEntity = new ParameterJsonEntity();
                parameterEntity.setName(param.get(NAME).asText());
                parameterEntity.setValue(param.get(VALUE).asText());
                parameterJsonEntities.add(parameterEntity);
            }
            self.setParameters(parameterJsonEntities);
        }
    }
}
