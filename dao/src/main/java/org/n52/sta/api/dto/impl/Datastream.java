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
package org.n52.sta.api.dto.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.api.dto.ThingDTO;

import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class Datastream extends Entity implements DatastreamDTO {

    private String name;

    private String description;

    private String observationType;

    private UnitOfMeasurement unitOfMeasurement;

    private Geometry observedArea;

    private Time phenomenonTime;

    private Time resultTime;

    private ObjectNode properties;

    private ThingDTO thing;

    private SensorDTO sensor;

    private ObservedPropertyDTO observedProperty;

    private Set<ObservationDTO> observations;

    public Datastream() {
    }

    @Override public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override public String getObservationType() {
        return observationType;
    }

    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }

    @Override public UnitOfMeasurement getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    @Override public Geometry getObservedArea() {
        return observedArea;
    }

    public void setObservedArea(Geometry observedArea) {
        this.observedArea = observedArea;
    }

    @Override public Time getPhenomenonTime() {
        return phenomenonTime;
    }

    @Override public void setPhenomenonTime(Time phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

    @Override public Time getResultTime() {
        return resultTime;
    }

    @Override public void setResultTime(Time resultTime) {
        this.resultTime = resultTime;
    }

    @Override public ObjectNode getProperties() {
        return properties;
    }

    public void setProperties(ObjectNode properties) {
        this.properties = properties;
    }

    @Override public ThingDTO getThing() {
        return thing;
    }

    public void setThing(ThingDTO thing) {
        this.thing = thing;
    }

    @Override public SensorDTO getSensor() {
        return sensor;
    }

    public void setSensor(SensorDTO sensor) {
        this.sensor = sensor;
    }

    @Override public ObservedPropertyDTO getObservedProperty() {
        return observedProperty;
    }

    public void setObservedProperty(ObservedPropertyDTO observedProperty) {
        this.observedProperty = observedProperty;
    }

    @Override public Set<ObservationDTO> getObservations() {
        return observations;
    }

    public void setObservations(Set<ObservationDTO> observations) {
        this.observations = observations;
    }
}
