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
package org.n52.sta.api.service;

import java.util.Objects;
import java.util.Optional;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.domain.aggregate.AggregateException;
import org.n52.sta.api.domain.aggregate.EntityAggregate;
import org.n52.sta.api.domain.aggregate.FeatureOfInterestAggregate;
import org.n52.sta.api.domain.service.DefaultDomainService;
import org.n52.sta.api.domain.service.DomainService;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureOfInterestService implements EntityService<FeatureOfInterest>, EntityEditor<FeatureOfInterest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureOfInterestService.class);

    private final EntityProvider<FeatureOfInterest> featureOfInterestProvider;

    private final DomainService<FeatureOfInterest> domainService;

    private Optional<EntityEditor<FeatureOfInterest>> featureOfInterestEditor;

    public FeatureOfInterestService(EntityProvider<FeatureOfInterest> provider) {
        this(provider, new DefaultDomainService<>(provider));
    }

    public FeatureOfInterestService(EntityProvider<FeatureOfInterest> provider,
            DomainService<FeatureOfInterest> domainService) {
        Objects.requireNonNull(provider, "provider must not be null");
        this.featureOfInterestProvider = provider;
        this.domainService = domainService == null
                ? new DefaultDomainService<>(provider)
                : domainService;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        return domainService.exists(id);
    }

    @Override
    public Optional<FeatureOfInterest> getEntity(StaRequest path) throws ProviderException {
        return domainService.getEntity(path);
    }

    @Override
    public EntityPage<FeatureOfInterest> getEntities(QueryOptions options) throws ProviderException {
        return domainService.getEntities(options);
    }

    @Override
    public FeatureOfInterest create(FeatureOfInterest entity) throws ProviderException {
        try {
            return createAggregate(entity).save();
        } catch (AggregateException e) {
            LOGGER.error("Could not create entity: {}", entity, e);
            throw new ProviderException("Could not create FeatureOfInterest!");
        }
    }

    @Override
    public FeatureOfInterest update(FeatureOfInterest entity) throws ProviderException {
        Objects.requireNonNull(entity, "entity must not be null!");
        try {
            String id = entity.getId();
            FeatureOfInterest featureOfInterest = getOrThrow(id);
            return createAggregate(featureOfInterest).save(entity);
        } catch (AggregateException e) {
            LOGGER.error("Could not update entity: {}", entity, e);
            throw new ProviderException("Could not update FeatureOfInterest!");
        }
    }

    @Override
    public void delete(String id) throws ProviderException {
        FeatureOfInterest entity = getOrThrow(id);
        try {
            createAggregate(entity).delete();
        } catch (AggregateException e) {
            LOGGER.error("Could not delete entity: {}", entity, e);
            throw new ProviderException("Could not delete FeatureOfInterest!");
        }
    }

    public void setFeatureOfInterestEditor(EntityEditor<FeatureOfInterest> editor) {
        featureOfInterestEditor = Optional.ofNullable(editor);
    }

    private EntityAggregate<FeatureOfInterest> createAggregate(FeatureOfInterest entity) {
        return new FeatureOfInterestAggregate(entity, domainService, featureOfInterestEditor.orElse(null));
    }

    private FeatureOfInterest getOrThrow(String id) throws ProviderException {
        return domainService.getEntity(id)
                .orElseThrow(() -> new ProviderException("Id '" + id + "' does not exist."));
    }

}
