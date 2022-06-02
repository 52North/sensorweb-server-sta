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

import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.path.Request;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.entity.LocationData;
import org.n52.sta.data.query.specifications.LocationQuerySpecification;
import org.n52.sta.data.repositories.entity.LocationRepository;
import org.n52.sta.data.support.LocationGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;
import java.util.Optional;

public class LocationEntityProvider extends BaseEntityProvider<Location> {

    private final LocationRepository locationRepository;

    public LocationEntityProvider(LocationRepository locationRepository, EntityPropertyMapping propertyMapping) {
        super(propertyMapping);
        Objects.requireNonNull(locationRepository, "locationRepository must not be null");
        this.locationRepository = locationRepository;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return locationRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<Location> getEntity(Request req) throws ProviderException {
        LocationGraphBuilder graphBuilder = new LocationGraphBuilder();
        addUnfilteredExpandItems(req.getQueryOptions(), graphBuilder);

        Specification<LocationEntity> spec = createSpecificationFromRequest(req, new LocationQuerySpecification());
        Optional<LocationEntity> platform = locationRepository.findOne(spec, graphBuilder);
        return platform.map(entity -> new LocationData(entity, propertyMapping));
    }

    @Override
    public EntityPage<Location> getEntities(Request req) throws ProviderException {
        QueryOptions options = req.getQueryOptions();
        Pageable pagable = StaPageRequest.create(options);

        LocationGraphBuilder graphBuilder = new LocationGraphBuilder();
        addUnfilteredExpandItems(options, graphBuilder);

        Specification<LocationEntity> spec = createSpecificationFromRequest(req, new LocationQuerySpecification());
        Page<LocationEntity> results = locationRepository.findAll(spec, pagable, graphBuilder);
        return new StaEntityPage<>(Location.class, results, entity -> new LocationData(entity, propertyMapping));
    }

}
