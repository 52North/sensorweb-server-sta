/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data.service;

import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
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
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interface for requesting Sensor Things entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractSensorThingsEntityService<T extends JpaRepository<?, ?>> {

    private T repository;

    public AbstractSensorThingsEntityService(T repository) {
        this.repository = repository;
    }

    /**
     * Requests the full EntityCollection
     * 
     * @param queryOptions
     *
     * @return the full EntityCollection
     */
    public abstract EntityCollection getEntityCollection(QueryOptions queryOptions);

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
     */
    public abstract EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType,
            QueryOptions queryOptions);

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
    public abstract boolean existsEntity(Long id);

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
     */
    public long getCount() {
        return getRepository().count();
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
    protected String checkPropertyForSorting(String property) {
        return property;
    }

    /**
     * {@link ExpressionVisitor} to get property name from {@link OrderByOption}
     */
    private static final class ExpressionGenerator implements ExpressionVisitor<String> {

        private AbstractSensorThingsEntityService<?> service;

        public ExpressionGenerator(AbstractSensorThingsEntityService<?> service) {
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
            return service.checkPropertyForSorting(visitMember(member.getResourcePath()));
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
