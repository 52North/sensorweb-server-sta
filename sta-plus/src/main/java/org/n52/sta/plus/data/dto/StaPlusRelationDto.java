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
package org.n52.sta.plus.data.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.n52.sta.api.dto.StaDto;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.plus.data.entity.StaPlusGroup;
import org.n52.sta.plus.data.entity.StaPlusParty;
import org.n52.sta.plus.data.entity.StaPlusRelation;
import org.n52.sta.plus.domain.TargetReference;

public class StaPlusRelationDto extends StaDto implements StaPlusRelation {

    private String role;

    private String description;

    private Observation<?> subject;

    private TargetReference object;

    private Map<String, Object> properties;

    private Set<StaPlusGroup> groups;

    private StaPlusParty party;

    public StaPlusRelationDto() {
        this.groups = new HashSet<>();
        this.properties = new HashMap<>();
    }

    @Override
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        assertNonEmpty(role, "role must not be null!");
        this.role = role;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    @Override
    public Observation<?> getSubject() {
        return subject;
    }

    public void setSubject(Observation<?> subject) {
        assertNonNull(subject, "subject must not be empty!");
        this.subject = subject;
    }

    @Override
    public TargetReference getObject() {
        return object;
    }

    public void setObject(String externalObject) {
        this.object = TargetReference.objectExternal(externalObject);
    }

    public void setObject(Observation<?> observation) {
        this.object = TargetReference.objectInternal(observation);
    }

    @Override
    public Set<StaPlusGroup> getGroups() {
        return new HashSet<>(groups);
    }

    @Override
    public Optional<StaPlusParty> getParty() {
        return Optional.ofNullable(party);
    }

    public void setParty(StaPlusParty party) {
        this.party = party;
    }

}
