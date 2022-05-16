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

import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.entity.ObservedPropertyData;
import org.n52.sta.data.query.FilterQueryParser;
import org.n52.sta.data.query.specifications.ObservedPropertyQuerySpecification;
import org.n52.sta.data.repositories.entity.PhenomenonRepository;
import org.n52.sta.data.support.ObservedPropertyGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;
import java.util.Optional;

public class ObservedPropertyEntityProvider extends BaseEntityProvider<ObservedProperty> {

    private final PhenomenonRepository observedPropertyRepository;

    public ObservedPropertyEntityProvider(PhenomenonRepository observedPropertyRepository) {
        Objects.requireNonNull(observedPropertyRepository, "observedPropertyRepository must not be null");
        this.observedPropertyRepository = observedPropertyRepository;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return observedPropertyRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<ObservedProperty> getEntity(String id, QueryOptions options) throws ProviderException {
        assertIdentifier(id);

        ObservedPropertyGraphBuilder graphBuilder = new ObservedPropertyGraphBuilder();
        addUnfilteredExpandItems(options, graphBuilder);

        Specification<PhenomenonEntity> spec =
            FilterQueryParser.parse(options, new ObservedPropertyQuerySpecification());
        Optional<PhenomenonEntity> platform = observedPropertyRepository.findOne(spec, graphBuilder);
        return platform.map(ObservedPropertyData::new);
    }

    @Override
    public EntityPage<ObservedProperty> getEntities(QueryOptions options) throws ProviderException {
        Pageable pagable = StaPageRequest.create(options);

        ObservedPropertyGraphBuilder graphBuilder = new ObservedPropertyGraphBuilder();
        addUnfilteredExpandItems(options, graphBuilder);

        Specification<PhenomenonEntity> spec =
            FilterQueryParser.parse(options, new ObservedPropertyQuerySpecification());
        Page<PhenomenonEntity> results = observedPropertyRepository.findAll(spec, pagable, graphBuilder);
        return new StaEntityPage<>(ObservedProperty.class, results, ObservedPropertyData::new);
    }

}
