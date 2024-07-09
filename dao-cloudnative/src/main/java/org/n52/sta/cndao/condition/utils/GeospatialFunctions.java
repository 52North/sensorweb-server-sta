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
package org.n52.sta.cndao.condition.utils;


import org.jooq.*;
import org.jooq.impl.DSL;
import org.locationtech.jts.geom.Geometry;


public class GeospatialFunctions {

    // Assuming that these are the SQL functions for the respective spatial operations
    public static Field<Double> st_distance(Field<?> geom, String wkt) {
        return DSL.field(
                "ST_Distance({0}, {1})",
                Double.class,
                geom, // check HibernateSpatialCriteriaBuilder; it calls GEOGRAPHY function on this
                geographyfromWKT(extractWKT(wkt))
        );
    }

    public static Field<Double> st_distance(String geom, String wkt) {
        return DSL.field(
                "ST_Distance({0}, {1})",
                Double.class,
                geographyfromWKT(extractWKT(geom)),
                geographyfromWKT(extractWKT(wkt))
        );
    }

    public static Field<Double> st_length(Field<?> geom) {
        return DSL.field(
                "ST_Length({0})",
                Double.class,
                geom // check HibernateSpatialCriteriaBuilder; it calls GEOGRAPHY function on this
        );
    }
    public static Field<Double> st_length(String wkt) {
        return DSL.field(
                "ST_Length({0})",
                Double.class,
                geographyfromWKT(extractWKT(wkt))
        );
    }

    public static Field<?> st_equals(Field<?> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Equals({0}, {1})", geom, wkt);
    }

    public static Field<?> st_disjoint(Field<?> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Disjoint({0}, {1})", geom, wkt);
    }

    public static Field<?> st_touches(Field<?> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Touches({0}, {1})", geom, wkt);
    }

    public static Field<?> st_within(Field<?> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Within({0}, {1})", geom, wkt);
    }

    public static Field<?> st_overlaps(Field<?> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Overlaps({0}, {1})", geom, wkt);
    }

    public static Field<?> st_crosses(Field<?> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Crosses({0}, {1})", geom, wkt);
    }

    public static Field<?> st_contains(Field<?> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Contains({0}, {1})", geom, wkt);
    }

    public static Field<Boolean> defaultSTMethodCallGeometry(String methodName, Field<?> geom, String wkt) {
        return DSL.field(
                methodName,
                Boolean.class,
                geom,
                geometryfromWKT(extractWKT(wkt))
        );
    }

    public static Field<Boolean> st_intersects(Field<?> geom, String wkt) {
        return DSL.field(
                "ST_Intersects({0}, {1})",
                Boolean.class,
                geom,
                geographyfromWKT(extractWKT(wkt))
        );
    }

    public static Field<Boolean> st_relate(Field<?> geom, String wkt, String mask) {
        return DSL.field(
                "ST_Relate({0}, {1}, {2})",
                Boolean.class,
                geom,
                geographyfromWKT(extractWKT(wkt)),
                mask
        );
    }

    private static Field<org.locationtech.jts.geom.Geometry> geometryfromWKT(String wkt) {
        return DSL.field(
                "ST_GeomFromText({0})",
                org.locationtech.jts.geom.Geometry.class,
                "SRID=4326;" + wkt
        );
    }

    private static Field<org.locationtech.jts.geom.Geometry> geographyfromWKT(String wkt) {
        return DSL.field(
                "ST_GeographyFromText({0})",
                org.locationtech.jts.geom.Geometry.class,
                wkt
        );
    }

    private static String extractWKT(String wktWithType) {
        return wktWithType.substring(wktWithType.indexOf("'") + 1, wktWithType.length() - 1);
    }
}
