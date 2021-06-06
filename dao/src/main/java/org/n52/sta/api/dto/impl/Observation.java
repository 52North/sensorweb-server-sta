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
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.api.dto.LicenseDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.api.dto.ObservationRelationDTO;

import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class Observation extends Entity implements ObservationDTO {

    private Time phenomenonTime;

    private Time resultTime;

    //TODO: check if we can accept every possible object here or need to implement some kind of "serializableToJSON"
    // interface
    private Object result;

    // It is still pending what this actually is. Returning nothing for now
    private Object resultQuality;

    private Time validTime;

    private ObjectNode parameters;

    private DatastreamDTO datastream;

    private FeatureOfInterestDTO featureOfInterest;

    private LicenseDTO license;

    private Set<ObservationRelationDTO> subjects;

    private Set<ObservationRelationDTO> objects;

    private Set<ObservationGroupDTO> observationGroups;

    public void setResultQuality(Object resultQuality) {
        this.resultQuality = resultQuality;
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

    @Override public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override public Time getValidTime() {
        return validTime;
    }

    @Override public void setValidTime(Time validTime) {
        this.validTime = validTime;
    }

    @Override public ObjectNode getParameters() {
        return parameters;
    }

    public void setParameters(ObjectNode parameters) {
        this.parameters = parameters;
    }

    @Override public FeatureOfInterestDTO getFeatureOfInterest() {
        return featureOfInterest;
    }

    public void setFeatureOfInterest(FeatureOfInterestDTO featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    @Override public DatastreamDTO getDatastream() {
        return datastream;
    }

    public void setDatastream(DatastreamDTO datastream) {
        this.datastream = datastream;
    }

    @Override public Set<ObservationRelationDTO> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<ObservationRelationDTO> subjects) {
        this.subjects = subjects;
    }

    @Override public Set<ObservationRelationDTO> getObjects() {
        return objects;
    }

    public void setObjects(Set<ObservationRelationDTO> objects) {
        this.objects = objects;
    }

    @Override public Set<ObservationGroupDTO> getObservationGroups() {
        return observationGroups;
    }

    @Override public void setObservationGroups(Set<ObservationGroupDTO> observationGroups) {
        this.observationGroups = observationGroups;
    }

    @Override public LicenseDTO getLicense() {
        return license;
    }

    @Override public void setLicense(LicenseDTO license) {
        this.license = license;
    }
}
