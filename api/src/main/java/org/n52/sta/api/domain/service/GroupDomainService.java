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

import org.apache.commons.lang.NotImplementedException;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.ServiceLookup;
import org.n52.sta.api.domain.DomainService.DomainServiceAdapter;
import org.n52.sta.api.domain.aggregate.GroupAggregate;
import org.n52.sta.api.domain.rules.ClosedGroupDomainRule;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.api.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class GroupDomainService extends DomainServiceAdapter<Group> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupDomainService.class);

    private final GroupService groupService;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public GroupDomainService(GroupService groupService, ServiceLookup serviceLookup) {
        super(groupService, serviceLookup);
        this.groupService = groupService;
    }

    @Override
    public Group save(Group entity) throws EditorException {

        // get timestamp of referenced observations
        // either: validate observations against the runtime of the group
        // or: auto-update runtime (preferred)

        GroupAggregate aggregate = groupService.createAggregate(entity);
        return super.save(aggregate);
    }

    @Override
    public Group update(String id, Group entity) throws EditorException {
        return groupService.getEntity(id)
                .map(stored -> {
                    GroupAggregate aggregate = groupService.createAggregate(entity);
                    Time runtime = entity.getRunTime();
                    if (runtime == null) {

                        // no edits allowed
                        ClosedGroupDomainRule rule = new ClosedGroupDomainRule(serviceLookup);
                        rule.assertUpdateGroup(stored);

                        // auto-updating runtime only makes sense with an owner,
                        // i.e. when there actually are users who are not allowed
                        // to add further observations when the group is closed

                    } else {

                        // case 0: closing group -> ok
                        // case 1: opening group -> ok
                        // case 2: updating runtime -> [ min(min(rt), min(ob)), max(max(rt), max(ob)) ]
                        // -> make aggregate writable

                        throw new NotImplementedException("Group update via domain service not supported yet.");

                    }

                    return super.save(aggregate);
                }).orElseThrow(() -> new EditorException("Group with id '" + id + "' is unknown"));
    }
}
