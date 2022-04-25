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
package org.n52.sta.plus.data.dto;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.plus.data.entity.StaPlusParty;
import org.n52.sta.plus.data.entity.StaPlusThing;

public class StaPlusThingDto implements StaPlusThing {

    private final Thing thing;

    private StaPlusParty party;

    public StaPlusThingDto(Thing thing) {
        this.thing = thing;
    }

    @Override
    public Optional<StaPlusParty> getParty() {
        return Optional.ofNullable(party);
    }

    public void setParty(StaPlusParty party) {
        this.party = party;
    }

    @Override
    public String getId() {
        return thing.getId();
    }

    @Override
    public String getName() {
        return thing.getName();
    }

    @Override
    public String getDescription() {
        return thing.getDescription();
    }

    @Override
    public Map<String, Object> getProperties() {
        return thing.getProperties();
    }

    @Override
    public Set<HistoricalLocation> getHistoricalLocations() {
        return thing.getHistoricalLocations();
    }

    @Override
    public Set<Location> getLocations() {
        return thing.getLocations();
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return thing.getDatastreams();
    }

}
