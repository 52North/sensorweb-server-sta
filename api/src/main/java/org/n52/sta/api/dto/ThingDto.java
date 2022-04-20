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
package org.n52.sta.api.dto;

import java.util.HashSet;
import java.util.Set;

import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

/**
 * Represents an Thing as defined in 15-078r6 Section 8.2.1
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ThingDto extends BaseDto implements Thing {

    private Set<Datastream> datastreams;

    private Set<HistoricalLocation> historicalLocations;

    private Set<Location> locations;

    public ThingDto() {
        this.datastreams = new HashSet<>();
        this.historicalLocations = new HashSet<>();
        this.locations = new HashSet<>();
    }

    @Override
    public Set<HistoricalLocation> getHistoricalLocations() {
        return new HashSet<>(historicalLocations);
    }

    public void setHistoricalLocations(Set<HistoricalLocation> historicalLocations) {
        this.historicalLocations = new HashSet<>(historicalLocations);
    }

    public void addHistoricalLocation(HistoricalLocation historicalLocation) {
        this.historicalLocations.add(historicalLocation);
    }

    @Override
    public Set<Location> getLocations() {
        return new HashSet<>(locations);
    }

    public void setLocations(Set<Location> locations) {
        this.locations = new HashSet<>(locations);
    }

    public void addLocation(Location location) {
        this.locations.add(location);
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return new HashSet<>(datastreams);
    }

    public void setDatastreams(Set<Datastream> datastreams) {
        this.datastreams = new HashSet<>(datastreams);
    }

    public void addDatastream(Datastream datastream) {
        this.datastreams.add(datastream);
    }
}
