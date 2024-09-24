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


import org.jooq.Condition;

import org.jooq.Field;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.filter.OrderProperty;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.*;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.StaEntityDao;
import org.n52.svalbard.odata.core.expr.Expr;

import org.n52.sta.api.CollectionWrapper;

import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.OffsetLimitBasedPageRequest;

import org.n52.sta.data.cloudnative.dao.util.FilterExprVisitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpMethod;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interface for requesting Sensor Things entities
 *
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Transactional(rollbackFor = Exception.class)
public abstract class CloudNativeAbstractSensorThingsEntityServiceImpl <T extends StaEntityDao<R>, R extends StaDTO> {

    protected static final String RESULT = "result";
    protected static final String NULL_ID_MASK = ((Long)(1L << 63)).toString();
    protected static final String HTTP_PUT_IS_NOT_YET_SUPPORTED = "Http PUT is not yet supported!";
    protected static final String IDENTIFIER_ALREADY_EXISTS = "Identifier already exists!";
    protected static final String UNABLE_TO_UPDATE_ENTITY_NOT_FOUND = "Unable to update. Entity not found.";
    protected static final String UNABLE_TO_DELETE_ENTITY_NOT_FOUND = "Unable to delete. Entity not found.";
    protected static final String UNABLE_TO_GET_ENTITY_NOT_FOUND = "Unable to retrieve. Entity not found.";
    protected static final String INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY = "Invalid http method for updating entity!";
    protected static final String TRYING_TO_FILTER_BY_UNRELATED_TYPE =
            "Trying to filter by unrelated type: %s not found!";
    public static final String INVALID_ENTITY_TYPE = "Cannot find Entity of type '%s'";
    protected static final String NO_S_WITH_ID_S_FOUND = "No %s with id %s found.";

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudNativeAbstractSensorThingsEntityServiceImpl.class);

    @Autowired private MutexFactory lock;
    private CloudNativeEntityServiceRepository serviceRepository;
    private final T StaEntityDao;
    protected final Class<R> entityClass;

    public CloudNativeAbstractSensorThingsEntityServiceImpl(T StaEntityDao, Class<R> entityClass) {
        this.entityClass = entityClass;
        this.StaEntityDao = StaEntityDao;
    }


    public void setServiceRepository(CloudNativeEntityServiceRepository entityServiceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /**
     * Gets a lock with given name from global lockMap. Name is unique per EntityType.
     * Uses weak references so Map is automatically cleared by GC.
     * Used to lock Entities to avoid race conditions
     *
     * @param key name of the lock
     * @return Object used for holding the lock
     * @throws STACRUDException If the lock can not be acquired
     */
    protected Object getLock(String key) throws STACRUDException {
        if (key == null) {
            throw new STACRUDException("Unable to acquire lock. Invalid key provided!");
        } else {
            return lock.getLock(key + entityClass.getSimpleName());
        }
    }

    public boolean existsEntity(String id) throws STAInvalidQueryException {
        return StaEntityDao.existsByStaIdentifier(id, entityClass);
    }

    public R getEntity(String id, QueryOptions queryOptions) throws STACRUDException {
        try {
            Optional<R> optionalEntity =  StaEntityDao.findByStaIdentifier(id, queryOptions, entityClass);
            if (optionalEntity.isPresent()) {
                R entity = optionalEntity.get();
                if (queryOptions.hasExpandFilter()) {
                    entity = fetchExpandEntitiesWithFilter(entity, queryOptions.getExpandFilter());
                }
                entity.setAndParseQueryOptions(queryOptions);
                return entity;
            }
            else {
                throw new STACRUDException(UNABLE_TO_GET_ENTITY_NOT_FOUND);
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        try {
            Page<R> pages = StaEntityDao.findAll(
                    getFilterPredicate(entityClass, queryOptions),
                    createPageableRequest(queryOptions),
                    queryOptions,
                    entityClass);
            return createCollectionWrapperAndExpand(queryOptions, pages);
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    public R getEntityByRelatedEntity(String relatedId, String relatedType, String ownId, QueryOptions queryOptions)
            throws STACRUDException{
        try {
            R entity = getEntityByRelatedEntityRaw(relatedId, relatedType, ownId, queryOptions);
            entity.setAndParseQueryOptions(queryOptions);
            return entity;
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                String relatedType,
                                                                QueryOptions queryOptions)
            throws STACRUDException {

        return createCollectionWrapperAndExpand(queryOptions,
                getEntityCollectionByRelatedEntityRaw(relatedId, relatedType, queryOptions));
    }

    public String getEntityIdByRelatedEntity(String relatedId, String relatedType)
            throws STAInvalidQueryException {
        Optional<String> entity = StaEntityDao.getColumn(
                this.byRelatedEntityFilter(relatedId, relatedType, null),
                getStaEntityId().getName(),
                entityClass);
        return entity.orElse(null);
    }

    public boolean existsEntityByRelatedEntity(String relatedId, String relatedType, String ownId)
            throws STAInvalidQueryException {
        return StaEntityDao.count(byRelatedEntityFilter(relatedId, relatedType, ownId), entityClass) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public R create(StaDTO entity) throws STACRUDException, STAInvalidQueryException {
        return createOrfetch((R) entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public R update(String id, StaDTO entity, HttpMethod method) throws STACRUDException, STAInvalidQueryException {
        return updateEntity(id, (R) entity, method);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) throws STACRUDException, STAInvalidQueryException {
        this.deleteEntity(id);
    }

    public R getEntityByIdRaw(Long id, QueryOptions queryOptions) throws STACRUDException {
        try {
            Optional<R> entity = StaEntityDao.findById(id, queryOptions, entityClass);
            if (entity.isPresent() && queryOptions.hasExpandFilter()) {
                return fetchExpandEntitiesWithFilter(entity.get(), queryOptions.getExpandFilter());
            } else {
                throw new STACRUDException(UNABLE_TO_GET_ENTITY_NOT_FOUND);
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    protected CollectionWrapper createCollectionWrapperAndExpand(QueryOptions queryOptions, Page<R> pages) {
        if (queryOptions.hasExpandFilter()) {
            Page<R> expanded = pages.map(e -> {
                try {
                    return fetchExpandEntitiesWithFilter(e, queryOptions.getExpandFilter());
                } catch (STACRUDException | STAInvalidQueryException ex) {
                    throw new RuntimeException(ex);
                }
            });
            long count = (queryOptions.hasCountFilter() && queryOptions.getCountFilter().getValue()) ?
                    expanded.getTotalElements() : -1;
            boolean hasNext = expanded.getTotalElements() == queryOptions.getTopFilter().getValue();
            return new CollectionWrapper(count, expanded.map(e -> {
                e.setAndParseQueryOptions(queryOptions);
                return e;
            }).getContent(), hasNext);
        } else {
            long count = (queryOptions.hasCountFilter() && queryOptions.getCountFilter().getValue()) ?
                    pages.getTotalElements() : -1;
            boolean hasNext = pages.getTotalElements() == queryOptions.getTopFilter().getValue();
            return new CollectionWrapper(count, pages.getContent(), hasNext);
        }
    }

    public R getEntityByRelatedEntityRaw(String relatedId,
                                          String relatedType,
                                          String ownId,
                                          QueryOptions queryOptions) throws STACRUDException {
        try {
            Optional<R> entity =
                    StaEntityDao.findOne(byRelatedEntityFilter(relatedId, relatedType, ownId).and(
                            getFilterPredicate(entityClass, queryOptions)),
                            queryOptions,
                            entityClass);
            if (entity.isPresent() && queryOptions.hasExpandFilter()) {
                return fetchExpandEntitiesWithFilter(entity.get(), queryOptions.getExpandFilter());
            } else {
                throw new STACRUDException(UNABLE_TO_GET_ENTITY_NOT_FOUND);
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    /**
     * Requests the EntityCollection that is related to a single Entity with the
     * given ID and type
     *
     * @param relatedId    the ID of the Entity the EntityCollection is related to
     * @param relatedType  EntityType of the related Entity
     * @param queryOptions {@link QueryOptions}
     * @return List of Entities that match
     * @throws STACRUDException if the queryOptions are invalid
     */
    protected Page<R> getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                         String relatedType,
                                                         QueryOptions queryOptions)
            throws STACRUDException {
        try {
            Page<R> pages = StaEntityDao.findAll(byRelatedEntityFilter(relatedId, relatedType, null).and(
                    getFilterPredicate(entityClass, queryOptions)),
                    createPageableRequest(queryOptions),
                    queryOptions,
                    entityClass);
            if (queryOptions.hasExpandFilter()) {
                return pages.map(e -> {
                    try {
                        return fetchExpandEntitiesWithFilter(e, queryOptions.getExpandFilter());
                    } catch (STACRUDException | STAInvalidQueryException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            } else {
                return pages;
            }

        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    protected abstract Condition byRelatedEntityFilter(String relatedId, String relatedType, String ownId)
            throws STAInvalidQueryException;

    /**
     * Fetches $expanded Entities that are filtered via $filter. An individual request is needed for each expanded
     * Item as $filter needs to be evaluated.
     *
     * @param entity       Base Entity
     * @param expandOption Entities to be expanded
     * @return Base Entity with embedded expanded parameters
     * @throws STACRUDException         if an error occurred
     * @throws STAInvalidQueryException if the query is invalid
     */
    protected abstract R fetchExpandEntitiesWithFilter(R entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException;

    /**
     * Query for the number of entities.
     *
     * @param queryOptions {@link QueryOptions}
     * @return count of entities
     */
    public long getCount(QueryOptions queryOptions) throws STAInvalidQueryException {
        return StaEntityDao.count(getFilterPredicate(entityClass, queryOptions), entityClass);
    }

    @Transactional(rollbackFor = Exception.class)
    protected abstract R createOrfetch(R entity)
            throws STACRUDException, STAInvalidQueryException;

    protected abstract R createOrUpdate(R entity)
            throws STACRUDException, STAInvalidQueryException;

    @Transactional(rollbackFor = Exception.class)
    protected abstract R updateEntity(String id, R entity, HttpMethod method)
            throws STACRUDException, STAInvalidQueryException;

    @Transactional(rollbackFor = Exception.class)
    protected abstract void deleteEntity(String id)
            throws STACRUDException, STAInvalidQueryException;


    protected void checkInlineDatastream(DatastreamDTO datastream) throws STACRUDException {
        if (datastream.getId() == null
                || datastream.getName() != null && !datastream.getName().isEmpty()
                || datastream.getDescription() != null && !datastream.getDescription().isEmpty()
                || datastream.getUnitOfMeasurement() != null) {
            throw new STACRUDException("Inlined datastream entities are not allowed for updates!");
        }
    }

    protected void checkInlineLocation(LocationDTO location) throws STACRUDException {
        if (location.getId() == null
                || location.getName() != null && !location.getName().isEmpty()
                || location.getDescription() != null && !location.getDescription().isEmpty()) {
            throw new STACRUDException("Inlined location entities are not allowed for updates!");
        }
    }
    /**
     * Create {@link PageRequest}
     *
     * @param queryOptions {@link QueryOptions} to create {@link PageRequest}
     * @return {@link PageRequest} of type {@link OffsetLimitBasedPageRequest}
     */
    OffsetLimitBasedPageRequest createPageableRequest(QueryOptions queryOptions) {
        long offset = queryOptions.hasSkipFilter() ? queryOptions.getSkipFilter().getValue() : 0;
        Sort sort;
        if (queryOptions.hasOrderByFilter()) {
            sort = Sort.unsorted();
            for (OrderProperty sortProperty : queryOptions.getOrderByFilter().getSortProperties()) {
                Sort.Direction direction =
                        sortProperty.isSetSortOrder() &&
                                sortProperty.getSortOrder().equals(FilterConstants.SortOrder.DESC) ?
                                Sort.Direction.DESC : Sort.Direction.ASC;
                sort = sort.and(sortProperty.getValueReference().equals(RESULT) ? handleResultSort(direction) :
                        Sort.by(direction, StaEntityDao.checkPropertyName(sortProperty.getValueReference()).getName()));
            }
        } else {
            sort = Sort.by(Sort.Direction.ASC,
                    getStaEntityId().getName());
        }
        return new OffsetLimitBasedPageRequest((int) offset,
                queryOptions.getTopFilter().getValue().intValue(),
                sort);
    }

    /**
     * Sort Observation->Result with different valueTypes.
     *
     * @param direction sort direction. either ascending or descending
     * @return Sort for results
     */
    private Sort handleResultSort(Sort.Direction direction) {
        return Sort.by(direction, StaEntity.OBSERVATION.VALUE_BOOLEAN.getName())
                .and(Sort.by(direction, StaEntity.OBSERVATION.VALUE_CATEGORY.getName()))
                .and(Sort.by(direction, StaEntity.OBSERVATION.VALUE_COUNT.getName()))
                .and(Sort.by(direction, StaEntity.OBSERVATION.VALUE_TEXT.getName()))
                .and(Sort.by(direction, StaEntity.OBSERVATION.VALUE_QUANTITY.getName()));
    }

    /**
     * Constructs Filtering Condition based on given queryOptions.
     *
     * @param entityClass  Class of the requested Entity
     * @param queryOptions QueryOptions Object
     * @return jOOQ Condition based on FilterOption from queryOptions
     */
    public Condition getFilterPredicate(Class<R> entityClass, QueryOptions queryOptions) {

        if (!queryOptions.hasFilterFilter()) {
            // Filter out non-root observations
            // e.g. Profile-/TrajectoryObservations
            return null;
        } else {
            FilterFilter filterOption = queryOptions.getFilterFilter();
            Expr filter = (Expr) filterOption.getFilter();
            try {
                return (Condition) filter.accept(new FilterExprVisitor(entityClass.getName()));
            } catch (STAInvalidQueryException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Translate STA property name to Database property name
     *
     * @param property name of the property in STA
     * @return name of the property in database
     */
    protected abstract String checkPropertyName(String property);

    protected abstract R merge(R existing, R toMerge) throws STACRUDException;

    protected <E extends HasNameAndDescription> void mergeNameDescription(E existing, E toMerge) {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
    }

    protected <E extends HasNameAndDescription> void mergeName(E existing, E toMerge) {
        if (toMerge.getName() != null && !toMerge.getName().isEmpty()) {
            existing.setName(toMerge.getName());
        }
    }

    protected <E extends HasNameAndDescription> void mergeDescription(E existing, E toMerge) {
        if (toMerge.getDescription() != null && !toMerge.getDescription().isEmpty()) {
            existing.setDescription(toMerge.getDescription());
        }
    }

    protected <E extends HasPhenomenonTime> void mergePhenomenonTime(E existing, E toMerge) {
        if (toMerge.getPhenomenonTime() != null) {
            existing.setPhenomenonTime(toMerge.getPhenomenonTime());
        }
    }

    protected <E extends HasDatastreams> void mergeDatastreams(E existing, E toMerge)
            throws STACRUDException {
        if (toMerge.getDatastreams() != null) {
            for (DatastreamDTO datastream : toMerge.getDatastreams()) {
                checkInlineDatastream(datastream);
            }
            Set<DatastreamDTO> ex = existing.getDatastreams();
            ex.addAll(toMerge.getDatastreams());
            existing.setDatastreams(ex);
        }
    }

    CloudNativeLocationService getLocationService() {
        return (CloudNativeLocationService) serviceRepository
                .getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Location);
    }

    CloudNativeHistoricalLocationService getHistoricalLocationService() {
        return (CloudNativeHistoricalLocationService) serviceRepository
                .getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.HistoricalLocation);
    }

    CloudNativeDatastreamService getDatastreamService() {
        return (CloudNativeDatastreamService) serviceRepository
                .getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Datastream);
    }

    CloudNativeFeatureOfInterestService getFeatureOfInterestService() {
        return (CloudNativeFeatureOfInterestService) serviceRepository
                .getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.FeatureOfInterest);
    }

    CloudNativeThingService getThingService() {
        return (CloudNativeThingService) serviceRepository
                .getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Thing);
    }

    CloudNativeSensorService getSensorService() {
        return (CloudNativeSensorService) serviceRepository
                .getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Sensor);
    }

    CloudNativeObservedPropertyService getObservedPropertyService() {
        return (CloudNativeObservedPropertyService) serviceRepository
                .getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.ObservedProperty);
    }

    CloudNativeObservationService getObservationService() {
        return (CloudNativeObservationService) serviceRepository
                .getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Observation);
    }

    // 1 million unique timestamps/second
    public Long getUniqueTimestamp() {
        AtomicLong TS = getStaEntityTS();
        long micros = System.currentTimeMillis() * 1000;
        for ( ; ; ) {
            long value = TS.get();
            if (micros <= value)
                micros = value + 1;
            if (TS.compareAndSet(value, micros))
                return micros;
        }
    }

    abstract Field<String> getStaEntityId();

    abstract AtomicLong getStaEntityTS();

}
