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
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.filter.OrderProperty;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.IdentifierRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.data.service.util.FilterExprVisitor;
import org.n52.sta.data.service.util.HibernateSpatialCriteriaBuilderImpl;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.svalbard.odata.core.expr.Expr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.Predicate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for requesting Sensor Things entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Transactional
public abstract class AbstractSensorThingsEntityService<T extends IdentifierRepository<S, Long>, S extends IdEntity,
        E extends S> {

    static final String IDENTIFIER = "identifier";
    static final String STAIDENTIFIER = "staIdentifier";
    static final String ENCODINGTYPE = "encodingType";

    @Autowired
    private EntityServiceRepository serviceRepository;

    private final Class<S> entityClass;
    private final EntityGraphRepository.FetchGraph[] defaultFetchGraphs;

    private T repository;

    public AbstractSensorThingsEntityService(T repository,
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

    public abstract EntityTypes[] getTypes();

    /**
     * Checks if an Entity with given id exists
     *
     * @param id the id of the Entity
     * @return true if an Entity with given id exists
     */
    public boolean existsEntity(String id) {
        return getRepository().existsByIdentifier(id);
    }

    /**
     * Gets the Entity with given id
     *
     * @param id           the id of the Entity
     * @param queryOptions query Options
     * @return ElementWithQueryOptions wrapping requested Entity
     * @throws STACRUDException if an error occurred
     */
    public ElementWithQueryOptions getEntity(String id, QueryOptions queryOptions) throws STACRUDException {
        try {
            S entity = getRepository().findByIdentifier(id, defaultFetchGraphs).get();
            if (queryOptions.hasExpandOption()) {
                return this.createWrapper(fetchExpandEntities(entity, (ExpandFilter) queryOptions.getExpandOption()),
                                          queryOptions);
            } else {
                return this.createWrapper(entity, queryOptions);
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    /**
     * Requests the full EntityCollection
     *
     * @param queryOptions {@link QueryOptions}
     * @return the full EntityCollection
     * @throws STACRUDException if the queryOptions are invalid
     */
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        try {
            Page<S> pages = getRepository().findAll(getFilterPredicate(entityClass, queryOptions),
                                                    createPageableRequest(queryOptions),
                                                    defaultFetchGraphs);
            return getCollectionWrapper(queryOptions, pages);
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    private CollectionWrapper getCollectionWrapper(QueryOptions queryOptions, Page<S> pages) {
        if (queryOptions.hasExpandOption()) {
            Page<E> expanded = pages.map(e -> {
                try {
                    return fetchExpandEntities(e, (ExpandFilter) queryOptions.getExpandOption());
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

    /**
     * Requests the Entity with given ownId that is related to a single Entity with given relatedId and relatedType
     *
     * @param relatedId    ID of the related Entity
     * @param relatedType  EntityType of the related Entity
     * @param ownId        ID of the requested Entity. Can be null.
     * @param queryOptions {@link QueryOptions} used for serialization
     * @return Entity that matches
     * @throws STACRUDException if an error occurred
     */
    public ElementWithQueryOptions<?> getEntityByRelatedEntity(String relatedId,
                                                               String relatedType,
                                                               String ownId,
                                                               QueryOptions queryOptions)
            throws STACRUDException {
        try {
            Optional<S> elem =
                    getRepository().findOne(byRelatedEntityFilter(relatedId, relatedType, ownId), defaultFetchGraphs);
            if (elem.isPresent()) {
                if (queryOptions.hasExpandOption()) {
                    return this.createWrapper(fetchExpandEntities(elem.get(),
                                                                  (ExpandFilter) queryOptions.getExpandOption()),
                                              queryOptions);
                } else {
                    return this.createWrapper(elem.get(), queryOptions);
                }
            } else {
                return null;
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
    public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                String relatedType,
                                                                QueryOptions queryOptions)
            throws STACRUDException {
        try {
            Page<S> pages = getRepository()
                    .findAll(byRelatedEntityFilter(relatedId, relatedType, null),
                             createPageableRequest(queryOptions),
                             defaultFetchGraphs);
            return getCollectionWrapper(queryOptions, pages);

        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    /**
     * Gets the Id on an Entity that is related to a single Entity with given relatedId and relatedType.
     * May be overwritten by classes that use a different field for storing the identifier.
     *
     * @param relatedId   ID of the related Entity
     * @param relatedType EntityType of the related Entity
     * @return Id of the Entity. Null if no entity is present
     */
    public String getEntityIdByRelatedEntity(String relatedId, String relatedType) {
        Optional<String> entity = getRepository().identifier(
                this.byRelatedEntityFilter(relatedId, relatedType, null),
                IDENTIFIER);
        return entity.orElse(null);
    }

    /**
     * Checks if an entity with given ownId exists that relates to an entity with given relatedId and relatedType
     *
     * @param relatedId   ID of the related Entity
     * @param relatedType EntityType of the related Entity
     * @param ownId       ID of the requested Entity. Can be null.
     * @return true if an Entity exists
     */
    public boolean existsEntityByRelatedEntity(String relatedId,
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

    /**
     * Gets a Map that holds definitions for all related Entities of the
     * requested Entity. In this Map K represents the EntityType and V the Set
     * of IDs for those Entities that has a Collection the requested Entity is
     * part of. Returns null if this Entity is not part of any collection.
     *
     * @param rawObject Raw Database Entity of type T
     * @return Map with definitions for all Entities the requested Entity is
     * related to
     */
    public abstract Map<String, Set<String>> getRelatedCollections(Object rawObject);

    /**
     * Get the {@link JpaRepository} for this
     * {@link AbstractSensorThingsEntityService}
     *
     * @return the concrete {@link JpaRepository}
     */
    public T getRepository() {
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

    @Transactional(rollbackFor = Exception.class)
    public ElementWithQueryOptions create(S entity) throws STACRUDException {
        return this.createWrapper(createEntity(entity), null);
    }

    @Transactional(rollbackFor = Exception.class)
    protected abstract S createEntity(S entity) throws STACRUDException;

    @Transactional(rollbackFor = Exception.class)
    public ElementWithQueryOptions update(String id, S entity, HttpMethod method) throws STACRUDException {
        return this.createWrapper(updateEntity(id, entity, method), null);
    }

    @Transactional(rollbackFor = Exception.class)
    protected abstract S updateEntity(String id, S entity, HttpMethod method) throws STACRUDException;

    @Transactional(rollbackFor = Exception.class)
    protected abstract S updateEntity(S entity) throws STACRUDException;

    @Transactional(rollbackFor = Exception.class)
    public abstract void delete(String id) throws STACRUDException;

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
    //    if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
    //        return update(entity, HttpMethod.PATCH);
    //    }
    //    return create(entity);
    //}

    protected void checkInlineDatastream(DatastreamEntity datastream) throws STACRUDException {
        if (datastream.getIdentifier() == null
                || datastream.isSetName()
                || datastream.isSetDescription()
                || datastream.isSetUnit()) {
            throw new STACRUDException("Inlined datastream entities are not allowed for updates!");
        }
    }

    protected void checkInlineLocation(LocationEntity location) throws STACRUDException {
        if (location.getIdentifier() == null || location.isSetName() || location.isSetDescription()) {
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
        long offset = queryOptions.hasSkipOption() ? queryOptions.getSkipOption().getValue() : 0;
        Sort sort = Sort.by(Sort.Direction.ASC, defaultSortingProperty);
        if (queryOptions.hasOrderByOption()) {
            boolean first = true;
            for (OrderProperty sortProperty :
                    queryOptions.getOrderByOption().getSortProperties()) {
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
                                               queryOptions.getTopOption().getValue().intValue(),
                                               sort);
    }

    protected OffsetLimitBasedPageRequest createPageableRequest(QueryOptions queryOptions) {
        return this.createPageableRequest(queryOptions, IDENTIFIER);
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
            if (!queryOptions.hasFilterOption()) {
                return null;
            } else {
                FilterFilter filterOption = (FilterFilter) queryOptions.getFilterOption();
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
    protected AbstractSensorThingsEntityService<?, LocationEntity, LocationEntity> getLocationService() {
        return (AbstractSensorThingsEntityService<?, LocationEntity, LocationEntity>)
                getEntityService(EntityTypes.Location);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityService<?, HistoricalLocationEntity, HistoricalLocationEntity>
    getHistoricalLocationService() {
        return (AbstractSensorThingsEntityService<?, HistoricalLocationEntity, HistoricalLocationEntity>)
                getEntityService(EntityTypes.HistoricalLocation);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityService<?, DatastreamEntity, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity, DatastreamEntity>)
                getEntityService(EntityTypes.Datastream);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityService<?, AbstractFeatureEntity<?>, StaFeatureEntity<?>>
    getFeatureOfInterestService() {
        return (AbstractSensorThingsEntityService<?, AbstractFeatureEntity<?>, StaFeatureEntity<?>>)
                getEntityService(EntityTypes.FeatureOfInterest);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityService<?, PlatformEntity, PlatformEntity> getThingService() {
        return (AbstractSensorThingsEntityService<?, PlatformEntity, PlatformEntity>)
                getEntityService(EntityTypes.Thing);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityService<?, ProcedureEntity, SensorEntity> getSensorService() {
        return (AbstractSensorThingsEntityService<?, ProcedureEntity, SensorEntity>)
                getEntityService(EntityTypes.Sensor);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityService<?, PhenomenonEntity, ObservablePropertyEntity>
    getObservedPropertyService() {
        return (AbstractSensorThingsEntityService<?, PhenomenonEntity, ObservablePropertyEntity>)
                getEntityService(EntityTypes.ObservedProperty);
    }

    @SuppressWarnings("unchecked")
    protected AbstractSensorThingsEntityService<?, DataEntity<?>, StaDataEntity<?>> getObservationService() {
        return (AbstractSensorThingsEntityService<?, DataEntity<?>, StaDataEntity<?>>)
                getEntityService(EntityTypes.Observation);
    }
}
