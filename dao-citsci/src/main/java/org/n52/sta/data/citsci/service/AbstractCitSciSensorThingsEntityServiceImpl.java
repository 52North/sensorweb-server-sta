/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
 * Software GmbH
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

import org.n52.series.db.beans.HibernateRelations;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.data.citsci.CitSciDTOTransformerImpl;
import org.n52.sta.data.citsci.CitSciEntityServiceRepository;
import org.n52.sta.data.vanilla.repositories.StaIdentifierRepository;
import org.n52.sta.data.vanilla.service.AbstractSensorThingsEntityServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class AbstractCitSciSensorThingsEntityServiceImpl<
    T extends StaIdentifierRepository<S>,
    R extends StaDTO,
    S extends HibernateRelations.HasId>
    extends AbstractSensorThingsEntityServiceImpl<T, R, S> {

    private CitSciEntityServiceRepository serviceRepository;

    public AbstractCitSciSensorThingsEntityServiceImpl(T repository,
                                                       EntityManager em,
                                                       Class entityClass) {
        super(repository, em, entityClass);
    }

    @Transactional(rollbackFor = Exception.class)
    protected R createWrapper(S entity, QueryOptions queryOptions) {
        return new CitSciDTOTransformerImpl<R, S>(null).toDTO(entity, queryOptions);
    }

    ObservationGroupService getObservationGroupService() {
        return (ObservationGroupService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.CitSciEntityTypes.ObservationGroup);
    }

    ObservationRelationService getObservationRelationService() {
        return (ObservationRelationService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.CitSciEntityTypes.Subject);
    }

    LicenseService getLicenseService() {
        return (LicenseService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.CitSciEntityTypes.License);
    }

    PartyService getPartyService() {
        return (PartyService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.CitSciEntityTypes.Party);
    }

    ProjectService getProjectService() {
        return (ProjectService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.CitSciEntityTypes.Project);
    }

}
