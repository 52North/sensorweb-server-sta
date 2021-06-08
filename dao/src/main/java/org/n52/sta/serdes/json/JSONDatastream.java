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

package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.impl.Datastream;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
public class JSONDatastream extends JSONBase.JSONwithIdNameDescriptionTime<DatastreamDTO>
    implements AbstractJSONEntity {

    private static final String COULD_NOT_PARSE_OBS_AREA = "Could not parse observedArea to GeoJSON. Error was: ";
    // JSON Properties. Matched by Annotation or variable name
    public String observationType;
    public DatastreamDTO.UnitOfMeasurement unitOfMeasurement;
    public JsonNode observedArea;
    public String phenomenonTime;
    public String resultTime;
    public ObjectNode properties;
    @JsonManagedReference
    public JSONSensor Sensor;
    @JsonManagedReference
    public JSONThing Thing;
    @JsonManagedReference
    public JSONObservedProperty ObservedProperty;
    @JsonManagedReference
    public JSONObservation[] Observations;
    @JsonManagedReference
    public JSONParty Party;
    @JsonManagedReference
    public JSONProject Project;
    private final GeometryFactory factory =
        new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    private final String OBS_TYPE_SENSORML_OBSERVATION =
        "http://www.52north.org/def/observationType/OGC-OM/2.0/OM_SensorML20Observation";
    private final String obsType = "observationType";
    private final String uomName = "unitOfMeasurement->name";
    private final String uomSymbol = "unitOfMeasurement->symbol";
    private final String uomDef = "unitOfMeasurement->definition";

    public JSONDatastream() {
        self = new Datastream();
    }

    private DatastreamDTO createPostEntity() {
        self.setId(identifier);
        self.setName(name);
        self.setDescription(description);
        self.setObservationType(observationType);

        if (observedArea != null) {
            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                self.setObservedArea(reader.read(observedArea.toString()));
            } catch (ParseException e) {
                Assert.notNull(null, COULD_NOT_PARSE_OBS_AREA + e.getMessage());
            }
        }

        if (unitOfMeasurement.getName() != null) {
            self.setUnitOfMeasurement(unitOfMeasurement);
        }

        if (resultTime != null) {
            Time time = parseTime(resultTime);
            self.setResultTime(parseTime(resultTime));
        }

        if (properties != null) {
            self.setProperties(properties);
        }

        if (Thing != null) {
            self.setThing(Thing.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONThing) {
            self.setThing(((JSONThing) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "Thing");
        }

        if (Sensor != null) {
            self.setSensor(Sensor.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONSensor) {
            self.setSensor(((JSONSensor) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "Sensor");
        }

        if (ObservedProperty != null) {
            self.setObservedProperty(
                ObservedProperty.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONObservedProperty) {
            self.setObservedProperty(((JSONObservedProperty) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "ObservedProperty");
        }

        if (Observations != null) {
            self.setObservations(Arrays.stream(Observations)
                                     .map(entity -> entity.parseToDTO(JSONBase.EntityType.FULL,
                                                                      JSONBase.EntityType.REFERENCE))
                                     .collect(Collectors.toSet()));
        } else if (backReference instanceof JSONObservation) {
            Set<ObservationDTO> observations = new HashSet<>(4);
            observations.add(((JSONObservation) backReference).self);
            self.setObservations(observations);
        }

        if (Party != null) {
            self.setParty(Party.parseToDTO(JSONBase.EntityType.FULL,
                                           JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONParty) {
            self.setParty(((JSONParty) backReference).self);
        }

        if (Project != null) {
            self.setProject(Project.parseToDTO(JSONBase.EntityType.FULL,
                                               JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONProject) {
            self.setProject(((JSONProject) backReference).self);
        }

        return self;
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
    public DatastreamDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                Assert.notNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                Assert.notNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
                Assert.notNull(observationType, INVALID_INLINE_ENTITY_MISSING + obsType);
                Assert.state(observationType.equals(OmConstants.OBS_TYPE_MEASUREMENT)
                                 || observationType.equals(OmConstants.OBS_TYPE_COUNT_OBSERVATION)
                                 || observationType.equals(OmConstants.OBS_TYPE_CATEGORY_OBSERVATION)
                                 || observationType.equals(OmConstants.OBS_TYPE_TEXT_OBSERVATION)
                                 || observationType.equals(OmConstants.OBS_TYPE_OBSERVATION)
                                 || observationType.equals(OmConstants.OBS_TYPE_TRUTH_OBSERVATION)
                                 || observationType.equals(OBS_TYPE_SENSORML_OBSERVATION),
                             INVALID_INLINE_ENTITY_INVALID_VALUE + obsType);
                Assert.notNull(unitOfMeasurement, INVALID_INLINE_ENTITY_MISSING + "unitOfMeasurement");
                // Check if we have special null-unit
                if (unitOfMeasurement.getName() != null) {
                    Assert.notNull(unitOfMeasurement.getName(), INVALID_INLINE_ENTITY_MISSING + uomName);
                    Assert.notNull(unitOfMeasurement.getSymbol(), INVALID_INLINE_ENTITY_MISSING + uomSymbol);
                    Assert.notNull(unitOfMeasurement.getDefinition(), INVALID_INLINE_ENTITY_MISSING + uomDef);
                } else {
                    Assert.isNull(unitOfMeasurement.getName(), INVALID_INLINE_ENTITY_MISSING + uomName);
                    Assert.isNull(unitOfMeasurement.getSymbol(), INVALID_INLINE_ENTITY_MISSING + uomSymbol);
                    Assert.isNull(unitOfMeasurement.getDefinition(), INVALID_INLINE_ENTITY_MISSING + uomDef);
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

                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }

    private DatastreamDTO createPatchEntity() {
        self.setId(identifier);
        self.setName(name);
        self.setDescription(description);

        if (observationType != null) {
            self.setObservationType(observationType);
        }

        if (observedArea != null) {
            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                self.setObservedArea(reader.read(observedArea.toString()));
            } catch (ParseException e) {
                Assert.notNull(null, COULD_NOT_PARSE_OBS_AREA + e.getMessage());
            }
        }

        if (unitOfMeasurement != null && unitOfMeasurement.getName() != null) {
            self.setUnitOfMeasurement(unitOfMeasurement);
        }

        if (resultTime != null) {
            self.setResultTime(parseTime(resultTime));
        }

        if (properties != null) {
            self.setProperties(properties);
        }

        // if (phenomenonTime != null) {
        // phenomenonTime (aka samplingTime) is automatically calculated based on associated Observations
        // phenomenonTime parsed from json is therefore ignored.
        // }
        
        if (Thing != null) {
            self.setThing(Thing.parseToDTO(JSONBase.EntityType.REFERENCE));
        }

        if (Sensor != null) {
            self.setSensor(Sensor.parseToDTO(JSONBase.EntityType.REFERENCE));
        }

        if (ObservedProperty != null) {
            self.setObservedProperty(ObservedProperty.parseToDTO(JSONBase.EntityType.REFERENCE));
        }

        if (Observations != null) {
            self.setObservations(Arrays.stream(Observations)
                                     .map(obs -> obs.parseToDTO(JSONBase.EntityType.REFERENCE))
                                     .collect(Collectors.toSet()));
        }

        if (Party != null) {
            self.setParty(Party.parseToDTO(JSONBase.EntityType.FULL,
                                           JSONBase.EntityType.REFERENCE));
        }

        if (Project != null) {
            self.setProject(Project.parseToDTO(JSONBase.EntityType.FULL,
                                               JSONBase.EntityType.REFERENCE));
        }
        return self;
    }
}
