/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data.service;

import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.n52.sta.service.query.FilterExpressionVisitor;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * Interface for requesting Sensor Things entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractSensorThingsEntityService<T extends JpaRepository<?, Long>, S extends IdEntity> {
   
    @Autowired
    private EntityServiceRepository serviceRepository;

    private T repository;

    public AbstractSensorThingsEntityService(T repository) {
        this.repository = repository;
    }
    
    @PostConstruct
    public void init() {
        serviceRepository.addEntityService(this);
    }
    
    public abstract EntityTypes getType();

    /**
     * Requests the full EntityCollection
     * 
     * @param queryOptions
     *
     * @return the full EntityCollection
     * @throws ODataApplicationException if the queryOptions are invalid
     */
    public abstract EntityCollection getEntityCollection(QueryOptions queryOptions) throws ODataApplicationException;

    /**
     * Requests the EntityCollection that is related to a single Entity with the
     * given ID and type
     *
     * @param sourceId
     *            the ID of the Entity the EntityCollection is related to
     * @param sourceEntityType
     *            EntityType of the related Entity
     * @param queryOptions
     * @return the EntityCollection that is related to the given Entity
     * @throws ODataApplicationException if the queryOptions are invalid
     */
    public abstract EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType,
            QueryOptions queryOptions) throws ODataApplicationException;

    /**
     * Request the count for the EntityCollection that is related to a single
     * Entity with the given ID and type
     * 
     * @param sourceId
     *            the ID of the Entity the EntityCollection is related to
     * @param sourceEntityType
     *            EntityType of the related Entity
     * @return the count of related entities
     */
    public long getRelatedEntityCollectionCount(Long sourceId, EdmEntityType sourceEntityType) {
        return 0;
    }

    /**
     * Requests the Entity in accordance to a given ID
     *
     * @param id
     *            the ID to determine the Entity for
     * @return the Entity that is conform to the given key predicates
     */
    public abstract Entity getEntity(Long id);

    /**
     * Requests the ID for an Entity that is related to a single Entity with the
     * given ID
     *
     * @param sourceId
     *            the ID for the Entity the requested Entity is related to
     * @param sourceEntityType
     *            EntityType of the related Entity
     * @return the ID for the Entity that is related to the Entity with the
     *         given Id
     */
    public abstract OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType);

    /**
     * Requests the ID for the Entity that is related to a single Entity with a
     * given ID and in accordance to a given ID
     *
     * @param sourceId
     *            the ID for the Entity the requested Entity is related to
     * @param sourceEntityType
     *            EntityType of the related Entity
     * @param targetId
     *            the ID for the requested Entity
     * @return the Entity that is related to the given Entity and is conform to
     *         the given ID
     */
    public abstract OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId);

    /**
     * Checks if an Entity exists in accordance to a given list of key
     * predicates
     *
     * @param id
     *            the ID to check the existence of an Entity for
     *
     * @return true if an Entity that is conform to the given key predicates
     *         exists
     */
    public boolean existsEntity(Long id) {
        return getRepository().existsById(id);
    }
    
    /**
     * Checks if an Entity exists that is related to a single Entity of the
     * given EntityType
     *
     * @param sourceId
     *            ID of the related Entity
     * @param sourceEntityType
     *            EntityType of the related Entity
     * @return true if an Entity exists that is related to a single Entity of
     *         the given EntityType and with the given KeyPredicates
     */
    public abstract boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType);

    /**
     * Checks if an Entity exists that is conform to given KeyPredicates and is
     * related to a single Entity of the given EntityType and with the given
     * KeyPredicates in accordance
     *
     * @param sourceId
     *            ID of the related Entity
     * @param sourceEntityType
     *            EntityType of the related Entity
     * @param targetId
     *            ID of the requested Entity
     * @return true if an Entity exists that is conform to the given
     *         KeyPredicates and is related to a single Entity of the given
     *         EntityType and with the given KeyPredicates
     */
    public abstract boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId);

    /**
     * Requests the Entity that is related to a single Entity with a given ID
     * and type
     *
     * @param sourceId
     *            ID of the related Entity
     * @param sourceEntityType
     *            EntityType of the related Entity
     * @return the Entity that is related to the Entity with given ID and type
     */
    public abstract Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType);

    /**
     * Requests the Entity that is related to a single Entity with a given ID
     * and type and that is conform to a given ID
     *
     * @param sourceId
     *            ID of the related Entity
     * @param sourceEntityType
     *            EntityType of the related Entity
     * @param targetId
     *            ID of the requested Entity
     * @return the Entity that is related to the given Entity and is conform to
     *         the given ID
     */
    public abstract Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId);
    
     /**
     * Gets a Map<K,V> that holds definitions for all related Entities of the
     * requested Entity. In this Map K represents the EntityType and V the Set
     * of IDs for those Entities that has a Collection the requested Entity is
     * part of. Returns null if this Entity is not part of any collection.
     *
     * @param rawObject Raw Database Entity of type T
     * @return Map with definitions for all Entities the requested Entity is
     * related to
     */
    public abstract Map<String, Set<Long>> getRelatedCollections(Object rawObject);

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
     * Query for the number of element.
     * 
     * @param queryOptions
     * 
     * @return the existing elements
     * @throws ODataApplicationException if the queryOptions could not be parsed.
     */
    public abstract long getCount(QueryOptions queryOptions) throws ODataApplicationException;
    
    /**
     * Constructs QueryDSL FilterPredicate based on given queryOptions.
     * 
     * @param entityClass Class of the requested Entity
     * @param queryOptions QueryOptions Object 
     * @return Predicate based on FilterOption from queryOptions
     * @throws ODataApplicationException if the queryOptions are invalid
     */
    public Specification<S> getFilterPredicate(Class entityClass, QueryOptions queryOptions, CriteriaBuilder criteriaBuilder) throws ODataApplicationException {
        if (!queryOptions.hasFilterOption()) {
            return null;
        } else {
            Expression filterExpression = queryOptions.getFilterOption().getExpression();
            
            FilterExpressionVisitor visitor = new FilterExpressionVisitor(entityClass, this, criteriaBuilder);
            try {
                return (Predicate) filterExpression.accept(visitor);
            } catch (ExpressionVisitException e) {
                throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public abstract S create(S entity) throws ODataApplicationException;

    @Transactional(rollbackFor = Exception.class)
    public abstract S update(S entity, HttpMethod method) throws ODataApplicationException;

    protected abstract S update(S entity) throws ODataApplicationException;

    @Transactional(rollbackFor = Exception.class)
    public abstract void delete(Long id) throws ODataApplicationException;
    
    protected abstract void delete(S entity) throws ODataApplicationException;

    protected S createOrUpdate(S entity) throws ODataApplicationException {
        if (entity.getId() != null && getRepository().existsById(entity.getId())) {
            return update(entity, HttpMethod.PATCH);
        }
        return create(entity);
    }
    
    protected void checkInlineDatastream(DatastreamEntity datastream) throws ODataApplicationException {
        if (datastream.getId() == null || datastream.isSetName() || datastream.isSetDescription() || datastream.isSetUnit()) {
            throw new ODataApplicationException("Inlined entities are not allowed for updates!",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
    }
    
    protected void checkInlineLocation(LocationEntity location) throws ODataApplicationException {
        if (location.getId() == null || location.isSetName() || location.isSetDescription()) {
            throw new ODataApplicationException("Inlined entities are not allowed for updates!",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
    }

    protected AbstractSensorThingsEntityService<?, ?> getEntityService(EntityTypes type) {
        return serviceRepository.getEntityService(type);
    }

    /**
     * Create {@link PageRequest}
     * 
     * @param queryOptions
     *            {@link QueryOptions} to create {@link PageRequest}
     * @return {@link PageRequest} of type {@link OffsetLimitBasedPageRequest}
     */
    protected OffsetLimitBasedPageRequest createPageableRequest(QueryOptions queryOptions) {
        int offset = queryOptions.hasSkipOption() ? queryOptions.getSkipOption().getValue() : 0;
        
        Sort sort = Sort.by(Direction.ASC, "id");
        if (queryOptions.hasOrderByOption()) {
            OrderByItem orderByItem = queryOptions.getOrderByOption().getOrders().get(0);
            try {
                sort = Sort.by(orderByItem.isDescending() ? Direction.DESC : Direction.ASC,
                        orderByItem.getExpression().accept(new ExpressionGenerator(this)));
            } catch (ExpressionVisitException | ODataApplicationException e) {
                // use default sort
            }
        }
        return new OffsetLimitBasedPageRequest(offset, queryOptions.getTopOption().getValue(), sort);
    }

    /**
     * Check property for sorting if different in database
     * 
     * @param property
     *            the sorting property to check
     * @return the databse property name
     */
    public String checkPropertyName(String property) {
        return property;
    }

    /**
     * {@link ExpressionVisitor} to get property name from {@link OrderByOption}
     */
    private static final class ExpressionGenerator implements ExpressionVisitor<String> {

        private AbstractSensorThingsEntityService<?,?> service;

        public ExpressionGenerator(AbstractSensorThingsEntityService<?,?> service) {
            this.service = service;
        }

        @Override
        public String visitLiteral(Literal literal) throws ExpressionVisitException {
            throw new ExpressionVisitException("Lambda expressions are not supported");
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

}
