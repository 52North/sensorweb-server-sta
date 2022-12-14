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
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.Relation;

import java.util.Map;
import java.util.Set;

public class ObservationNode extends StaNode implements Observation {

    public ObservationNode(JsonNode node, ObjectMapper mapper) {
        super(node, mapper);
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
    public Object getResult() {
        // TODO return typed JSON value or object
        return getOrNull(StaConstants.PROP_RESULT, JsonNode::asText);
    }

    @Override
    public Object getResultQuality() {
        return null;
    }

    @Override
    public Time getValidTime() {
        return parseTime(StaConstants.PROP_VALID_TIME);
    }

    @Override
    public Map<String, Object> getParameters() {
        return toMap(StaConstants.PROP_PARAMETERS);
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return getOrNull(StaConstants.FEATURE_OF_INTEREST, n -> new FeatureOfInterestNode(n, mapper));
    }

    @Override
    public Datastream getDatastream() {
        return getOrNull(StaConstants.DATASTREAM, n -> new DatastreamNode(n, mapper));
    }

    @Override
    public Set<Group> getGroups() {
        return toSet(StaConstants.GROUPS, node -> new GroupNode(node, mapper));
    }

    @Override
    public Set<Relation> getSubjects() {
        return toSet(StaConstants.SUBJECTS, node -> new RelationNode(node, mapper));
    }

    @Override
    public Set<Relation> getObjects() {
        return toSet(StaConstants.OBJECTS, node -> new RelationNode(node, mapper));
    }

    @Override
    public String getValueType() {
        throw new RuntimeException("not yet implemented");
        // TODO determine type
    }
}
