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
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.series.db.beans.sta.ObservationEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
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

import javax.annotation.PostConstruct;
import javax.persistence.criteria.Predicate;
import java.util.Optional;

/**
 * Interface for requesting Sensor Things entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Transactional
public abstract class AbstractSensorThingsEntityServiceImpl<T extends StaIdentifierRepository<S>,
        S extends HibernateRelations.HasId,
        E extends S> implements AbstractSensorThingsEntityService<T, S, E> {

    // protected static final String IDENTIFIER = "identifier";
    protected static final String STAIDENTIFIER = "staIdentifier";
    protected static final String ENCODINGTYPE = "encodingType";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSensorThingsEntityServiceImpl.class);

    @Autowired
    private EntityServiceRepository serviceRepository;

    @Autowired
    private MutexFactory lock;

    private final Class<S> entityClass;
    private final EntityGraphRepository.FetchGraph[] defaultFetchGraphs;

    private T repository;

    public AbstractSensorThingsEntityServiceImpl(T repository,
                                                 Class entityClass,
                                                 EntityGraphRepository.FetchGraph... defaultFetchGraphs) {
        this.entityClass = entityClass;
        this.repository = repository;
        this.defaultFetchGraphs = defaultFetchGraphs;
    }

    @PostConstruct
    public void init() {
        serviceRepository.addEntityService(this);
    }

    /**
     * Gets a lock with given name from global lockMap. Name is unique per EntityType.
     * Uses weak references so Map is automatically cleared by GC.
     * Used to lock Entities to avoid race conditions
     *
     * @param key name of the lock
     * @return Object used for holding the lock
     */
    protected Object getLock(String key) throws STACRUDException {
        return lock.getLock(key + entityClass.getSimpleName());
    }

    public abstract EntityTypes[] getTypes();

    @Override public boolean existsEntity(String id) {
        return getRepository().existsByStaIdentifier(id);
    }

    @Override public ElementWithQueryOptions getEntity(String id, QueryOptions queryOptions) throws STACRUDException {
        try {
            S entity = getRepository().findByStaIdentifier(id, defaultFetchGraphs).get();
            if (queryOptions.hasExpandFilter()) {
                return this.createWrapper(fetchExpandEntities(entity, queryOptions.getExpandFilter()), queryOptions);
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
                                                    defaultFetchGraphs);
            return getCollectionWrapper(queryOptions, pages);
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    protected CollectionWrapper getCollectionWrapper(QueryOptions queryOptions, Page<S> pages) {
        if (queryOptions.hasExpandFilter()) {
            Page expanded = pages.map(e -> {
                try {
                    return fetchExpandEntities(e, queryOptions.getExpandFilter());
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

    public S getEntityByRelatedEntityRaw(String relatedId,
                                         String relatedType,
                                         String ownId,
                                         QueryOptions queryOptions)
            throws STACRUDException {
        try {
            Optional<S> elem =
                    getRepository().findOne(byRelatedEntityFilter(relatedId, relatedType, ownId), defaultFetchGraphs);
            if (elem.isPresent()) {
                if (queryOptions.hasExpandFilter()) {
                    return fetchExpandEntities(elem.get(), queryOptions.getExpandFilter());
                } else {
                    return elem.get();
                }
            } else {
                return null;
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                          String relatedType,
                                                                          QueryOptions queryOptions)
            throws STACRUDException {
        return getCollectionWrapper(queryOptions,
                                    getEntityCollectionByRelatedEntityRaw(relatedId, relatedType, queryOptions));
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
                             defaultFetchGraphs);
            if (queryOptions.hasExpandFilter()) {
                return pages.map(e -> {
                    try {
                        return fetchExpandEntities(e, queryOptions.getExpandFilter());
                    } catch (STACRUDException | STAInvalidQueryException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            } else {
                return pages;
            }
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override public String getEntityIdByRelatedEntity(String relatedId, String relatedType) {
        Optional<String> entity = getRepository().identifier(
                this.byRelatedEntityFilter(relatedId, relatedType, null),
                STAIDENTIFIER);
        return entity.orElse(null);
    }

    @Override public boolean existsEntityByRelatedEntity(String relatedId,
                                                         String relatedType,
                                                         String ownId) {
        return getRepository().count(byRelatedEntityFilter(relatedId, relatedType, ownId)) > 0;
    }

    protected abstract E fetchExpandEntities(S entity, ExpandFilter expandOption)
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

    @Override public T getRepository() {
        return this.repository;
    }

    /**
     * Query for the number of entities.
     *
     * @param queryOptions {@link QueryOptions}
     * @return count of entities
     */
    public long getCount(QueryOptions queryOptions) {
        return getRepository().count(getFilterPredicate(entityClass, queryOptions));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElementWithQueryOptions create(S entity) throws STACRUDException {
        return this.createWrapper(createEntity(entity), null);
    }

    @Transactional(rollbackFor = Exception.class)
    protected abstract S createEntity(S entity) throws STACRUDException;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElementWithQueryOptions update(String id, S entity, HttpMethod method) throws STACRUDException {
        return this.createWrapper(updateEntity(id, entity, method), null);
    }

    @Transactional(rollbackFor = Exception.class)
    protected abstract S updateEntity(String id, S entity, HttpMethod method) throws STACRUDException;

    @Transactional(rollbackFor = Exception.class)
    protected abstract S updateEntity(S entity) throws STACRUDException;

    protected abstract void delete(S entity) throws STACRUDException;

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

    protected void checkInlineDatastream(DatastreamEntity datastream) throws STACRUDException {
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

    protected AbstractSensorThingsEntityService<?, ?, ?> getEntityService(EntityTypes type) {
        return serviceRepository.getEntityService(type);
    }

    /**
     * Create {@link PageRequest}
     *
     * @param queryOptions           {@link QueryOptions} to create {@link PageRequest}
     * @param defaultSortingProperty Teh defualt sorting property
     * @return {@link PageRequest} of type {@link OffsetLimitBasedPageRequest}
     */
    OffsetLimitBasedPageRequest createPageableRequest(QueryOptions queryOptions,
                                                      String defaultSortingProperty) {
        long offset = queryOptions.hasSkipFilter() ? queryOptions.getSkipFilter().getValue() : 0;
        Sort sort = Sort.by(Sort.Direction.ASC, defaultSortingProperty);
        if (queryOptions.hasOrderByFilter()) {
            boolean first = true;
            for (OrderProperty sortProperty :
                    queryOptions.getOrderByFilter().getSortProperties()) {
                if (first) {
                    sort = Sort.by(sortProperty.isSetSortOrder() &&
                                           sortProperty.getSortOrder().equals(FilterConstants.SortOrder.DESC) ?
                                           Sort.Direction.DESC : Sort.Direction.ASC,
                                   checkPropertyName(sortProperty.getValueReference()));
                    first = false;
                } else {
                    sort = sort.and(Sort.by(sortProperty.isSetSortOrder() &&
                                                    sortProperty.getSortOrder().equals(FilterConstants.SortOrder.DESC) ?
                                                    Sort.Direction.DESC : Sort.Direction.ASC,
                                            checkPropertyName(sortProperty.getValueReference())));
                }
            }
        }
        return new OffsetLimitBasedPageRequest((int) offset,
                                               queryOptions.getTopFilter().getValue().intValue(),
                                               sort);
    }

    protected OffsetLimitBasedPageRequest createPageableRequest(QueryOptions queryOptions) {
        return this.createPageableRequest(queryOptions, STAIDENTIFIER);
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

    protected void mergeIdentifierNameDescription(DescribableEntity existing, DescribableEntity toMerge) {
        if (toMerge.isSetIdentifier()) {
            existing.setIdentifier(toMerge.getIdentifier());
        }
        if (toMerge.isSetStaIdentifier()) {
            existing.setStaIdentifier(toMerge.getStaIdentifier());
        }
        mergeNameDescription(existing, toMerge);
    }

    protected void mergeNameDescription(DescribableEntity existing, DescribableEntity toMerge) {
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

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityServiceImpl<?, LocationEntity, LocationEntity> getLocationService() {
        return (AbstractSensorThingsEntityServiceImpl<?, LocationEntity, LocationEntity>)
                getEntityService(EntityTypes.Location);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityServiceImpl<?, HistoricalLocationEntity, HistoricalLocationEntity>
    getHistoricalLocationService() {
        return (AbstractSensorThingsEntityServiceImpl<?, HistoricalLocationEntity, HistoricalLocationEntity>)
                getEntityService(EntityTypes.HistoricalLocation);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityServiceImpl<?, DatastreamEntity, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityServiceImpl<?, DatastreamEntity, DatastreamEntity>)
                getEntityService(EntityTypes.Datastream);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityServiceImpl<?, AbstractFeatureEntity<?>, StaFeatureEntity<?>>
    getFeatureOfInterestService() {
        return (AbstractSensorThingsEntityServiceImpl<?, AbstractFeatureEntity<?>, StaFeatureEntity<?>>)
                getEntityService(EntityTypes.FeatureOfInterest);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityServiceImpl<?, PlatformEntity, PlatformEntity> getThingService() {
        return (AbstractSensorThingsEntityServiceImpl<?, PlatformEntity, PlatformEntity>)
                getEntityService(EntityTypes.Thing);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityServiceImpl<?, ProcedureEntity, SensorEntity> getSensorService() {
        return (AbstractSensorThingsEntityServiceImpl<?, ProcedureEntity, SensorEntity>)
                getEntityService(EntityTypes.Sensor);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityServiceImpl<?, PhenomenonEntity, ObservablePropertyEntity>
    getObservedPropertyService() {
        return (AbstractSensorThingsEntityServiceImpl<?, PhenomenonEntity, ObservablePropertyEntity>)
                getEntityService(EntityTypes.ObservedProperty);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityServiceImpl<?, ObservationEntity<?>, ObservationEntity<?>>
    getObservationService() {
        return (AbstractSensorThingsEntityServiceImpl<?, ObservationEntity<?>, ObservationEntity<?>>)
                getEntityService(EntityTypes.Observation);

    }
}
