/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.data;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.sta.api.EntityPage;
import org.springframework.data.domain.Page;

public class StaEntityPage<E, T> implements EntityPage<T> {

    private final Class<T> type;

    private final Page<E> results;

    private final Function<E, T> entityFactory;

    public StaEntityPage(Class<T> type, Page<E> results, Function<E, T> factory) {
        this.type = type;
        this.results = results;
        this.entityFactory = factory;
    }

    @Override
    public Class<T> getEntityType() {
        return type;
    }

    @Override
    public long getTotalCount() {
        return results.getTotalElements();
    }

    @Override
    public boolean hasNextPage() {
        int currentPage = results.getNumber();
        // TODO check this condition
        return currentPage < results.getTotalPages();
    }

    @Override
    public Collection<T> getEntities() {
        return Streams.stream(results)
                      .map(entityFactory)
                      .collect(Collectors.toList());
    }

}
