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

package org.n52.sta.plus.persistence.service;

import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.sta.plus.GroupEntity;
import org.n52.series.db.beans.sta.plus.LicenseEntity;
import org.n52.series.db.beans.sta.plus.PartyEntity;
import org.n52.series.db.beans.sta.plus.ProjectEntity;
import org.n52.series.db.beans.sta.plus.RelationEntity;
import org.n52.sta.api.old.dto.common.StaDTO;
import org.n52.sta.data.old.DaoSemaphore;
import org.n52.sta.data.old.SerDesConfig;
import org.n52.sta.data.old.common.CommonSTAServiceImpl;
import org.n52.sta.data.old.common.CommonServiceFacade;
import org.n52.sta.plus.old.entity.GroupDTO;
import org.n52.sta.plus.old.entity.LicenseDTO;
import org.n52.sta.plus.old.entity.PartyDTO;
import org.n52.sta.plus.old.entity.ProjectDTO;
import org.n52.sta.plus.old.entity.RelationDTO;

public class StaPlusServiceFacade<R extends StaDTO, S extends HibernateRelations.HasId>
        extends
        CommonServiceFacade<R, S> {

    public StaPlusServiceFacade(CommonSTAServiceImpl< ? , R, S> serviceImpl,
                                DaoSemaphore semaphore,
                                SerDesConfig config) {
        super(serviceImpl, semaphore, config);
    }

    // @Component
    static class LicenseServiceFacade extends CommonServiceFacade<LicenseDTO, LicenseEntity> {

        LicenseServiceFacade(LicenseService serviceImpl,
                             DaoSemaphore semaphore,
                             SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }

    // @Component
    static class PartyServiceFacade extends CommonServiceFacade<PartyDTO, PartyEntity> {

        PartyServiceFacade(PartyService serviceImpl,
                           DaoSemaphore semaphore,
                           SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }

    // @Component
    static class ProjectServiceFacade extends CommonServiceFacade<ProjectDTO, ProjectEntity> {

        ProjectServiceFacade(ProjectService serviceImpl,
                             DaoSemaphore semaphore,
                             SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }

    // @Component
    static class RelationServiceFacade
            extends
            CommonServiceFacade<RelationDTO, RelationEntity> {

        RelationServiceFacade(RelationService serviceImpl,
                              DaoSemaphore semaphore,
                              SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }

    // @Component
    static class GroupServiceFacade
            extends
            CommonServiceFacade<GroupDTO, GroupEntity> {

        GroupServiceFacade(GroupService serviceImpl,
                           DaoSemaphore semaphore,
                           SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }
}
