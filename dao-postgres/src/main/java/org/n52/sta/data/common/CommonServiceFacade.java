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
package org.n52.sta.data.common;

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.Dataset;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.api.AbstractSensorThingsEntityService;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.data.vanilla.DTOTransformerImpl;
import org.n52.sta.data.vanilla.DaoSemaphore;
import org.n52.sta.data.vanilla.SerDesConfig;
import org.n52.sta.data.vanilla.service.DatastreamService;
import org.n52.sta.data.vanilla.service.FeatureOfInterestService;
import org.n52.sta.data.vanilla.service.HistoricalLocationService;
import org.n52.sta.data.vanilla.service.LocationService;
import org.n52.sta.data.vanilla.service.ObservationService;
import org.n52.sta.data.vanilla.service.ObservedPropertyService;
import org.n52.sta.data.vanilla.service.SensorService;
import org.n52.sta.data.vanilla.service.ThingService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Facade proxying actual Service implementations. Used to restrict the number of parallel threads accessing the Data
 * Persistence Layer. This is necessary as each Thread uses a seperate Database Transaction and therefore needs a
 * dedicated DatabaseConnection.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class CommonServiceFacade<R extends StaDTO, S extends HibernateRelations.HasId>
    implements AbstractSensorThingsEntityService<R> {

    protected final DaoSemaphore semaphore;
    protected final SerDesConfig config;
    protected final CommonSTAServiceImpl<?, R, S> serviceImpl;

    public CommonServiceFacade(CommonSTAServiceImpl<?, R, S> serviceImpl,
                               DaoSemaphore semaphore,
                               SerDesConfig config) {
        this.serviceImpl = serviceImpl;
        this.semaphore = semaphore;
        this.config = config;
    }

    public CommonSTAServiceImpl<?, ?, ?> getServiceImpl() {
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

    @Override public R getEntity(String id, QueryOptions queryOptions) throws STACRUDException {
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

    @Override public R getEntityByRelatedEntity(String relatedId,
                                                String relatedType,
                                                String ownId,
                                                QueryOptions queryOptions)
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

    @Override public R create(R entity) throws STACRUDException {
        R result;
        try {
            semaphore.acquire();
            result = serviceImpl.create((S) new DTOTransformerImpl<>(config).fromDTO(entity));
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
            result = serviceImpl.update(id, (S) new DTOTransformerImpl<>(config).fromDTO(entity), method);
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

    @Component
    public static class ThingServiceFacade extends CommonServiceFacade<ThingDTO, PlatformEntity> {

        ThingServiceFacade(ThingService serviceImpl,
                           DaoSemaphore semaphore,
                           SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    public static class LocationServiceFacade extends CommonServiceFacade<LocationDTO, LocationEntity> {

        LocationServiceFacade(LocationService serviceImpl,
                              DaoSemaphore semaphore,
                              SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    public static class HistoricalLocationServiceFacade
        extends CommonServiceFacade<HistoricalLocationDTO, HistoricalLocationEntity> {

        HistoricalLocationServiceFacade(HistoricalLocationService serviceImpl,
                                        DaoSemaphore semaphore,
                                        SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    public static class SensorServiceFacade extends CommonServiceFacade<SensorDTO, ProcedureEntity> {

        SensorServiceFacade(SensorService serviceImpl,
                            DaoSemaphore semaphore,
                            SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    public static class ObservedPropertyServiceFacade
        extends CommonServiceFacade<ObservedPropertyDTO, PhenomenonEntity> {

        ObservedPropertyServiceFacade(ObservedPropertyService serviceImpl,
                                      DaoSemaphore semaphore,
                                      SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    public static class ObservationServiceFacade
        extends CommonServiceFacade<ObservationDTO, DataEntity<?>> {

        ObservationServiceFacade(ObservationService serviceImpl,
                                 DaoSemaphore semaphore,
                                 SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    public static class DatastreamServiceFacade extends CommonServiceFacade<DatastreamDTO, Dataset> {

        DatastreamServiceFacade(DatastreamService serviceImpl,
                                DaoSemaphore semaphore,
                                SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    public static class FeatureOfInterestServiceFacade
        extends CommonServiceFacade<FeatureOfInterestDTO, AbstractFeatureEntity<?>> {

        FeatureOfInterestServiceFacade(FeatureOfInterestService serviceImpl,
                                       DaoSemaphore semaphore,
                                       SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }
}
