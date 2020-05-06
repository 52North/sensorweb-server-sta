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

package org.n52.sta.data.repositories;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.graph.EntityGraphs;
import org.hibernate.graph.GraphParser;
import org.hibernate.graph.RootGraph;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.SpringApplicationContext;
import org.n52.sta.data.STAEventHandler;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageBusRepository<T, I extends Serializable>
        extends SimpleJpaRepository<T, I> implements RepositoryConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusRepository.class);

    private final Map<String, String> entityTypeToStaType;
    private final String FETCHGRAPH_HINT = "javax.persistence.fetchgraph";
    private final String IDENTIFIER = "identifier";
    private final String STAIDENTIFIER = "staIdentifier";

    private final JpaEntityInformation entityInformation;
    private final STAEventHandler mqttHandler;
    private final EntityManager em;
    private final Class<T> entityClass;
    private final CriteriaBuilder criteriaBuilder;

    // Is set in Repositories that need it to get related Collections for mqtt handling
    private DatastreamRepository datastreamRepository;
    private DatastreamQuerySpecifications dQs = new DatastreamQuerySpecifications();

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    MessageBusRepository(JpaEntityInformation<T, Long> entityInformation,
                         EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.em = entityManager;
        this.entityInformation = entityInformation;
        this.entityClass = entityInformation.getJavaType();
        this.entityTypeToStaType = this.createEntityTypeToStaTypeMapping();
        this.criteriaBuilder = em.getCriteriaBuilder();

        this.mqttHandler = (STAEventHandler) SpringApplicationContext.getBean(STAEventHandler.class);
        Assert.notNull(this.mqttHandler, "Could not autowire Mqtt handler!");

        if (this.entityClass.equals(DataEntity.class)
                || this.entityClass.equals(ProcedureEntity.class)
                || this.entityClass.equals(PhenomenonEntity.class)) {
            this.datastreamRepository =
                    (DatastreamRepository) SpringApplicationContext.getBean(DatastreamRepository.class);
            Assert.notNull(this.datastreamRepository, "Could not autowire DatastreamRepository!");
        }
    }

    private TypedQuery<T> createIdentifierQuery(String identifier, String column) {
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> root = criteriaQuery.from(entityClass);

        ParameterExpression<String> params = criteriaBuilder.parameter(String.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(column), params));
        return em.createQuery(criteriaQuery).setParameter(params, identifier);
    }

    private HashMap<String, String> createEntityTypeToStaTypeMapping() {
        HashMap<String, String> map = new HashMap<>(11);
        map.put(ENTITYNAME_OBSERVATION, StaConstants.OBSERVATIONS);
        map.put(ENTITYNAME_DATASTREAM, StaConstants.DATASTREAMS);
        map.put(ENTITYNAME_FEATURE_OF_INTEREST, StaConstants.FEATURES_OF_INTEREST);
        map.put(ENTITYNAME_AFEATURE_OF_INTEREST, StaConstants.FEATURES_OF_INTEREST);
        map.put(ENTITYNAME_HIST_LOCATION, StaConstants.HISTORICAL_LOCATIONS);
        map.put(ENTITYNAME_LOCATION, StaConstants.LOCATIONS);
        map.put(ENTITYNAME_OBSERVED_PROPERTY, StaConstants.OBSERVED_PROPERTIES);
        map.put(ENTITYNAME_SENSOR, StaConstants.SENSORS);
        map.put(ENTITYNAME_THING, StaConstants.THINGS);
        return map;
    }

    @Transactional(readOnly = true)
    public Optional<String> identifier(Specification<T> spec, String columnName) {
        CriteriaQuery<Object> query = criteriaBuilder.createQuery(Object.class);
        Root<T> root = query.from(getDomainClass());
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        if (columnName != null) {
            query.select(root.get(columnName));
        }
        try {
            return Optional.of((String) em.createQuery(query).getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public List<String> identifierList(Specification<T> spec, Pageable pageable, String columnName) {
        CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
        Root<T> root = query.from(getDomainClass());
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        if (columnName != null) {
            query.select(root.get(columnName));
        }
        if (pageable.getSort().isSorted()) {
//            if (getDomainClass().getName().equals(DataEntity.class.getName())) {
//                StringBuffer buffer = new StringBuffer("SELECT d.id, ");
//                String when = " WHEN TYPE(e) = ";
//                buffer.append(" CASE ");
//                buffer.append(when).append(QuantityDataEntity.class.getSimpleName())
//                        .append(" THEN q.value ");
//                buffer.append(when).append(CountDataEntity.class.getSimpleName())
//                        .append(" THEN c.value ");
//                buffer.append(" END as value ");
//                buffer.append(" FROM DataEntity d, QuantityDataEntity q, CountDataEntity c");
//                Query createQuery = em.createQuery(buffer.toString());
//
//                query.where(criteriaBuilder.and(query.getRestriction(),
//                        criteriaBuilder.in(root.get(DataEntity.PROPERTY_ID)).value(createQuery)));
//            } else {
                query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
//            }
        }

        TypedQuery<String> typedQuery = em.createQuery(query);
        if (pageable.isUnpaged()) {
            return typedQuery.getResultList();
        } else {
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
            return typedQuery.getResultList();
        }
    }

    @Transactional
    public Optional<T> findByIdentifier(String identifier, EntityGraphRepository.FetchGraph... entityGraphs) {
        return findByQuery(createIdentifierQuery(identifier, IDENTIFIER), entityGraphs);
    }

    @Transactional
    public Optional<T> findByStaIdentifier(String identifier, EntityGraphRepository.FetchGraph... entityGraphs) {
        return findByQuery(createIdentifierQuery(identifier, STAIDENTIFIER), entityGraphs);
    }

    private Optional<T> findByQuery(TypedQuery<T> query, EntityGraphRepository.FetchGraph... entityGraphs) {
        EntityGraph<T> fetchGraph = createEntityGraph(entityGraphs);
        if (fetchGraph != null) {
            query.setHint(FETCHGRAPH_HINT, fetchGraph);
        }
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public boolean existsByStaIdentifier(String identifier) {
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(entityClass);

        criteriaQuery.select(criteriaBuilder.count(root));
        ParameterExpression<String> params = criteriaBuilder.parameter(String.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(STAIDENTIFIER), params));
        TypedQuery<Long> query = em.createQuery(criteriaQuery).setParameter(params, identifier);

        return query.getSingleResult() > 0;
    }

    @Transactional
    @Override
    public <S extends T> S save(S newEntity) {
        String entityType = entityTypeToStaType.get(entityInformation.getEntityName());
        boolean intercept =
                mqttHandler.getWatchedEntityTypes().contains(entityType);

        if (entityInformation.isNew(newEntity)) {
            em.persist(newEntity);
            em.flush();
            if (intercept) {
                this.mqttHandler.handleEvent(newEntity, entityType, null, getRelatedCollections(newEntity));
            }
        } else {
            if (intercept) {
                S oldEntity = (S) em.find(newEntity.getClass(), entityInformation.getId(newEntity));
                Map<String, Object> oldProperties = getPropertyMap(oldEntity);
                S entity = em.merge(newEntity);
                em.flush();
                this.mqttHandler.handleEvent(newEntity,
                                             entityType,
                                             computeDifference(oldProperties, getPropertyMap(newEntity)),
                                             getRelatedCollections(entity));
                // Entity was saved multiple times without changes. As reference is the same
                if (oldEntity == entity) {
                    return entity;
                }
            } else {
                return em.merge(newEntity);
            }
        }

        return newEntity;
    }

    private <S extends T> Map<String, Set<String>> getRelatedCollections(S rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        if (rawObject instanceof ProcedureEntity) {
            if (rawObject instanceof SensorEntity) {
                SensorEntity entity = (SensorEntity) rawObject;
                if (entity.hasDatastreams()) {
                    collections.put(STAEntityDefinition.DATASTREAMS,
                                    entity.getDatastreams()
                                          .stream()
                                          .map(DatastreamEntity::getStaIdentifier)
                                          .collect(Collectors.toSet()));
                }
            } else {
                ProcedureEntity entity = (ProcedureEntity) rawObject;
                collections.put(STAEntityDefinition.DATASTREAM,
                                datastreamRepository
                                        .findAll(dQs.withSensorStaIdentifier(entity.getStaIdentifier()))
                                        .stream()
                                        .map(DatastreamEntity::getStaIdentifier)
                                        .collect(Collectors.toSet())
                );
            }
        } else if (rawObject instanceof LocationEntity) {
            LocationEntity entity = (LocationEntity) rawObject;
            if (entity.hasHistoricalLocations()) {
                collections.put(STAEntityDefinition.HISTORICAL_LOCATIONS,
                                entity.getHistoricalLocations()
                                      .stream()
                                      .map(HistoricalLocationEntity::getStaIdentifier)
                                      .collect(Collectors.toSet()));
            }

            if (entity.hasThings()) {
                collections.put(STAEntityDefinition.THINGS,
                                entity.getThings()
                                      .stream()
                                      .map(PlatformEntity::getStaIdentifier)
                                      .collect(Collectors.toSet()));
            }
        } else if (rawObject instanceof PlatformEntity) {
            PlatformEntity entity = (PlatformEntity) rawObject;
            if (entity.hasLocationEntities()) {
                collections.put(
                        STAEntityDefinition.LOCATIONS,
                        entity.getLocations()
                              .stream()
                              .map(LocationEntity::getStaIdentifier)
                              .collect(Collectors.toSet()));
            }

            if (entity.hasHistoricalLocations()) {
                collections.put(
                        STAEntityDefinition.HISTORICAL_LOCATIONS,
                        entity.getHistoricalLocations()
                              .stream()
                              .map(HistoricalLocationEntity::getStaIdentifier)
                              .collect(Collectors.toSet()));
            }

            if (entity.hasDatastreams()) {
                collections.put(STAEntityDefinition.DATASTREAMS,
                                entity.getDatastreams()
                                      .stream()
                                      .map(DatastreamEntity::getStaIdentifier)
                                      .collect(Collectors.toSet()));
            }
        } else if (rawObject instanceof DatastreamEntity) {
            DatastreamEntity entity = (DatastreamEntity) rawObject;

            if (entity.hasThing()) {
                collections.put(STAEntityDefinition.THINGS,
                                Collections.singleton(entity.getThing().getStaIdentifier()));
            }

            if (entity.hasProcedure()) {
                collections.put(STAEntityDefinition.SENSORS,
                                Collections.singleton(entity.getProcedure().getStaIdentifier()));
            }

            if (entity.hasObservableProperty()) {
                collections.put(STAEntityDefinition.OBSERVED_PROPERTIES,
                                Collections.singleton(entity.getObservableProperty().getStaIdentifier()));
            }
        } else if (rawObject instanceof HistoricalLocationEntity) {
            HistoricalLocationEntity entity = (HistoricalLocationEntity) rawObject;

            if (entity.hasThing()) {
                collections.put(STAEntityDefinition.THINGS,
                                Collections.singleton(entity.getThing().getStaIdentifier()));
            }

            if (entity.hasLocationEntities()) {
                collections.put(STAEntityDefinition.LOCATIONS,
                                entity.getLocations()
                                      .stream()
                                      .map(LocationEntity::getStaIdentifier)
                                      .collect(Collectors.toSet()));
            }
        } else if (rawObject instanceof DataEntity<?>) {
            DataEntity<?> entity = (DataEntity<?>) rawObject;

            if (entity.getDataset() != null && entity.getDataset().getFeature() != null) {
                collections.put(STAEntityDefinition.FEATURES_OF_INTEREST,
                                Collections.singleton(entity.getDataset().getFeature().getStaIdentifier()));
            }

            Optional<DatastreamEntity> datastreamEntity =
                    datastreamRepository.findOne(dQs.withObservationStaIdentifier(entity.getStaIdentifier()));
            if (datastreamEntity.isPresent()) {
                collections.put(STAEntityDefinition.DATASTREAMS,
                                Collections.singleton(datastreamEntity.get().getStaIdentifier()));
            } else {
                LOGGER.debug("No Datastream associated with this Entity {}", entity.getStaIdentifier());
            }
        } else if (rawObject instanceof AbstractFeatureEntity) {
            return collections;
        } else if (rawObject instanceof PhenomenonEntity) {
            PhenomenonEntity entity = (PhenomenonEntity) rawObject;

            List<DatastreamEntity> observations = datastreamRepository
                    .findAll(dQs.withObservedPropertyStaIdentifier(entity.getStaIdentifier()));
            collections.put(
                    STAEntityDefinition.DATASTREAMS,
                    observations
                            .stream()
                            .map(DatastreamEntity::getStaIdentifier)
                            .collect(Collectors.toSet()));
        } else {
            LOGGER.error("Error while computing related Collections: Could not identify Entity Type");
        }
        return collections;
    }

    private Set<String> computeDifference(Map<String, Object> oldProperties, Map<String, Object> newProperties) {
        HashSet<String> result = new HashSet<>();
        for (Map.Entry<String, Object> entry : oldProperties.entrySet()) {
            if (!Objects.equals(entry.getValue(), newProperties.get(entry.getKey()))) {
                if (SAMPLINGTIMEEND.equals(entry.getKey()) || SAMPLINGTIMESTART.equals(entry.getKey())) {
                    result.add(PHENOMENONTIME);
                } else if (RESULTTIMESTART.equals(entry.getKey()) || RESULTTIMEEND.equals(entry.getKey())) {
                    result.add(RESULTTIME);
                } else if (VALIDTIMESTART.equals(entry.getKey()) || VALIDTIMEEND.equals(entry.getKey())) {
                    result.add(VALIDTIME);
                } else {
                    result.add(entry.getKey());
                }
            }
        }
        return result;
    }

    private <S extends T> Map<String, Object> getPropertyMap(S entity) {
        Map<String, Object> result = new HashMap<>();
        if (entity instanceof ProcedureEntity) {
            result.put(DESCRIPTION, ((ProcedureEntity) entity).getDescription());
            result.put(NAME, ((ProcedureEntity) entity).getName());
            result.put(METADATA, ((ProcedureEntity) entity).getDescriptionFile());
            result.put(ENCODINGTYPE, ((ProcedureEntity) entity).getFormat().getFormat());
        } else if (entity instanceof LocationEntity) {
            result.put(DESCRIPTION, ((LocationEntity) entity).getDescription());
            result.put(NAME, ((LocationEntity) entity).getName());
            result.put(LOCATION, ((LocationEntity) entity).getGeometryEntity().getGeometry());
            result.put(ENCODINGTYPE, ((LocationEntity) entity).getLocationEncoding());
        } else if (entity instanceof PlatformEntity) {
            result.put(DESCRIPTION, ((PlatformEntity) entity).getDescription());
            result.put(NAME, ((PlatformEntity) entity).getName());
            result.put(PROPERTIES, ((PlatformEntity) entity).getProperties());
        } else if (entity instanceof DatastreamEntity) {
            result.put(DESCRIPTION, ((DatastreamEntity) entity).getDescription());
            result.put(NAME, ((DatastreamEntity) entity).getName());
            result.put(OBSERVATIONTYPE, ((DatastreamEntity) entity).getObservationType().getFormat());
            result.put(UOM, ((DatastreamEntity) entity).getUnitOfMeasurement());
            result.put(OBSERVEDAREA,
                       (((DatastreamEntity) entity).getGeometryEntity() != null) ?
                               ((DatastreamEntity) entity).getGeometryEntity().getGeometry() : null);
            result.put(SAMPLINGTIMESTART, ((DatastreamEntity) entity).getSamplingTimeStart());
            result.put(SAMPLINGTIMEEND, ((DatastreamEntity) entity).getSamplingTimeEnd());
            result.put(RESULTTIMESTART, ((DatastreamEntity) entity).getResultTimeStart());
            result.put(RESULTTIMEEND, ((DatastreamEntity) entity).getResultTimeEnd());
        } else if (entity instanceof HistoricalLocationEntity) {
            result.put(TIME, ((HistoricalLocationEntity) entity).getTime());
        } else if (entity instanceof DataEntity<?>) {
            result.put(SAMPLINGTIMESTART, ((DataEntity<?>) entity).getSamplingTimeStart());
            result.put(SAMPLINGTIMEEND, ((DataEntity<?>) entity).getSamplingTimeEnd());
            result.put(RESULTTIME, ((DataEntity<?>) entity).getResultTime());
            result.put(VALIDTIMESTART, ((DataEntity<?>) entity).getValidTimeStart());
            result.put(VALIDTIMEEND, ((DataEntity<?>) entity).getValidTimeEnd());
            Set<ParameterEntity<?>> parameters = new HashSet<>();
            parameters.addAll(((DataEntity<?>) entity).getParameters());
            result.put(PARAMETERS, parameters);
            result.put(RESULT, ((DataEntity<?>) entity).getValue());
            //TODO: implement difference map for "resultQuality"
        } else if (entity instanceof AbstractFeatureEntity) {
            result.put(NAME, ((AbstractFeatureEntity) entity).getName());
            result.put(DESCRIPTION, ((AbstractFeatureEntity) entity).getDescription());
            result.put(FEATURE, ((AbstractFeatureEntity) entity).getGeometry());
        } else if (entity instanceof PhenomenonEntity) {
            result.put(NAME, ((PhenomenonEntity) entity).getName());
            result.put(DESCRIPTION, ((PhenomenonEntity) entity).getDescription());
            result.put(DEFINITION, ((PhenomenonEntity) entity).getIdentifier());
        } else {
            LOGGER.error("Error while computing difference map: Could not identify Entity Type");
        }
        return result;
    }

    private EntityGraph<T> createEntityGraph(EntityGraphRepository.FetchGraph... fetchGraphs) {
        if (fetchGraphs != null && fetchGraphs.length != 0) {
            Set<RootGraph<T>> roots = new HashSet<>();
            for (EntityGraphRepository.FetchGraph entityGraph : fetchGraphs) {
                roots.add(GraphParser.parse(entityClass,
                                            entityGraph.value(),
                                            (SessionImplementor) em.getDelegate()));
            }
            return EntityGraphs.merge(
                    (EntityManager) em.getDelegate(),
                    entityClass,
                    roots.toArray(new RootGraph[] {}));
        } else {
            return null;
        }
    }

    public Optional<T> findOne(Specification<T> spec, EntityGraphRepository.FetchGraph... fetchGraphs) {
        try {
            return Optional.of(getQuery(spec, Sort.unsorted(), createEntityGraph(fetchGraphs)).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<T> findAll(Specification<T> spec, EntityGraphRepository.FetchGraph... fetchGraphs) {
        return getQuery(spec, Sort.unsorted(), createEntityGraph(fetchGraphs)).getResultList();
    }

    public Page<T> findAll(Specification<T> spec, Pageable pageable, EntityGraphRepository.FetchGraph... fetchGraphs) {
        TypedQuery<T> query = getQuery(spec, pageable, createEntityGraph(fetchGraphs));
        return pageable.isUnpaged() ? new PageImpl<>(query.getResultList())
                : readPage(query, getDomainClass(), pageable, spec);
    }

    public List<T> findAll(Specification<T> spec, Sort sort, EntityGraphRepository.FetchGraph... fetchGraphs) {
        return getQuery(spec, sort, createEntityGraph(fetchGraphs)).getResultList();
    }

    protected TypedQuery<T> getQuery(@Nullable Specification<T> spec,
                                     Pageable pageable,
                                     EntityGraph<T> entityGraph) {

        Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        return getQuery(spec, getDomainClass(), sort, entityGraph);
    }

    /*
    private <S extends T> TypedQuery<S> getQuery(@Nullable Specification<S> spec,
                                                 Class<S> domainClass,
                                                 Pageable pageable,
                                                 EntityGraph<S> entityGraph) {

        Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        return getQuery(spec, domainClass, sort, entityGraph);
    }
    */

    private TypedQuery<T> getQuery(@Nullable Specification<T> spec,
                                   Sort sort,
                                   EntityGraph<T> entityGraph) {
        return getQuery(spec, getDomainClass(), sort, entityGraph);
    }

    private <S extends T> TypedQuery<S> getQuery(@Nullable Specification<S> spec,
                                                 Class<S> domainClass,
                                                 Sort sort,
                                                 EntityGraph<S> entityGraph) {
        CriteriaQuery<S> query = criteriaBuilder.createQuery(domainClass);
        Root<S> root = query.from(domainClass);
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        query.select(root);

        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, criteriaBuilder));
        }

        TypedQuery<S> typedQuery = em.createQuery(query);
        if (entityGraph != null) {
            typedQuery.setHint(FETCHGRAPH_HINT, entityGraph);
        }
        return typedQuery;
    }

    /**
     * Saves an entity to the Datastore without intercepting for mqtt subscription checking.
     * Used when Entity is saved multiple times during creation
     *
     * @param entity Entity to be saved
     * @param <S>    raw entity type
     * @return saved entity.
     */
    @Transactional
    public <S extends T> S intermediateSave(S entity) {
        if (entityInformation.isNew(entity)) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }

}
