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

package org.n52.sta.data.cloudnative.service;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.data.DaoSemaphore;
import org.n52.sta.api.AbstractSensorThingsEntityService;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.api.dto.*;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * jOOQ's service facade proxying actual Service implementations.
 * Used to restrict the number of parallel threads accessing the Data Persistence Layer.
 * This is necessary as each Thread uses a separate Database Transaction and therefore needs a dedicated Connection.
 *
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class ServiceFacade <R extends StaDTO> implements AbstractSensorThingsEntityService<R> {

    private final DaoSemaphore semaphore;
    private CloudNativeAbstractSensorThingsEntityServiceImpl<?, R> serviceImpl;

    public ServiceFacade(CloudNativeAbstractSensorThingsEntityServiceImpl<?, R> serviceImpl,
                         DaoSemaphore semaphore) {
        this.serviceImpl = serviceImpl;
        this.semaphore = semaphore;
    }

    public CloudNativeAbstractSensorThingsEntityServiceImpl<?, ?> getServiceImpl() {
        return serviceImpl;
    }


    @Override
    public boolean existsEntity(String id)
            throws STACRUDException {
        boolean result;
        try {
            semaphore.acquire();
            result = serviceImpl.existsEntity(id);
        } catch (InterruptedException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override
    public R getEntity(String id, QueryOptions queryOptions)
            throws STACRUDException {
        R result;
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

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions)
            throws STACRUDException {
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

    @Override
    public R getEntityByRelatedEntity(String relatedId, String relatedType, String ownId, QueryOptions queryOptions)
            throws STACRUDException {
        R result;
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

    @Override
    public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
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

    @Override
    public String getEntityIdByRelatedEntity(String relatedId, String relatedType) throws STACRUDException {
        String result;
        try {
            semaphore.acquire();
            result = serviceImpl.getEntityIdByRelatedEntity(relatedId, relatedType);
        } catch (InterruptedException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override
    public boolean existsEntityByRelatedEntity(String relatedId, String relatedType, String ownId) throws STACRUDException {
        boolean result;
        try {
            semaphore.acquire();
            result = serviceImpl.existsEntityByRelatedEntity(relatedId, relatedType, ownId);
        } catch (InterruptedException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override
    public StaDTO create(StaDTO entity) throws STACRUDException {
        R result;
        try {
            semaphore.acquire();
            result = serviceImpl.create(entity);
        } catch (InterruptedException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override
    public StaDTO update(String id, StaDTO entity, HttpMethod method) throws STACRUDException {
        R result;
        try {
            semaphore.acquire();
            result = serviceImpl.update(id, entity, method);
        } catch (InterruptedException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Override
    public void delete(String id) throws STACRUDException {
        try {
            semaphore.acquire();
            serviceImpl.delete(id);
        } catch (InterruptedException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
    }

    @Component
    public static class CloudNativeThingServiceFacade extends ServiceFacade<ThingDTO> {

        CloudNativeThingServiceFacade(CloudNativeThingService serviceImpl,
                           DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    public static class CloudNativeLocationServiceFacade extends ServiceFacade<LocationDTO> {

        CloudNativeLocationServiceFacade(CloudNativeLocationService serviceImpl,
                              DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    public static class CloudNativeHistoricalLocationServiceFacade
            extends ServiceFacade<HistoricalLocationDTO> {

        CloudNativeHistoricalLocationServiceFacade(CloudNativeHistoricalLocationService serviceImpl,
                                        DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    public static class CloudNativeSensorServiceFacade extends ServiceFacade<SensorDTO> {

        CloudNativeSensorServiceFacade(CloudNativeSensorService serviceImpl,
                            DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    public static class CloudNativeObservedPropertyServiceFacade
            extends ServiceFacade<ObservedPropertyDTO> {

        CloudNativeObservedPropertyServiceFacade(CloudNativeObservedPropertyService serviceImpl,
                                      DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    public static class CloudNativeObservationServiceFacade
            extends ServiceFacade<ObservationDTO> {

        CloudNativeObservationServiceFacade(CloudNativeObservationService serviceImpl,
                                 DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    public static class CloudNativeDatastreamServiceFacade extends ServiceFacade<DatastreamDTO> {

        CloudNativeDatastreamServiceFacade(CloudNativeDatastreamService serviceImpl,
                                DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }


    @Component
    public static class CloudNativeFeatureOfInterestServiceFacade
            extends ServiceFacade<FeatureOfInterestDTO> {

        CloudNativeFeatureOfInterestServiceFacade(CloudNativeFeatureOfInterestService serviceImpl,
                                       DaoSemaphore semaphore) {
            super(serviceImpl, semaphore);
        }
    }
}
