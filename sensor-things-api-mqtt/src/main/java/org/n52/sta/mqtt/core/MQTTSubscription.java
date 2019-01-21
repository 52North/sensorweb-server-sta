/*
 * Copyright (C) 2012-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.sta.mqtt.core;

import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class MQTTSubscription {

    private String topic;

    private List<SelectItem> fields;

    private List<UriResource> pattern;

    private String olingoEntityType;

    private boolean isCollection;
    
    /**
     * Parses Topic String into usable Properties to determine whether Entities fit this Subscription.
     * @param topic MQTT-Topic
     * @throws UriValidationException 
     * @throws UriParserException 
     * @throws Exception If Topic String is malformed
     */
    public MQTTSubscription(String topic, Parser parser) throws UriParserException, UriValidationException {
        // Validate that Topic is valid URI
        UriInfo uriInfo = parser.parseUri(topic, null, null, "");
        
        this.topic = topic;
        this.pattern = uriInfo.getUriResourceParts();
        if (uriInfo.getSelectOption() != null) {
            fields = uriInfo.getSelectOption().getSelectItems();
        }
        
        // Parse Entitytype
        switch(uriInfo.getUriResourceParts().get(uriInfo.getUriResourceParts().size()-1).toString()) {
        case "Observations":
            isCollection = true;
        case "Observation":
            olingoEntityType = "iot.Observation";
            break;
        case "Datastreams":
            isCollection = true;
        case "Datastream":
            olingoEntityType = "iot.Datastream";
            break;
        case "FeatureOfInterests":
            isCollection = true;
        case "FeatureOfInterest":
            olingoEntityType = "iot.FeatureOfInterest";
            break;
        case "HistoricalLocations":
            isCollection = true;
        case "HistoricalLocation":
            olingoEntityType = "iot.HistoricalLocation";
            break;
        case "Locations":
            isCollection = true;
        case "Location":
            olingoEntityType = "iot.Location";
            break;
        case "ObservedProperties":
            isCollection = true;
        case "ObservedProperty":
            olingoEntityType = "iot.ObservedProperty";
            break;
        case "Sensors":
            isCollection = true;
        case "Sensor":
            olingoEntityType = "iot.Sensor";
            break;
        case "Things":
            isCollection = true;
        case "Thing":
            olingoEntityType = "iot.Thing";
            break;
        default: throw new IllegalArgumentException("Invalid topic supplied! Cannot Get Resource Type.");
        }
    }

    /**
     * Returns the topic given entity should be posted to. null if the entity does not match this subscription.
     * @param entity Entity to be posted
     * @param differenceMap differenceMap names of properties that have changed. if null all properties have changed (new entity)
     * @return Topic to be posted to. May be null if Entity does not match this subscription.
     */
    public String checkSubscription(Entity entity, Set<String> differenceMap) {
        return matches(entity, differenceMap) ? topic : null;
    }

    /**
     * Checks whether given Entity matches this Subscription's topic. 
     * @param entity Entity newly created/updated
     * @param differenceMap names of properties that have changed. if null all properties have changed (new entity)
     * @return true if Entity should be posted to this Subscription
     */
    private boolean matches(Entity entity, Set<String> differenceMap) {
        // Check type and fail-fast on type mismatched
        if (!(entity.getType().equals(olingoEntityType))) {
            return false;
        }

        //TODO: fix
        // Check ID (if not collection) and fail-fast if wrong id is present
//        if (!isCollection && !pattern[pattern.length-1][1].equals(entity.getProperty("id").getValue())) {
//            return false;
//        }

        //TODO: Respect differenceMap for subscriptions on specific properties
        //TODO: Check for more complex Paths
        return true;
    }

    public String getEntityType() {
        return olingoEntityType;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof MQTTSubscription && ((MQTTSubscription)other).getTopic().equals(this.topic));
    }
}
