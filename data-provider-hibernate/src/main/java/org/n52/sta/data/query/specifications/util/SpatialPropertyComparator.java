/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation. If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked library is not considered a
 * "derivative work" of the program: - Apache License, version 2.0 - Apache Software License, version 1.0 -
 * GNU Lesser General Public License, version 3 - Mozilla Public License, versions 1.0, 1.1 and 2.0 - Common
 * Development and Distribution License (CDDL), version 1.0 Therefore the distribution of the program linked
 * with libraries licensed under the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2 and the aforementioned
 * licenses. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
//
// package org.n52.sta.data.query.specifications.util;
//
// import javax.persistence.criteria.CriteriaBuilder;
// import javax.persistence.criteria.Expression;
// import javax.persistence.criteria.Path;
// import javax.persistence.criteria.Predicate;
// import javax.persistence.criteria.Root;
//
// import org.hibernate.spatial.SpatialAggregate;
// import org.hibernate.spatial.SpatialFunction;
// import org.hibernate.spatial.criterion.SpatialProjections;
// import org.hibernate.spatial.predicate.SpatialPredicates;
// import org.locationtech.jts.geom.Geometry;
// import org.locationtech.jts.io.ParseException;
// import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
// import org.n52.shetland.util.JTSHelper;
// import org.n52.sta.data.query.specifications.SpecificationsException;
// import org.springframework.data.jpa.domain.Specification;
//
// public class SpatialPropertyComparator<R, T extends Comparable<? super T>> implements PropertyComparator<R,
// T> {
//
// protected final String entityPath;
//
// public SpatialPropertyComparator(String entityPath) {
// this.entityPath = entityPath;
// }
//
// /**
// * Get a Path to the specified property. May be overwritten if processing of the Root entity (e.g. joining)
// is
// * needed to construct the path.
// *
// * @param root Root
// * @return Path to this.entityPath
// */
// protected Path<T> getPath(Root<R> root) {
// return root.get(entityPath);
// }
//
// /**
// * Creates a specification that compares the specified right expression to the instance's entity path by
// * using given operator.
// *
// * @param right the entity's property
// * @param operator the comparison operator
// * @return a specification comparing entity path and right expression
// * @throws SpecificationsException if comparison fails
// * @throws ClassCastException if the comparison types do not match
// * @see #compare(Expression, ComparisonOperator, Expression, CriteriaBuilder) for a list of supported
// * operators
// */
// @Override
// public Specification<R> compareToRight(Expression<?> right, ComparisonOperator operator) {
// return (root, query, builder) -> {
// Path<T> selectPath = getPath(root);
// Expression<T> rightExpr = castToComparable(right);
// return compare(selectPath, operator, rightExpr, builder);
// };
// }
//
// /**
// * Creates a specification that compares the specified left expression to the instance's entity path by
// * using given operator.
// *
// * @param left the entity's property
// * @param operator the comparison operator
// * @return a specification comparing entity path and left expression
// * @throws SpecificationsException if comparison fails
// * @throws ClassCastException if the comparison types do not match
// * @see #compare(Expression, ComparisonOperator, Expression, CriteriaBuilder) for a list of supported
// * operators
// */
// @Override
// public Specification<R> compareToLeft(Expression<?> left, ComparisonOperator operator) {
// return (root, query, builder) -> {
// Path<T> selectPath = getPath(root);
// Geometry leftExpr = wktToGeometry("wkt");
// return compare(leftExpr, operator, selectPath, builder);
// };
// }
//
// /**
// * Creates a specification that compares two expressions with the specified operator.
// * <p>
// * Delegates to {@link #compare(Expression, ComparisonOperator, Expression, CriteriaBuilder)}.
// *
// * @param left the left expression
// * @param operator the comparison operator
// * @param right the right expression
// * @return a specification comparing both expressions
// * @throws ClassCastException if the comparison types do not match
// * @see #compare(Expression, ComparisonOperator, Expression, CriteriaBuilder) for a list of supported
// * operators
// */
// private Specification<T> compare(Expression<?> left,
// ComparisonOperator operator,
// Expression<?> right) {
// return (root, query, builder) -> compare(left, operator, right, builder);
// }
//
// /**
// * Creates a predicate that compares two expressions with the specified operator.
// * <p>
// * The following {@link ComparisonOperator operators} are mapped to SQL:
// * <ul>
// * <li>{@link ComparisonOperator#PropertyIsEqualTo PropertyIsEqualTo }
// * <li>{@link ComparisonOperator#PropertyIsNotEqualTo PropertyIsNotEqualTo}
// * <li>{@link ComparisonOperator#PropertyIsLessThan PropertyIsLessThan}
// * <li>{@link ComparisonOperator#PropertyIsLessThanOrEqualTo PropertyIsLessThanOrEqualTo}
// * <li>{@link ComparisonOperator#PropertyIsGreaterThan PropertyIsGreaterThan}
// * <li>{@link ComparisonOperator#PropertyIsGreaterThanOrEqualTo PropertyIsGreaterThanOrEqualTo}
// * </ul>
// * <p>
// * Throws in case of unsupported operation, for example {@link ComparisonOperator#PropertyIsBetween
// * PropertyIsBetween}.
// *
// * @param <Y> the type of the expressions to compare
// * @param leftExpr the left expression
// * @param operator the comparison operator
// * @param rightExpr the right expression
// * @param builder the criteria builder
// * @return a predicate comparing both expressions, or null
// * @throws SpecificationsException if operator is not supported
// * @throws ClassCastException if the comparison types do not match
// */
// private <Y extends Geometry> Predicate compare(Expression<?> leftExpr,
// ComparisonOperator operator,
// Expression<?> rightExpr,
// CriteriaBuilder builder)
// throws SpecificationsException {
// /*
// Expression<Y> left = castToComparable(leftExpr);
// Expression<Y> right = castToComparable(rightExpr);
// switch (operator) {
// case PropertyIsEqualTo:
// return SpatialPredicates.contains(builder, right, left);
// return builder.equal(left, right);
// case PropertyIsNotEqualTo:
// return builder.notEqual(left, right);
// case PropertyIsLessThan:
// return builder.lessThan(left, right);
// case PropertyIsLessThanOrEqualTo:
// return builder.lessThanOrEqualTo(left, right);
// case PropertyIsGreaterThan:
// return builder.greaterThan(left, right);
// case PropertyIsGreaterThanOrEqualTo:
// return builder.greaterThanOrEqualTo(left, right);
// case PropertyIsBetween:
// // unsupported between
// default:
// throw new SpecificationsException("Unsupported comparison operator: '" + operator + "'");
// }*/
// throw new SpecificationsException("Unsupported comparison operator: '" + operator + "'");
// }
//
// private Geometry wktToGeometry(String wkt) {
// try {
// return JTSHelper.createGeometryFromWKT(wkt, 4326);
// } catch (ParseException e) {
// throw new RuntimeException(e);
// }
// }
// }
