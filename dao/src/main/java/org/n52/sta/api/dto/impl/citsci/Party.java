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

package org.n52.sta.api.dto.impl.citsci;

import jdk.nashorn.internal.ir.ObjectNode;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.PartyDTO;
import org.n52.sta.api.dto.impl.Entity;

import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class Party extends Entity implements PartyDTO {

    private String nickname;
    private Role role;
    private String authId;
    private String description;
    private Set<DatastreamDTO> datastreams;
    private ObjectNode properties;

    @Override public String getNickname() {
        return nickname;
    }

    @Override public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override public Role getRole() {
        return role;
    }

    @Override public void setRole(Role role) {
        this.role = role;
    }

    @Override public String getAuthId() {
        return authId;
    }

    @Override public void setAuthId(String authId) {
        this.authId = authId;
    }

    @Override public Set<DatastreamDTO> getDatastreams() {
        return datastreams;
    }

    @Override public void setDatastreams(Set<DatastreamDTO> datasets) {
        this.datastreams = datasets;
    }

    @Override public ObjectNode getProperties() {
        return properties;
    }

    @Override public void setProperties(ObjectNode properties) {
        this.properties = properties;
    }

    @Override public String getDescription() {
        return description;
    }

    @Override public void setDescription(String description) {
        this.description = description;
    }
}
