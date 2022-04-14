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

import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.io.Serializable;

/**
 * Implements Spatial operations as defined in SensorThingsAPI 15-078r6 Section 9.3.3.5.2
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface HibernateSpatialCriteriaBuilder extends HibernateCriteriaBuilder, Serializable {

    Predicate st_equals(Expression<Geometry> x, String wktWithType);

    Predicate st_disjoint(Expression<Geometry> x, String wktWithType);

    Predicate st_touches(Expression<Geometry> x, String wktWithType);

    Predicate st_within(Expression<Geometry> x, String wktWithType);

    Predicate st_overlaps(Expression<Geometry> x, String wktWithType);

    Predicate st_crosses(Expression<Geometry> x, String wktWithType);

    Predicate st_contains(Expression<Geometry> x, String wktWithType);

    Predicate st_intersects(Expression<String> x, String wktWithType);

    Predicate st_relate(Expression<Geometry> x, String wktWithType, String mask);

    Expression<Float> st_length(Expression<Geometry> x);

    Expression<Float> st_length(String wkt);

    Expression<Float> st_distance(Expression<Geometry> x, String wktWithType);

    Expression<Float> st_distance(String x, String wktWithType);
}
