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
package org.n52.sta.utils;

import org.n52.sta.data.service.AbstractSensorThingsEntityService;

import java.util.Set;

/**
 * Abstract Interface to hold Query Parameters for {@link AbstractSensorThingsEntityService}
 *
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface QueryOptions {

    int DEFAULT_TOP = 100;

    /**
     * Get the baseURI
     *
     * @return the baseURI
     */
    // String getBaseURI();

    /**
     * @return <code>true</code>, if $count option is present
     */
    boolean hasCountOption();

    /**
     * @return return true if count option is present
     */
    boolean getCountOption();

    /**
     * Get the value of the top query option. If missing returns {@link QueryOptions#DEFAULT_TOP}
     *
     * @return value of $top query option
     */
    int getTopOption();

    /**
     * @return <code>true</code>, if $skip option is present
     */
    boolean hasSkipOption();

    /**
     * @return the value of $skip option
     */
    int getSkipOption();

    /**
     * @return <code>true</code>, if $orderby option is present
     */
    boolean hasOrderByOption();

    /**
     * @return the value of $orderby option
     */
    String getOrderByOption();

    /**
     * @return <code>true</code>, if the $select is present
     */
    boolean hasSelectOption();

    /**
     * @return the value of $select option
     */
    Set<String> getSelectOption();

    /**
     * @return <code>true</code>, if $expand option is present
     */
    boolean hasExpandOption();

    /**
     * @return the value of $expand option
     */
    Set<String> getExpandOption();

    /**
     * @return <code>true</code>, if $filter option is present
     */
    boolean hasFilterOption();

    /**
     * @return the value of $filter option
     */
    Set<String> getFilterOption();

}