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
import org.n52.sta.plus.old.entity.GroupDTO;
import org.n52.sta.plus.old.entity.LicenseDTO;
import org.n52.sta.plus.old.entity.PlusObservationDTO;
import org.n52.sta.plus.old.entity.RelationDTO;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class Group extends DtoEntity implements GroupDTO {

    private String name;
    private String description;
    private String purpose;
    private Time creationTime;
    private Time endTime;
    private ObjectNode properties;
    private Set<RelationDTO> observationRelations;
    private Set<PlusObservationDTO> observations;
    private LicenseDTO license;

    @Override
    public LicenseDTO getLicense() {
        return license;
    }

    @Override
    public void setLicense(LicenseDTO license) {
        this.license = license;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getPurpose() {
        return purpose;
    }

    @Override
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @Override
    public Set<RelationDTO> getRelations() {
        return observationRelations;
    }

    @Override
    public void setRelations(Set<RelationDTO> observationRelations) {
        this.observationRelations = observationRelations;
    }

    @Override
    public Set<PlusObservationDTO> getObservations() {
        return observations;
    }

    @Override
    public void setObservations(Set<PlusObservationDTO> observations) {
        this.observations = observations;
    }

    @Override
    public ObjectNode getProperties() {
        return properties;
    }

    @Override
    public void setProperties(ObjectNode properties) {
        this.properties = properties;
    }

    @Override
    public Time getEndTime() {
        return this.endTime;
    }

    @Override
    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    @Override
    public Time getCreationTime() {
        return this.creationTime;
    }

    @Override
    public void setCreationTime(Time creationTime) {
        this.creationTime = creationTime;
    }
}
