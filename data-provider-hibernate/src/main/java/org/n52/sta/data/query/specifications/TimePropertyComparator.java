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

import javax.persistence.criteria.Expression;

import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings("checkstyle:MultipleStringLiterals")
class TimePropertyComparator<R, T extends Comparable< ? super T>> implements PropertyComparator<R, T> {

    private String startProperty;
    private String endProperty;

    TimePropertyComparator(String startProperty, String endProperty) {
        this.startProperty = startProperty;
        this.endProperty = endProperty;
    }

    /**
     * Creates a specification that compares the specified right expression to the instance's entity path by
     * using given operator.
     *
     * @param rightExpr
     *        the value to be compared
     * @param operator
     *        the comparison operator
     * @return a specification comparing entity path and right expression
     * @throws SpecificationsException
     *         if comparison fails
     * @throws ClassCastException
     *         if the comparison types do not match
     */
    @Override
    public Specification<R> compareToRight(Expression< ? > rightExpr, ComparisonOperator operator) {
        return (root, query, builder) -> {
            Expression<T> right = (Expression<T>) rightExpr;
            Expression<T> startPath = root.get(startProperty);
            Expression<T> endPath = root.get(endProperty);
            switch (operator) {
                case PropertyIsEqualTo:
                    return builder.and(
                                       builder.equal(right, endPath),
                                       builder.equal(right, startPath));
                case PropertyIsNotEqualTo:
                    return builder.not(builder.and(
                                                   builder.equal(right, endPath),
                                                   builder.equal(right, startPath)));
                case PropertyIsLessThan:
                    return builder.lessThan(startPath, right);
                case PropertyIsLessThanOrEqualTo:
                    return builder.lessThanOrEqualTo(startPath, right);
                case PropertyIsGreaterThan:
                    return builder.greaterThan(endPath, right);
                case PropertyIsGreaterThanOrEqualTo:
                    return builder.greaterThanOrEqualTo(endPath, right);
                case PropertyIsBetween:
                    // unsupported between
                default:
                    throw new SpecificationsException("Unsupported comparison operator: '" + operator + "'");
            }
        };
    }

    /**
     * Creates a specification that compares the specified left expression to the instance's entity path by
     * using given operator. e.g. 2020-12-03T23:00:00.000Z/2022-06-30T21:59:59.000Z gt phenomenonTime
     *
     * @param leftExpr
     *        the expression to compare to
     * @param operator
     *        the comparison operator
     * @return a specification comparing entity path and left expression
     * @throws SpecificationsException
     *         if comparison fails
     * @throws ClassCastException
     *         if the comparison types do not match
     */
    @Override
    public Specification<R> compareToLeft(Expression< ? > leftExpr, ComparisonOperator operator) {
        return (root, query, builder) -> {
            Expression<T> left = (Expression<T>) leftExpr;
            Expression<T> startPath = root.get(startProperty);
            Expression<T> endPath = root.get(endProperty);
            switch (operator) {
                case PropertyIsEqualTo:
                    return builder.and(
                                       builder.equal(left, endPath),
                                       builder.equal(left, startPath));
                case PropertyIsNotEqualTo:
                    return builder.not(builder.and(
                                                   builder.equal(left, endPath),
                                                   builder.equal(left, startPath)));
                case PropertyIsLessThan:
                    return builder.lessThan(left, startPath);
                case PropertyIsLessThanOrEqualTo:
                    return builder.lessThanOrEqualTo(left, startPath);
                case PropertyIsGreaterThan:
                    return builder.greaterThan(left, endPath);
                case PropertyIsGreaterThanOrEqualTo:
                    return builder.greaterThanOrEqualTo(left, endPath);
                case PropertyIsBetween:
                    // unsupported between
                default:
                    throw new SpecificationsException("Unsupported comparison operator: '" + operator + "'");
            }
        };
    }

}
