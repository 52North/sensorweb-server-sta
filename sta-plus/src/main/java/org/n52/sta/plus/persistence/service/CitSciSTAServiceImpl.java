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
package org.n52.sta.plus.persistence.service;

import org.n52.series.db.beans.HibernateRelations;
import org.n52.sta.api.old.dto.common.StaDTO;
import org.n52.sta.data.CommonSTAServiceImpl;
import org.n52.sta.data.repositories.StaIdentifierRepository;
import org.n52.sta.data.service.CommonEntityServiceRepository;

import javax.persistence.EntityManager;

@SuppressWarnings("checkstyle:LineLength")
public abstract class CitSciSTAServiceImpl<T extends StaIdentifierRepository<S>, R extends StaDTO, S extends HibernateRelations.HasId>
        extends CommonSTAServiceImpl<T, R, S> {

    public CitSciSTAServiceImpl(T repository,
            EntityManager em,
            Class entityClass) {
        super(repository, em, entityClass);
    }

    protected GroupService getObservationGroupService() {
        return (GroupService) serviceRepository.getEntityServiceRaw(
                CitSciEntityServiceRepository.StaPlusEntityTypes.Group.name());
    }

    protected RelationService getObservationRelationService() {
        return (RelationService) serviceRepository
                .getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.Relation.name());
    }

    protected LicenseService getLicenseService() {
        return (LicenseService) serviceRepository
                .getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.License.name());
    }

    protected CitSciDatastreamService getStaPlusDatastreamService() {
        return (CitSciDatastreamService) serviceRepository
                .getEntityServiceRaw(CommonEntityServiceRepository.EntityTypes.Datastream.name());
    }

    protected PartyService getPartyService() {
        return (PartyService) serviceRepository
                .getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.Party.name());
    }

    protected ProjectService getProjectService() {
        return (ProjectService) serviceRepository
                .getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.Project.name());
    }

}
