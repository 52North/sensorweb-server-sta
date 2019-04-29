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
import java.util.Set;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.n52.sta.service.query.QueryOptions;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class MqttPropertySubscription extends AbstractMqttSubscription {

    private EdmEntitySet entitySet;

    private Long entityId;

    private EdmProperty watchedEdmProperty;

    private Set<String> watchedProperties;

    public MqttPropertySubscription(EdmEntitySet targetEntitySet,
                                    EdmEntityType entityType,
                                    Long targetId,
                                    EdmProperty watchedProperty,
                                    String topic,
                                    QueryOptions queryOptions,
                                    Set<String> watchedProperties) {
        super(topic, queryOptions, entityType, targetEntitySet);
        this.entitySet = targetEntitySet;
        this.entityId = targetId;
        this.watchedEdmProperty = watchedProperty;
        this.watchedProperties = watchedProperties;
    }

    @Override
    public boolean matches(Entity entity, Map<String, Set<Long>> collections, Set<String> differenceMap) {

        // Check type and fail-fast on type mismatch
        if (!(entity.getType().equals(getEdmEntityType().getName()))) {
            return false;
        }

        // Check ID (if not collection) and fail-fast if wrong id is present
        if (!entityId.equals(entity.getProperty("id").getValue())) {
            return false;
        }

        // Check for property changes
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

}
