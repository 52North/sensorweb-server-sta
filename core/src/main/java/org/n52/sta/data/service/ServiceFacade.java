/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.service;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservationGroupEntity;
import org.n52.series.db.beans.sta.ObservationRelationEntity;
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.DaoSemaphore;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Facade proxying actual Service implementations. Used to restrict the number of parallel threads accessing the Data
 * Persistence Layer. This is necessary as each Thread uses a seperate Database Transaction and therefore needs a
 * dedicated DatabaseConnection.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ServiceFacade<S extends HibernateRelations.HasId>
    implements AbstractSensorThingsEntityService<S> {

    private final DaoSemaphore semaphore;
    private AbstractSensorThingsEntityService<S> serviceImpl;

    public ServiceFacade(AbstractSensorThingsEntityService<S> serviceImpl, DaoSemaphore semaphore) {
        this.serviceImpl = serviceImpl;
        this.semaphore = semaphore;
    }

    AbstractSensorThingsEntityService<S> getServiceImpl() {
        return serviceImpl;
    }

    @Override public boolean existsEntity(String id) throws STACRUDException {
        boolean result;
        try {
            semaphore.acquire();
            result = serviceImpl.existsEntity(id);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public ElementWithQueryOptions getEntity(String id, QueryOptions queryOptions) throws STACRUDException {
        ElementWithQueryOptions result;
        try {
            semaphore.acquire();
            result = serviceImpl.getEntity(id, queryOptions);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        CollectionWrapper result;
        try {
            semaphore.acquire();
            result = serviceImpl.getEntityCollection(queryOptions);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public ElementWithQueryOptions<?> getEntityByRelatedEntity(String relatedId,
                                                                         String relatedType,
                                                                         String ownId,
                                                                         QueryOptions queryOptions)
        throws STACRUDException {
        ElementWithQueryOptions<?> result;
        try {
            semaphore.acquire();
            result = serviceImpl.getEntityByRelatedEntity(relatedId, relatedType, ownId, queryOptions);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                          String relatedType,
                                                                          QueryOptions queryOptions)
        throws STACRUDException {
        CollectionWrapper result;
        try {
            semaphore.acquire();
            result = serviceImpl.getEntityCollectionByRelatedEntity(relatedId, relatedType, queryOptions);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public String getEntityIdByRelatedEntity(String relatedId, String relatedType) throws STACRUDException {
        String result;
        try {
            semaphore.acquire();
            result = serviceImpl.getEntityIdByRelatedEntity(relatedId, relatedType);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public boolean existsEntityByRelatedEntity(String relatedId, String relatedType, String ownId)
        throws STACRUDException {
        boolean result;
        try {
            semaphore.acquire();
            result = serviceImpl.existsEntityByRelatedEntity(relatedId, relatedType, ownId);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public ElementWithQueryOptions create(S entity) throws STACRUDException {
        ElementWithQueryOptions result;
        try {
            semaphore.acquire();
            result = serviceImpl.create(entity);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public ElementWithQueryOptions update(String id, S entity, HttpMethod method) throws STACRUDException {
        ElementWithQueryOptions result;
        try {
            semaphore.acquire();
            result = serviceImpl.update(id, entity, method);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override public void delete(String id) throws STACRUDException {
        try {
            semaphore.acquire();
            serviceImpl.delete(id);
        } catch (InterruptedException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
    }

    @Override public void setServiceRepository(EntityServiceRepository serviceRepository) {
        serviceImpl.setServiceRepository(serviceRepository);
    }

    @Component
    static class ThingServiceFacade extends ServiceFacade<PlatformEntity> {

        ThingServiceFacade(ThingService serviceImpl,
                           DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class LocationServiceFacade extends ServiceFacade<LocationEntity> {

        LocationServiceFacade(LocationService serviceImpl,
                              DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class HistoricalLocationServiceFacade
        extends ServiceFacade<HistoricalLocationEntity> {

        HistoricalLocationServiceFacade(HistoricalLocationService serviceImpl,
                                        DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class SensorServiceFacade extends ServiceFacade<ProcedureEntity> {

        SensorServiceFacade(SensorService serviceImpl,
                            DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class ObservedPropertyServiceFacade
        extends ServiceFacade<PhenomenonEntity> {

        ObservedPropertyServiceFacade(ObservedPropertyService serviceImpl,
                                      DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class ObservationServiceFacade
        extends ServiceFacade<DataEntity<?>> {

        ObservationServiceFacade(ObservationService serviceImpl,
                                 DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class DatastreamServiceFacade extends ServiceFacade<AbstractDatasetEntity> {

        DatastreamServiceFacade(DatastreamService serviceImpl,
                                DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class FeatureOfInterestServiceFacade
        extends ServiceFacade<AbstractFeatureEntity<?>> {

        FeatureOfInterestServiceFacade(FeatureOfInterestService serviceImpl,
                                       DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class LicenseServiceFacade extends ServiceFacade<LicenseEntity> {

        LicenseServiceFacade(LicenseService serviceImpl,
                             DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class PartyServiceFacade extends ServiceFacade<PartyEntity> {

        PartyServiceFacade(PartyService serviceImpl,
                           DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class ProjectServiceFacade extends ServiceFacade<ProjectEntity> {

        ProjectServiceFacade(ProjectService serviceImpl,
                             DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class ObservationRelationServiceFacade extends ServiceFacade<ObservationRelationEntity> {

        ObservationRelationServiceFacade(ObservationRelationService serviceImpl,
                                         DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    static class ObservationGroupServiceFacade extends ServiceFacade<ObservationGroupEntity> {

        ObservationGroupServiceFacade(ObservationGroupService serviceImpl,
                                      DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }
}
