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

import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.sta.api.RequestUtils;
import org.n52.sta.api.old.dto.common.StaDTO;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class MqttPropertySubscription extends MqttEntitySubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPropertySubscription.class);

    private String watchedProperty;

    private QueryOptions queryOptions;

    public MqttPropertySubscription(String topic, Matcher mt) {
        super(topic, mt, true);
        watchedProperty = mt.group(RequestUtils.GROUPNAME_PROPERTY);
        Assert.notNull(watchedProperty, "Unable to parse topic. Could not extract watchedProperty");
        LOGGER.debug(this.toString());

        QueryOptionsFactory qof = new QueryOptionsFactory();
        HashSet<FilterClause> filters = new HashSet<>();
        filters.add(new SelectFilter(watchedProperty));
        queryOptions = qof.createQueryOptions(filters);
    }

    @Override
    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    @Override
    public String toString() {
        String base = super.toString();
        return new StringBuilder()
            .append(base)
            .deleteCharAt(base.length() - 1)
            .append(",watchedProperty=")
            .append(watchedProperty)
            .append("]")
            .toString();
    }

    @Override
    public boolean matches(StaDTO entity,
                           String realEntityType,
                           Map<String, Set<String>> collections,
                           Set<String> differenceMap) {
        boolean superMatches = super.matches(entity, realEntityType, collections, differenceMap);

        if (superMatches) {
            return differenceMap == null || differenceMap.contains(watchedProperty);
        } else {
            return false;
        }
    }
}
