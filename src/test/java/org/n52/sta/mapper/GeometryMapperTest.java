package org.n52.sta.mapper;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.junit.Test;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.sta.mapping.GeometryMapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class GeometryMapperTest {
    
    GeometryMapper mapper = new GeometryMapper();
    GeometryFactory gf = new GeometryFactory(new PrecisionModel(0.01), 4326);
    
    @Test
    public void test_point_mapping() {
        Point p = gf.createPoint(new Coordinate(52.7, 7.52));
        GeometryEntity ge = new GeometryEntity();
        ge.setGeometry(p);
        ComplexValue geom = mapper.resolveGeometry(ge);
        assertTrue(geom.toString().equals("[type=Feature, geometry=GEOMETRY'SRID=4326;Point(52.7 7.52)']"));
    }

    @Test
    public void test_line_string_mapping() {
        List<Coordinate> list = new LinkedList<>();
        list.add(new Coordinate(52.7, 7.52));
        list.add(new Coordinate(7.52, 52.7));
        LineString ls = gf.createLineString(list.toArray(new Coordinate[0]));
        GeometryEntity ge = new GeometryEntity();
        ge.setGeometry(ls);
        ComplexValue geom = mapper.resolveGeometry(ge);
        assertTrue(geom.toString().equals(
                "[type=Feature, geometry=[GEOMETRY'SRID=4326;Point(52.7 7.52)', GEOMETRY'SRID=4326;Point(7.52 52.7)']]"));
    }

}
