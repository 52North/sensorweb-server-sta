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

package org.n52.sta.data.query;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterConstants.BinaryLogicOperator;
import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sta.data.query.specifications.BaseQuerySpecifications;
import org.n52.svalbard.odata.core.expr.Expr;
import org.n52.svalbard.odata.core.expr.ExprVisitor;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;
import org.n52.svalbard.odata.core.expr.MemberExpr;
import org.n52.svalbard.odata.core.expr.MethodCallExpr;
import org.n52.svalbard.odata.core.expr.StringValueExpr;
import org.n52.svalbard.odata.core.expr.arithmetic.NumericValueExpr;
import org.n52.svalbard.odata.core.expr.arithmetic.SimpleArithmeticExpr;
import org.n52.svalbard.odata.core.expr.bool.BooleanBinaryExpr;
import org.n52.svalbard.odata.core.expr.bool.BooleanExpr;
import org.n52.svalbard.odata.core.expr.bool.BooleanUnaryExpr;
import org.n52.svalbard.odata.core.expr.bool.ComparisonExpr;
import org.n52.svalbard.odata.core.expr.temporal.TimeValueExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Optional;
import java.util.function.Function;

public final class FilterQueryParser<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterQueryParser.class);

    public static <E> Specification<E> parse(QueryOptions options,
                                             BaseQuerySpecifications<E> specs) {
        return (root, query, builder) -> {
            FilterQueryRoot<E> filterRoot = new FilterQueryRoot<>(root, query, specs);
            FilterQueryVisitor<E> visitor = new FilterQueryVisitor<>(filterRoot, builder);
            Optional<Specification<E>> defaultPredicate = specs.isStaEntity();

            Optional<Predicate> filterPredicate = Optional.ofNullable(options.getFilterFilter())
                                                          .map(filter -> (Expr) filter.getFilter())
                                                          .map(visitExpression(visitor));

            if (filterPredicate.isPresent() && defaultPredicate.isPresent()) {
                return builder.and(defaultPredicate.get()
                                                   .toPredicate(root, query, builder),
                                   filterPredicate.get());
            } else if (defaultPredicate.isPresent()) {
                return defaultPredicate.get()
                                       .toPredicate(root, query, builder);
            } else {
                return filterPredicate.orElse(null);
            }
        };
    }

    private static <E> Function<Expr, Predicate> visitExpression(FilterQueryVisitor<E> visitor) {
        return expression -> {
            try {
                return (Predicate) expression.accept(visitor);
            } catch (Throwable e) {
                String filter = expression.toODataString();
                LOGGER.debug("Could not create predicate from expression: '" + filter, e);
                throw new RuntimeException("Invalid filter expression!");
            }
        };
    }

    private static final class FilterQueryVisitor<T> implements ExprVisitor<Expression< ? >, FilterQueryException> {

        private final Root<T> root;
        private final CriteriaQuery< ? > query;
        private final CriteriaBuilder criteriaBuilder;
        private final BaseQuerySpecifications<T> rootSpecification;
        private final QuerySpecificationFactory qsFactory;

        private FilterQueryVisitor(FilterQueryRoot<T> filterQueryRoot, CriteriaBuilder criteriaBuilder) {
            this.root = filterQueryRoot.getRoot();
            this.query = filterQueryRoot.getCriteriaQuery();
            this.rootSpecification = filterQueryRoot.getRootSpecifications();
            this.criteriaBuilder = criteriaBuilder;
            this.qsFactory = new QuerySpecificationFactory();
        }

        @Override
        public Predicate visitBooleanBinary(BooleanBinaryExpr expr) throws FilterQueryException {
            Predicate left = (Predicate) expr.getLeft()
                                             .accept(this);
            Predicate right = (Predicate) expr.getRight()
                                              .accept(this);
            BinaryLogicOperator operator = expr.getOperator();
            return operator.equals(BinaryLogicOperator.And)
                    ? criteriaBuilder.and(left, right)
                    : criteriaBuilder.or(left, right);
        }

        @Override
        public Predicate visitBooleanUnary(BooleanUnaryExpr expr) throws FilterQueryException {
            BooleanExpr operand = expr.getOperand();
            Expression< ? > result = operand.accept(this);
            return criteriaBuilder.not((Predicate) result);
        }

        @Override
        public Predicate visitComparison(ComparisonExpr expr) throws FilterQueryException {

            Expr left = expr.getLeft();
            Expr right = expr.getRight();
            ComparisonOperator operator = expr.getOperator();
            return left.isMember() || right.isMember()
                    ? compareMember(left, right, operator)
                    : compareNonMembers(left, right, operator);
        }

        private Predicate compareMember(Expr left, Expr right, ComparisonOperator operator)
                throws FilterQueryException {
            if (right.isMember()) {
                String member = toMember(right);
                if (isOnRoot(member)) {
                    return compareMemberOnRight(left, operator, member);
                } else {
                    // TODO
                    return null;
                }
            } else {
                String member = toMember(left);
                if (isOnRoot(member)) {
                    return compareMemberOnLeft(member, operator, right);
                } else {
                    // TODO
                    return null;
                }
            }
        }

        private Predicate compareMemberOnLeft(String member,
                                              ComparisonOperator operator,
                                              Expr right)
                throws FilterQueryException {
            Expression< ? > rightExpr = right.accept(this);
            return rootSpecification.compareProperty(member, operator, rightExpr)
                                    .toPredicate(root, query, criteriaBuilder);
        }

        private Predicate compareMemberOnRight(Expr left, ComparisonOperator operator, String member)
                throws FilterQueryException {
            Expression< ? > leftExpr = left.accept(this);
            return rootSpecification.compareProperty(leftExpr, operator, member)
                                    .toPredicate(root, query, criteriaBuilder);
        }

        @SuppressWarnings("unchecked")
        private <Y extends Comparable< ? super Y>> Predicate compareNonMembers(Expr left,
                                                                               Expr right,
                                                                               ComparisonOperator operator)
                throws FilterQueryException {
            Expression< ? extends Y> leftExpr = (Expression< ? extends Y>) left.accept(this);
            Expression< ? extends Y> rightExpr = (Expression< ? extends Y>) right.accept(this);
            return rootSpecification.compare(leftExpr, rightExpr, operator, criteriaBuilder);
        }

        @Override
        public Predicate visitMethodCall(MethodCallExpr expr) throws FilterQueryException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Predicate visitMember(MemberExpr expr) throws FilterQueryException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Predicate visitString(StringValueExpr expr) throws FilterQueryException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Predicate visitSimpleArithmetic(SimpleArithmeticExpr expr) throws FilterQueryException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Predicate visitTime(TimeValueExpr expr) throws FilterQueryException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Predicate visitGeometry(GeoValueExpr expr) throws FilterQueryException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Predicate visitNumeric(NumericValueExpr expr) throws FilterQueryException {
            // TODO Auto-generated method stub
            return null;
        }

        private boolean isOnRoot(String member) {
            return !isPath(member);
        }

        private boolean isPath(String member) {
            return member.contains("/");
        }

        private String toMember(Expr right) throws FilterQueryException {
            return right.asMember()
                        .map(MemberExpr::getValue)
                        .orElseThrow(() -> new FilterQueryException("no member found!"));
        }

    }

}
