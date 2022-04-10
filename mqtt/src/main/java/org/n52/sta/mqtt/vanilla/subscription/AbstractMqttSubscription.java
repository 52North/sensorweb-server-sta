/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.mqtt.vanilla.subscription;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.dto.StaDTO;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractMqttSubscription {

    protected String sourceEntityType;
    protected String sourceId;
    protected String wantedEntityType;
    private final String topic;

    public AbstractMqttSubscription(String topic) {
        this.topic = topic;
    }

    /**
     * Returns the topic given entity should be posted to. null if the entity
     * does not match this subscription.
     *
     * @param rawObject       Entity to be posted
     * @param entityType      Type of Entity
     * @param relatedEntities Map with EntityType-ID pairs for the related
     *                        entities
     * @param differenceMap   differenceMap names of properties that have changed.
     *                        if null all properties have changed (new entity)
     * @return Topic to be posted to. May be null if Entity does not match this
     * subscription.
     */
    public String checkSubscription(StaDTO rawObject,
                                    String entityType,
                                    Map<String, Set<String>> relatedEntities,
                                    Set<String> differenceMap) {
        return matches(rawObject, entityType, relatedEntities, differenceMap) ? topic : null;
    }

    public String getTopic() {
        return topic;
    }

    public String getEntityType() {
        return wantedEntityType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AbstractMqttSubscription
            && ((AbstractMqttSubscription) other).getTopic().equals(this.topic);
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append(this.getClass().getSimpleName())
            .append("[")
            .append("topic=")
            .append(topic)
            .append(",")
            .append("sourceEntityType=")
            .append(sourceEntityType)
            .append(",")
            .append("sourceId=")
            .append(sourceId)
            .append(",")
            .append("wantedEntityType=")
            .append(wantedEntityType)
            .append("]")
            .toString();
    }

    /**
     * Returns the selectOption extracted from the Topic.
     *
     * @return SelectOption if present, else null
     */
    public abstract QueryOptions getQueryOptions();

    public abstract boolean matches(StaDTO entity,
                                    String realEntityType,
                                    Map<String, Set<String>> collections,
                                    Set<String> differenceMap);
}
