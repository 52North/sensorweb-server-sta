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

import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;

public class ObservationAggregate extends EntityAggregate<Observation> implements Observation {

    private final Observation observation;

    public ObservationAggregate(Observation observation) {
        this(observation, null);
    }

    public ObservationAggregate(Observation observation, EntityEditor<Observation> editor) {
        super(observation, editor);
        this.observation = observation;
        assertRequired(observation.getDatastream(), "Datastream is mandatory!");

        //TODO: implement auto-generation of FOI based on Thing-Location
        //TODO: see 18-088 Section 10.2 Special Case #1

        assertRequired(observation.getFeatureOfInterest(), "FeatureOfInterest is mandatory!");
    }

    public String getId() {
        return observation.getId();
    }

    @Override
    public Time getPhenomenonTime() {
        return observation.getPhenomenonTime();
    }

    @Override
    public Time getResultTime() {
        return observation.getResultTime();
    }

    @Override
    public Object getResult() {
        return observation.getResult();
    }

    @Override
    public Object getResultQuality() {
        return observation.getResultQuality();
    }

    @Override
    public Time getValidTime() {
        return observation.getValidTime();
    }

    @Override
    public Map<String, Object> getParameters() {
        return observation.getParameters();
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return observation.getFeatureOfInterest();
    }

    @Override
    public Datastream getDatastream() {
        return observation.getDatastream();
    }

    @Override
    public String getValueType() {
        return observation.getValueType();
    }

}
