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

package org.n52.sta.api.old.dto;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.locationtech.jts.geom.Geometry;
import org.n52.sta.api.old.entity.HistoricalLocationDTO;
import org.n52.sta.api.old.entity.LocationDTO;
import org.n52.sta.api.old.entity.ThingDTO;

/**
 * Represents an Thing as defined in 15-078r6 Section 8.2.2
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class Location extends DtoEntity implements LocationDTO {

    private String name;
    private String description;
    private Geometry geometry;
    private Set<HistoricalLocationDTO> historicalLocations;
    private Set<ThingDTO> things;
    private ObjectNode properties;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    @Override
    public ObjectNode getProperties() {
        return properties;
    }

    public void setProperties(ObjectNode properties) {
        this.properties = properties;
    }

    @Override
    public void addHistoricalLocation(HistoricalLocationDTO historicalLocation) {
        if (this.historicalLocations == null) {
            this.historicalLocations = new HashSet<>();
        }
        this.historicalLocations.add(historicalLocation);
    }

    @Override
    public Set<HistoricalLocationDTO> getHistoricalLocations() {
        return historicalLocations;
    }

    public void setHistoricalLocations(Set<HistoricalLocationDTO> historicalLocations) {
        this.historicalLocations = historicalLocations;
    }

    @Override
    public Set<ThingDTO> getThings() {
        return things;
    }

    public void setThings(Set<ThingDTO> things) {
        this.things = things;
    }
}
