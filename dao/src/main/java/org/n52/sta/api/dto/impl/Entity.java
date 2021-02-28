/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.api.dto.impl;

import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.dto.StaDTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class Entity implements StaDTO {

    protected boolean hasSelectOption;
    protected boolean hasExpandOption;
    private String id;
    private QueryOptions queryOptions;
    private Set<String> fieldsToSerialize = new HashSet<>();
    private Map<String, QueryOptions> fieldsToExpand = new HashMap<>();

    @Override public String getId() {
        return this.id;
    }

    @Override public void setId(String id) {
        this.id = id;
    }

    @Override public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    @Override public void setAndParseQueryOptions(QueryOptions queryOptions) {
        if (queryOptions != null) {
            if (queryOptions.hasSelectFilter()) {
                hasSelectOption = true;
                fieldsToSerialize.addAll(queryOptions.getSelectFilter().getItems());
            }
            if (queryOptions.hasExpandFilter()) {
                hasExpandOption = true;
                for (ExpandItem item : queryOptions.getExpandFilter().getItems()) {
                    fieldsToExpand.put(item.getPath(), item.getQueryOptions());
                    // Add expanded items to $select replacing implicit selection with explicit selection
                    if (hasSelectOption) {
                        fieldsToSerialize.add(item.getPath());
                    }
                }
            }
        }
        this.queryOptions = queryOptions;
    }

    @Override public Set<String> getFieldsToSerialize() {
        return fieldsToSerialize;
    }

    @Override public Map<String, QueryOptions> getFieldsToExpand() {
        return fieldsToExpand;
    }

    public boolean hasSelectOption() {
        return hasSelectOption;
    }

    public boolean hasExpandOption() {
        return hasExpandOption;
    }
}
