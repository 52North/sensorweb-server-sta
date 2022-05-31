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

import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.query.FilterQueryParser;
import org.n52.sta.data.query.specifications.ObservationQuerySpecification;
import org.n52.sta.data.repositories.entity.ObservationRepository;
import org.n52.sta.data.support.ObservationGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;
import java.util.Optional;

public class ObservationEntityProvider extends BaseEntityProvider<Observation> {

    private final ObservationRepository observationRepository;

    public ObservationEntityProvider(ObservationRepository observationRepository) {
        Objects.requireNonNull(observationRepository, "observationRepository must not be null");
        this.observationRepository = observationRepository;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return observationRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<Observation> getEntity(StaRequest path) throws ProviderException {
        assertIdentifier(id);

        ObservationGraphBuilder graphBuilder = new ObservationGraphBuilder();
        addUnfilteredExpandItems(path, graphBuilder);

        Optional<DataEntity<?>> platform = observationRepository.findByStaIdentifier(id, graphBuilder);
        return platform.map(ObservationData::new);
    }

    @Override
    public EntityPage<Observation> getEntities(QueryOptions options) throws ProviderException {
        Pageable pagable = StaPageRequest.create(options);

        ObservationGraphBuilder graphBuilder = new ObservationGraphBuilder();
        addUnfilteredExpandItems(options, graphBuilder);

        Specification<DataEntity<?>> spec = FilterQueryParser.parse(options, new ObservationQuerySpecification());
        Page<DataEntity<?>> results = observationRepository.findAll(spec, pagable, graphBuilder);
        return new StaEntityPage<>(Observation.class, results, ObservationData::new);
    }

}
