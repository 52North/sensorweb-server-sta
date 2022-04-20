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

import org.locationtech.jts.geom.Geometry;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

/**
 * Represents an Thing as defined in 15-078r6 Section 8.2.2
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class LocationDto extends BaseDto implements Location {

    private Geometry geometry;

    private Set<HistoricalLocation> historicalLocations;

    private Set<Thing> things;

    public LocationDto() {
        this.historicalLocations = new HashSet<>();
        this.things = new HashSet<>();
    }

    @Override
    public String getEncodingType() {
        return "application/vnd.geo+json";
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public void addHistoricalLocation(HistoricalLocation historicalLocation) {
        this.historicalLocations.add(historicalLocation);
    }

    @Override
    public Set<HistoricalLocation> getHistoricalLocations() {
        return new HashSet<>(historicalLocations);
    }

    public void setHistoricalLocations(Set<HistoricalLocation> historicalLocations) {
        this.historicalLocations = new HashSet<>(historicalLocations);
    }

    @Override
    public Set<Thing> getThings() {
        return new HashSet<>(things);
    }

    public void setThings(Set<Thing> things) {
        this.things = new HashSet<>(things);
    }
}
