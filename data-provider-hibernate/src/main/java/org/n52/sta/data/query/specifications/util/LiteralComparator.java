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

package org.n52.sta.data.query.specifications.util;

import javax.persistence.criteria.Expression;

import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sta.data.query.specifications.SpecificationsException;
import org.springframework.data.jpa.domain.Specification;

public class LiteralComparator<R, T extends Comparable<? super T>> implements PropertyComparator<R, T> {

    private final String literal;

    public LiteralComparator(String literal) {
        this.literal = literal;
    }

    /**
     * Creates a specification that compares the specified right expression to a literal value
     *
     * @param rightExpr the value to be compared
     * @param operator  the comparison operator
     * @return a specification comparing entity path and right expression
     * @throws SpecificationsException if comparison fails
     */
    @Override
    public Specification<R> compareToRight(Expression<?> rightExpr, ComparisonOperator operator) {
        return compare(rightExpr, operator);
    }

    /**
     * Creates a specification that compares the specified left expression to a literal value
     *
     * @param leftExpr: the expression to compare to
     * @param operator  the comparison operator
     * @return a specification comparing entity path and left expression
     * @throws SpecificationsException if comparison fails
     */
    @Override
    public Specification<R> compareToLeft(Expression<?> leftExpr, ComparisonOperator operator) {
        return compare(leftExpr, operator);
    }

    private Specification<R> compare(Expression<?> expr, ComparisonOperator operator) {
        return (root, query, builder) -> {
            switch (operator) {
            case PropertyIsEqualTo:
                return builder.equal(expr, literal);
            case PropertyIsNotEqualTo:
                return builder.notEqual(expr, literal);
            default:
                throw new SpecificationsException("Unsupported comparison operator: '" + operator + "'");
            }
        };
    }

}



