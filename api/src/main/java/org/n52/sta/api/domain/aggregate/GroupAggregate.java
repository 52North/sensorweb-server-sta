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
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.License;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Relation;

public class GroupAggregate extends EntityAggregate<Group> implements Group {

    private final Group group;

    public GroupAggregate(Group group) {
        this(group, null);
    }

    public GroupAggregate(Group group, EntityEditor<Group> editor) {
        super(group, editor);
        this.group = group;
    }

    public String getId() {
        return group.getId();
    }

    public String getName() {
        return group.getName();
    }

    public String getDescription() {
        return group.getDescription();
    }

    public Map<String, Object> getProperties() {
        return group.getProperties();
    }

    @Override
    public String getPurpose() {
        return group.getPurpose();
    }

    @Override
    public Time getRunTime() {
        return group.getRunTime();
    }

    @Override
    public Time getCreationTime() {
        return group.getCreationTime();
    }

    @Override
    public Set<Relation> getRelations() {
        return group.getRelations();
    }

    @Override
    public License getLicense() {
        return group.getLicense();
    }

    @Override
    public Party getParty() {
        return group.getParty();
    }

    @Override
    public Set<Observation> getObservations() {
        return group.getObservations();
    }

}
