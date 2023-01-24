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

import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.Relation;

import java.util.Map;
import java.util.Set;

public class ObservationAggregate extends EntityAggregate<Observation> implements Observation {

    public ObservationAggregate(Observation entity) {
        super(entity);
        assertRequired(entity.getDatastream(), "Datastream is mandatory!");

        // XXX: do logic in domain services
        // TODO: implement auto-generation of FOI based on Thing-Location
        // TODO: see 18-088 Section 10.2 Special Case #1

        assertRequired(entity.getFeatureOfInterest(), "FeatureOfInterest is mandatory!");
    }

    @Override
    public Time getPhenomenonTime() {
        return entity.getPhenomenonTime();
    }

    @Override
    public Time getResultTime() {
        return entity.getResultTime();
    }

    @Override
    public Object getResult() {
        return entity.getResult();
    }

    @Override
    public Object getResultQuality() {
        return entity.getResultQuality();
    }

    @Override
    public Time getValidTime() {
        return entity.getValidTime();
    }

    @Override
    public Map<String, Object> getParameters() {
        return entity.getParameters();
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return entity.getFeatureOfInterest();
    }

    @Override
    public Datastream getDatastream() {
        return entity.getDatastream();
    }

    @Override
    public Set<Group> getGroups() {
        return entity.getGroups();
    }

    @Override
    public Set<Relation> getSubjects() {
        return entity.getSubjects();
    }

    @Override
    public Set<Relation> getObjects() {
        return entity.getObjects();
    }

    @Override
    public String getValueType() {
        return entity.getValueType();
    }

}
