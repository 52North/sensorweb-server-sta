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
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.utils.IdGenerator;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONObservation extends JSONBase.JSONwithIdTime<DataEntity<?>> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String phenomenonTime;
    public String resultTime;
    public Object result;
    public Object resultQuality;
    public String validTime;
    public ObjectNode parameters;

    @JsonManagedReference
    public JSONFeatureOfInterest FeatureOfInterest;
    @JsonManagedReference
    public JSONDatastream Datastream;

    private final String NAME = "name";
    private final String VALUE = "value";

    public JSONObservation() {
        self = new TextDataEntity();
    }

    @Override
    protected void parseReferencedFrom() {
        if (referencedFromType != null) {
            switch (referencedFromType) {
                case StaConstants.DATASTREAMS:
                    Assert.isNull(Datastream, INVALID_DUPLICATE_REFERENCE);
                    this.Datastream = new JSONDatastream();
                    this.Datastream.identifier = referencedFromID;
                    return;
                case StaConstants.FEATURES_OF_INTEREST:
                    Assert.isNull(FeatureOfInterest, INVALID_DUPLICATE_REFERENCE);
                    this.FeatureOfInterest = new JSONFeatureOfInterest();
                    this.FeatureOfInterest.identifier = referencedFromID;
                    return;
                default:
                    throw new IllegalArgumentException(INVALID_BACKREFERENCE);
            }
        }
    }

    @Override
    public DataEntity<?> toEntity(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                Assert.notNull(result, INVALID_INLINE_ENTITY_MISSING + "result");
                return createPostEntity();
            case PATCH:
                parseReferencedFrom();
                return createPatchEntity();
            case REFERENCE:
                Assert.isNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
                Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
                Assert.isNull(result, INVALID_REFERENCED_ENTITY);
                Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
                Assert.isNull(resultQuality, INVALID_REFERENCED_ENTITY);
                Assert.isNull(parameters, INVALID_REFERENCED_ENTITY);

                self.setIdentifier(identifier);
                self.setStaIdentifier(identifier);
                return self;
            default:
                return null;
        }
    }

    private DataEntity<?> createPatchEntity() {
        self.setIdentifier(identifier);
        self.setStaIdentifier(identifier);

        // parameters
        self.setParameters(convertParameters(parameters, self));

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

        self.setValueText(result.toString());

        // Link to Datastream
        if (Datastream != null) {
            self.setDataset(Datastream.toEntity(JSONBase.EntityType.REFERENCE));
        }

        // Link to FOI
        if (FeatureOfInterest != null) {
            self.setFeature(FeatureOfInterest.toEntity(JSONBase.EntityType.REFERENCE));
        }

        return self;
    }

    private DataEntity<?> createPostEntity() {
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
        self.setParameters(convertParameters(parameters, self));

        // result
        self.setValueText(result.toString());

        // Link to Datastream
        if (Datastream != null) {
            self.setDataset(
                Datastream.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONDatastream) {
            self.setDataset(((JSONDatastream) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "Datastream");
        }

        // Link to FOI
        if (FeatureOfInterest != null) {
            self.setFeature(
                FeatureOfInterest.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONFeatureOfInterest) {
            self.setFeature(((JSONFeatureOfInterest) backReference).getEntity());
        }

        if (generatedId) {
            identifier = generateIdentifier();
        }
        self.setIdentifier(identifier);
        self.setStaIdentifier(identifier);

        return self;
    }

    private String generateIdentifier() {
        StringBuffer buffer = new StringBuffer();
        if (self.isSetDataset()) {
            buffer.append(self.getDataset().getIdentifier());
        }
        buffer.append(phenomenonTime)
            .append(result)
            .append(resultTime)
            .append(self.getVerticalFrom())
            .append(self.getVerticalTo());
        return IdGenerator.generate(buffer.toString());
    }

    public JSONObservation parseParameters(Map<String, String> propertyMapping) {
        if (parameters != null) {
            for (Map.Entry<String, String> mapping : propertyMapping.entrySet()) {
                Iterator<String> keyIt = parameters.fieldNames();
                while (keyIt.hasNext()) {
                    String paramName = keyIt.next();
                    if (paramName.equals(mapping.getValue())) {
                        JsonNode jsonNode = parameters.get(paramName);
                        switch (mapping.getKey()) {
                            case "samplingGeometry":
                                // Add as samplingGeometry to enable interoperability with SOS
                                GeometryFactory factory =
                                    new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
                                GeoJsonReader reader = new GeoJsonReader(factory);
                                try {
                                    GeometryEntity geometryEntity = new GeometryEntity();
                                    geometryEntity.setGeometry(reader.read(jsonNode.toString()));
                                    self.setGeometryEntity(geometryEntity);
                                } catch (ParseException e) {
                                    Assert.notNull(null, "Could not parse" + e.getMessage());
                                }
                                continue;
                            case "verticalFrom":
                                // Add as verticalTo to enable interoperability with SOS
                                self.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                                if (!self.hasVerticalFrom()) {
                                    self.setVerticalFrom(self.getVerticalTo());
                                }
                                continue;
                            case "verticalTo":
                                // Add as verticalTo to enable interoperability with SOS
                                self.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                                if (!self.hasVerticalTo()) {
                                    self.setVerticalTo(self.getVerticalFrom());
                                }
                                continue;
                            case "verticalFromTo":
                                // Add as verticalTo to enable interoperability with SOS
                                self.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                                self.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                                continue;
                            default:
                                throw new RuntimeException("Unable to parse Parameters!");
                        }
                    }
                }
            }
            // Remove parameters
            for (Map.Entry<String, String> mapping : propertyMapping.entrySet()) {
                parameters.remove(mapping.getValue());
            }
        }
        return this;
    }
}
