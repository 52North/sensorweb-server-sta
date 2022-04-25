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

import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.dto.StaDto;
import org.n52.sta.plus.data.entity.StaPlusGroup;
import org.n52.sta.plus.data.entity.StaPlusLicense;
import org.n52.sta.plus.data.entity.StaPlusParty;
import org.n52.sta.plus.data.entity.StaPlusRelation;

public class StaPlusGroupDto extends StaDto implements StaPlusGroup {

    private String name;

    private String description;

    private String purpose;

    private Time creationTime;

    private Time endTime;

    private Map<String, Object> properties;

    private Set<StaPlusRelation> relations;

    private StaPlusLicense license;

    private StaPlusParty party;

    private StaPlusGroup parent;

    private Set<StaPlusGroup> children;

    public StaPlusGroupDto() {
        this.properties = new HashMap<>();
        this.relations = new HashSet<>();
        this.children = new HashSet<>();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        assertNonEmpty(name, "name must not be empty!");
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        assertNonEmpty(description, "description must not be empty");
        this.description = description;
    }

    @Override
    public Optional<String> getPurpose() {
        return Optional.ofNullable(purpose);
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @Override
    public Time getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Time creationTime) {
        assertNonNull(creationTime, "creationTime must not be null!");
        this.creationTime = creationTime;
    }

    @Override
    public Optional<Time> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    @Override
    public Optional<StaPlusParty> getParty() {
        return Optional.ofNullable(party);
    }

    public void setParty(StaPlusParty party) {
        this.party = party;
    }

    @Override
    public Optional<StaPlusLicense> getLicense() {
        return Optional.ofNullable(license);
    }

    public void setLicense(StaPlusLicense license) {
        this.license = license;
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

    @Override
    public Optional<StaPlusGroup> getParent() {
        return Optional.ofNullable(parent);
    }

    public void setParent(StaPlusGroup parent) {
        this.parent = parent;
    }

    @Override
    public Set<StaPlusGroup> getChildren() {
        return new HashSet<>(children);
    }

    public void setChildren(Set<StaPlusGroup> children) {
        this.children = new HashSet<>(children);
    }

    public void addChild(StaPlusGroup child) {
        assertNonNull(child, "child must not be null!");
        this.children.add(child);
    }

}
