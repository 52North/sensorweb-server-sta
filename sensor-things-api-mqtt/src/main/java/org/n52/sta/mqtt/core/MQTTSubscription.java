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
import java.util.Objects;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ServiceMetadata;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.query.URIQueryOptions;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class MQTTSubscription {

    private String topic;

    private List<SelectItem> fields;

    private List<UriResource> pattern;

    private String olingoEntityType;

    private Set<String> watchedProperty;

    private boolean isCollection;

    private Long entityId;

    private ServiceMetadata edm;

    private QueryOptions queryOptions;

    private EdmEntitySet entitySet;

    private EdmEntityType entityType;

    /**
     * Parses Topic String into usable Properties to determine whether Entities
     * fit this Subscription.
     *
     * @param topic MQTT-Topic
     * @throws UriValidationException
     * @throws UriParserException
     * @throws Exception If Topic String is malformed
     */
    public MQTTSubscription(String topic, Parser parser, ServiceMetadata edm) throws UriParserException, UriValidationException {

        //TODO set base URI
        String baseUri = "";

        // Validate that Topic is valid URI
        UriInfo uriInfo = parser.parseUri(topic, null, null, baseUri);

        this.topic = topic;
        this.pattern = uriInfo.getUriResourceParts();
        this.edm = edm;
        this.queryOptions = new URIQueryOptions(uriInfo, baseUri);

        if (queryOptions.hasSelectOption()) {
            fields = queryOptions.getSelectOption().getSelectItems();
        }

        // Parse select Option if present
//        if (uriInfo.getSelectOption() != null) {
//            fields = uriInfo.getSelectOption().getSelectItems();
//        }
        // Parse specifically adressed property if present
        UriResource lastResource = pattern.get(pattern.size() - 1);

        String propertyResource = null;
        if (!(lastResource instanceof UriResourceEntitySet)) {
            // Last Resource is property
            propertyResource = ((UriResourceProperty) lastResource).getProperty().getName();
            lastResource = pattern.get(pattern.size() - 2);
        }

        // Parse ID if present
        List<UriParameter> idParameter = ((UriResourceEntitySet) lastResource).getKeyPredicates();
        entityType = ((UriResourceEntitySet) lastResource).getEntityType();
        entitySet = ((UriResourceEntitySet) lastResource).getEntitySet();

        if (idParameter.size() == 0) {
            isCollection = true;
        } else {
            entityId = Long.parseLong(idParameter.get(0).getText());
        }

        // Parse Entitytype
        switch (lastResource.toString()) {
            case "Observations":
                olingoEntityType = "iot.Observation";
                break;
            case "Datastreams":
                olingoEntityType = "iot.Datastream";
                break;
            case "FeatureOfInterests":
                olingoEntityType = "iot.FeatureOfInterest";
                break;
            case "HistoricalLocations":
                olingoEntityType = "iot.HistoricalLocation";
                break;
            case "Locations":
                olingoEntityType = "iot.Location";
                break;
            case "ObservedProperties":
                olingoEntityType = "iot.ObservedProperty";
                break;
            case "Sensors":
                olingoEntityType = "iot.Sensor";
                break;
            case "Things":
                olingoEntityType = "iot.Thing";
                break;
            default:
                throw new IllegalArgumentException("Invalid topic supplied! Cannot Get Resource Type.");
        }

        // Parse STA Property to Database Property after entityType has been determined
        if (propertyResource != null) {
            watchedProperty = MQTTUtil.translateSTAtoToDbProperty(olingoEntityType + "." + propertyResource);
        }
    }

    /**
     * Returns the topic given entity should be posted to. null if the entity
     * does not match this subscription.
     *
     * @param entity Entity to be posted
     * @param differenceMap differenceMap names of properties that have changed.
     * if null all properties have changed (new entity)
     * @return Topic to be posted to. May be null if Entity does not match this
     * subscription.
     */
    public String checkSubscription(Entity entity, Set<String> differenceMap) {
        return matches(entity, differenceMap) ? topic : null;
    }

    /**
     * Checks whether given Entity matches this Subscription's topic.
     *
     * @param entity Entity newly created/updated
     * @param differenceMap names of properties that have changed. if null all
     * properties have changed (new entity)
     * @return true if Entity should be posted to this Subscription
     */
    private boolean matches(Entity entity, Set<String> differenceMap) {
        // Check type and fail-fast on type mismatch
        if (!(entity.getType().equals(olingoEntityType))) {
            return false;
        }

        // Check ID (if not collection) and fail-fast if wrong id is present
        if (!isCollection && !entityId.equals(entity.getProperty("id").getValue())) {
            return false;
        }

        // Check changed property
        if (!isCollection && watchedProperty != null) {
            if (differenceMap == null) {
                return true;
            } else {
                for (String changedProperty : differenceMap) {
                    if (watchedProperty.contains(changedProperty)) {
                        return true;
                    }
                }
                return false;
            }
        }

        //TODO: Check/Resolve for more complex Paths
        return true;
    }

    public String getEntityType() {
        return olingoEntityType;
    }

    public String getTopic() {
        return topic;
    }

    public ServiceMetadata getMetadata() {
        return this.edm;
    }

    public QueryOptions getQueryOptions() {
        return this.queryOptions;
    }

    public EdmEntitySet getEdmEntitySet() {
        return this.entitySet;
    }

    public EdmEntityType getEdmEntityType() {
        return entityType;
    }

//    public ByteBuf encodeEntity(Entity entity) {
//        //TODO: Actually serialize Object to JSON
//        InputStream payload = createResponseContent(edm, response, queryOptions);
//        if (watchedProperty != null) {
//            // Only return updated property
//            return Unpooled.copiedBuffer(entity.toString().getBytes());
//        } else {
//            // Return normally serialized object with this.fields selectItems
//            return Unpooled.copiedBuffer(entity.toString().getBytes());
//        }
//    }
    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof MQTTSubscription && ((MQTTSubscription) other).getTopic().equals(this.topic));
    }
}
