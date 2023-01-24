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

import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.License;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Project;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;

public class DatastreamAggregate extends EntityAggregate<Datastream> implements Datastream {

    public DatastreamAggregate(Datastream entity) {
        super(entity);
        assertRequired(entity.getThing(), "Thing is mandatory!");
        assertRequired(entity.getSensor(), "Sensor is mandatory!");
        assertRequired(entity.getObservedProperty(), "ObservedProperty is mandatory!");
    }

    public String getName() {
        return entity.getName();
    }

    public String getDescription() {
        return entity.getDescription();
    }

    public Map<String, Object> getProperties() {
        return entity.getProperties();
    }

    public String getObservationType() {
        return entity.getObservationType();
    }

    public UnitOfMeasurement getUnitOfMeasurement() {
        return entity.getUnitOfMeasurement();
    }

    public Geometry getObservedArea() {
        return entity.getObservedArea();
    }

    public Time getPhenomenonTime() {
        return entity.getPhenomenonTime();
    }

    public Time getResultTime() {
        return entity.getResultTime();
    }

    public Thing getThing() {
        return entity.getThing();
    }

    public Sensor getSensor() {
        return entity.getSensor();
    }

    public ObservedProperty getObservedProperty() {
        return entity.getObservedProperty();
    }

    public Set<Observation> getObservations() {
        return entity.getObservations();
    }

    @Override
    public Project getProject() {
        return entity.getProject();
    }

    @Override
    public Party getParty() {
        return entity.getParty();
    }

    @Override
    public License getLicense() {
        return entity.getLicense();
    }

    private boolean isTrajectory(Datastream entity) {
        // Thing thing = entity.getThing();
        // return isMobileEnabled
        // && thing.hasParameters()
        // && thing
        // .getParameters()
        // .stream()
        // .filter(p -> p instanceof BooleanParameterEntity)
        // .filter(p -> p.getName()
        // .equals("isMobile"))
        // .anyMatch(p -> ((ParameterEntity<Boolean>) p).getValue()))
        return false;
    }

}
