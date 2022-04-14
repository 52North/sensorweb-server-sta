/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

/**
 * Represents an Thing as defined in 15-078r6 Section 8.2.1
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ThingDto extends StaDto implements Thing {

    private String name;

    private String description;

    private ObjectNode properties;

    private Set<Datastream> datastreams;

    private Set<HistoricalLocation> historicalLocations;

    private Set<Location> locations;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public ObjectNode getProperties() {
        return properties;
    }

    @Override
    public void setProperties(ObjectNode properties) {
        this.properties = properties;
    }

    @Override
    public Set<HistoricalLocation> getHistoricalLocations() {
        return historicalLocations;
    }

    @Override
    public void setHistoricalLocations(Set<HistoricalLocation> historicalLocations) {
        this.historicalLocations = historicalLocations;
    }

    @Override
    public void addHistoricalLocation(HistoricalLocation historicalLocation) {
        if (this.historicalLocations == null) {
            this.historicalLocations = new HashSet<>();
        }
        this.historicalLocations.add(historicalLocation);
    }

    @Override
    public Set<Location> getLocations() {
        return locations;
    }

    @Override
    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    @Override
    public void addLocations(Location location) {
        if (this.locations == null) {
            this.locations = new HashSet<>();
        }
        this.locations.add(location);
    }

    @Override
    public Set<Datastream> getDatastream() {
        return this.datastreams;
    }

    @Override
    public void setDatastreams(Set<Datastream> datastreams) {
        this.datastreams = datastreams;
    }
}
