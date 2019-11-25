package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class JSONLocation extends JSONBase.JSONwithIdNameDescription<LocationEntity> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String encodingType;
    public JSONNestedLocation location;

    @JsonManagedReference
    public JSONThing[] Things;
    @JsonManagedReference
    public JSONHistoricalLocation[] HistoricalLocations;

    private final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

    private final GeometryFactory factory =
            new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    private static class JSONNestedLocation {
        public String type;
        public JsonNode geometry;
    }

    public JSONLocation() {
        self = new LocationEntity();
    }

    public LocationEntity toEntity() {
        if (!generatedId && name == null) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(encodingType, INVALID_REFERENCED_ENTITY);
            Assert.isNull(location, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Things, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        } else {
            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
            Assert.notNull(encodingType, INVALID_INLINE_ENTITY + "encodingType");
            Assert.notNull(location, INVALID_INLINE_ENTITY + "location");
            Assert.notNull(location.type, INVALID_INLINE_ENTITY + "location->type");
            Assert.notNull(location.geometry, INVALID_INLINE_ENTITY + "location->geometry");
            Assert.state(encodingType.equals(ENCODINGTYPE_GEOJSON),
                    "Invalid encodingType supplied. Only GeoJSON (application/vnd.geo+json) is supported!");

            self.setIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);
            self.setLocationEncoding(new FormatEntity().setFormat(encodingType));

            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                Assert.state(location.type.equals("Feature"));
                self.setGeometry(reader.read(location.geometry.toString()));
            } catch (ParseException e) {
                Assert.notNull(null, "Could not parse location to GeoJSON. Error was:" + e.getMessage());
            }

            if (Things != null) {
                self.setThings(Arrays.stream(Things)
                        .map(JSONThing::toEntity)
                        .collect(Collectors.toSet()));
            }
            if (HistoricalLocations != null) {
                self.setHistoricalLocations(Arrays.stream(HistoricalLocations)
                        .map(JSONHistoricalLocation::toEntity)
                        .collect(Collectors.toSet()));
            }


            if (backReference != null) {
                if (backReference instanceof JSONThing) {
                    if (self.getThings() != null) {
                        self.getThings().add(((JSONThing) backReference).getEntity());
                    } else {
                        self.setThings(Collections.singleton(((JSONThing) backReference).getEntity()));
                    }
                } else {
                    self.addHistoricalLocation(((JSONHistoricalLocation) backReference).getEntity());
                }
            }
            return self;
        }
    }
}
