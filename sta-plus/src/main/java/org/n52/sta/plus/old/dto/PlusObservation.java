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

import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.old.dto.DtoEntity;
import org.n52.sta.api.old.dto.Observation;
import org.n52.sta.api.old.entity.DatastreamDTO;
import org.n52.sta.api.old.entity.FeatureOfInterestDTO;
import org.n52.sta.api.old.entity.ObservationDTO;
import org.n52.sta.plus.old.entity.GroupDTO;
import org.n52.sta.plus.old.entity.LicenseDTO;
import org.n52.sta.plus.old.entity.PlusObservationDTO;
import org.n52.sta.plus.old.entity.RelationDTO;

public class PlusObservation extends DtoEntity implements PlusObservationDTO {

    private ObservationDTO observation;

    private LicenseDTO license;

    private Set<RelationDTO> subjects;

    private Set<RelationDTO> objects;

    private Set<GroupDTO> observationGroups;

    public PlusObservation() {
        this(new Observation());
    }

    public PlusObservation(ObservationDTO observation) {
        this.observation = observation;
    }

    public Time getPhenomenonTime() {
        return observation.getPhenomenonTime();
    }

    public void setPhenomenonTime(Time phenomenonTime) {
        observation.setPhenomenonTime(phenomenonTime);
    }

    public Time getResultTime() {
        return observation.getResultTime();
    }

    public void setResultTime(Time resultTime) {
        observation.setResultTime(resultTime);
    }

    public Object getResult() {
        return observation.getResult();
    }

    public void setResult(Object result) {
        observation.setResult(result);
    }

    public Time getValidTime() {
        return observation.getValidTime();
    }

    public void setValidTime(Time validTime) {
        observation.setValidTime(validTime);
    }

    public ObjectNode getParameters() {
        return observation.getParameters();
    }

    public void setParameters(ObjectNode parameters) {
        observation.setParameters(parameters);
    }

    public FeatureOfInterestDTO getFeatureOfInterest() {
        return observation.getFeatureOfInterest();
    }

    public void setFeatureOfInterest(FeatureOfInterestDTO featureOfInterest) {
        observation.setFeatureOfInterest(featureOfInterest);
    }

    public DatastreamDTO getDatastream() {
        return observation.getDatastream();
    }

    public void setDatastream(DatastreamDTO datastream) {
        observation.setDatastream(datastream);
    }

    @Override
    public Set<RelationDTO> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<RelationDTO> subjects) {
        this.subjects = subjects;
    }

    @Override
    public Set<RelationDTO> getObjects() {
        return objects;
    }

    public void setObjects(Set<RelationDTO> objects) {
        this.objects = objects;
    }

    @Override
    public Set<GroupDTO> getObservationGroups() {
        return observationGroups;
    }

    @Override
    public void setObservationGroups(Set<GroupDTO> observationGroups) {
        this.observationGroups = observationGroups;
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
