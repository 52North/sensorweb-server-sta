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
import org.n52.sta.plus.data.entity.StaPlusDatastream;
import org.n52.sta.plus.data.entity.StaPlusGroup;
import org.n52.sta.plus.data.entity.StaPlusParty;
import org.n52.sta.plus.data.entity.StaPlusRelation;
import org.n52.sta.plus.data.entity.StaPlusThing;
import org.n52.sta.plus.domain.PartyRole;

public class StaPlusPartyDto extends StaDto implements StaPlusParty {

    private String description;

    private String authId;

    private PartyRole role;

    private String displayName;

    private Map<String, Object> personalData;

    private Set<StaPlusDatastream> datastreams;

    private Set<StaPlusGroup> groups;

    private Set<StaPlusRelation> relations;

    private Set<StaPlusThing> things;

    public StaPlusPartyDto() {
        this.personalData = new HashMap<>();
        this.datastreams = new HashSet<>();
        this.relations = new HashSet<>();
        this.groups = new HashSet<>();
        this.things = new HashSet<>();
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public void setDescripotion(String description) {
        this.description = description;
    }

    @Override
    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        assertNonEmpty(authId, "authId must not be empty");
        this.authId = authId;
    }

    @Override
    public PartyRole getRole() {
        return role;
    }

    public void setRole(PartyRole role) {
        assertNonNull(role, "role must not be empty!");
    }

    @Override
    public Optional<String> getDisplayName() {
        return Optional.of(displayName);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public Map<String, Object> getPersonalData() {
        return new HashMap<>(personalData);
    }

    public void setPersonalData(Map<String, Object> personalData) {
        this.personalData = new HashMap<>(personalData);
    }

    public void addPersonalData(String key, Object value) {
        this.personalData.put(key, value);
    }

    @Override
    public Set<StaPlusDatastream> getDatastreams() {
        return new HashSet<>(datastreams);
    }

    public void setDatastreams(Set<StaPlusDatastream> datastreams) {
        this.datastreams = new HashSet<>(datastreams);
    }

    public void addDatastream(StaPlusDatastream datastream) {
        assertNonNull(datastream, "datastream must not be null!");
        this.datastreams.add(datastream);
    }

    @Override
    public Set<StaPlusThing> getThings() {
        return new HashSet<>(things);
    }

    public void setThings(Set<StaPlusThing> things) {
        this.things = new HashSet<>(things);
    }

    public void addThing(StaPlusThing thing) {
        assertNonNull(thing, "thing must not be null!");
        this.things.add(thing);
    }

    @Override
    public Set<StaPlusGroup> getGroups() {
        return new HashSet<>(groups);
    }

    public void setGroups(Set<StaPlusGroup> groups) {
        this.groups = new HashSet<>(groups);
    }

    public void addGroup(StaPlusGroup group) {
        assertNonNull(group, "group must not be null!");
        this.groups.add(group);
    }

    @Override
    public Set<StaPlusRelation> getRelations() {
        return new HashSet<>(relations);
    }

    public void setRelations(Set<StaPlusRelation> relations) {
        this.relations = new HashSet<>(relations);
    }

    public void addRelation(StaPlusRelation relation) {
        assertNonNull(relation, "relation must not be null!");
        this.relations.add(relation);
    }

}
