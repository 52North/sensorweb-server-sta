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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data.service;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.n52.sta.data.repositories.IdentifierRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.serdes.model.ElementWithQueryOptions;
import org.n52.sta.utils.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for requesting Sensor Things entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class AbstractSensorThingsEntityService<T extends IdentifierRepository<S>, S extends IdEntity> {

    protected final String IDENTIFIER = "identifier";
    protected final String STAIDENTIFIER = "staIdentifier";
    protected final String ENCODINGTYPE = "encodingType";

    @Autowired
    private EntityServiceRepository serviceRepository;


    private Class entityClass;

    private T repository;

    public AbstractSensorThingsEntityService(T repository, Class entityClass) {
        this.entityClass = entityClass;
        this.repository = repository;
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
     * @param id the id of the Entity
     * @return
     */
    public ElementWithQueryOptions getEntity(String id , QueryOptions queryOptions) throws STACRUDException {
        try {
            return this.createWrapper(getRepository().findByIdentifier(id).get(), queryOptions);
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage());
        }
    }

    /**
     * Requests the full EntityCollection
     *
     * @param queryOptions {@link QueryOptions}
     * @return the full EntityCollection
     * @throws STAInvalidQueryException if the queryOptions are invalid
     */
    public List<ElementWithQueryOptions> getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        try {
            Specification<S> filter = getFilterPredicate(entityClass, queryOptions);
            return getRepository()
                    .findAll(filter, createPageableRequest(queryOptions))
                    .map((S e) -> createWrapper(e, queryOptions))
                    .getContent();
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage());
        }
    }

    /**
     * Wraps the raw Entity into a Wrapper object to associate with QueryOptions used for this request
     * @param entity entity
     * @param queryOptions query options
     * @return instance of ElementWithQueryOptions
     */
    protected abstract ElementWithQueryOptions createWrapper(Object entity, QueryOptions queryOptions);

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

    /**
     * Requests the Entity with given ownId that is related to a single Entity with given relatedId and relatedType
     *
     * @param relatedId   ID of the related Entity
     * @param relatedType EntityType of the related Entity
     * @param ownId       ID of the requested Entity. Can be null.
     * @param queryOptions {@link QueryOptions} used for serialization
     * @return Entity that matches
     */
    public ElementWithQueryOptions getEntityByRelatedEntity(String relatedId,
                                                            String relatedType,
                                                            String ownId,
                                                            QueryOptions queryOptions)
            throws STACRUDException {
        try {
            Optional<S> elem = getRepository().findOne(byRelatedEntityFilter(relatedId, relatedType, ownId));
            if (elem.isPresent()) {
                return this.createWrapper(
                        elem.get(),
                        queryOptions);
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage());
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
        return entity.isPresent() ? entity.get() : null;
    }

    /**
     * Requests the EntityCollection that is related to a single Entity with the
     * given ID and type
     *
     * @param relatedId    the ID of the Entity the EntityCollection is related to
     * @param relatedType  EntityType of the related Entity
     * @param queryOptions {@link QueryOptions}
     * @return List of Entities that match
     * @throws STAInvalidQueryException if the queryOptions are invalid
     */
    public List<ElementWithQueryOptions> getEntityCollectionByRelatedEntity(String relatedId,
                                                                            String relatedType,
                                                                            QueryOptions queryOptions)
            throws STACRUDException {
        try {
            return getRepository()
                    .findAll(byRelatedEntityFilter(relatedId, relatedType, null),
                            createPageableRequest(queryOptions))
                    .map((S e) -> createWrapper(e, queryOptions))
                    .getContent();

        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage());
        }
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
     * Request the count for the EntityCollection that is related to a single
     * Entity with the given ID and type
     *
     * @param relatedId   the ID of the Entity the EntityCollection is related to
     * @param relatedType EntityType of the related Entity
     * @return the count of related entities
     */
    // public long getEntityCollectionCountByRelatedEntity(String relatedId, String relatedType) {
    //    return 0;
    //}


    /**
     * Requests the ID for the Entity that is related to a single Entity with a
     * given ID and in accordance to a given ID
     *
     * @param relatedId         the ID for the Entity the requested Entity is related to
     * @param relatedType EntityType of the related Entity
     * @param ownId         the ID for the requested Entity. Can be null.
     * @return the Entity that is related to the given Entity and is conform to
     * the given ID
     */
    //public abstract Optional<String> getIdByRelatedEntity(String relatedId,
    //                                                     String relatedType,
    //                                                      String ownId);

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
     * @throws STAInvalidQueryException if the queryOptions are invalid
     */
    public long getCount(QueryOptions queryOptions) {
        return getRepository().count(getFilterPredicate(entityClass, queryOptions));
    }


    /**
     * Constructs QueryDSL FilterPredicate based on given queryOptions.
     *
     * @param entityClass  Class of the requested Entity
     * @param queryOptions QueryOptions Object
     * @return Predicate based on FilterOption from queryOptions
     */
    public Specification<S> getFilterPredicate(Class entityClass, QueryOptions queryOptions) {
        return (root, query, builder) -> {
            return null;
//            try {
//                //if (queryOptions.hasOrderByOption()) {
//                //    TODO: add orderby option for observation.result here?
//                //}
//                if (!queryOptions.hasFilterOption()) {
//                    return null;
//                } else {
//                    Expression filterExpression = queryOptions.getFilterOption().getExpression();
//
//                    FilterExpressionVisitor visitor = new FilterExpressionVisitor(
//                            entityClass,
//                            this,
//                            builder, root);
//                    try {
//                        Object accept = filterExpression.accept(visitor);
//                        if (accept instanceof Specification) {
//                            return ((Specification<S>) accept).toPredicate(root, query, builder);
//                        } else if (accept instanceof Predicate) {
//                            return (Predicate) accept;
//                        } else {
//                            throw new ODataApplicationException(
//                                    "Received invalid FilterExpression.",
//                                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
//                                    Locale.ENGLISH);
//                        }
//                    } catch (ExpressionVisitException e) {
//                        throw new ODataApplicationException(
//                                e.getMessage(),
//                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
//                                Locale.ENGLISH);
//                    }
//                }
//            } catch (ODataApplicationException e) {
//                throw new RuntimeException(e);
//            }
        };
    }

    @Transactional(rollbackFor = Exception.class)
    public ElementWithQueryOptions create(S entity) throws STACRUDException {
        return this.createWrapper(createEntity(entity), null);
    }

    protected abstract S createEntity(S entity) throws STACRUDException;

    @Transactional(rollbackFor = Exception.class)
    public ElementWithQueryOptions update(String id, S entity, HttpMethod method) throws STACRUDException {
        return this.createWrapper(updateEntity(id, entity, method), null);
    }

    protected abstract S updateEntity(String id, S entity, HttpMethod method) throws STACRUDException;

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
    //protected S createOrUpdate(S entity) throws ODataApplicationException {
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
            throw new STACRUDException("Inlined datastream entities are not allowed for updates!",
                    HttpStatus.BAD_REQUEST);
        }
    }

    protected void checkInlineLocation(LocationEntity location) throws STACRUDException {
        if (location.getIdentifier() == null || location.isSetName() || location.isSetDescription()) {
            throw new STACRUDException("Inlined location entities are not allowed for updates!",
                    HttpStatus.BAD_REQUEST);
        }
    }

    protected AbstractSensorThingsEntityService<?, ?> getEntityService(EntityTypes type) {
        return serviceRepository.getEntityService(type);
    }


    /**
     * Create {@link PageRequest}
     *
     * @param queryOptions {@link QueryOptions} to create {@link PageRequest}
     * @return {@link PageRequest} of type {@link OffsetLimitBasedPageRequest}
     */
    protected OffsetLimitBasedPageRequest createPageableRequest(QueryOptions queryOptions,
                                                                String defaultSortingProperty) {
        //TODO(specki): implement
//        int offset = queryOptions.hasSkipOption() ? queryOptions.getSkipOption().getValue() : 0;
//        Sort sort = Sort.by(Direction.ASC, defaultSortingProperty);
//        if (queryOptions.hasOrderByOption()) {
//            boolean first = true;
//            try {
//                for (OrderByItem orderByItem : queryOptions.getOrderByOption().getOrders()) {
//                    if (first) {
//                        sort = Sort.by(orderByItem.isDescending() ? Direction.DESC : Direction.ASC,
//                                orderByItem.getExpression().accept(new ExpressionGenerator(this)));
//                        first = false;
//                    } else {
//                        sort.and(Sort.by(orderByItem.isDescending() ? Direction.DESC : Direction.ASC,
//                                orderByItem.getExpression().accept(new ExpressionGenerator(this))));
//                    }
//                }
//            } catch (ExpressionVisitException | ODataApplicationException e) {
//                // use default sort
//            }
//        }
//        return new OffsetLimitBasedPageRequest(offset, queryOptions.getTopOption().getValue(), sort);
        return new OffsetLimitBasedPageRequest(0, 100);
    }

    //TODO(specki): check if this needs to be overridden by observedproperty that uses different field
    protected OffsetLimitBasedPageRequest createPageableRequest(QueryOptions queryOptions) {
        return this.createPageableRequest(queryOptions, IDENTIFIER);
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

    /**
     * {@link ExpressionVisitor} to get property name from {@link OrderByOption}
     */
    /*
    private static final class ExpressionGenerator implements ExpressionVisitor<String> {

        private AbstractSensorThingsEntityService<?, ?> service;

        ExpressionGenerator(AbstractSensorThingsEntityService<?, ?> service) {
            this.service = service;
        }

        @Override
        public String visitLiteral(Literal literal) throws ExpressionVisitException {
            throw new ExpressionVisitException("Literal expressions are not supported");
        }

        @Override
        public String visitLambdaExpression(String fun, String var, Expression expr) throws ExpressionVisitException {
            throw new ExpressionVisitException("Lambda expressions are not supported");
        }

        @Override
        public String visitMember(Member member) throws ExpressionVisitException {
            return service.checkPropertyName(visitMember(member.getResourcePath()));
        }

        public String visitMember(UriInfoResource member) throws ExpressionVisitException {
            return member.getUriResourceParts().stream().map(UriResource::getSegmentValue)
                    .collect(Collectors.joining("/"));
        }

        @Override
        public String visitAlias(String aliasName) throws ExpressionVisitException {
            throw new ExpressionVisitException("aliases are not supported");
        }

        @Override
        public String visitTypeLiteral(EdmType type) throws ExpressionVisitException {
            throw new ExpressionVisitException("type literals are not supported");
        }

        @Override
        public String visitLambdaReference(String variableName) throws ExpressionVisitException {
            throw new ExpressionVisitException("Lambda references are not supported");
        }

        @Override
        public String visitEnum(EdmEnumType type, List<String> enumValues) throws ExpressionVisitException {
            throw new ExpressionVisitException("enums are not supported");
        }

        @Override
        public String visitBinaryOperator(BinaryOperatorKind operator, String left, String right)
                throws ExpressionVisitException, ODataApplicationException {
            throw new ExpressionVisitException("BinaryOperatorKind expressions are not supported");
        }

        @Override
        public String visitUnaryOperator(UnaryOperatorKind operator, String operand)
                throws ExpressionVisitException, ODataApplicationException {
            throw new ExpressionVisitException("UnaryOperatorKind expressions are not supported");
        }

        @Override
        public String visitMethodCall(MethodKind methodCall, List<String> parameters)
                throws ExpressionVisitException, ODataApplicationException {
            throw new ExpressionVisitException("MethodKind expressions are not supported");
        }
    }
*/
}
