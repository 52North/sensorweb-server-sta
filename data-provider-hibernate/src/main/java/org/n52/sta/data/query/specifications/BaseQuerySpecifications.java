/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.data.query.specifications;

import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.springframework.data.jpa.domain.Specification;

public interface BaseQuerySpecifications<T> {

    /**
     * Filters out Database Entities that are not valid STA Entities. e.g. Datasets in state: not_initialized
     * Defaults to not filter out any entities
     */
    default Optional<Specification<T>> isStaEntity() {
        return Optional.empty();
    }

    /**
     * Creates an equals specification on the {@code name} property.
     *
     * @param value
     *        the value to match the {@code name} property
     * @return a specification including the equals expression
     */
    Specification<T> equalsName(String value);

    /**
     * Creates an equals specification on the {@code staIdentifier} property.
     *
     * @param value
     *        the value to match the {@code staIdentifier} property
     * @return a specification including the equals expression
     */
    Specification<T> equalsStaIdentifier(String value);

    /**
     * Creates an {@literal IN} specification on the {@code staIdentifier} property.
     * <p>
     * The specification matches all entities whose {@code staIdentifier} is contained in the specified value
     * list.
     *
     * @param values
     *        the value candidates matching the {@code staIdentifier} property
     * @return a specification including the {@literal in} expression
     */
    Specification<T> equalsOneOfStaIdentifiers(String... values);

    Specification<T> compareProperty(String property, ComparisonOperator operator, Expression< ? > rightExpr)
            throws SpecificationsException;

    Specification<T> compareProperty(Expression< ? > leftExpr, ComparisonOperator operator, String property)
            throws SpecificationsException;

    /**
     * Creates a specification that compares two expressions with the specified operator.
     * <p>
     * Delegates to {@link #compare(Expression, Expression, ComparisonOperator, CriteriaBuilder)}.
     *
     * @param <Y>
     *        the type of the expressions to compare
     * @param left
     *        the left expression
     * @param right
     *        the right expression
     * @param operator
     *        the comparison operator
     * @return a specification comparing both expressions
     * @see #compare(Expression, Expression, ComparisonOperator, CriteriaBuilder) for a list of supported
     *      operators
     */
    <Y extends Comparable< ? super Y>> Specification<T> compare(
                                                                Expression< ? extends Y> left,
                                                                Expression< ? extends Y> right,
                                                                FilterConstants.ComparisonOperator operator);

    /**
     * Creates a predicate that compares two expressions with the specified operator.
     * <p>
     * The following {@link FilterConstants.ComparisonOperator operators} are mapped to SQL:
     * <ul>
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsEqualTo PropertyIsEqualTo }
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsNotEqualTo PropertyIsNotEqualTo}
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsLessThan PropertyIsLessThan}
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsLessThanOrEqualTo PropertyIsLessThanOrEqualTo}
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsGreaterThan PropertyIsGreaterThan}
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsGreaterThanOrEqualTo
     * PropertyIsGreaterThanOrEqualTo}
     * </ul>
     * <p>
     * The operator {@link FilterConstants.ComparisonOperator#PropertyIsBetween PropertyIsBetween} is not
     * supported and null is returned.
     *
     * @param <Y>
     *        the type of the expressions to compare
     * @param left
     *        the left expression
     * @param right
     *        the right expression
     * @param operator
     *        the comparison operator
     * @param builder
     *        the criteria builder
     * @return a predicate comparing both expressions, or null
     */
    <Y extends Comparable< ? super Y>> Predicate compare(
                                                         Expression< ? extends Y> left,
                                                         Expression< ? extends Y> right,
                                                         FilterConstants.ComparisonOperator operator,
                                                         CriteriaBuilder builder);

    /**
     * Applies given specification on a member of the root entity.
     * <p>
     * The {@code member} is not checked if available on the root entity. In case it is not available, an
     * exception shall be thrown.
     *
     * @param member
     *        a member of the root entity
     * @param memberSpec
     *        the specification to be applied on the member
     * @return a specification including the applied member specification
     * @throws SpecificationsException
     *         in case the application fails.
     */
    Specification<T> applyOnMember(String member, Specification< ? > memberSpec) throws SpecificationsException;

    /**
     * Creates a query on the specified member of the specified type.
     *
     * @param onMember
     *        the member's property
     * @param ofEntity
     *        the member's type
     * @return a member query which can be used whithin a {@link Specification}
     */
    EntityQuery createQuery(String onMember, Class< ? > ofEntity);

    /**
     * Selects a property of a specified entity to perform a subquery on. The returned
     * {@link PreparedSubquery} allows to apply a where clause before returning the actual {@link Subquery}.
     *
     * @param <E>
     *        the entity's type of the subquery
     * @param property
     *        the property to select
     * @param entityType
     *        the entity type
     * @return a prepared subquery
     */
    <E> PreparedSubquery<E> selectOnSubquery(String property, Class<E> entityType);

    @FunctionalInterface
    interface MemberFilter<T> {

        Specification<T> apply(Specification< ? > memberSpecification);
    }

    // TODO get rid of this impl class?
    abstract class MemberFilterImpl<T> implements MemberFilter<T> {

        public Specification<T> apply(Specification< ? > specification) {
            return prepareQuery(specification);
        }

        protected abstract Specification<T> prepareQuery(Specification< ? > specification);

    }

    @FunctionalInterface
    interface EntityQuery {

        /**
         * Creates a subquery based on dynamic {@link Specification} parameters.
         *
         * @param specification
         *        representing the where clause
         * @param query
         *        the criteria query
         * @param builder
         *        the criteria builder
         * @return the member's subquery
         */
        Subquery< ? > create(Specification< ? > specification, CriteriaQuery< ? > query, CriteriaBuilder builder);
    }

    @FunctionalInterface
    interface PreparedSubquery<E> {

        /**
         * Creates a subquery with a where clause applied.
         *
         * @param specification
         *        representing the where clause
         * @param query
         *        the criteria query
         * @param builder
         *        the criteria builder
         * @return the subquery
         */
        Subquery<E> where(Specification< ? > specification, CriteriaQuery< ? > query, CriteriaBuilder builder);
    }

}
