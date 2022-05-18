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

package org.n52.sta.data.service.util;

import org.n52.sta.serdes.util.ElementWithQueryOptions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class CollectionWrapper {

    /**
     * Count of total entities. Value -1 indicates that $count=true was not included in the url and is therefore not
     * returned by the API
     */
    private final long totalEntityCount;

    private final List<ElementWithQueryOptions> entities;

    private final boolean hasNextPage;

    private String requestURL;

    public CollectionWrapper(long entityCount,
                             List<ElementWithQueryOptions> entity,
                             boolean hasNextPage) {
        this.totalEntityCount = entityCount;
        this.entities = entity;
        this.hasNextPage = hasNextPage;
    }

    public long getTotalEntityCount() {
        return totalEntityCount;
    }

    public List<ElementWithQueryOptions> getEntities() {
        return entities;
    }

    public boolean hasNextPage() {
        return hasNextPage;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public CollectionWrapper setRequestURL(String requestURL) {
        this.requestURL = requestURL;
        return this;
    }
}
