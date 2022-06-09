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

import org.n52.series.db.beans.ProcedureEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.path.Request;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.StaEntityPage;
import org.n52.sta.data.StaPageRequest;
import org.n52.sta.data.entity.SensorData;
import org.n52.sta.data.query.specifications.SensorQuerySpecification;
import org.n52.sta.data.repositories.entity.ProcedureRepository;
import org.n52.sta.data.support.SensorGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class SensorEntityProvider extends BaseEntityProvider<Sensor> {

    private final ProcedureRepository sensorRepository;
    private final SensorQuerySpecification rootSpecification;

    public SensorEntityProvider(ProcedureRepository sensorRepository, EntityPropertyMapping propertyMapping) {
        super(propertyMapping);
        Objects.requireNonNull(sensorRepository, "sensorRepository must not be null");
        this.sensorRepository = sensorRepository;
        this.rootSpecification = new SensorQuerySpecification();
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        assertIdentifier(id);
        return sensorRepository.existsByStaIdentifier(id);
    }

    @Override
    public Optional<Sensor> getEntity(Request req) throws ProviderException {
        SensorGraphBuilder graphBuilder = SensorGraphBuilder.createWith(req.getQueryOptions());
        return getEntity(rootSpecification.buildSpecification(req), graphBuilder);
    }

    @Override
    public Optional<Sensor> getEntity(String id, QueryOptions queryOptions) throws ProviderException {
        SensorGraphBuilder graphBuilder = SensorGraphBuilder.createEmpty();
        return getEntity(rootSpecification.buildSpecification(queryOptions), graphBuilder);
    }

    private Optional<Sensor> getEntity(Specification<ProcedureEntity> spec, SensorGraphBuilder graphBuilder) {
        Optional<ProcedureEntity> platform = sensorRepository.findOne(spec, graphBuilder);
        return platform.map(entity -> new SensorData(entity, propertyMapping));
    }

    @Override
    public EntityPage<Sensor> getEntities(Request req) throws ProviderException {
        Pageable pageable = StaPageRequest.create(req.getQueryOptions());
        SensorGraphBuilder graphBuilder = SensorGraphBuilder.createWith(req.getQueryOptions());
        Specification<ProcedureEntity> spec = rootSpecification.buildSpecification(req);
        Page<ProcedureEntity> results = sensorRepository.findAll(spec, pageable, graphBuilder);
        return new StaEntityPage<>(Sensor.class, results, entity -> new SensorData(entity, propertyMapping));
    }

}
