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
package org.n52.sta.data.entity;

import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Project;
import org.n52.sta.api.utils.TimeUtil;
import org.n52.sta.config.EntityPropertyMapping;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ProjectData extends StaData<ProjectEntity> implements Project {

    public ProjectData(ProjectEntity project, Optional<EntityPropertyMapping> mapping) {
        super(project, mapping);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public String getDescription() {
        return data.getDescription();
    }

    @Override
    public Optional<String> getClassification() {
        return Optional.of(data.getClassification());
    }

    @Override
    public String getTermsOfUse() {
        return data.getTermsOfUse();
    }

    @Override
    public Optional<String> getPrivacyPolicy() {
        return Optional.of(data.getPrivacyPolicy());
    }

    @Override
    public Time getCreationTime() {
        return TimeUtil.createTime(data.getCreationTime());
    }

    @Override
    public Optional<Time> getRunTime() {
        return Optional.of(TimeUtil.createTime(data.getRunTimeStart(), data.getRunTimeEnd()));
    }

    @Override
    public Optional<String> getUrl() {
        return Optional.of(data.getUrl());
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(data.getParameters());
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return toSet(data.getDatasets(), entity -> new DatastreamData(entity, propertyMapping));
    }
}
