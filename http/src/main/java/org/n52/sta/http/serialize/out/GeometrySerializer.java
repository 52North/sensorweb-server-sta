package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

public class GeometrySerializer extends StdSerializer<Geometry> {

    private final transient GeoJsonWriter geometryWriter;

    public GeometrySerializer() {
        super(Geometry.class);
        this.geometryWriter = new GeoJsonWriter();
    }

    @Override
    public void serialize(Geometry value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String geoJson = geometryWriter.write(value);
        gen.writeRawValue(geoJson);
    }

}
