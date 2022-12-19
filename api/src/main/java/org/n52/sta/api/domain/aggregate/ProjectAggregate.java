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

package org.n52.sta.api.domain.aggregate;

import java.util.Map;
import java.util.Set;

import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Project;

public class ProjectAggregate extends EntityAggregate<Project> implements Project {

    private final Project project;

    public ProjectAggregate(Project project) {
        this(project, null);
    }

    public ProjectAggregate(Project project, EntityEditor<Project> editor) {
        super(project, editor);
        this.project = project;
    }

    public String getId() {
        return project.getId();
    }

    public String getName() {
        return project.getName();
    }

    public String getDescription() {
        return project.getDescription();
    }

    public Map<String, Object> getProperties() {
        return project.getProperties();
    }

    @Override
    public Time getRunTime() {
        return project.getRunTime();
    }

    @Override
    public Time getCreationTime() {
        return project.getCreationTime();
    }

    @Override
    public String getClassification() {
        return project.getClassification();
    }

    @Override
    public String getTermsOfUse() {
        return project.getTermsOfUse();
    }

    @Override
    public String getPrivacyPolicy() {
        return project.getPrivacyPolicy();
    }

    @Override
    public String getUrl() {
        return project.getUrl();
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return project.getDatastreams();
    }


}
