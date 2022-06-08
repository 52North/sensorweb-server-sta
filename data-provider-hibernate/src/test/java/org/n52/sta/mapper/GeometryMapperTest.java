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
/// *
// * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
// * Software GmbH
// *
// * This program is free software; you can redistribute it and/or modify it
// * under the terms of the GNU General Public License version 2 as published
// * by the Free Software Foundation.
// *
// * If the program is linked with libraries which are licensed under one of
// * the following licenses, the combination of the program with the linked
// * library is not considered a "derivative work" of the program:
// *
// * - Apache License, version 2.0
// * - Apache Software License, version 1.0
// * - GNU Lesser General Public License, version 3
// * - Mozilla Public License, versions 1.0, 1.1 and 2.0
// * - Common Development and Distribution License (CDDL), version 1.0
// *
// * Therefore the distribution of the program linked with libraries licensed
// * under the aforementioned licenses, is permitted by the copyright holders
// * if the distribution is compliant with both the GNU General Public
// * License version 2 and the aforementioned licenses.
// *
// * This program is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// * Public License for more details.
// */
// package org.n52.sta.mapper;
//
// import static org.hamcrest.core.Is.is;
// import static org.hamcrest.core.IsInstanceOf.instanceOf;
// import static org.junit.Assert.assertThat;
//
// import java.util.Iterator;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Optional;
//
// import org.apache.olingo.commons.api.data.ComplexValue;
// import org.apache.olingo.commons.api.data.Property;
// import org.apache.olingo.commons.api.edm.geo.Geospatial;
// import org.junit.Test;
// import org.locationtech.jts.geom.Coordinate;
// import org.locationtech.jts.geom.Geometry;
// import org.locationtech.jts.geom.GeometryFactory;
// import org.locationtech.jts.geom.LineString;
// import org.locationtech.jts.geom.Point;
// import org.locationtech.jts.geom.Polygon;
// import org.locationtech.jts.geom.PrecisionModel;
// import org.n52.series.db.beans.GeometryEntity;
// import org.n52.sta.edm.provider.complextypes.FeatureComplexType;
// import org.n52.sta.mapping.GeometryMapper;
//
// public class GeometryMapperTest {
//
// private GeometryMapper mapper = new GeometryMapper();
// private GeometryFactory gf = new GeometryFactory(new PrecisionModel(0.01), 4326);
// private double x = 52.7;
// private double y = 7.52;
// private double xy = 527.0;
// private double z = 0.52;
//
// @Test
// public void test_point_mapping() {
// Point point = gf.createPoint(new Coordinate(x, y));
// // check
// ComplexValue geom = create(point);
// Geospatial geo = check(geom);
// assertThat(geo, instanceOf(org.apache.olingo.commons.api.edm.geo.Point.class));
// checkPoint((org.apache.olingo.commons.api.edm.geo.Point) geo);
// }
//
// @Test
// public void test_point_z_mapping() {
// Point point = gf.createPoint(new Coordinate(x, y, z));
// // check
// ComplexValue geom = create(point);
// Geospatial geo = check(geom);
// assertThat(geo, instanceOf(org.apache.olingo.commons.api.edm.geo.Point.class));
// checkPoint((org.apache.olingo.commons.api.edm.geo.Point) geo, true);
// }
//
// @Test
// public void test_line_string_mapping() {
// List<Coordinate> list = new LinkedList<>();
// list.add(new Coordinate(x, y));
// list.add(new Coordinate(y, x));
// LineString ls = gf.createLineString(list.toArray(new Coordinate[0]));
// // check
// ComplexValue geom = create(ls);
// Geospatial geo = check(geom);
// assertThat(geo, instanceOf(org.apache.olingo.commons.api.edm.geo.LineString.class));
// checkLineString((org.apache.olingo.commons.api.edm.geo.LineString) geo);
// }
//
// @Test
// public void test_polygon_mapping() {
// List<Coordinate> list = new LinkedList<>();
// list.add(new Coordinate(x, y));
// list.add(new Coordinate(x, xy));
// list.add(new Coordinate(xy, y));
// list.add(new Coordinate(x, y));
// Polygon poly = gf.createPolygon(list.toArray(new Coordinate[0]));
// // check
// ComplexValue geom = create(poly);
// Geospatial geo = check(geom);
// assertThat(geo, instanceOf(org.apache.olingo.commons.api.edm.geo.Polygon.class));
// checkPolygon((org.apache.olingo.commons.api.edm.geo.Polygon) geo);
// }
//
// private ComplexValue create(Geometry g) {
// g.setSRID(4326);
// GeometryEntity ge = new GeometryEntity();
// ge.setGeometry(g);
// return mapper.resolveComplexValueGeometry(ge);
// }
//
// private Geospatial check(ComplexValue geom) {
// Property p = getGeospatialProperty(geom);
// assertThat(p.asGeospatial() != null, is(true));
// //assertThat(p.asGeospatial().getSrid().toString().equals("4326"), is(true));
// return p.asGeospatial();
// }
//
// private void checkPoint(org.apache.olingo.commons.api.edm.geo.Point p) {
// checkPoint(p, true);
// }
//
// private void checkPoint(org.apache.olingo.commons.api.edm.geo.Point p, boolean xFirst) {
// checkPoint(p, xFirst, false);
// }
//
// private void checkPoint(org.apache.olingo.commons.api.edm.geo.Point p, boolean xFirst, boolean withZ) {
// if (xFirst) {
// checkPoint(p, x, y, withZ);
// } else {
// checkPoint(p, y, x, withZ);
// }
// }
//
// private void checkPoint(org.apache.olingo.commons.api.edm.geo.Point p, double x, double y) {
// checkPoint(p, x, y, false);
// }
//
// private void checkPoint(org.apache.olingo.commons.api.edm.geo.Point p, double x, double y, boolean withZ) {
// assertThat(p.getX(), is(x));
// assertThat(p.getY(), is(y));
// if (withZ) {
// assertThat(p.getZ(), is(z));
// }
// }
//
// private void checkLineString(org.apache.olingo.commons.api.edm.geo.LineString lineString) {
// assertThat(lineString.isEmpty(), is(false));
// Iterator<org.apache.olingo.commons.api.edm.geo.Point> it = lineString.iterator();
// boolean first = true;
// while (it.hasNext()) {
// if (first) {
// checkPoint(it.next());
// first = false;
// } else {
// checkPoint(it.next(), false);
// }
// }
// }
//
// private void checkPolygon(org.apache.olingo.commons.api.edm.geo.Polygon polygon) {
// assertThat(polygon.getInterior().isEmpty(), is(true));
// assertThat(polygon.getExterior().isEmpty(), is(false));
// Iterator<org.apache.olingo.commons.api.edm.geo.Point> it = polygon.getExterior().iterator();
// int counter = 0;
// while (it.hasNext()) {
// if (counter == 1) {
// checkPoint(it.next(), x, xy);
// } else if (counter == 2) {
// checkPoint(it.next(), xy, y);
// } else {
// checkPoint(it.next());
// }
// counter++;
// }
// }
//
// private Property getGeospatialProperty(ComplexValue geom) {
// Optional<Property> p = geom.getValue().stream().filter(g -> g.getName().equals(FeatureComplexType
// .PROP_GEOMETRY)).findFirst();
// assertThat(p.isPresent(), is(true));
// return p.get();
// }
//
// }
