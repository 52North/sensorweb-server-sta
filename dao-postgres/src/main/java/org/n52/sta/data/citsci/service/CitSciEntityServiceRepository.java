/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.data.citsci.service;

import org.n52.sta.data.common.service.CommonEntityServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CitSciEntityServiceRepository extends CommonEntityServiceRepository {

    @Autowired private StaPlusServiceFacade.GroupServiceFacade groupService;

    @Autowired private StaPlusServiceFacade.RelationServiceFacade relationService;

    @Autowired private StaPlusServiceFacade.LicenseServiceFacade licenseService;

    @Autowired private StaPlusServiceFacade.PartyServiceFacade partyService;

    @Autowired private StaPlusServiceFacade.ProjectServiceFacade projectService;

    @PostConstruct
    public void postConstruct() {
        super.postConstruct();
        entityServices.put(StaPlusEntityTypes.Group.name(), groupService);
        entityServices.put(StaPlusEntityTypes.Groups.name(), groupService);

        entityServices.put(StaPlusEntityTypes.Relation.name(), relationService);
        entityServices.put(StaPlusEntityTypes.Relations.name(), relationService);

        entityServices.put(StaPlusEntityTypes.Objects.name(), relationService);
        entityServices.put(StaPlusEntityTypes.Object.name(), relationService);

        entityServices.put(StaPlusEntityTypes.Subjects.name(), relationService);
        entityServices.put(StaPlusEntityTypes.Subject.name(), relationService);

        entityServices.put(StaPlusEntityTypes.License.name(), licenseService);
        entityServices.put(StaPlusEntityTypes.Licenses.name(), licenseService);

        entityServices.put(StaPlusEntityTypes.Party.name(), partyService);
        entityServices.put(StaPlusEntityTypes.Parties.name(), partyService);

        entityServices.put(StaPlusEntityTypes.Project.name(), projectService);
        entityServices.put(StaPlusEntityTypes.Projects.name(), projectService);
    }

    public enum StaPlusEntityTypes {
        Group, Groups, Subject, Subjects, Object, Objects,
        Relation, Relations, License, Licenses, Party, Parties, Project, Projects
    }
}
