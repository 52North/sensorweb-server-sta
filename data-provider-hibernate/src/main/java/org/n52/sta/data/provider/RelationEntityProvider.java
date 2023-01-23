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

import org.n52.series.db.beans.sta.RelationEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.exception.ProviderException;
import org.n52.sta.api.entity.Relation;
import org.n52.sta.api.path.Request;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.entity.RelationData;
import org.n52.sta.data.query.specifications.RelationQuerySpecification;
import org.n52.sta.data.repositories.entity.RelationRepository;
import org.n52.sta.data.support.RelationGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class RelationEntityProvider extends BaseEntityProvider<Relation> {

    private final RelationRepository relationRepository;
    private final RelationQuerySpecification rootSpecification;

    public RelationEntityProvider(RelationRepository relationRepository, EntityPropertyMapping propertyMapping) {
        super(propertyMapping);
        Objects.requireNonNull(relationRepository, "relationRepository must not be null");
        this.relationRepository = relationRepository;
        this.rootSpecification = new RelationQuerySpecification();
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return relationRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<Relation> getEntity(Request request) throws ProviderException {
        RelationGraphBuilder graphBuilder = request.isRefRequest() ? RelationGraphBuilder.createEmpty()
                : RelationGraphBuilder.createWith(request.getQueryOptions());
        return getEntity(rootSpecification.buildSpecification(request), graphBuilder);
    }

    @Override
    public Optional<Relation> getEntity(String id, QueryOptions queryOptions) throws ProviderException {
        RelationGraphBuilder graphBuilder = RelationGraphBuilder.createEmpty();
        return getEntity(
                rootSpecification.buildSpecification(queryOptions).and(rootSpecification.equalsStaIdentifier(id)),
                graphBuilder);
    }

    private Optional<Relation> getEntity(Specification<RelationEntity> spec, RelationGraphBuilder graphBuilder) {
        Optional<RelationEntity> platform = relationRepository.findOne(spec, graphBuilder);
        return platform.map(entity -> new RelationData(entity, Optional.of(propertyMapping)));
    }

    @Override
    public EntityPage<Relation> getEntities(Request request) throws ProviderException {
        QueryOptions options = request.getQueryOptions();
        Pageable pageable = StaPageRequest.create(options);

        RelationGraphBuilder graphBuilder =
                request.isRefRequest() ? RelationGraphBuilder.createEmpty() : RelationGraphBuilder.createWith(options);
        Specification<RelationEntity> spec = rootSpecification.buildSpecification(request);
        Page<RelationEntity> results = relationRepository.findAll(spec, pageable, graphBuilder);
        return new StaEntityPage<>(Relation.class, results,
                entity -> new RelationData(entity, Optional.of(propertyMapping)));
    }

}