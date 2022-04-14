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
package org.n52.sta.data.support;

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Extension of Hibernate CriteriaBuilder API Implementation to include Spatial functions.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class HibernateSpatialCriteriaBuilderImpl extends CriteriaBuilderImpl
    implements HibernateSpatialCriteriaBuilder {

    private static final String GEOMETRY = "geometry";
    private static final String GEOGRAPHY = "geography";
    private static final String ST_LENGTH = "ST_LENGTH";
    private static final String ST_DISTANCE = "ST_DISTANCE";

    public HibernateSpatialCriteriaBuilderImpl(CriteriaBuilderImpl hibernateCriteriaBuilder) {
        super(hibernateCriteriaBuilder.getEntityManagerFactory());
    }

    // st_equals(location, geography'POINT (30 10)')
    @Override public Predicate st_equals(Expression<Geometry> x, String wktWithType) {
        return defaultSTMethodCallGeometry("ST_EQUALS", x, wktWithType);
    }

    @Override public Predicate st_disjoint(Expression<Geometry> x, String wktWithType) {
        return defaultSTMethodCallGeometry("ST_DISJOINT", x, wktWithType);
    }

    @Override public Predicate st_touches(Expression<Geometry> x, String wktWithType) {
        return defaultSTMethodCallGeometry("ST_TOUCHES", x, wktWithType);
    }

    @Override public Predicate st_within(Expression<Geometry> x, String wktWithType) {
        return defaultSTMethodCallGeometry("ST_WITHIN", x, wktWithType);
    }

    @Override public Predicate st_overlaps(Expression<Geometry> x, String wktWithType) {
        return defaultSTMethodCallGeometry("ST_OVERLAPS", x, wktWithType);
    }

    @Override public Predicate st_crosses(Expression<Geometry> x, String wktWithType) {
        return defaultSTMethodCallGeometry("ST_CROSSES", x, wktWithType);
    }

    @Override public Predicate st_contains(Expression<Geometry> x, String wktWithType) {
        return defaultSTMethodCallGeometry("ST_CONTAINS", x, wktWithType);
    }

    @Override public Predicate st_intersects(Expression<String> x, String wktWithType) {
        return this.isTrue(
            this.function(
                "ST_Intersects",
                Boolean.class,
                this.function(GEOGRAPHY, Geometry.class, x),
                geographyfromWKT(extractWKT(wktWithType))
            )
        );
    }

    @Override public Predicate st_relate(Expression<Geometry> x, String wktWithType, String mask) {
        return this.isTrue(
            this.function(
                "ST_RELATE",
                Boolean.class,
                this.function(GEOMETRY, Geometry.class, x),
                geometryfromWKT(extractWKT(wktWithType)),
                this.literal(mask)
            )
        );
    }

    @Override public Expression<Float> st_length(Expression<Geometry> x) {
        return this.function(
            ST_LENGTH,
            Float.class,
            this.function(GEOGRAPHY, Geometry.class, x)
        );
    }

    @Override public Expression<Float> st_length(String wkt) {
        return this.function(
            ST_LENGTH,
            Float.class,
            geographyfromWKT(extractWKT(wkt))
        );
    }

    @Override public Expression<Float> st_distance(Expression<Geometry> x, String wkt) {
        return this.function(
            ST_DISTANCE,
            Float.class,
            this.function(GEOGRAPHY, Geometry.class, x),
            geographyfromWKT(extractWKT(wkt))
        );
    }

    @Override public Expression<Float> st_distance(String x, String wkt) {
        return this.function(
            ST_DISTANCE,
            Float.class,
            geographyfromWKT(extractWKT(x)),
            geographyfromWKT(extractWKT(wkt))
        );
    }

    private Predicate defaultSTMethodCallGeometry(String methodName, Expression<Geometry> x, String wktWithType) {
        return this.isTrue(
            this.function(
                methodName,
                Boolean.class,
                this.function(GEOMETRY, Geometry.class, x),
                geometryfromWKT(extractWKT(wktWithType))
            )
        );
    }

    private Expression<Geometry> geometryfromWKT(String wkt) {
        return this.function(
            "ST_GeomFromText",
            Geometry.class,
            this.literal("SRID=4326;" + wkt)
        );
    }

    private Expression<Geometry> geographyfromWKT(String wkt) {
        return this.function(
            "ST_GeographyFromText",
            Geometry.class,
            this.literal(wkt)
        );
    }

    private String extractWKT(String wktWithType) {
        return wktWithType.substring(wktWithType.indexOf("'") + 1, wktWithType.length() - 1);
    }

}
