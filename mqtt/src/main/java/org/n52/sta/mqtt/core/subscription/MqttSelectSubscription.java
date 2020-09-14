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

package org.n52.sta.mqtt.core.subscription;

import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.sta.utils.RequestUtils;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class MqttSelectSubscription extends MqttEntityCollectionSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSelectSubscription.class);

    private String selectOption;

    private QueryOptions queryOptions;

    public MqttSelectSubscription(String topic, Matcher mt) {
        super(topic, mt, true);

        selectOption = mt.group(RequestUtils.GROUPNAME_SELECT);
        Assert.notNull(selectOption, "Unable to parse topic. Could not extract selectOption");

        QueryOptionsFactory qof = new QueryOptionsFactory();
        HashSet<FilterClause> filters = new HashSet<>();
        HashSet<String> filterItems = new HashSet<>();
        Collections.addAll(filterItems, mt.group(RequestUtils.GROUPNAME_SELECT).split(","));
        filters.add(new SelectFilter(filterItems));
        queryOptions = qof.createQueryOptions(filters);
        LOGGER.debug(this.toString());
    }

    @Override
    public String toString() {
        String base = super.toString();
        return new StringBuilder()
            .append(base)
            .deleteCharAt(base.length() - 1)
            .append(",selectOption=")
            .append(selectOption)
            .append("]")
            .toString();
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }
}
