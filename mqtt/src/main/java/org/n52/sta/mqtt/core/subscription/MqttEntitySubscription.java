/*
 * Copyright (C) 2018-2023 52°North Initiative for Geospatial Open Source
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
import org.n52.sta.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class MqttEntitySubscription extends AbstractMqttSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttEntitySubscription.class);

    private String wantedIdentifier;

    public MqttEntitySubscription(String topic, Matcher mt) {
        super(topic);
        init(mt);
        LOGGER.debug(this.toString());
    }

    public MqttEntitySubscription(String topic, Matcher mt, boolean calledFromSubclass) {
        super(topic);
        init(mt);
    }

    private void init(Matcher mt) {
        // Referenced Entity
        // E.g. /Datastream(52)/Sensor
        if (mt.group(RequestUtils.GROUPNAME_WANTED_IDENTIFIER) == null) {
            sourceEntityType = mt.group(RequestUtils.GROUPNAME_SOURCE_NAME);
            sourceId = mt.group(RequestUtils.GROUPNAME_SOURCE_IDENTIFIER);
            sourceId = sourceId.substring(1, sourceId.length() - 1);
            wantedEntityType = mt.group(RequestUtils.GROUPNAME_WANTED_NAME);
            Assert.notNull(sourceId, "Unable to parse topic. Could not extract sourceId");
            Assert.notNull(sourceEntityType, "Unable to parse topic. Could not extract sourceEntityType");
        } else {
            // Direct Entity
            // E.g. /Things(52)
            wantedEntityType = mt.group(RequestUtils.GROUPNAME_WANTED_NAME);
            wantedIdentifier = mt.group(RequestUtils.GROUPNAME_WANTED_IDENTIFIER);
            wantedIdentifier = wantedIdentifier.substring(1, wantedIdentifier.length() - 1);
            Assert.notNull(wantedIdentifier, "Unable to parse topic. Could not extract wantedIdentifier");
        }

        Assert.notNull(wantedEntityType, "Unable to parse topic. Could not extract wantedEntityType");
    }

    @Override
    public String toString() {
        String base = super.toString();
        return new StringBuilder()
            .append(base)
            .deleteCharAt(base.length() - 1)
            .append(",wantedIdentifier=")
            .append(wantedIdentifier)
            .append("]")
            .toString();
    }

    @Override
    public boolean matches(HibernateRelations.HasStaIdentifier entity,
                           String realEntityType,
                           Map<String, Set<String>> collections,
                           Set<String> differenceMap) {

        // Check type and fail-fast on type mismatch
        if (!(wantedEntityType.equals(realEntityType))) {
            return false;
        }

        // Direct Entity
        if (wantedIdentifier != null) {
            return wantedIdentifier.equals(entity.getStaIdentifier());
        } else {
            // Referenced Entity
            // Check if Entity belongs to collection of this Subscription
            if (collections != null) {
                for (Map.Entry<String, Set<String>> collection : collections.entrySet()) {
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
        }
        return false;
    }
}
