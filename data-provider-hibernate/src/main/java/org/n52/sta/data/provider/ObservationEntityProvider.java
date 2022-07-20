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

import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.path.Request;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.query.specifications.ObservationQuerySpecification;
import org.n52.sta.data.repositories.entity.ObservationRepository;
import org.n52.sta.data.support.ObservationGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class ObservationEntityProvider extends BaseEntityProvider<Observation> {

    private final ObservationRepository observationRepository;
    private final ObservationQuerySpecification rootSpecification;

    public ObservationEntityProvider(ObservationRepository observationRepository,
                                     EntityPropertyMapping propertyMapping) {
        super(propertyMapping);
        Objects.requireNonNull(observationRepository, "observationRepository must not be null!");
        this.observationRepository = observationRepository;
        this.rootSpecification = new ObservationQuerySpecification();
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return observationRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<Observation> getEntity(Request request) throws ProviderException {
        ObservationGraphBuilder graphBuilder = request.isRefRequest()
                ? ObservationGraphBuilder.createEmpty()
                : ObservationGraphBuilder.createWith(request.getQueryOptions());
        return getEntity(rootSpecification.buildSpecification(request), graphBuilder);
    }

    @Override
    public Optional<Observation> getEntity(String id, QueryOptions queryOptions) throws ProviderException {
        ObservationGraphBuilder graphBuilder = ObservationGraphBuilder.createEmpty();
        return getEntity(rootSpecification.buildSpecification(queryOptions), graphBuilder);
    }

    private Optional<Observation> getEntity(Specification<DataEntity< ? >> spec,
            ObservationGraphBuilder graphBuilder) {
        Optional<DataEntity< ? >> platform = observationRepository.findOne(spec, graphBuilder);
        return platform.map(entity -> new ObservationData(entity, propertyMapping));
    }

    @Override
    public EntityPage<Observation> getEntities(Request request) throws ProviderException {
        QueryOptions options = request.getQueryOptions();
        Pageable pageable = StaPageRequest.create(options);

        ObservationGraphBuilder graphBuilder = request.isRefRequest()
                ? ObservationGraphBuilder.createEmpty()
                : ObservationGraphBuilder.createWith(options);
        Specification< ? > spec = rootSpecification.buildSpecification(request);
        Page<DataEntity< ? >> results = observationRepository.findAll(spec, pageable, graphBuilder);
        return new StaEntityPage<>(Observation.class, results, data -> new ObservationData(data, propertyMapping));
    }

}
