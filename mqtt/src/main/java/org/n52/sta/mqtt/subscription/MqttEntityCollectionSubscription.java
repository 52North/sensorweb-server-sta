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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.n52.sta.mqtt.subscription;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.RequestUtils;
import org.n52.sta.api.dto.common.StaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class MqttEntityCollectionSubscription extends AbstractMqttSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttEntityCollectionSubscription.class);

    public MqttEntityCollectionSubscription(String topic, Matcher mt) {
        super(topic);
        init(mt);
        LOGGER.debug(this.toString());
    }

    public MqttEntityCollectionSubscription(String topic, Matcher mt, boolean calledFromSubclass) {
        super(topic);
        init(mt);
    }

    private void init(Matcher mt) {
        // Root collection
        // E.g. /Things
        if (!mt.pattern().pattern().contains(RequestUtils.GROUPNAME_SOURCE_IDENTIFIER)) {
            wantedEntityType = mt.group(1);
        } else {
            // Related collection
            // E.g. /Things(52)/Datastreams
            sourceEntityType = mt.group(RequestUtils.GROUPNAME_SOURCE_NAME);
            sourceId = mt.group(RequestUtils.GROUPNAME_SOURCE_IDENTIFIER);
            sourceId = sourceId.substring(1, sourceId.length() - 1);
            wantedEntityType = mt.group(RequestUtils.GROUPNAME_WANTED_NAME);
            Assert.notNull(sourceEntityType, "Unable to parse topic. Could not extract sourceEntityType");
            Assert.notNull(sourceId, "Unable to parse topic. Could not extract sourceId");
        }
        Assert.notNull(wantedEntityType, "Unable to parse topic. Could not extract wantedEntityType");
    }

    @Override public QueryOptions getQueryOptions() {
        return null;
    }

    @Override
    public boolean matches(StaDTO entity,
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
