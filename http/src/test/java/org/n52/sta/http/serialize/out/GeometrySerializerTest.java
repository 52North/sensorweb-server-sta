
package org.n52.sta.http.serialize.out;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class GeometrySerializerTest {

    @Test
    public void expectValidJsonOnSerializingGeometry() throws JsonProcessingException {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(52, 7.5));

        SimpleModule geometryModule = new SimpleModule();
        geometryModule.addSerializer(new GeometrySerializer());
        ObjectMapper om = new ObjectMapper().registerModule(geometryModule);
        String json = om.writeValueAsString(point);
        assertThat(json, is(not(nullValue())));
    }
}
