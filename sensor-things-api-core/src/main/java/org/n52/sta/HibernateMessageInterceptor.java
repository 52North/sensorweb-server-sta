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
package org.n52.sta;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.sta.data.STAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@SuppressWarnings("serial")
@Component
public class HibernateMessageInterceptor extends EmptyInterceptor {

    private final Logger LOGGER = LoggerFactory.getLogger(HibernateMessageInterceptor.class);

    @Autowired
    private STAEventHandler mqttclient;

    /**
     * Handle new Create Events
     */
    @Override
    public boolean onSave(
                          Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types) {
        LOGGER.debug("Parsed Entity to MQTTHandler: " + entity.toString());
        boolean result = super.onSave(entity, id, state, propertyNames, types);
        mqttclient.handleEvent(entity, null);
        return result;
    }

    /**
     * Handle updates of existing Entites
     */
    @Override
    public boolean onFlushDirty(
                                Object entity,
                                Serializable id,
                                Object[] currentState,
                                Object[] previousState,
                                String[] propertyNames,
                                Type[] types) {
        LOGGER.debug("Parsed Entity to MQTTHandler: " + entity.toString());
        boolean result = super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        mqttclient.handleEvent(entity, findDifferences(currentState, previousState, propertyNames));
        return result;
    }

    private Set<String> findDifferences(Object[] current, Object[] previous, String[] propertyNames) {
        Set<String> differenceMap = new HashSet<>();
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null
            && !(current[i] instanceof PlatformEntity)
            && !current[i].equals(previous[i])) {
                differenceMap.add(propertyNames[i]);
            }
        }
        return differenceMap;
    }
}
