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
package org.n52.sta.api.domain;

import java.util.Optional;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.domain.event.DomainEvent;
import org.n52.sta.api.domain.event.DomainEventService;
import org.n52.sta.api.entity.Identifiable;

public interface DomainService<T extends Identifiable> extends EntityProvider<T> {

    void sendDomainEvent(DomainEvent<T> event);

    abstract class DomainServiceAdapter<T extends Identifiable> implements DomainService<T> {

        private final EntityProvider<T> entityProvider;

        private Optional<DomainEventService> domainEventService;

        protected DomainServiceAdapter(EntityProvider<T> entityProvider) {
            this.entityProvider = entityProvider;
            this.domainEventService = Optional.empty();
        }

        @Override
        public void sendDomainEvent(DomainEvent<T> event) {
            domainEventService.ifPresent(service -> service.handleDomainEvent(event));
        }

        @Override
        public boolean exists(String id) throws ProviderException {
            return entityProvider.exists(id);
        }

        @Override
        public Optional<T> getEntity(String id, QueryOptions options) throws ProviderException {
            return entityProvider.getEntity(id, options);
        }

        @Override
        public EntityPage<T> getEntities(QueryOptions options) throws ProviderException {
            return entityProvider.getEntities(options);
        }

        public void setDomainEventService(DomainEventService domainEventService) {
            this.domainEventService = Optional.ofNullable(domainEventService);
        }

    }

}
