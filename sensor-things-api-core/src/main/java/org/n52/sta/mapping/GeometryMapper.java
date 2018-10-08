/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mapping;

import java.util.LinkedList;
import java.util.List;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.edm.geo.GeospatialCollection;
import org.apache.olingo.commons.api.edm.geo.LineString;
import org.apache.olingo.commons.api.edm.geo.MultiLineString;
import org.apache.olingo.commons.api.edm.geo.MultiPoint;
import org.apache.olingo.commons.api.edm.geo.MultiPolygon;
import org.apache.olingo.commons.api.edm.geo.Point;
import org.apache.olingo.commons.api.edm.geo.Polygon;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.sta.edm.provider.complextypes.FeatureComplexType;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class GeometryMapper {

    private static final String LOCATION_TYPE = "Feature";

    public ComplexValue resolveGeometry(GeometryEntity geometry) {
        //TODO: geometry creation dependend on the GeometryType
        ComplexValue value = null;
        if (geometry.isSetGeometry()) {
            Geospatial geospatial = createGeospatial(geometry.getGeometry(), null);
            value = new ComplexValue();
            value.getValue().add(new Property(null, FeatureComplexType.PROP_TYPE, ValueType.PRIMITIVE, LOCATION_TYPE));
            value.getValue().add(new Property(null, FeatureComplexType.PROP_GEOMETRY, ValueType.GEOSPATIAL, geospatial));
        }
        return value;
    }
    
    private Geospatial createGeospatial(com.vividsolutions.jts.geom.Geometry geom, SRID srid) {
        if (srid == null) {
            srid = getSRID(geom);
        }
        if (geom instanceof com.vividsolutions.jts.geom.Point) {
            return createPoint((com.vividsolutions.jts.geom.Point) geom);
        } else if (geom instanceof com.vividsolutions.jts.geom.LineString) {
            return createLineString((com.vividsolutions.jts.geom.LineString) geom, srid);
        } else if (geom instanceof com.vividsolutions.jts.geom.Polygon) {
            com.vividsolutions.jts.geom.Polygon poly = (com.vividsolutions.jts.geom.Polygon) geom;
            return new Polygon(Geospatial.Dimension.GEOMETRY, srid, createInteriorPointList(poly, srid),
                    createPointList(poly.getExteriorRing().getCoordinates(), srid));
            // would be supported in Olingo 4.6.0
            // return new Polygon(Geospatial.Dimension.GEOMETRY,
            // srid, createLineStringList(poly, srid),
            // createLineString(poly.getExteriorRing(), srid));
        } else if (geom instanceof com.vividsolutions.jts.geom.MultiPoint) {
            return new MultiPoint(Geospatial.Dimension.GEOMETRY, srid,
                    createPointList(((com.vividsolutions.jts.geom.MultiPoint) geom).getCoordinates(), srid));
        } else if (geom instanceof com.vividsolutions.jts.geom.MultiLineString) {
            return new MultiLineString(Geospatial.Dimension.GEOMETRY, srid,
                    createLineStringList((com.vividsolutions.jts.geom.MultiLineString) geom, srid));
        } else if (geom instanceof com.vividsolutions.jts.geom.MultiPolygon) {
            return new MultiPolygon(Geospatial.Dimension.GEOMETRY, srid,  createPolygonList((com.vividsolutions.jts.geom.MultiPolygon) geom, srid));
        } else if (geom instanceof com.vividsolutions.jts.geom.GeometryCollection) {
            return new GeospatialCollection(Geospatial.Dimension.GEOMETRY, srid,
                    createGeospatialList((com.vividsolutions.jts.geom.GeometryCollection) geom, srid));
        }
        return null;
    }

    private Point createPoint(com.vividsolutions.jts.geom.Point geometry) {
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

    private List<Point> createInteriorPointList(com.vividsolutions.jts.geom.Polygon poly, SRID srid) {
        List<Point> list = new LinkedList<>();
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            list.addAll(createPointList(poly.getInteriorRingN(i).getCoordinates(), srid));
        }
        return list;
    }

    private LineString createLineString(com.vividsolutions.jts.geom.LineString geom, SRID srid) {
        return new LineString(Geospatial.Dimension.GEOMETRY, srid == null ? getSRID(geom) : srid,
                createPointList(geom.getCoordinates(), getSRID(geom)));
    }

    
     private List<LineString> createLineStringList(com.vividsolutions.jts.geom.MultiLineString geom, SRID srid) {
        List<LineString> list = new LinkedList<>();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            list.add((LineString) createGeospatial(geom.getGeometryN(i), srid));
        }
        return list;
    }

   // would be used for Olingo 4.6.0
    private List<LineString> createLineStringList(com.vividsolutions.jts.geom.Polygon poly, SRID srid) {
        List<LineString> list = new LinkedList<>();
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            list.add(createLineString(poly.getInteriorRingN(i), srid));
        }
        return list;
    }
    
    private List<Polygon> createPolygonList(com.vividsolutions.jts.geom.MultiPolygon geom, SRID srid) {
        List<Polygon> list = new LinkedList<>();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            list.add((Polygon) createGeospatial(geom.getGeometryN(i), srid));
        }
        return list;
    }

    private List<Geospatial> createGeospatialList(com.vividsolutions.jts.geom.GeometryCollection geom, SRID srid) {
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

}
