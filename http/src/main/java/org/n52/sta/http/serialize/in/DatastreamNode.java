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
import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.License;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Project;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;

import java.util.Map;
import java.util.Set;

public class DatastreamNode extends StaNode implements Datastream {

    public DatastreamNode(JsonNode node, ObjectMapper mapper) {
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
    public String getObservationType() {
        return getOrNull(StaConstants.PROP_OBSERVATION_TYPE, JsonNode::asText);
    }

    @Override
    public UnitOfMeasurement getUnitOfMeasurement() {
        return getOrNull(StaConstants.PROP_UOM, n -> {
            String name = n.get("name")
                           .asText();
            String symbol = n.get("symbol")
                             .asText();
            String definition = n.get("definition")
                                 .asText();
            return new UnitOfMeasurement(symbol, name, definition);
        });
    }

    @Override
    public Geometry getObservedArea() {
        return parseGeometry(StaConstants.PROP_OBSERVED_AREA);
    }

    @Override
    public Time getPhenomenonTime() {
        return parseTime(StaConstants.PROP_PHENOMENON_TIME);
    }

    @Override
    public Time getResultTime() {
        return parseTime(StaConstants.PROP_RESULT_TIME);
    }

    @Override
    public Thing getThing() {
        return getOrNull(StaConstants.THING, n -> new ThingNode(n, mapper));
    }

    @Override
    public Sensor getSensor() {
        return getOrNull(StaConstants.SENSOR, n -> new SensorNode(n, mapper));
    }

    @Override
    public ObservedProperty getObservedProperty() {
        return getOrNull(StaConstants.OBSERVED_PROPERTY, n -> new ObservedPropertyNode(n, mapper));
    }

    @Override
    public Set<Observation> getObservations() {
        return toSet(StaConstants.OBSERVATIONS, n -> new ObservationNode(n, mapper));
    }

    @Override
    public Project getProject() {
        return getOrNull(StaConstants.PROJECT, n -> new ProjectNode(n, mapper));
    }

    @Override
    public Party getParty() {
        return getOrNull(StaConstants.PARTY, n -> new PartyNode(n, mapper));
    }

    @Override
    public License getLicense() {
        return getOrNull(StaConstants.LICENSE, n -> new LicenseNode(n, mapper));
    }

}
