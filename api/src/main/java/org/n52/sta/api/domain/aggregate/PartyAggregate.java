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

package org.n52.sta.api.domain.aggregate;

import java.util.Set;

import org.n52.sta.api.domain.PartyRole;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Thing;

public class PartyAggregate extends EntityAggregate<Party> implements Party {

    public PartyAggregate(Party entity) {
        super(entity);
    }

    public String getName() {
        return entity.getName();
    }

    public String getDescription() {
        return entity.getDescription();
    }

    @Override
    public String getAuthId() {
        return entity.getAuthId();
    }

    @Override
    public PartyRole getRole() {
        return entity.getRole();
    }

    @Override
    public String getDisplayName() {
        return entity.getDisplayName();
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return entity.getDatastreams();
    }

    @Override
    public Set<Thing> getThings() {
        return entity.getThings();
    }

    @Override
    public Set<Group> getGroups() {
        return entity.getGroups();
    }

}
