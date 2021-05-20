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

package org.n52.sta.data.citsci;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.series.db.beans.sta.ObservationRelationEntity;
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.api.dto.CitSciDatastreamDTO;
import org.n52.sta.api.dto.CitSciObservationDTO;
import org.n52.sta.api.dto.LicenseDTO;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.api.dto.ObservationRelationDTO;
import org.n52.sta.api.dto.PartyDTO;
import org.n52.sta.api.dto.ProjectDTO;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.data.citsci.service.CitSciDatastreamService;
import org.n52.sta.data.citsci.service.CitSciObservationService;
import org.n52.sta.data.citsci.service.LicenseService;
import org.n52.sta.data.citsci.service.ObservationGroupService;
import org.n52.sta.data.citsci.service.ObservationRelationService;
import org.n52.sta.data.citsci.service.PartyService;
import org.n52.sta.data.citsci.service.ProjectService;
import org.n52.sta.data.vanilla.DaoSemaphore;
import org.n52.sta.data.vanilla.service.AbstractSensorThingsEntityServiceImpl;
import org.n52.sta.data.vanilla.service.ServiceFacade;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class CitSciServiceFacade<R extends StaDTO, S extends HibernateRelations.HasId>
    extends ServiceFacade<R, S> {

    public CitSciServiceFacade(AbstractSensorThingsEntityServiceImpl serviceImpl,
                               DaoSemaphore semaphore) {
        super(serviceImpl, semaphore, null);
    }

    @Override public R create(R entity) throws STACRUDException {
        R result;
        try {
            semaphore.acquire();
            result = serviceImpl.create((S) new CitSciDTOTransformer().fromDTO(entity));
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public R update(String id, R entity, HttpMethod method) throws STACRUDException {
        R result;
        try {
            semaphore.acquire();
            result = serviceImpl.update(id, (S) new CitSciDTOTransformer().fromDTO(entity), method);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Component
    static class CitSciObservationServiceFacade extends CitSciServiceFacade<CitSciObservationDTO, DataEntity<?>> {

        CitSciObservationServiceFacade(CitSciObservationService serviceImpl,
                                       DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class CitSciDatastreamServiceFacade extends CitSciServiceFacade<CitSciDatastreamDTO, AbstractDatasetEntity> {

        CitSciDatastreamServiceFacade(CitSciDatastreamService serviceImpl,
                                      DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class LicenseServiceFacade extends CitSciServiceFacade<LicenseDTO, LicenseEntity> {

        LicenseServiceFacade(LicenseService serviceImpl,
                             DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class PartyServiceFacade extends CitSciServiceFacade<PartyDTO, PartyEntity> {

        PartyServiceFacade(PartyService serviceImpl,
                           DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class ProjectServiceFacade extends CitSciServiceFacade<ProjectDTO, ProjectEntity> {

        ProjectServiceFacade(ProjectService serviceImpl,
                             DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class ObservationRelationServiceFacade
        extends CitSciServiceFacade<ObservationRelationDTO, ObservationRelationEntity> {

        ObservationRelationServiceFacade(ObservationRelationService serviceImpl,
                                         DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class ObservationGroupServiceFacade extends CitSciServiceFacade<ObservationGroupDTO, DataEntity<?>> {

        ObservationGroupServiceFacade(ObservationGroupService serviceImpl,
                                      DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }

}
