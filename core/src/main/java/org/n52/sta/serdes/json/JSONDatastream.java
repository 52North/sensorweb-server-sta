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
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONDatastream extends JSONBase.JSONwithIdNameDescriptionTime<DatastreamEntity>
        implements AbstractJSONEntity {

    private static final String COULD_NOT_PARSE_OBS_AREA = "Could not parse observedArea to GeoJSON. Error was: ";
    // JSON Properties. Matched by Annotation or variable name
    public String observationType;
    public JSONUnitOfMeasurement unitOfMeasurement;
    public JsonNode observedArea;
    public String phenomenonTime;
    public String resultTime;

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

    public JSONDatastream() {
        self = new DatastreamEntity();
    }

    @Override
    public DatastreamEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
        case FULL:
            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
            Assert.notNull(observationType, INVALID_INLINE_ENTITY + obsType);
            Assert.state(observationType.equals(OM_Measurement) || observationType.equals(OM_CountObservation)
                                 || observationType.equals(OM_CategoryObservation) ||
                                 observationType.equals(OM_Observation)
                                 || observationType.equals(OM_TruthObservation), INVALID_INLINE_ENTITY + obsType);
            Assert.notNull(unitOfMeasurement, INVALID_INLINE_ENTITY + "unitOfMeasurement");
            Assert.notNull(unitOfMeasurement.name, INVALID_INLINE_ENTITY + "unitOfMeasurement->name");
            Assert.notNull(unitOfMeasurement.symbol, INVALID_INLINE_ENTITY + "unitOfMeasurement->symbol");
            Assert.notNull(unitOfMeasurement.definition, INVALID_INLINE_ENTITY + "unitOfMeasurement->definition");

            return createPostEntity();
        case PATCH:
            return createPatchEntity();

        case REFERENCE:
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(observationType, INVALID_REFERENCED_ENTITY);
            Assert.isNull(unitOfMeasurement, INVALID_REFERENCED_ENTITY);
            Assert.isNull(observedArea, INVALID_REFERENCED_ENTITY);
            Assert.isNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);

            Assert.isNull(Sensor, INVALID_REFERENCED_ENTITY);
            Assert.isNull(ObservedProperty, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Thing, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Observations, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        default:
            return null;
        }
    }

    private DatastreamEntity createPatchEntity() {
        self.setIdentifier(identifier);
        self.setName(name);
        self.setDescription(description);

        if (observationType != null) {
            self.setObservationType(new FormatEntity().setFormat(observationType));
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

        // if (phenomenonTime != null) {
        // phenomenonTime (aka samplingTime) is automatically calculated based
        // on associated Observations
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
            self.setObservations(Arrays.stream(Observations).map(obs -> obs.toEntity(JSONBase.EntityType.REFERENCE))
                                       .collect(Collectors.toSet()));
        }

        return self;
    }

    private DatastreamEntity createPostEntity() {
        self.setIdentifier(identifier);
        self.setName(name);
        self.setDescription(description);
        self.setObservationType(new FormatEntity().setFormat(observationType));

        if (observedArea != null) {
            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                self.setGeometry(reader.read(observedArea.toString()));
            } catch (ParseException e) {
                Assert.notNull(null, COULD_NOT_PARSE_OBS_AREA + e.getMessage());
            }
        }

        UnitEntity unit = new UnitEntity();
        unit.setLink(unitOfMeasurement.definition);
        unit.setName(unitOfMeasurement.name);
        unit.setSymbol(unitOfMeasurement.symbol);
        self.setUnit(unit);

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

        if (Thing != null) {
            self.setThing(Thing.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONThing) {
            self.setThing(((JSONThing) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY + "Thing");
        }

        if (Sensor != null) {
            self.setProcedure(Sensor.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONSensor) {
            self.setProcedure(((JSONSensor) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY + "Sensor");
        }

        if (ObservedProperty != null) {
            self.setObservableProperty(
                    ObservedProperty.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONObservedProperty) {
            self.setObservableProperty(((JSONObservedProperty) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY + "ObservedProperty");
        }

        if (Observations != null) {
            self.setObservations(Arrays.stream(Observations)
                                       .map(entity -> entity.toEntity(JSONBase.EntityType.FULL,
                                                                      JSONBase.EntityType.REFERENCE))
                                       .map(entity -> {
                                           entity.setId(new Random().nextLong());
                                           return entity;
                                       })
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
