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
package org.n52.sta.service.query;

import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.core.uri.queryoption.TopOptionImpl;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;

/**
 * Class that holds an {@link ExpandItem} to get {@link SystemQueryOption}s to
 * use them in the {@link AbstractSensorThingsEntityService}s.
 *
 * @see org.n52.sta.service.query.QueryOptions
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @since 1.0.0
 */
public class ExpandItemQueryOptions implements QueryOptions {

    private ExpandItem item;
    private String baseURI;

    public ExpandItemQueryOptions(ExpandItem item, String baseURI) {
        this.item = item;
        this.baseURI = baseURI;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getUriInfo()
     */
    @Override
    public UriInfo getUriInfo() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getBaseURI()
     */
    @Override
    public String getBaseURI() {
        return baseURI;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasCountOption()
     */
    @Override
    public boolean hasCountOption() {
        return item.getCountOption() != null && item.getCountOption().getValue();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getCountOption()
     */
    @Override
    public CountOption getCountOption() {
        return item.getCountOption();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getTopOption()
     */
    @Override
    public TopOption getTopOption() {
        if (item.getTopOption() != null && item.getTopOption().getValue() <= DEFAULT_TOP) {
            return item.getTopOption();
        }
        return new TopOptionImpl().setValue(DEFAULT_TOP);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasSkipOption()
     */
    @Override
    public boolean hasSkipOption() {
        return item.getSkipOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getSkipOption()
     */
    @Override
    public SkipOption getSkipOption() {
        return item.getSkipOption();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasOrderByOption()
     */
    @Override
    public boolean hasOrderByOption() {
        return item.getOrderByOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getOrderByOption()
     */
    @Override
    public OrderByOption getOrderByOption() {
        return item.getOrderByOption();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasSelectOption()
     */
    @Override
    public boolean hasSelectOption() {
        return item.getSelectOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getSelectOption()
     */
    @Override
    public SelectOption getSelectOption() {
        return item.getSelectOption();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasExpandOption()
     */
    @Override
    public boolean hasExpandOption() {
        return item.getExpandOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getExpandOption()
     */
    @Override
    public ExpandOption getExpandOption() {
        return item.getExpandOption();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasFilterOption()
     */
    @Override
    public boolean hasFilterOption() {
        return item.getFilterOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getFilterOption()
     */
    @Override
    public FilterOption getFilterOption() {
        return item.getFilterOption();
    }

}
