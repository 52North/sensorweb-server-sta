/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.api.dto.impl.citsci;

import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.api.dto.ObservationRelationDTO;
import org.n52.sta.api.dto.impl.Entity;

import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationRelation extends Entity implements ObservationRelationDTO {

    private String name;
    private String description;
    private String role;
    private String namespace;
    private ObservationDTO subject;
    private ObservationDTO object;
    private Set<ObservationGroupDTO> observationGroups;

    @Override public String getName() {
        return name;
    }

    @Override public void setName(String name) {
        this.name = name;
    }

    @Override public String getRole() {
        return role;
    }

    @Override public void setRole(String role) {
        this.role = role;
    }

    @Override public String getNamespace() {
        return namespace;
    }

    @Override public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override public ObservationDTO getSubject() {
        return subject;
    }

    @Override public void setSubject(ObservationDTO subject) {
        this.subject = subject;
    }

    @Override public ObservationDTO getObject() {
        return object;
    }

    @Override public void setObject(ObservationDTO object) {
        this.object = object;
    }

    @Override public Set<ObservationGroupDTO> getObservationGroups() {
        return observationGroups;
    }

    @Override public void setObservationGroups(Set<ObservationGroupDTO> observationGroups) {
        this.observationGroups = observationGroups;
    }

    @Override public String getDescription() {
        return description;
    }

    @Override public void setDescription(String description) {
        this.description = description;
    }
}
