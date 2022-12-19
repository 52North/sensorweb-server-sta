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

import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.exception.ProviderException;
import org.n52.sta.api.entity.License;
import org.n52.sta.api.path.Request;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.entity.LicenseData;
import org.n52.sta.data.query.specifications.LicenseQuerySpecification;
import org.n52.sta.data.repositories.entity.LicenseRepository;
import org.n52.sta.data.support.LicenseGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class LicenseEntityProvider extends BaseEntityProvider<License> {

    private final LicenseRepository licenseRepository;
    private final LicenseQuerySpecification rootSpecification;

    public LicenseEntityProvider(LicenseRepository licenseRepository, EntityPropertyMapping propertyMapping) {
        super(propertyMapping);
        Objects.requireNonNull(licenseRepository, "licenseRepository must not be null");
        this.licenseRepository = licenseRepository;
        this.rootSpecification = new LicenseQuerySpecification();
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return licenseRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<License> getEntity(Request request) throws ProviderException {
        LicenseGraphBuilder graphBuilder = request.isRefRequest()
                ? LicenseGraphBuilder.createEmpty()
                : LicenseGraphBuilder.createWith(request.getQueryOptions());
        return getEntity(rootSpecification.buildSpecification(request), graphBuilder);
    }

    @Override
    public Optional<License> getEntity(String id, QueryOptions queryOptions) throws ProviderException {
        LicenseGraphBuilder graphBuilder = LicenseGraphBuilder.createEmpty();
        return getEntity(rootSpecification.buildSpecification(queryOptions)
                                          .and(rootSpecification.equalsStaIdentifier(id)),
                         graphBuilder);
    }

    private Optional<License> getEntity(Specification<LicenseEntity> spec, LicenseGraphBuilder graphBuilder) {
        Optional<LicenseEntity> platform = licenseRepository.findOne(spec, graphBuilder);
        return platform.map(entity -> new LicenseData(entity, Optional.of(propertyMapping)));
    }

    @Override
    public EntityPage<License> getEntities(Request request) throws ProviderException {
        QueryOptions options = request.getQueryOptions();
        Pageable pageable = StaPageRequest.create(options);

        LicenseGraphBuilder graphBuilder = request.isRefRequest()
                ? LicenseGraphBuilder.createEmpty()
                : LicenseGraphBuilder.createWith(options);
        Specification<LicenseEntity> spec = rootSpecification.buildSpecification(request);
        Page<LicenseEntity> results = licenseRepository.findAll(spec, pageable, graphBuilder);
        return new StaEntityPage<>(License.class, results,
                entity -> new LicenseData(entity, Optional.of(propertyMapping)));
    }

}
