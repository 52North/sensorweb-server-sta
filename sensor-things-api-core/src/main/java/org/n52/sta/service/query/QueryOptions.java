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
package org.n52.sta.service.query;

import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;

/**
 * Abstract Interface to hold Query Parameters for {@link AbstractSensorThingsEntityService}
 *
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface QueryOptions {

    int DEFAULT_TOP = 100;

    /**
     * Get the {@link UriInfo}
     *
     * @return the uriInfo
     */
    UriInfo getUriInfo();

    /**
     * Get the baseURI
     *
     * @return the baseURI
     */
    String getBaseURI();

    /**
     * Check if {@link CountOption} is present
     *
     * @return <code>true</code>, if {@link CountOption} is present
     */
    boolean hasCountOption();

    /**
     * Get the {@link CountOption}
     *
     * @return the {@link CountOption}
     */
    CountOption getCountOption();

    /**
     * Get the {@link TopOption}. If {@link TopOption} is missing return {@link QueryOptions#DEFAULT_TOP}
     *
     * @return the {@link TopOption}
     */
    TopOption getTopOption();

    /**
     * Check if {@link SkipOption} is present
     *
     * @return <code>true</code>, if {@link SkipOption} is present
     */
    boolean hasSkipOption();

    /**
     * Get the {@link SkipOption}
     *
     * @return the {@link SkipOption}
     */
    SkipOption getSkipOption();

    /**
     * Check if {@link OrderByOption} is present
     *
     * @return <code>true</code>, if {@link OrderByOption} is present
     */
    boolean hasOrderByOption();

    /**
     * Get the {@link OrderByOption}
     *
     * @return the {@link OrderByOption}
     */
    OrderByOption getOrderByOption();

    /**
     * * Check if {@link SelectOption} is present
     *
     * @return <code>true</code>, if the {@link SelectOption} is present
     */
    boolean hasSelectOption();

    /**
     * Get the {@link SelectOption}
     *
     * @return the {@link SelectOption}
     */
    SelectOption getSelectOption();

    /**
     * * Check if {@link ExpandOption} is present
     *
     * @return <code>true</code>, if {@link ExpandOption} is present
     */
    boolean hasExpandOption();

    /**
     * Get the {@link ExpandOption}
     *
     * @return the {@link ExpandOption}
     */
    ExpandOption getExpandOption();

    /**
     * * Check if {@link ExpandOption} is present
     *
     * @return <code>true</code>, if {@link ExpandOption} is present
     */
    boolean hasFilterOption();

    /**
     * Get the {@link ExpandOption}
     *
     * @return the {@link ExpandOption}
     */
    FilterOption getFilterOption();

}
