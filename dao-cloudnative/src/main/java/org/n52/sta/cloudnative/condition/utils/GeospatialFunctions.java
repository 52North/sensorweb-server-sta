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
package org.n52.sta.cloudnative.condition.utils;


import org.jooq.*;
import org.jooq.impl.DSL;


/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */

public interface GeospatialFunctions {

    String ST_DISTANCE = "ST_Distance({0}, {1})";
    String ST_LENGTH = "ST_Length({0})";

    /* DuckDB expects only GEOMETRY type arguments in all spatial functions */

    static Condition st_relate(Field<Geometry> geom, String wkt, String mask) {
        return DSL.condition(
                DSL.function(
                        "ST_Relate({0}, {1}, {2})",
                        Boolean.class,
                        geometryFromWKB(geom),
                        geometryfromWKT(extractWKT(wkt)),
                        DSL.inline(mask)
                )
        );
    }

    static Field<Double> st_length(Field<Geometry> geom) {
        return DSL.function(
            ST_LENGTH,
            Double.class,
            geometryFromWKB(geom)
        );
    }

    static Field<Double> st_length(String wkt) {
        return DSL.function(
                ST_LENGTH,
                Double.class,
                geometryfromWKT(extractWKT(wkt))
        );
    }

    static Field<Double> st_distance(Field<Geometry> geom, String wkt) {
        return DSL.function(
                ST_DISTANCE,
                Double.class,
                geometryFromWKB(geom),
                geometryfromWKT(extractWKT(wkt))
        );
    }

    static Field<Double> st_distance(String geom, String wkt) {
        return DSL.function(
                ST_DISTANCE,
                Double.class,
                geometryfromWKT(extractWKT(geom)),
                geometryfromWKT(extractWKT(wkt))
        );
    }

    static Condition st_intersects(Field<Geometry> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Intersects({0}, {1})", geom, wkt);
    }

    static Condition st_equals(Field<Geometry> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Equals({0}, {1})", geom, wkt);
    }

    static Condition st_disjoint(Field<Geometry> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Disjoint({0}, {1})", geom, wkt);
    }

    static Condition st_touches(Field<Geometry> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Touches({0}, {1})", geom, wkt);
    }

    static Condition st_within(Field<Geometry> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Within({0}, {1})", geom, wkt);
    }

    static Condition st_overlaps(Field<Geometry> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Overlaps({0}, {1})", geom, wkt);
    }

    static Condition st_crosses(Field<Geometry> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Crosses({0}, {1})", geom, wkt);
    }

    static Condition st_contains(Field<Geometry> geom, String wkt) {
        return defaultSTMethodCallGeometry("ST_Contains({0}, {1})", geom, wkt);
    }

    static Condition defaultSTMethodCallGeometry(String methodName, Field<Geometry> geom, String wkt) {
        return DSL.condition(DSL.function(
                methodName,
                Boolean.class,
                geometryFromWKB(geom),
                geometryfromWKT(extractWKT(wkt))
                )
        );
    }

    private static Field<Geometry> geometryfromWKT(String wkt) {
        return DSL.function(
                "ST_GeomFromText({0})",
                Geometry.class,
                DSL.inline(wkt)
        );
    }

    private static Field<Geometry> geometryFromWKB(Field<Geometry> geom) {
        return DSL.function(
                "ST_GeomFromWKB({0})",
                Geometry.class,
                geom
        );
    }

    private static String extractWKT(String wktWithType) {
        return wktWithType.substring(wktWithType.indexOf("'") + 1, wktWithType.length() - 1);
    }
}
