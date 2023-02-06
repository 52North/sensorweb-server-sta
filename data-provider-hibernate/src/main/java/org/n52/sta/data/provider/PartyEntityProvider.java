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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.exception.ProviderException;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.path.Request;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.entity.ObservedPropertyData;
import org.n52.sta.data.entity.PartyData;
import org.n52.sta.data.query.specifications.PartyQuerySpecification;
import org.n52.sta.data.repositories.entity.PartyRepository;
import org.n52.sta.data.support.PartyGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class PartyEntityProvider extends BaseEntityProvider<Party> {

    private final PartyRepository partyRepository;
    private final PartyQuerySpecification rootSpecification;

    public PartyEntityProvider(PartyRepository partyRepository, EntityPropertyMapping propertyMapping) {
        super(propertyMapping);
        Objects.requireNonNull(partyRepository, "partyRepository must not be null");
        this.partyRepository = partyRepository;
        this.rootSpecification = new PartyQuerySpecification();
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return partyRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<Party> getEntity(Request request) throws ProviderException {
        PartyGraphBuilder graphBuilder = request.isRefRequest()
                ? PartyGraphBuilder.createEmpty()
                : PartyGraphBuilder.createWith(request.getQueryOptions());
        return getEntity(rootSpecification.buildSpecification(request), graphBuilder);
    }

    @Override
    public Optional<Party> getEntity(String id, QueryOptions queryOptions) throws ProviderException {
        PartyGraphBuilder graphBuilder = PartyGraphBuilder.createEmpty();
        return getEntity(rootSpecification.buildSpecification(queryOptions)
                                          .and(rootSpecification.equalsStaIdentifier(id)),
                         graphBuilder);
    }

    private Optional<Party> getEntity(Specification<PartyEntity> spec, PartyGraphBuilder graphBuilder) {
        Optional<PartyEntity> platform = partyRepository.findOne(spec, graphBuilder);
        return platform.map(entity -> new PartyData(entity, Optional.of(propertyMapping)));
    }

    @Override
    public List<Party> getEntities(Set<String> ids) throws ProviderException {
        List<PartyEntity> allByStaIdentifier = partyRepository.findAllByStaIdentifier(ids);
        return allByStaIdentifier.stream()
            .map(entity -> new PartyData(entity, Optional.of(propertyMapping)))
            .collect(Collectors.toList());
    }
    @Override
    public EntityPage<Party> getEntities(Request request) throws ProviderException {
        QueryOptions options = request.getQueryOptions();
        Pageable pageable = StaPageRequest.create(options);

        PartyGraphBuilder graphBuilder = request.isRefRequest()
                ? PartyGraphBuilder.createEmpty()
                : PartyGraphBuilder.createWith(options);
        Specification<PartyEntity> spec = rootSpecification.buildSpecification(request);
        Page<PartyEntity> results = partyRepository.findAll(spec, pageable, graphBuilder);
        return new StaEntityPage<>(Party.class, results,
                entity -> new PartyData(entity, Optional.of(propertyMapping)));
    }

}
