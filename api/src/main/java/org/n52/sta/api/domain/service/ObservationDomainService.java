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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.domain.DomainService.DomainServiceAdapter;
import org.n52.sta.api.domain.aggregate.EntityAggregate;
import org.n52.sta.api.domain.aggregate.ObservationAggregate;
import org.n52.sta.api.domain.rules.ClosedGroupDomainRule;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.api.path.Request;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.api.service.ObservationService;

public class ObservationDomainService extends DomainServiceAdapter<Observation> {

    private final EntityServiceLookup serviceLookup;

    private final ObservationService entityProvider;

    public ObservationDomainService(ObservationService entityProvider, EntityServiceLookup serviceLookup) {
        super(entityProvider);
        this.entityProvider = entityProvider;
        this.serviceLookup = serviceLookup;
    }

    @Override
    public Observation save(Observation entity) throws EditorException {
        ObservationAggregate aggregate = entityProvider.createAggregate(entity);

        // Iterator<DomainServiceAdapter<Observation>.DomainRule> domainRules = getDomainRules();
        // while (domainRules.hasNext()) {

        //     DomainRule rule = domainRules.next();
        //     // TODO rule.assertSave(ObservationAggregate aggregate)

        //     // TODO make checkGroupClosed a ClosedGroupDomainRule object
        //     checkGroupClosed(aggregate);
        // }

        ClosedGroupDomainRule rule = new ClosedGroupDomainRule();
        Streams.stream(getGroups(entity)).forEach(rule::assertEditableGroup);

        return save(aggregate);
    }

    @Override
    public Observation update(String id, Observation entity) throws EditorException {
        ObservationAggregate aggregate = entityProvider.createAggregate(entity);

        // Iterator<DomainServiceAdapter<Observation>.DomainRule> domainRules = getDomainRules();
        // while (domainRules.hasNext()) {

        //     DomainRule rule = domainRules.next();
        //     // TODO rule.assertUpdate(ObservationAggregate aggregate)

        //     // TODO make checkGroupClosed a ClosedGroupDomainRule object
        //     checkGroupClosed(aggregate);

        // }

        ClosedGroupDomainRule rule = new ClosedGroupDomainRule();
        Streams.stream(entity.getGroups()).forEach(rule::assertEditableGroup);

        return update(id, aggregate);
    }

    @Override
    public void delete(String id) throws EditorException {
        getEntity(Request.createIdRequest(id))
            .ifPresent(entity -> {
                ObservationAggregate aggregate = entityProvider.createAggregate(entity);
                // Iterator<DomainServiceAdapter<Observation>.DomainRule> domainRules = getDomainRules();
                // while (domainRules.hasNext()) {

                //     DomainRule rule = domainRules.next();
                //     // TODO rule.assertDelete(ObservationAggregate aggregate)

                //     // TODO make checkGroupClosed a ClosedGroupDomainRule object
                //     checkGroupClosed(aggregate);

                // }

                ClosedGroupDomainRule rule = new ClosedGroupDomainRule();
                Streams.stream(entity.getGroups()).forEach(rule::assertEditableGroup);

                delete(id);
            });
    }

    private List<Group> getGroups(Observation entity) {
        EntityService<Group> groupService = serviceLookup.getService(Group.class).get();
        return Streams.stream(entity.getGroups())
                        .map(group -> Request.createIdRequest(group.getId()))
                        .map(request -> groupService.getEntity(request))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList());
    }


    // those that do not own the group) can no longer add observations or update or
    private void checkGroupClosed(ObservationAggregate entity) throws EditorException {
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
