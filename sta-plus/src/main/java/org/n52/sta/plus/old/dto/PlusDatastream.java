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

package org.n52.sta.plus.old.dto;

import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.old.dto.Datastream;
import org.n52.sta.api.old.dto.DtoEntity;
import org.n52.sta.api.old.entity.DatastreamDTO;
import org.n52.sta.api.old.entity.ObservationDTO;
import org.n52.sta.api.old.entity.ObservedPropertyDTO;
import org.n52.sta.api.old.entity.SensorDTO;
import org.n52.sta.api.old.entity.ThingDTO;
import org.n52.sta.plus.old.entity.LicenseDTO;
import org.n52.sta.plus.old.entity.PartyDTO;
import org.n52.sta.plus.old.entity.PlusDatastreamDTO;
import org.n52.sta.plus.old.entity.ProjectDTO;

public class PlusDatastream extends DtoEntity implements PlusDatastreamDTO {

    private DatastreamDTO datastream;

    private ProjectDTO project;

    private PartyDTO party;

    private LicenseDTO license;

    public PlusDatastream() {
        this(new Datastream());
    }

    public PlusDatastream(Datastream datastream) {
        this.datastream = datastream;
    }

    public String getName() {
        return datastream.getName();
    }

    public void setName(String name) {
        datastream.setName(name);
    }

    public String getDescription() {
        return datastream.getDescription();
    }

    public void setDescription(String description) {
        datastream.setDescription(description);
    }

    public String getObservationType() {
        return datastream.getObservationType();
    }

    public void setObservationType(String observationType) {
        datastream.setObservationType(observationType);
    }

    public UnitOfMeasurement getUnitOfMeasurement() {
        return datastream.getUnitOfMeasurement();
    }

    public void setUnitOfMeasurement(UnitOfMeasurement uom) {
        datastream.setUnitOfMeasurement(uom);
    }

    public Geometry getObservedArea() {
        return datastream.getObservedArea();
    }

    public void setObservedArea(Geometry ObservedArea) {
        datastream.setObservedArea(ObservedArea);
    }

    public Time getPhenomenonTime() {
        return datastream.getPhenomenonTime();
    }

    public void setPhenomenonTime(Time phenomenonTime) {
        datastream.setPhenomenonTime(phenomenonTime);
    }

    public Time getResultTime() {
        return datastream.getResultTime();
    }

    public void setResultTime(Time resultTimeStart) {
        datastream.setResultTime(resultTimeStart);
    }

    public ObjectNode getProperties() {
        return datastream.getProperties();
    }

    public void setProperties(ObjectNode properties) {
        datastream.setProperties(properties);
    }

    public ThingDTO getThing() {
        return datastream.getThing();
    }

    public void setThing(ThingDTO thing) {
        datastream.setThing(thing);
    }

    public SensorDTO getSensor() {
        return datastream.getSensor();
    }

    public void setSensor(SensorDTO sensor) {
        datastream.setSensor(sensor);
    }

    public ObservedPropertyDTO getObservedProperty() {
        return datastream.getObservedProperty();
    }

    public void setObservedProperty(ObservedPropertyDTO observedProperty) {
        datastream.setObservedProperty(observedProperty);
    }

    public Set<ObservationDTO> getObservations() {
        return datastream.getObservations();
    }

    public void setObservations(Set<ObservationDTO> observations) {
        datastream.setObservations(observations);
    }

    @Override
    public ProjectDTO getProject() {
        return project;
    }

    @Override
    public void setProject(ProjectDTO project) {
        this.project = project;
    }

    @Override
    public PartyDTO getParty() {
        return party;
    }

    @Override
    public void setParty(PartyDTO party) {
        this.party = party;
    }

    @Override
    public LicenseDTO getLicense() {
        return license;
    }

    @Override
    public void setLicense(LicenseDTO license) {
        this.license = license;
    }

}
