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

import org.n52.series.db.beans.PlatformEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.dto.ThingFactory;
import org.n52.sta.data.query.FilterQueryParser;
import org.n52.sta.data.query.specifications.ThingQuerySpecifications;
import org.n52.sta.data.repositories.entity.ThingRepository;
import org.n52.sta.data.support.EntityGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class ThingEntityProvider extends BaseEntityProvider<Thing> {

    private final ThingRepository thingRepository;

    public ThingEntityProvider(ThingRepository thingRepository) {
        Objects.requireNonNull(thingRepository, "thingRepository must not be null");
        this.thingRepository = thingRepository;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return thingRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<Thing> getEntity(String id, QueryOptions options) throws ProviderException {
        assertIdentifier(id);
        EntityGraphBuilder<PlatformEntity> graphBuilder = createEntityGraph(options, PlatformEntity.class);
        Specification<PlatformEntity> spec = FilterQueryParser.parse(options, new ThingQuerySpecifications());
        Optional<PlatformEntity> platform = thingRepository.findOne(spec, graphBuilder);
        return platform.map(ThingFactory::create);
    }

    @Override
    public EntityPage<Thing> getEntities(QueryOptions options) throws ProviderException {
        Pageable pagable = StaPageRequest.create(options);
        EntityGraphBuilder<PlatformEntity> graphBuilder = createEntityGraph(options, PlatformEntity.class);
        Specification<PlatformEntity> spec = FilterQueryParser.parse(options, new ThingQuerySpecifications());
        Page<PlatformEntity> results = thingRepository.findAll(spec, pagable, graphBuilder);
        return new StaEntityPage<>(results, ThingFactory::create);
    }

}