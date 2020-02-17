/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.mqtt.core.subscription;

import org.n52.series.db.beans.HibernateRelations;
import org.n52.sta.service.STARequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class MqttEntityCollectionSubscription extends AbstractMqttSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttEntityCollectionSubscription.class);

    public MqttEntityCollectionSubscription(String topic, Matcher mt) {
        super(topic, mt);

        // Root collection
        // E.g. /Things
        if (mt.group(2) == null) {
            wantedEntityType = mt.group(0);
        } else {
            // Related collection
            // E.g. /Things(52)/Datastreams
            sourceEntityType = mt.group(STARequestUtils.GROUPNAME_SOURCE_NAME);
            sourceId = mt.group(STARequestUtils.GROUPNAME_SOURCE_IDENTIFIER);
            wantedEntityType = mt.group(STARequestUtils.GROUPNAME_WANTED_NAME);
            Assert.notNull(sourceEntityType, "Unable to parse topic. Could not extract sourceEntityType");
            Assert.notNull(sourceId, "Unable to parse topic. Could not extract sourceId");
        }

        Assert.notNull(wantedEntityType, "Unable to parse topic. Could not extract wantedEntityType");
        LOGGER.debug(this.toString());
    }

    @Override
    public boolean matches(HibernateRelations.HasIdentifier entity,
                           String realEntityType,
                           Map<String, Set<String>> collections,
                           Set<String> differenceMap) {
        // Check type and fail-fast on type mismatch
        if (!(wantedEntityType.equals(realEntityType))) {
            return false;
        }

        // Check if Subscription is on root level (e.g. `/Things`)
        // Type was already checked so we can success-fast
        if (sourceId == null) {
            return true;
        }

        // Check if Entity belongs to collection of this Subscription
        //TODO(specki): check if this acutally works as names have changed
        if (collections != null) {
            for (Entry<String, Set<String>> collection : collections.entrySet()) {
                if (collection.getKey().equals(sourceEntityType)) {
                    for (String id : collection.getValue()) {
                        if (id.equals(sourceId)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }
}
