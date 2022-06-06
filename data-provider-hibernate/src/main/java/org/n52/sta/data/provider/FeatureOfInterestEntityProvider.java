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

import org.n52.sta.api.EntityPage;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.path.Request;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.repositories.entity.FeatureOfInterestRepository;

import java.util.Objects;
import java.util.Optional;

public class FeatureOfInterestEntityProvider extends BaseEntityProvider<FeatureOfInterest> {

    private final FeatureOfInterestRepository featureOfInterestRepository;

    public FeatureOfInterestEntityProvider(FeatureOfInterestRepository featureOfInterestRepository,
                                           EntityPropertyMapping propertyMapping) {
        super(propertyMapping);
        Objects.requireNonNull(featureOfInterestRepository, "featureOfInterestRepository must not be null");
        this.featureOfInterestRepository = featureOfInterestRepository;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return featureOfInterestRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<FeatureOfInterest> getEntity(Request req) throws ProviderException {
        return Optional.empty();
    }

    @Override
    public EntityPage<FeatureOfInterest> getEntities(Request req) throws ProviderException {
        return null;
    }

    /*
    @Override
    public Optional<FeatureOfInterest> getEntity(String id, QueryOptions options) throws ProviderException {
        assertIdentifier(id);

        FeatureOfInterestGraphBuilder graphBuilder = new FeatureOfInterestGraphBuilder();
        addUnfilteredExpandItems(options, graphBuilder);

        Specification<AbstractFeatureEntity<?>> spec = FilterQueryParser.parse(options, new
        FeatureOfInterestQuerySpecification());
        Optional<AbstractFeatureEntity<?>> platform = featureOfInterestRepository.findOne(spec, graphBuilder);
        return platform.map(FeatureOfInterestData::new);
    }

    @Override
    public EntityPage<FeatureOfInterest> getEntities(Request req)throws ProviderException {
        Pageable pageable = StaPageRequest.create(options);

        FeatureOfInterestGraphBuilder graphBuilder = new FeatureOfInterestGraphBuilder();
        addUnfilteredExpandItems(options, graphBuilder);

        Specification<AbstractFeatureEntity<?>> spec = FilterQueryParser.parse(options, new
        FeatureOfInterestQuerySpecification());
        Page<AbstractFeatureEntity<?>> results = featureOfInterestRepository.findAll(spec, pageable, graphBuilder);
        return new StaEntityPage<>(FeatureOfInterest.class, results, FeatureOfInterestData::new);
    }
    */

}
