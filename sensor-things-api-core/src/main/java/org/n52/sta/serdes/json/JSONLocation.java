package org.n52.sta.serdes.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JSONLocation extends JSONBase.JSONwithIdNameDescription implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String encodingType;
    public JSONThing[] Things;
    public JsonNode location;

    private final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

    private final GeometryFactory factory =
            new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    public JSONLocation() {
    }

    public LocationEntity toEntity() {
        LocationEntity entity = new LocationEntity();

        if (!generatedId && name != null) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(encodingType, INVALID_REFERENCED_ENTITY);
            Assert.isNull(location, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Things, INVALID_REFERENCED_ENTITY);

            entity.setIdentifier(identifier);
            return entity;
        } else {
            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
            Assert.notNull(encodingType, INVALID_INLINE_ENTITY + "encodingType");
            Assert.notNull(location, INVALID_INLINE_ENTITY + "location");
            Assert.state(encodingType.equals(ENCODINGTYPE_GEOJSON),
                    "Invalid encodingType supplied. Only GeoJSON (application/vnd.geo+json) is supported!");

            entity.setIdentifier(identifier);
            entity.setName(name);
            entity.setDescription(description);
            entity.setLocationEncoding(new FormatEntity().setFormat(encodingType));

            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                entity.setGeometry(reader.read(this.location.toString()));
            } catch (ParseException e) {
                Assert.notNull(null, "Could not parse location to GeoJSON. Error was:" + e.getMessage());
            }

            if (Things != null) {
                entity.setThings(Arrays.stream(Things)
                        .map(JSONThing::toEntity)
                        .collect(Collectors.toSet()));
            }
            return entity;
        }
    }
}
