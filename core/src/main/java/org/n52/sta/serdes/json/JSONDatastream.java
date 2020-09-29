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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sta.StaConstants;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONDatastream extends JSONBase.JSONwithIdNameDescriptionTime<AbstractDatasetEntity>
    implements AbstractJSONEntity {

    private static final String COULD_NOT_PARSE_OBS_AREA = "Could not parse observedArea to GeoJSON. Error was: ";
    // JSON Properties. Matched by Annotation or variable name
    public String observationType;
    public JSONUnitOfMeasurement unitOfMeasurement;
    public JsonNode observedArea;
    public String phenomenonTime;
    public String resultTime;
    public JsonNode properties;

    @JsonManagedReference
    public JSONSensor Sensor;

    @JsonManagedReference
    public JSONThing Thing;

    @JsonManagedReference
    public JSONObservedProperty ObservedProperty;

    @JsonManagedReference
    public JSONObservation[] Observations;

    private final GeometryFactory factory =
        new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    private final String OM_CategoryObservation =
        "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation";
    private final String OM_CountObservation =
        "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation";
    private final String OM_Measurement =
        "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";
    private final String OM_Observation =
        "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";
    private final String OM_TruthObservation =
        "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation";
    private final String obsType = "observationType";
    private final String uomName = "unitOfMeasurement->name";
    private final String uomSymbol = "unitOfMeasurement->symbol";
    private final String uomDef = "unitOfMeasurement->definition";

    public JSONDatastream() {
        self = new AbstractDatasetEntity();
    }

    @Override protected void parseReferencedFrom() {
        if (referencedFromType != null) {
            switch (referencedFromType) {
                case StaConstants.SENSORS:
                    Assert.isNull(Sensor, INVALID_DUPLICATE_REFERENCE);
                    this.Sensor = new JSONSensor();
                    this.Sensor.identifier = referencedFromID;
                    return;
                case StaConstants.OBSERVED_PROPERTIES:
                    Assert.isNull(ObservedProperty, INVALID_DUPLICATE_REFERENCE);
                    this.ObservedProperty = new JSONObservedProperty();
                    this.ObservedProperty.identifier = referencedFromID;
                    return;
                case StaConstants.THINGS:
                    Assert.isNull(Thing, INVALID_DUPLICATE_REFERENCE);
                    this.Thing = new JSONThing();
                    this.Thing.identifier = referencedFromID;
                    return;
                default:
                    throw new IllegalArgumentException(INVALID_BACKREFERENCE);
            }
        }
    }

    @Override
    public AbstractDatasetEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                Assert.notNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                Assert.notNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
                Assert.notNull(observationType, INVALID_INLINE_ENTITY_MISSING + obsType);
                Assert.state(observationType.equals(OM_Measurement)
                                 || observationType.equals(OM_CountObservation)
                                 || observationType.equals(OM_CategoryObservation)
                                 || observationType.equals(OM_Observation)
                                 || observationType.equals(OM_TruthObservation),
                             INVALID_INLINE_ENTITY_INVALID_VALUE + obsType);
                Assert.notNull(unitOfMeasurement, INVALID_INLINE_ENTITY_MISSING + "unitOfMeasurement");
                // Check if we have special null-unit
                if (unitOfMeasurement.name != null) {
                    Assert.notNull(unitOfMeasurement.name, INVALID_INLINE_ENTITY_MISSING + uomName);
                    Assert.notNull(unitOfMeasurement.symbol, INVALID_INLINE_ENTITY_MISSING + uomSymbol);
                    Assert.notNull(unitOfMeasurement.definition, INVALID_INLINE_ENTITY_MISSING + uomDef);
                } else {
                    Assert.isNull(unitOfMeasurement.name, INVALID_INLINE_ENTITY_MISSING + uomName);
                    Assert.isNull(unitOfMeasurement.symbol, INVALID_INLINE_ENTITY_MISSING + uomSymbol);
                    Assert.isNull(unitOfMeasurement.definition, INVALID_INLINE_ENTITY_MISSING + uomDef);
                }
                return createPostEntity();
            case PATCH:
                parseReferencedFrom();
                return createPatchEntity();

            case REFERENCE:
                Assert.isNull(name, INVALID_REFERENCED_ENTITY);
                Assert.isNull(description, INVALID_REFERENCED_ENTITY);
                Assert.isNull(observationType, INVALID_REFERENCED_ENTITY);
                Assert.isNull(unitOfMeasurement, INVALID_REFERENCED_ENTITY);
                Assert.isNull(observedArea, INVALID_REFERENCED_ENTITY);
                Assert.isNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
                Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
                Assert.isNull(properties, INVALID_REFERENCED_ENTITY);

                Assert.isNull(Sensor, INVALID_REFERENCED_ENTITY);
                Assert.isNull(ObservedProperty, INVALID_REFERENCED_ENTITY);
                Assert.isNull(Thing, INVALID_REFERENCED_ENTITY);
                Assert.isNull(Observations, INVALID_REFERENCED_ENTITY);

                self.setIdentifier(identifier);
                self.setStaIdentifier(identifier);
                return self;
            default:
                return null;
        }
    }

    private AbstractDatasetEntity createPatchEntity() {
        self.setIdentifier(identifier);
        self.setStaIdentifier(identifier);
        self.setName(name);
        self.setDescription(description);

        if (observationType != null) {
            self.setOMObservationType(new FormatEntity().setFormat(observationType));
        }

        if (observedArea != null) {
            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                self.setGeometry(reader.read(observedArea.toString()));
            } catch (ParseException e) {
                Assert.notNull(null, COULD_NOT_PARSE_OBS_AREA + e.getMessage());
            }
        }

        if (unitOfMeasurement != null) {
            if (unitOfMeasurement.name == null) {
                self.setUnit(null);
            } else {
                UnitEntity unit = new UnitEntity();
                unit.setLink(unitOfMeasurement.definition);
                unit.setName(unitOfMeasurement.name);
                unit.setSymbol(unitOfMeasurement.symbol);
                self.setUnit(unit);
            }
        }

        if (resultTime != null) {
            Time time = parseTime(resultTime);
            if (time instanceof TimeInstant) {
                self.setResultTimeStart(((TimeInstant) time).getValue().toDate());
                self.setResultTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setResultTimeStart(((TimePeriod) time).getStart().toDate());
                self.setResultTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }

        if (properties != null) {
            self.setParameters(convertParameters(properties, ParameterFactory.EntityType.DATASET));
        }

        // if (phenomenonTime != null) {
        // phenomenonTime (aka samplingTime) is automatically calculated based on associated Observations
        // phenomenonTime parsed from json is therefore ignored.
        // }
        if (Thing != null) {
            self.setThing(Thing.toEntity(JSONBase.EntityType.REFERENCE));
        }

        if (Sensor != null) {
            self.setProcedure(Sensor.toEntity(JSONBase.EntityType.REFERENCE));
        }

        if (ObservedProperty != null) {
            self.setObservableProperty(ObservedProperty.toEntity(JSONBase.EntityType.REFERENCE));
        }

        if (Observations != null) {
            self.setObservations(Arrays.stream(Observations)
                                     .map(obs -> obs.toEntity(JSONBase.EntityType.REFERENCE))
                                     .collect(Collectors.toSet()));
        }
        return self;
    }

    private AbstractDatasetEntity createPostEntity() {
        self.setIdentifier(identifier);
        self.setStaIdentifier(identifier);
        self.setName(name);
        self.setDescription(description);
        self.setOMObservationType(new FormatEntity().setFormat(observationType));

        if (observedArea != null) {
            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                self.setGeometry(reader.read(observedArea.toString()));
            } catch (ParseException e) {
                Assert.notNull(null, COULD_NOT_PARSE_OBS_AREA + e.getMessage());
            }
        }

        if (unitOfMeasurement.name != null) {
            UnitEntity unit = new UnitEntity();
            unit.setLink(unitOfMeasurement.definition);
            unit.setName(unitOfMeasurement.name);
            unit.setSymbol(unitOfMeasurement.symbol);
            self.setUnit(unit);
        }

        if (resultTime != null) {
            Time time = parseTime(resultTime);
            if (time instanceof TimeInstant) {
                self.setResultTimeStart(((TimeInstant) time).getValue().toDate());
                self.setResultTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setResultTimeStart(((TimePeriod) time).getStart().toDate());
                self.setResultTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }

        if (properties != null) {
            self.setParameters(convertParameters(properties, ParameterFactory.EntityType.DATASET));
        }

        if (Thing != null) {
            self.setThing(Thing.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONThing) {
            self.setThing(((JSONThing) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "Thing");
        }

        if (Sensor != null) {
            self.setProcedure(Sensor.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONSensor) {
            self.setProcedure(((JSONSensor) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "Sensor");
        }

        if (ObservedProperty != null) {
            self.setObservableProperty(
                ObservedProperty.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONObservedProperty) {
            self.setObservableProperty(((JSONObservedProperty) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "ObservedProperty");
        }

        if (Observations != null) {
            self.setObservations(Arrays.stream(Observations)
                                     .map(entity -> entity.toEntity(JSONBase.EntityType.FULL,
                                                                    JSONBase.EntityType.REFERENCE))
                                     .collect(Collectors.toSet()));
        } else if (backReference instanceof JSONObservation) {
            self.setObservations(Collections.singleton(((JSONObservation) backReference).self));
        }
        return self;
    }

    static class JSONUnitOfMeasurement {

        public String symbol;

        public String name;

        public String definition;
    }

}
