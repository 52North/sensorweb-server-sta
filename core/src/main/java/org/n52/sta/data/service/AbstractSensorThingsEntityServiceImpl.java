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

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.filter.OrderProperty;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.StaIdentifierRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.data.service.util.FilterExprVisitor;
import org.n52.sta.data.service.util.HibernateSpatialCriteriaBuilderImpl;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.svalbard.odata.core.expr.Expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for requesting Sensor Things entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Transactional(rollbackFor = Exception.class)
public abstract class AbstractSensorThingsEntityServiceImpl<T extends StaIdentifierRepository<S>,
    S extends HibernateRelations.HasId> implements AbstractSensorThingsEntityService<S> {

    // protected static final String IDENTIFIER = "identifier";
    protected static final String STAIDENTIFIER = "staIdentifier";
    protected static final String ENCODINGTYPE = "encodingType";
    protected static final String RESULT = "result";

    protected static final String HTTP_PUT_IS_NOT_YET_SUPPORTED = "Http PUT is not yet supported!";
    protected static final String IDENTIFIER_ALREADY_EXISTS = "Identifier already exists!";
    protected static final String UNABLE_TO_UPDATE_ENTITY_NOT_FOUND = "Unable to update. Entity not found.";
    protected static final String UNABLE_TO_DELETE_ENTITY_NOT_FOUND = "Unable to delete. Entity not found.";
    protected static final String UNABLE_TO_GET_ENTITY_NOT_FOUND = "Unable to retrieve. Entity not found.";
    protected static final String INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY = "Invalid http method for updating entity!";
    protected static final String TRYING_TO_FILTER_BY_UNRELATED_TYPE =
        "Trying to filter by unrelated type: %s not found!";
    protected static final String INVALID_EXPAND_OPTION_SUPPLIED =
        "Invalid expandOption supplied. Cannot find %s on Entity of type '%s'";
    protected static final String NO_S_WITH_ID_S_FOUND = "No %s with id %s found.";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSensorThingsEntityServiceImpl.class);
    private final EntityManager em;
    private final Class<S> entityClass;

    @Autowired
    private EntityServiceRepository serviceRepository;

    @Autowired
    private MutexFactory lock;

    private T repository;

    public AbstractSensorThingsEntityServiceImpl(T repository,
                                                 EntityManager em,
                                                 Class entityClass) {
        this.em = em;
        this.entityClass = entityClass;
        this.repository = repository;
    }

    /**
     * Gets a lock with given name from global lockMap. Name is unique per EntityType.
     * Uses weak references so Map is automatically cleared by GC.
     * Used to lock Entities to avoid race conditions
     *
     * @param key name of the lock
     * @return Object used for holding the lock
     * @throws STACRUDException If the lock can not be aquired
     */
    protected Object getLock(String key) throws STACRUDException {
        if (key == null) {
            throw new STACRUDException("Unable to aquire lock. Invalid key provided!");
        } else {
            return lock.getLock(key + entityClass.getSimpleName());
        }
    }

    @Override
    public boolean existsEntity(String id) {
        return getRepository().existsByStaIdentifier(id);
    }

    @Override
    public ElementWithQueryOptions getEntity(String id, QueryOptions queryOptions) throws STACRUDException {
        try {
            S entity = getRepository().findByStaIdentifier(id, createFetchGraph(queryOptions.getExpandFilter())).get();
            if (queryOptions.hasExpandFilter()) {
                return this.createWrapper(fetchExpandEntitiesWithFilter(entity, queryOptions.getExpandFilter()),
                                          queryOptions);
            } else {
                return this.createWrapper(entity, queryOptions);
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        try {
            Page<S> pages = getRepository().findAll(getFilterPredicate(entityClass, queryOptions),
                                                    createPageableRequest(queryOptions),
                                                    createFetchGraph(queryOptions.getExpandFilter()));
            return createCollectionWrapperAndExpand(queryOptions, pages);
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override public ElementWithQueryOptions<?> getEntityByRelatedEntity(String relatedId,
                                                                         String relatedType,
                                                                         String ownId,
                                                                         QueryOptions queryOptions)
        throws STACRUDException {
        S entityByRelatedEntityRaw =
            getEntityByRelatedEntityRaw(relatedId, relatedType, ownId, queryOptions);
        if (entityByRelatedEntityRaw != null) {
            return this.createWrapper(entityByRelatedEntityRaw, queryOptions);
        } else {
            return null;
        }
    }

    @Override public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                          String relatedType,
                                                                          QueryOptions queryOptions)
        throws STACRUDException {
        return createCollectionWrapperAndExpand(queryOptions,
                                                getEntityCollectionByRelatedEntityRaw(relatedId,
                                                                                      relatedType,
                                                                                      queryOptions));
    }

    @Override public String getEntityIdByRelatedEntity(String relatedId, String relatedType) {
        Optional<String> entity = getRepository().getColumn(
            this.byRelatedEntityFilter(relatedId, relatedType, null),
            STAIDENTIFIER);
        return entity.orElse(null);
    }

    @Override public boolean existsEntityByRelatedEntity(String relatedId,
                                                         String relatedType,
                                                         String ownId) {
        return getRepository().count(byRelatedEntityFilter(relatedId, relatedType, ownId)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElementWithQueryOptions create(S entity) throws STACRUDException {
        return this.createWrapper(createOrfetch(entity), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElementWithQueryOptions update(String id, S entity, HttpMethod method) throws STACRUDException {
        return this.createWrapper(updateEntity(id, entity, method), null);
    }

    protected T getRepository() {
        return this.repository;
    }

    public S getEntityByIdRaw(Long id, QueryOptions queryOptions) throws STACRUDException {
        try {
            S entity = getRepository().findById(id, createFetchGraph(queryOptions.getExpandFilter())).get();
            if (queryOptions.hasExpandFilter()) {
                return fetchExpandEntitiesWithFilter(entity, queryOptions.getExpandFilter());
            } else {
                return entity;
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    protected CollectionWrapper createCollectionWrapperAndExpand(QueryOptions queryOptions, Page<S> pages) {
        if (queryOptions.hasExpandFilter()) {
            Page expanded = pages.map(e -> {
                try {
                    em.detach(e);
                    return fetchExpandEntitiesWithFilter(e, queryOptions.getExpandFilter());
                } catch (STACRUDException | STAInvalidQueryException ex) {
                    throw new RuntimeException(ex);
                }
            });
            return new CollectionWrapper(expanded.getTotalElements(),
                                         expanded.map(e -> createWrapper(e, queryOptions))
                                             .getContent(),
                                         expanded.hasNext());
        } else {
            return new CollectionWrapper(pages.getTotalElements(),
                                         pages.map(e -> createWrapper(e, queryOptions))
                                             .getContent(),
                                         pages.hasNext());
        }
    }

    public S getEntityByRelatedEntityRaw(String relatedId,
                                         String relatedType,
                                         String ownId,
                                         QueryOptions queryOptions)
        throws STACRUDException {
        try {
            Optional<S> elem =
                getRepository().findOne(byRelatedEntityFilter(relatedId, relatedType, ownId)
                                            .and(getFilterPredicate(entityClass, queryOptions)),
                                        createFetchGraph(queryOptions.getExpandFilter()));
            if (elem.isPresent()) {
                if (queryOptions.hasExpandFilter()) {
                    return fetchExpandEntitiesWithFilter(elem.get(), queryOptions.getExpandFilter());
                } else {
                    return elem.get();
                }
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
    protected Page getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                         String relatedType,
                                                         QueryOptions queryOptions)
        throws STACRUDException {
        try {
            Page<S> pages = getRepository()
                .findAll(byRelatedEntityFilter(relatedId, relatedType, null)
                             .and(getFilterPredicate(entityClass, queryOptions)),
                         createPageableRequest(queryOptions),
                         createFetchGraph(queryOptions.getExpandFilter()));
            if (queryOptions.hasExpandFilter()) {
                return pages.map(e -> {
                    try {
                        em.detach(e);
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

    /**
     * Creates a Fetchgraph for this Entity. Includes relations that need to be fetched by default as well as directly
     * fetching $expanded Entities that are NOT  filtered via $filter. As they are not filtered individually they
     * can be fetched at the same time for all entities.
     *
     * @param expandOption Specification of the $expand parameter
     * @return FetchGraph fetching the required
     * @throws STAInvalidQueryException if the query is invalid
     */
    protected abstract EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
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
    protected abstract S fetchExpandEntitiesWithFilter(S entity, ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException;

    /**
     * Wraps the raw Entity into a Wrapper object to associate with QueryOptions used for this request
     *
     * @param entity       entity
     * @param queryOptions query options
     * @return instance of ElementWithQueryOptions
     */
    @Transactional(rollbackFor = Exception.class)
    ElementWithQueryOptions createWrapper(Object entity, QueryOptions queryOptions) {
        return ElementWithQueryOptions.from(entity, queryOptions);
    }

    /**
     * Constructs Specification for for entity with given ownId and
     * related entity with type relatedType and id relatedId.
     *
     * @param relatedId   Id of the Related Entity
     * @param relatedType Type of the Related Entity
     * @param ownId       Id of the Entity. Can be null
     * @return Optional&lt;ProcedureEntity&gt; Requested Entity
     */
    protected abstract Specification<S> byRelatedEntityFilter(String relatedId,
                                                              String relatedType,
                                                              String ownId);

    /**
     * Query for the number of entities.
     *
     * @param queryOptions {@link QueryOptions}
     * @return count of entities
     */
    public long getCount(QueryOptions queryOptions) {
        return getRepository().count(getFilterPredicate(entityClass, queryOptions));
    }

    @Transactional(rollbackFor = Exception.class)
    protected abstract S createOrfetch(S entity) throws STACRUDException;

    @Transactional(rollbackFor = Exception.class)
    protected abstract S updateEntity(String id, S entity, HttpMethod method) throws STACRUDException;

    @Transactional(rollbackFor = Exception.class)
    protected S save(S entity) {
        return getRepository().save(entity);
    }

    /**
     * Must be implemented by each Service individually as S is not known to have identifier here.
     * Example Code to be pasted into each Service below
     *
     * @param entity entity to be persisted or updated
     * @return updated entity
     * @throws STACRUDException if an error occurred
     */

    protected abstract S createOrUpdate(S entity) throws STACRUDException;
    //protected S createOrUpdate(S entity) throws STACRUDException {
    //    if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
    //        return update(entity, HttpMethod.PATCH);
    //    }
    //    return create(entity);
    //}

    protected void checkInlineDatastream(AbstractDatasetEntity datastream) throws STACRUDException {
        if (datastream.getStaIdentifier() == null
            || datastream.isSetName()
            || datastream.isSetDescription()
            || datastream.isSetUnit()) {
            throw new STACRUDException("Inlined datastream entities are not allowed for updates!");
        }
    }

    protected void checkInlineLocation(LocationEntity location) throws STACRUDException {
        if (location.getStaIdentifier() == null || location.isSetName() || location.isSetDescription()) {
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
                                    Sort.by(direction, checkPropertyName(sortProperty.getValueReference())));
            }
        } else {
            sort = Sort.by(Sort.Direction.ASC, STAIDENTIFIER);
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
        Sort sort;
        sort = Sort.by(direction, DataEntity.PROPERTY_VALUE_BOOLEAN);
        sort = sort.and(Sort.by(direction, DataEntity.PROPERTY_VALUE_CATEGORY));
        sort = sort.and(Sort.by(direction, DataEntity.PROPERTY_VALUE_COUNT));
        sort = sort.and(Sort.by(direction, DataEntity.PROPERTY_VALUE_TEXT));
        sort = sort.and(Sort.by(direction, DataEntity.PROPERTY_VALUE_QUANTITY));
        return sort;
    }

    /**
     * Constructs FilterPredicate based on given queryOptions.
     *
     * @param entityClass  Class of the requested Entity
     * @param queryOptions QueryOptions Object
     * @return Predicate based on FilterOption from queryOptions
     */
    public Specification<S> getFilterPredicate(Class entityClass, QueryOptions queryOptions) {
        return (root, query, builder) -> {
            if (!queryOptions.hasFilterFilter()) {
                return null;
            } else {
                FilterFilter filterOption = queryOptions.getFilterFilter();
                Expr filter = (Expr) filterOption.getFilter();
                try {
                    HibernateSpatialCriteriaBuilderImpl staBuilder =
                        new HibernateSpatialCriteriaBuilderImpl((CriteriaBuilderImpl) builder);
                    return (Predicate) filter.accept(new FilterExprVisitor<S>(root, query, staBuilder));
                } catch (STAInvalidQueryException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Translate STA property name to Database property name
     *
     * @param property name of the property in STA
     * @return name of the property in database
     */
    public String checkPropertyName(String property) {
        return property;
    }

    protected abstract S merge(S existing, S toMerge) throws STACRUDException;

    protected <U extends HibernateRelations.HasIdentifier & HibernateRelations.HasStaIdentifier
        & HasName & HasDescription> void mergeIdentifierNameDescription(U existing, U toMerge) {
        if (toMerge.isSetIdentifier()) {
            existing.setIdentifier(toMerge.getIdentifier());
        }
        if (toMerge.isSetStaIdentifier()) {
            existing.setStaIdentifier(toMerge.getStaIdentifier());
        }
        mergeNameDescription(existing, toMerge);
    }

    protected <U extends HasName & HasDescription> void mergeNameDescription(U existing, U toMerge) {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
    }

    protected void mergeName(HasName existing,
                             HasName toMerge) {
        if (toMerge.isSetName()) {
            existing.setName(toMerge.getName());
        }
    }

    protected void mergeDescription(HasDescription existing,
                                    HasDescription toMerge) {
        if (toMerge.isSetDescription()) {
            existing.setDescription(toMerge.getDescription());
        }
    }

    protected void mergeSamplingTime(HibernateRelations.HasPhenomenonTime existing,
                                     HibernateRelations.HasPhenomenonTime toMerge) {
        if (toMerge.hasSamplingTimeStart() && toMerge.hasSamplingTimeEnd()) {
            existing.setSamplingTimeStart(toMerge.getSamplingTimeStart());
            existing.setSamplingTimeEnd(toMerge.getSamplingTimeEnd());
        }
    }

    protected void mergeDatastreams(HibernateRelations.HasAbstractDatasets existing,
                                    HibernateRelations.HasAbstractDatasets toMerge)
        throws STACRUDException {
        if (toMerge.getDatasets() != null) {
            for (AbstractDatasetEntity datastream : toMerge.getDatasets()) {
                checkInlineDatastream(datastream);
            }
            Set<AbstractDatasetEntity> ex = existing.getDatasets();
            ex.addAll(toMerge.getDatasets());
            existing.setDatasets(ex);
        }
    }

    LocationService getLocationService() {
        return (LocationService) serviceRepository.getEntityServiceRaw(EntityTypes.Location);
    }

    HistoricalLocationService getHistoricalLocationService() {
        return (HistoricalLocationService) serviceRepository.getEntityServiceRaw(EntityTypes.HistoricalLocation);
    }

    DatastreamService getDatastreamService() {
        return (DatastreamService) serviceRepository.getEntityServiceRaw(EntityTypes.Datastream);
    }

    FeatureOfInterestService getFeatureOfInterestService() {
        return (FeatureOfInterestService) serviceRepository.getEntityServiceRaw(EntityTypes.FeatureOfInterest);
    }

    ThingService getThingService() {
        return (ThingService) serviceRepository.getEntityServiceRaw(EntityTypes.Thing);
    }

    SensorService getSensorService() {
        return (SensorService) serviceRepository.getEntityServiceRaw(EntityTypes.Sensor);
    }

    ObservedPropertyService getObservedPropertyService() {
        return (ObservedPropertyService) serviceRepository.getEntityServiceRaw(EntityTypes.ObservedProperty);
    }

    ObservationService getObservationService() {
        return (ObservationService) serviceRepository.getEntityServiceRaw(EntityTypes.Observation);
    }
}
