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

package org.n52.sta.api.domain.service;

import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.domain.DomainService.DomainServiceAdapter;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.api.path.Request;
import org.n52.sta.api.service.EntityService;

import java.util.Optional;

public class ObservationDomainService extends DomainServiceAdapter<Observation> {

    private final EntityServiceLookup serviceLookup;

    public ObservationDomainService(EntityService<Observation> entityProvider, EntityServiceLookup serviceLookup) {
        super(entityProvider);
        this.serviceLookup = serviceLookup;
    }

    @Override
    public Observation save(Observation entity) throws EditorException {
        checkGroupClosed(entity);
        return super.save(entity);
    }

    @Override
    public Observation update(String id, Observation entity) throws EditorException {
        checkGroupClosed(entity);
        return super.update(id, entity);
    }

    @Override
    public void delete(String id) throws EditorException {
        Optional<Observation> observation = getEntity(Request.createIdRequest(id));
        observation.ifPresent(this::checkGroupClosed);
        super.delete(id);
    }

    // those that do not own the group) can no longer add observations or update or
    private void checkGroupClosed(Observation entity) throws EditorException {
        //TODO: This does not apply if you are the Group Owner
        EntityService<Group> groupService = serviceLookup.getService(Group.class).get();
        for (Group group : entity.getGroups()) {
            Optional<Group> groupEntity = groupService.getEntity(Request.createIdRequest(group.getId()));
            Time runTime = groupEntity
                    .orElseThrow(() -> new EditorException("Cannot find group with id: " + group.getId()))
                    .getRunTime();
            // check if Group is already closed
            if (runTime instanceof TimePeriod && ((TimePeriod) runTime).getEnd().isBeforeNow()) {
                throw new EditorException("Cannot add to group - group is closed");
            }
        }

    }
}
