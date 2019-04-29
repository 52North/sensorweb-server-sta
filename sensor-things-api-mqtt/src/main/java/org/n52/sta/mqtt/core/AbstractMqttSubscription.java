/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.core;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.n52.sta.service.query.QueryOptions;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractMqttSubscription {

    private String topic;

    private QueryOptions queryOptions;

    private EdmEntityType entityType;

    private EdmEntitySet entitySet;

    protected String entityTypeName;

    public AbstractMqttSubscription(String topic,
                                    QueryOptions queryOptions,
                                    EdmEntityType entityType,
                                    EdmEntitySet entitySet) {
        this.topic = topic;
        this.queryOptions = queryOptions;
        this.entityType = entityType;
        this.entitySet = entitySet;
        this.entityTypeName = "iot." + getEdmEntityType().getName();
    }

    /**
     * Returns the topic given entity should be posted to. null if the entity
     * does not match this subscription.
     *
     * @param entity Entity to be posted
     * @param relatedEntities Map with EntityType-ID pairs for the related
     * entities
     * @param differenceMap differenceMap names of properties that have changed.
     * if null all properties have changed (new entity)
     * @return Topic to be posted to. May be null if Entity does not match this
     * subscription.
     */
    public String checkSubscription(Entity entity, Map<String, Set<Long>> relatedEntities, Set<String> differenceMap) {
        return matches(entity, relatedEntities, differenceMap) ? topic : null;
    }

    public abstract boolean matches(Entity entity, Map<String, Set<Long>> collections, Set<String> differenceMap);

    public String getTopic() {
        return topic;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public EdmEntityType getEdmEntityType() {
        return entityType;
    }

    public EdmEntitySet getEdmEntitySet() {
        return this.entitySet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof AbstractMqttSubscription && ((AbstractMqttSubscription) other).getTopic().equals(this.topic));
    }

}
