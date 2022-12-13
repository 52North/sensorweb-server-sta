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
package org.n52.sta.data.entity;

import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.sta.api.domain.PartyRole;
import org.n52.sta.api.entity.*;
import org.n52.sta.config.EntityPropertyMapping;

import java.util.Optional;
import java.util.Set;

public class PartyData extends StaData<PartyEntity> implements Party {

    public PartyData(PartyEntity dataEntity, Optional<EntityPropertyMapping> propertyMapping) {
        super(dataEntity, propertyMapping);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public String getDescription() {
        return data.getDescription();
    }

    @Override
    public String getAuthId() {
        return data.getAuthId();
    }

    @Override
    public PartyRole getRole() {
        return PartyRole.valueOf(data.getRole().name());
    }

    @Override
    public Optional<String> getDisplayName() {
        return Optional.of(data.getDisplayName());
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return toSet(data.getDatasets(), entity -> new DatastreamData(entity, propertyMapping));
    }

    @Override
    public Set<Thing> getThings() {
        return toSet(data.getPlatforms(), platformEntity -> new ThingData(platformEntity, propertyMapping));
    }

    @Override
    public Set<Group> getGroups() {
        return toSet(data.getGroups(), groupEntity -> new GroupData(groupEntity, propertyMapping));
    }

}
