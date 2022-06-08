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
import org.n52.sta.plus.data.entity.StaPlusLicense;

public class StaPlusLicenseDto extends StaDto implements StaPlusLicense {

    private String name;

    private String description;

    private String definition;

    private String logo;

    private Map<String, Object> properties;

    private Set<StaPlusDatastream> datastreams;

    private Set<StaPlusGroup> groups;

    public StaPlusLicenseDto() {
        this.properties = new HashMap<>();
        this.datastreams = new HashSet<>();
        this.groups = new HashSet<>();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        assertNonEmpty(name, "name must not be empty");
        this.name = name;
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
    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        assertNonEmpty(definition, "definition must not be empty");
        this.definition = definition;
    }

    @Override
    public Optional<String> getLogo() {
        return Optional.ofNullable(logo);
    }

    public void setLogo(String logo) {
        this.logo = logo;
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
    public Set<StaPlusGroup> getGroups() {
        return new HashSet<>(groups);
    }

    public void setGroups(Set<StaPlusGroup> groups) {
        this.groups = new HashSet<>(groups);
    }

    public void addGroups(StaPlusGroup group) {
        assertNonNull(group, "group must not be null!");
        this.groups.add(group);
    }

    @Override
    public Set<StaPlusDatastream> getDatastreams() {
        return new HashSet<>(datastreams);
    }

    public void setDatastreams(Set<StaPlusDatastream> datastream) {
        this.datastreams = new HashSet<>(datastream);
    }

    public void addDataStream(StaPlusDatastream datastream) {
        assertNonNull(datastream, "datastream must not be null!");
        this.datastreams.add(datastream);
    }

}
