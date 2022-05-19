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

package org.n52.sta.data.provider;

import java.util.Objects;
import java.util.Optional;

import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.entity.HistoricalLocationData;
import org.n52.sta.data.query.FilterQueryParser;
import org.n52.sta.data.query.specifications.HistoricalLocationQuerySpecification;
import org.n52.sta.data.repositories.entity.HistoricalLocationRepository;
import org.n52.sta.data.support.HistoricalLocationGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class HistoricalLocationEntityProvider extends BaseEntityProvider<HistoricalLocation> {

    private final HistoricalLocationRepository historicalLocationRepository;

    public HistoricalLocationEntityProvider(HistoricalLocationRepository historicalLocationRepository,
            EntityPropertyMapping propertyMapping) {
        super(propertyMapping);
        Objects.requireNonNull(historicalLocationRepository, "historicalLocationRepository must not be null");
        this.historicalLocationRepository = historicalLocationRepository;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return historicalLocationRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<HistoricalLocation> getEntity(String id, QueryOptions options) throws ProviderException {
        assertIdentifier(id);

        HistoricalLocationGraphBuilder graphBuilder = new HistoricalLocationGraphBuilder();
        addUnfilteredExpandItems(options, graphBuilder);

        Optional<HistoricalLocationEntity> platform = historicalLocationRepository.findByStaIdentifier(id,
                graphBuilder);
        return platform.map(entity -> new HistoricalLocationData(entity, propertyMapping));
    }

    @Override
    public EntityPage<HistoricalLocation> getEntities(QueryOptions options) throws ProviderException {
        Pageable pagable = StaPageRequest.create(options);

        HistoricalLocationGraphBuilder graphBuilder = new HistoricalLocationGraphBuilder();
        addUnfilteredExpandItems(options, graphBuilder);

        Specification<HistoricalLocationEntity> spec = FilterQueryParser.parse(options,
                new HistoricalLocationQuerySpecification());
        Page<HistoricalLocationEntity> results = historicalLocationRepository.findAll(spec, pagable, graphBuilder);
        return new StaEntityPage<>(HistoricalLocation.class, results,
                entity -> new HistoricalLocationData(entity, propertyMapping));
    }

}
