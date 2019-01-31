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
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ServiceMetadata;
import org.n52.sta.service.query.QueryOptions;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class MqttSubscription {

    private String topic;

    private List<SelectItem> fields;

//    private List<UriResource> pattern;

    private String olingoEntityType;

    private Set<String> watchedProperties;

    private boolean isCollection;

    private Long entityId;

    private QueryOptions queryOptions;

    private EdmEntitySet entitySet;

    private EdmEntityType entityType;

    public MqttSubscription(String topic, List<SelectItem> fields,
            String olingoEntityType, Set<String> watchedProperties, boolean isCollection,
            Long entityId, QueryOptions queryOptions, EdmEntitySet entitySet, EdmEntityType entityType) {
        this.topic = topic;
        this.fields = fields;       
        this.olingoEntityType = olingoEntityType;
        this.watchedProperties = watchedProperties;
        this.isCollection = isCollection;
        this.entityId = entityId;
        this.queryOptions = queryOptions;
        this.entitySet = entitySet;
        this.entityType = entityType;
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
        if (!isCollection && watchedProperties != null) {
            if (differenceMap == null) {
                return true;
            } else {
                for (String changedProperty : differenceMap) {
                    if (watchedProperties.contains(changedProperty)) {
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

    public QueryOptions getQueryOptions() {
        return this.queryOptions;
    }

    public EdmEntitySet getEdmEntitySet() {
        return this.entitySet;
    }

    public EdmEntityType getEdmEntityType() {
        return entityType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof MqttSubscription && ((MqttSubscription) other).getTopic().equals(this.topic));
    }
}
