/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mapping;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.geo.ComposedGeospatial;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.edm.geo.GeospatialCollection;
import org.apache.olingo.commons.api.edm.geo.LineString;
import org.apache.olingo.commons.api.edm.geo.MultiLineString;
import org.apache.olingo.commons.api.edm.geo.MultiPoint;
import org.apache.olingo.commons.api.edm.geo.MultiPolygon;
import org.apache.olingo.commons.api.edm.geo.Point;
import org.apache.olingo.commons.api.edm.geo.Polygon;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.sta.edm.provider.complextypes.FeatureComplexType;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class GeometryMapper {

    private static final String LOCATION_TYPE = "Feature";
    
    private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    public Geospatial resolveGeometry(GeometryEntity geometry) {
        if (geometry != null && geometry.isSetGeometry()) {
            return createGeospatial(geometry.getGeometry(), null);
        }
        return null;
    }
    
    public ComplexValue resolveComplexValueGeometry(GeometryEntity geometry) {
        //TODO: geometry creation dependend on the GeometryType
        ComplexValue value = null;
        Geospatial geospatial = resolveGeometry(geometry);
        if (geospatial != null) {
            value = new ComplexValue();
            value.getValue().add(new Property(null, FeatureComplexType.PROP_TYPE, ValueType.PRIMITIVE, LOCATION_TYPE));
            value.getValue().add(new Property(null, FeatureComplexType.PROP_GEOMETRY, ValueType.GEOSPATIAL, geospatial));
        }
        return value;
    }
    
    private Geospatial createGeospatial(org.locationtech.jts.geom.Geometry geom, SRID srid) {
        if (srid == null) {
            srid = getSRID(geom);
        }
        if (geom instanceof org.locationtech.jts.geom.Point) {
            return createPoint((org.locationtech.jts.geom.Point) geom);
        } else if (geom instanceof org.locationtech.jts.geom.LineString) {
            return createLineString((org.locationtech.jts.geom.LineString) geom, srid);
        } else if (geom instanceof org.locationtech.jts.geom.Polygon) {
            org.locationtech.jts.geom.Polygon poly = (org.locationtech.jts.geom.Polygon) geom;
            return new Polygon(Geospatial.Dimension.GEOMETRY, srid, createInteriorPointList(poly, srid),
                    createPointList(poly.getExteriorRing().getCoordinates(), srid));
            // would be supported in Olingo 4.6.0
            // return new Polygon(Geospatial.Dimension.GEOMETRY,
            // srid, createLineStringList(poly, srid),
            // createLineString(poly.getExteriorRing(), srid));
        } else if (geom instanceof org.locationtech.jts.geom.MultiPoint) {
            return new MultiPoint(Geospatial.Dimension.GEOMETRY, srid,
                    createPointList(((org.locationtech.jts.geom.MultiPoint) geom).getCoordinates(), srid));
        } else if (geom instanceof org.locationtech.jts.geom.MultiLineString) {
            return new MultiLineString(Geospatial.Dimension.GEOMETRY, srid,
                    createLineStringList((org.locationtech.jts.geom.MultiLineString) geom, srid));
        } else if (geom instanceof org.locationtech.jts.geom.MultiPolygon) {
            return new MultiPolygon(Geospatial.Dimension.GEOMETRY, srid,  createPolygonList((org.locationtech.jts.geom.MultiPolygon) geom, srid));
        } else if (geom instanceof org.locationtech.jts.geom.GeometryCollection) {
            return new GeospatialCollection(Geospatial.Dimension.GEOMETRY, srid,
                    createGeospatialList((org.locationtech.jts.geom.GeometryCollection) geom, srid));
        }
        return null;
    }

    private Point createPoint(org.locationtech.jts.geom.Point geometry) {
        return createPoint(geometry.getCoordinate(), getSRID(geometry));
    }

    private Point createPoint(Coordinate coordinate, SRID srid) {
        Point point = new Point(Geospatial.Dimension.GEOMETRY, srid);
        point.setX(coordinate.x);
        point.setY(coordinate.y);
        if (!Double.isNaN(coordinate.z)) {
            point.setZ(coordinate.z);
        }
        return point;
    }
    
    private List<Point> createPointList(Coordinate[] coordinates, SRID srid) {
        List<Point> list = new LinkedList<>();
        for (Coordinate coordinate : coordinates) {
            list.add(createPoint(coordinate, srid));
        }
        return list;
    }

    private List<Point> createInteriorPointList(org.locationtech.jts.geom.Polygon poly, SRID srid) {
        List<Point> list = new LinkedList<>();
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            list.addAll(createPointList(poly.getInteriorRingN(i).getCoordinates(), srid));
        }
        return list;
    }

    private LineString createLineString(org.locationtech.jts.geom.LineString geom, SRID srid) {
        return new LineString(Geospatial.Dimension.GEOMETRY, srid == null ? getSRID(geom) : srid,
                createPointList(geom.getCoordinates(), getSRID(geom)));
    }

    
     private List<LineString> createLineStringList(org.locationtech.jts.geom.MultiLineString geom, SRID srid) {
        List<LineString> list = new LinkedList<>();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            list.add((LineString) createGeospatial(geom.getGeometryN(i), srid));
        }
        return list;
    }

   // would be used for Olingo 4.6.0
    private List<LineString> createLineStringList(org.locationtech.jts.geom.Polygon poly, SRID srid) {
        List<LineString> list = new LinkedList<>();
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            list.add(createLineString(poly.getInteriorRingN(i), srid));
        }
        return list;
    }
    
    private List<Polygon> createPolygonList(org.locationtech.jts.geom.MultiPolygon geom, SRID srid) {
        List<Polygon> list = new LinkedList<>();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            list.add((Polygon) createGeospatial(geom.getGeometryN(i), srid));
        }
        return list;
    }

    private List<Geospatial> createGeospatialList(org.locationtech.jts.geom.GeometryCollection geom, SRID srid) {
        List<Geospatial> list = new LinkedList<>();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            list.add(createGeospatial(geom.getGeometryN(i), srid));
        }
        return list;
    }

    private SRID getSRID(Geometry geometry) {
        // Olingo checks for default by checking if value is null
//        if (geometry.getSRID() > 0) {
//            return SRID.valueOf(Integer.toString(geometry.getSRID()));
//        }
        return null;
    }

    public GeometryEntity createGeometryEntity(ComplexValue value) {
        if (value.getTypeName().equals("iot.Feature")) {
            GeometryEntity geometryEntity = new GeometryEntity();
            geometryEntity.setGeometry(createGeometry(value.getValue()));
            return geometryEntity;
        }
        return null;
    }

    public GeometryEntity createGeometryEntity(Geospatial geospatial) {
        if (geospatial != null) {
            GeometryEntity geometryEntity = new GeometryEntity();
            geometryEntity.setGeometry(createGeometry(geospatial));
            return geometryEntity;
        }
        return null;
    }

    private Geometry createGeometry(List<Property> list) {
        for (Property property : list) {
            if (property.getType().equals("Edm.Geometry") && property.getValue() != null) {
                return createGeometry((Geospatial) property.getValue());
            }
        }
        return null;
    }
    
    private Geometry createGeometry(Geospatial geospatial) {
        if (geospatial instanceof Point) {
            return createPoint((Point) geospatial);
        } else if (geospatial instanceof LineString) {
            return createLineString((LineString) geospatial);
        } else if (geospatial instanceof Polygon) {
            return createPolygon((Polygon) geospatial);
        } else if (geospatial instanceof MultiPoint) {
            return createMultiPoint((MultiPoint) geospatial);
        } else if (geospatial instanceof MultiLineString) {
            return createMultiLineString((MultiLineString) geospatial);
        } else if (geospatial instanceof MultiPolygon) {
            return createMultiPolygon((MultiPolygon) geospatial);
        } else if (geospatial instanceof GeospatialCollection) {
            return createGemetryCollection((GeospatialCollection) geospatial);
        }
        return null;
    }

    private org.locationtech.jts.geom.Point createPoint(Point point) {
        return factory.createPoint(createCoordinate(point));
    }

    private org.locationtech.jts.geom.LineString createLineString(LineString lineString) {
        return factory.createLineString(createCoordinates(lineString.iterator()));
    }

    private org.locationtech.jts.geom.Polygon createPolygon(Polygon polygon) {
        return factory.createPolygon(createLinearRing(polygon.getExterior()), createLinearRings(polygon.getInterior()));
    }

    private org.locationtech.jts.geom.MultiPoint createMultiPoint(MultiPoint multiPoint) {
        return factory.createMultiPoint(createPoints(multiPoint.iterator()));
    }

    private org.locationtech.jts.geom.MultiLineString createMultiLineString(MultiLineString multiLineString) {
        return factory.createMultiLineString(createLineStrings(multiLineString.iterator()));
    }

    private org.locationtech.jts.geom.MultiPolygon createMultiPolygon(MultiPolygon multiPolygon) {
        return factory.createMultiPolygon(createMultyPolygons(multiPolygon.iterator()));
    }

    private Geometry createGemetryCollection(GeospatialCollection geospatialCollection) {
        // TODO Auto-generated method stub
        return factory.createGeometryCollection(createGeometries(geospatialCollection.iterator()));
    }

    private Coordinate createCoordinate(Point point) {
        return new Coordinate(point.getX(), point.getY(), point.getZ());
    }

    private Coordinate[] createCoordinates(Iterator<Point> iterator) {
        List<Coordinate> coordinates = new LinkedList<>();
        while (iterator.hasNext()) {
            coordinates.add(createCoordinate(iterator.next()));
        }
        return coordinates.toArray(new Coordinate[0]);
    }
    
    private org.locationtech.jts.geom.Point[] createPoints(Iterator<Point> iterator) {
        List<org.locationtech.jts.geom.Point> points = new LinkedList<>();
        while (iterator.hasNext()) {
            points.add(createPoint(iterator.next()));
        }
        return points.toArray(new org.locationtech.jts.geom.Point[0]);
    }

    private org.locationtech.jts.geom.LineString[] createLineStrings(Iterator<LineString> iterator) {
        List<org.locationtech.jts.geom.LineString> lineStrings = new LinkedList<>();
        while (iterator.hasNext()) {
           lineStrings.add(createLineString(iterator.next()));
        }
        return lineStrings.toArray(new org.locationtech.jts.geom.LineString[0]);
    }

    private LinearRing createLinearRing(ComposedGeospatial<Point> ring) {
         return !ring.isEmpty() ? factory.createLinearRing(createCoordinates(ring.iterator())) : null;
    }

    private LinearRing[] createLinearRings(ComposedGeospatial<Point> ring) {
        if (!ring.isEmpty()) {
            List<LinearRing> rings = new LinkedList<>();
            rings.add(createLinearRing(ring));
            return rings.toArray(new LinearRing[0]);
        }
        return null;
    }

    private org.locationtech.jts.geom.Polygon[] createMultyPolygons(Iterator<Polygon> iterator) {
        List<org.locationtech.jts.geom.Polygon> polygons = new LinkedList<>();
        while (iterator.hasNext()) {
            polygons.add(createPolygon(iterator.next()));
        }
        return polygons.toArray(new org.locationtech.jts.geom.Polygon[0]);
    }

    private Geometry[] createGeometries(Iterator<Geospatial> iterator) {
        List<Geometry> polygons = new LinkedList<>();
        while (iterator.hasNext()) {
            polygons.add(createGeometry(iterator.next()));
        }
        return polygons.toArray(new Geometry[0]);
    }

}
