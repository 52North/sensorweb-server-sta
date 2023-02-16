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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.License;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Project;

public class DatastreamAggregate extends EntityAggregate<Datastream> implements Datastream {

    private final ThingAggregate thingAggregate;
    private final SensorAggregate sensorAggregate;
    private final ObservedPropertyAggregate observedPropertyAggregate;
    private final Set<ObservationAggregate> observationAggregates;

    public DatastreamAggregate(Datastream entity) {
        super(entity);
        assertRequired(entity.getThing(), "Thing is mandatory!");
        assertRequired(entity.getSensor(), "Sensor is mandatory!");
        assertRequired(entity.getObservedProperty(), "ObservedProperty is mandatory!");

        this.thingAggregate = new ThingAggregate(entity.getThing());
        this.sensorAggregate = new SensorAggregate(entity.getSensor());
        this.observedPropertyAggregate = new ObservedPropertyAggregate(entity.getObservedProperty());
        Set<? extends Observation> observations = entity.getObservations();
        observationAggregates = observations.isEmpty()
            ? new HashSet<>()
            : observations.stream()
                          .map(ObservationAggregate::new)
                          .collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public String getDescription() {
        return entity.getDescription();
    }

    @Override
    public Map<String, Object> getProperties() {
        return entity.getProperties();
    }

    @Override
    public String getObservationType() {
        return entity.getObservationType();
    }

    @Override
    public UnitOfMeasurement getUnitOfMeasurement() {
        return entity.getUnitOfMeasurement();
    }

    @Override
    public Geometry getObservedArea() {
        return entity.getObservedArea();
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
    public ThingAggregate getThing() {
        return thingAggregate;
    }

    @Override
    public SensorAggregate getSensor() {
        return sensorAggregate;
    }

    @Override
    public ObservedProperty getObservedProperty() {
        return observedPropertyAggregate;
    }

    @Override
    public Set<ObservationAggregate> getObservations() {
        return new HashSet<>(observationAggregates);
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
