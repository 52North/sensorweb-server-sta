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
import org.n52.sta.plus.data.entity.StaPlusDatastream;
import org.n52.sta.plus.data.entity.StaPlusProject;

public class StaPlusProjectDto extends StaDto implements StaPlusProject {

    private String name;

    private String description;

    private String classification;

    private String termsOfUse;

    private String privacyPolicy;

    private Time creationTime;

    private Time startTime;

    private Time endTime;

    private String url;

    private Map<String, Object> properties;

    private Set<StaPlusDatastream> datastreams;

    public StaPlusProjectDto() {
        this.properties = new HashMap<>();
        this.datastreams = new HashSet<>();
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

    public void setDesciption(String description) {
        assertNonEmpty(description, "description must not be empty");
        this.description = description;
    }

    @Override
    public Optional<String> getClassification() {
        return Optional.ofNullable(classification);
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    @Override
    public String getTermsOfUse() {
        return termsOfUse;
    }

    public void setTermsOfUse(String termsOfUse) {
        assertNonEmpty(termsOfUse, "termsOfUse must not be empty");
        this.termsOfUse = termsOfUse;
    }

    @Override
    public Optional<String> getPrivacyPolicy() {
        return Optional.ofNullable(privacyPolicy);
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
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
    public Optional<Time> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    @Override
    public Optional<Time> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    @Override
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    public void setUrl(String url) {
        this.url = url;
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
    public Set<StaPlusDatastream> getDatastreams() {
        return new HashSet<>(datastreams);
    }

    public void setDatastreams(Set<StaPlusDatastream> datastreams) {
        this.datastreams = new HashSet<>(datastreams);
    }

    public void addDatastream(StaPlusDatastream datastream) {
        assertNonNull(datastream, "datastream must not be empty");
        this.datastreams.add(datastream);
    }

}
