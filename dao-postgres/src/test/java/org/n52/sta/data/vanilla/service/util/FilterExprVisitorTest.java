/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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
 *     - Apache License, Version 2.0
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
package org.n52.sta.data.vanilla.service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.svalbard.odata.core.expr.Expr;
import org.n52.svalbard.odata.core.expr.MemberExpr;
import org.n52.svalbard.odata.core.expr.StringValueExpr;
import org.n52.svalbard.odata.core.expr.arithmetic.NumericValueExpr;
import org.n52.svalbard.odata.core.expr.bool.ComparisonExpr;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that filtering by an Entity's id works with numeric literals
 * (e.g. {@code Datastream/id eq 12}), not only with quoted ones
 * (e.g. {@code Datastream/id eq '12'}), as identifiers are stored as Strings.
 */
public class FilterExprVisitorTest {

    private HibernateSpatialCriteriaBuilderImpl builder;
    private CriteriaQuery query;
    private Predicate predicate;

    @BeforeEach
    public void setUp() {
        builder = mock(HibernateSpatialCriteriaBuilderImpl.class);
        query = mock(CriteriaQuery.class);
        predicate = mock(Predicate.class);

        // literal() returns an Expression reporting the type of the given value,
        // mirroring what Hibernate's CriteriaBuilder does for literals.
        when(builder.literal(any())).thenAnswer(invocation -> {
            Object value = invocation.getArgument(0);
            Expression expression = mock(Expression.class);
            doReturn(value.getClass()).when(expression).getJavaType();
            return expression;
        });
        when(builder.equal(any(Expression.class), any(Expression.class))).thenReturn(predicate);
        when(builder.notEqual(any(Expression.class), any(Expression.class))).thenReturn(predicate);

        CriteriaBuilder.In in = mock(CriteriaBuilder.In.class);
        when(in.value(any(Expression.class))).thenReturn(in);
        when(builder.in(any(Expression.class))).thenReturn(in);
    }

    @Test
    public void filterByRelatedEntityIdWithNumericLiteral() throws Exception {
        FilterExprVisitor<DataEntity<?>> visitor = createObservationVisitor();
        ComparisonExpr filter = comparison(FilterConstants.ComparisonOperator.PropertyIsEqualTo,
                                           "Datastream/id",
                                           new NumericValueExpr(12L));

        Predicate result = assertDoesNotThrow(() -> (Predicate) filter.accept(visitor));

        assertNotNull(result);
        // The numeric literal has to be converted to its String representation,
        // as identifiers are stored as Strings.
        verify(builder).literal("12");
    }

    @Test
    public void filterByRelatedEntityIdWithNumericLiteralAndNotEqual() throws Exception {
        FilterExprVisitor<DataEntity<?>> visitor = createObservationVisitor();
        ComparisonExpr filter = comparison(FilterConstants.ComparisonOperator.PropertyIsNotEqualTo,
                                           "Datastream/id",
                                           new NumericValueExpr(12L));

        Predicate result = assertDoesNotThrow(() -> (Predicate) filter.accept(visitor));

        assertNotNull(result);
        verify(builder).literal("12");
    }

    @Test
    public void filterByRelatedEntityIdWithQuotedLiteral() throws Exception {
        FilterExprVisitor<DataEntity<?>> visitor = createObservationVisitor();
        ComparisonExpr filter = comparison(FilterConstants.ComparisonOperator.PropertyIsEqualTo,
                                           "Datastream/id",
                                           new StringValueExpr("12"));

        Predicate result = assertDoesNotThrow(() -> (Predicate) filter.accept(visitor));

        assertNotNull(result);
    }

    @Test
    public void filterByEntityIdWithNumericLiteral() throws Exception {
        Root root = createRoot(AbstractDatasetEntity.class);
        FilterExprVisitor<AbstractDatasetEntity> visitor = new FilterExprVisitor<>(root, query, builder);
        ComparisonExpr filter = comparison(FilterConstants.ComparisonOperator.PropertyIsEqualTo,
                                           "id",
                                           new NumericValueExpr(12L));

        Predicate result = assertDoesNotThrow(() -> (Predicate) filter.accept(visitor));

        assertNotNull(result);
        verify(builder).literal("12");
    }

    @Test
    public void filterByEntityIdWithQuotedLiteral() throws Exception {
        Root root = createRoot(AbstractDatasetEntity.class);
        FilterExprVisitor<AbstractDatasetEntity> visitor = new FilterExprVisitor<>(root, query, builder);
        ComparisonExpr filter = comparison(FilterConstants.ComparisonOperator.PropertyIsEqualTo,
                                           "id",
                                           new StringValueExpr("12"));

        Predicate result = assertDoesNotThrow(() -> (Predicate) filter.accept(visitor));

        assertNotNull(result);
    }

    @Test
    public void filterByNameWithNumericLiteralIsStillRejected() throws Exception {
        Root root = createRoot(AbstractDatasetEntity.class);
        FilterExprVisitor<AbstractDatasetEntity> visitor = new FilterExprVisitor<>(root, query, builder);
        ComparisonExpr filter = comparison(FilterConstants.ComparisonOperator.PropertyIsEqualTo,
                                           "name",
                                           new NumericValueExpr(12L));

        // Only identifiers accept numeric literals; other String properties keep
        // rejecting them as a datatype mismatch.
        assertThrows(RuntimeException.class, () -> filter.accept(visitor));
    }

    private ComparisonExpr comparison(FilterConstants.ComparisonOperator operator, String member, Expr value) {
        return new ComparisonExpr(operator, new MemberExpr(member), value);
    }

    private FilterExprVisitor<DataEntity<?>> createObservationVisitor() throws STAInvalidFilterExpressionException {
        Root root = createRoot(DataEntity.class);
        Subquery subquery = mock(Subquery.class);
        Root datastreamRoot = createRoot(AbstractDatasetEntity.class);
        doReturn(subquery).when(query).subquery(DatasetEntity.class);
        doReturn(datastreamRoot).when(subquery).from(AbstractDatasetEntity.class);
        doReturn(subquery).when(subquery).select(any(Expression.class));
        return new FilterExprVisitor<>(root, query, builder);
    }

    @SuppressWarnings("unchecked")
    private Root createRoot(Class<?> entityClass) {
        Root root = mock(Root.class);
        doReturn(entityClass).when(root).getJavaType();
        Path path = mock(Path.class);
        when(root.get(anyString())).thenReturn(path);
        return root;
    }
}
