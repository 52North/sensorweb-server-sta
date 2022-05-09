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



import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.springframework.data.jpa.domain.Specification;

public interface BaseQuerySpecifications<T> {

    /**
     * Creates an equals specification on the {@code name} property.
     *
     * @param value the value to match the {@code name} property
     * @return a specification including the equals expression
     */
    default Specification<T> equalsName(String value) {
        return (root, query, builder) -> {
            String property = HibernateRelations.HasName.PROPERTY_NAME;
            return builder.equal(root.get(property), value);
        };
    }

    /**
     * Creates an equals specification on the {@code staIdentifier}
     * property.
     *
     * @param value the value to match the {@code staIdentifier} property
     * @return a specification including the equals expression
     */
    default Specification<T> equalsStaIdentifier(String value) {
        return (root, query, builder) -> {
            String property = DescribableEntity.PROPERTY_STA_IDENTIFIER;
            return builder.equal(root.get(property), value);
        };
    }

    /**
     * Creates an {@literal IN} specification on the {@code staIdentifier} property.
     * <p>
     * The specification matches all entities whose {@code staIdentifier}
     * is contained in the specified value list.
     *
     * @param values the value candidates matching the {@code staIdentifier} property
     * @return a specification including the {@literal in} expression
     */
    default Specification<T> equalsOneOfStaIdentifiers(String... values) {
        return (root, query, builder) -> {
            if (values == null || values.length == 0) {
                return null;
            }
            String property = DescribableEntity.PROPERTY_IDENTIFIER;
            return builder.in(root.get(property)).value(values);
        };
    }

    Specification<T> compareProperty(String property, ComparisonOperator operator, Expression<?> rightExpr)
            throws SpecificationsException;

    Specification<T> compareProperty(Expression<?> leftExpr, ComparisonOperator operator, String property)
            throws SpecificationsException;

    /**
     * Creates a specification that compares two expressions with the specified
     * operator.
     * <p>
     * Delegates to
     * {@link #compare(Expression, Expression, ComparisonOperator, CriteriaBuilder)}.
     *
     * @param <Y>      the type of the expressions to compare
     * @param left     the left expression
     * @param right    the right expression
     * @param operator the comparison operator
     * @return a specification comparing both expressions
     * @see #compare(Expression, Expression, ComparisonOperator, CriteriaBuilder)
     *      for a list of supported operators
     */
    default <Y extends Comparable<? super Y>> Specification<T> compare(
            Expression<? extends Y> left,
            Expression<? extends Y> right,
            FilterConstants.ComparisonOperator operator) {
        return (root, query, builder) -> compare(left, right, operator, builder);
    }

    /**
     * Creates a predicate that compares two expressions with the specified
     * operator.
     * <p>
     * The following {@link FilterConstants.ComparisonOperator operators} are mapped
     * to SQL:
     * <ul>
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsEqualTo
     * PropertyIsEqualTo }
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsNotEqualTo
     * PropertyIsNotEqualTo}
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsLessThan
     * PropertyIsLessThan}
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsLessThanOrEqualTo
     * PropertyIsLessThanOrEqualTo}
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsGreaterThan
     * PropertyIsGreaterThan}
     * <li>{@link FilterConstants.ComparisonOperator#PropertyIsGreaterThanOrEqualTo
     * PropertyIsGreaterThanOrEqualTo}
     * </ul>
     * <p>
     * The operator {@link FilterConstants.ComparisonOperator#PropertyIsBetween
     * PropertyIsBetween} is not supported and null is returned.
     *
     * @param <Y>      the type of the expressions to compare
     * @param left     the left expression
     * @param right    the right expression
     * @param operator the comparison operator
     * @param builder  the criteria builder
     * @return a predicate comparing both expressions, or null
     */
    default <Y extends Comparable<? super Y>> Predicate compare(
            Expression<? extends Y> left,
            Expression<? extends Y> right,
            FilterConstants.ComparisonOperator operator,
            CriteriaBuilder builder) {
        switch (operator) {
            case PropertyIsEqualTo:
                return builder.equal(left, right);
            case PropertyIsNotEqualTo:
                return builder.notEqual(left, right);
            case PropertyIsLessThan:
                return builder.lessThan(left, right);
            case PropertyIsLessThanOrEqualTo:
                return builder.lessThanOrEqualTo(left, right);
            case PropertyIsGreaterThan:
                return builder.greaterThan(left, right);
            case PropertyIsGreaterThanOrEqualTo:
                return builder.greaterThanOrEqualTo(left, right);
            case PropertyIsBetween:
                // unsupported between
            default:
                return null;
        }
    }

    /**
     * Applies given specification on a member of the root entity.
     * <p>
     * The {@code member} is not checked if available on the root entity.
     * In case it is not available, an exception shall be thrown.
     *
     * @param member     a member of the root entity
     * @param memberSpec the specification to be applied on the member
     * @return a specification including the applied member specification
     * @throws SpecificationsException in case the application fails.
     */
    Specification<T> applyOnMember(String member, Specification<?> memberSpec) throws SpecificationsException;

    /**
     * Creates a query on the specified member of the specified type.
     *
     * @param onMember the member's property
     * @param ofEntity the member's type
     * @return a member query which can be used whithin a {@link Specification}
     */
    default MemberQuery createMemberQuery(String onMember, Class<?> ofEntity) {
        return (specification, query, builder) -> {
            PreparedSubquery<?> subquery = selectOnSubquery(onMember, ofEntity);
            return subquery.where(specification, query, builder);
        };
    }

    /**
     * Selects a property of a specified entity to perform a subquery on.
     *
     * The returned {@link PreparedSubquery} allows to apply a where clause
     * before returning the actual {@link Subquery}.
     *
     * @param <E>        the entity's type of the subquery
     * @param property   the property to select
     * @param entityType the entity type
     * @return a prepared subquery
     */
    @SuppressWarnings("unchecked")
    default <E> PreparedSubquery<E> selectOnSubquery(String property, Class<E> entityType) {
        return (specification, query, builder) -> {
            Subquery<E> subquery = query.subquery(entityType);
            Root<E> member = subquery.from(entityType);

            Specification<E> where = (Specification<E>) specification;
            return subquery.select(member.get(property))
                    .where(where.toPredicate(member, query, builder));
        };
    }

    @FunctionalInterface
    interface MemberFilter<T> {
        Specification<T> apply(Specification<?> memberSpecification);
    }

    @FunctionalInterface
    interface MemberQuery {
        /**
         * Creates a subquery based on dynamic {@link Specification} parameters.
         *
         * @param specification representing the where clause
         * @param query         the criteria query
         * @param builder       the criteria builder
         * @return the member's subquery
         */
        Subquery<?> create(Specification<?> specification, CriteriaQuery<?> query, CriteriaBuilder builder);
    }

    @FunctionalInterface
    interface PreparedSubquery<E> {
        /**
         * Creates a subquery with a where clause applied.
         *
         * @param specification representing the where clause
         * @param query         the criteria query
         * @param builder       the criteria builder
         * @return the subquery
         */
        Subquery<E> where(Specification<?> specification, CriteriaQuery<?> query, CriteriaBuilder builder);
    }

}
