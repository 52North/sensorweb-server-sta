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

package org.n52.sta.http.serialize.in;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Thing;

import java.util.Map;
import java.util.Set;

public class ThingNode extends StaNode implements Thing {

    public ThingNode(JsonNode node, ObjectMapper mapper) {
        super(node, mapper);
    }

    @Override
    public String getName() {
        return getOrNull(StaConstants.PROP_NAME, JsonNode::asText);
    }

    @Override
    public String getDescription() {
        return getOrNull(StaConstants.PROP_DESCRIPTION, JsonNode::asText);
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(StaConstants.PROP_PROPERTIES);
    }

    @Override
    public Set<HistoricalLocation> getHistoricalLocations() {
        return toSet(StaConstants.HISTORICAL_LOCATIONS, n -> new HistoricalLocationNode(n, mapper));
    }

    @Override
    public Set<Location> getLocations() {
        return toSet(StaConstants.LOCATIONS, n -> new LocationNode(n, mapper));
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return toSet(StaConstants.DATASTREAMS, n -> new DatastreamNode(n, mapper));
    }

    @Override
    public Party getParty() {
        throw new RuntimeException(NIY);
    }

}
