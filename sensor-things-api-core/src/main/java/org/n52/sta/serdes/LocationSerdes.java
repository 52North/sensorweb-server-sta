package org.n52.sta.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.serdes.json.JSONLocation;
import org.n52.sta.serdes.model.ElementWithQueryOptions.LocationWithQueryOptions;
import org.n52.sta.serdes.model.LocationEntityDefinition;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.utils.QueryOptions;

import java.io.IOException;
import java.util.Set;

public class LocationSerdes {

    public static class LocationEntityPatch extends LocationEntity implements EntityPatch<LocationEntity> {
        private final LocationEntity entity;

        public LocationEntityPatch (LocationEntity entity) {
            this.entity = entity;
        }

        public LocationEntity getEntity() {
            return entity;
        }
    }


    public static class LocationSerializer extends AbstractSTASerializer<LocationWithQueryOptions> {

        private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();

        public LocationSerializer(String rootUrl) {
            super(LocationWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = LocationEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(LocationWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            LocationEntity location = value.getEntity();
            QueryOptions options = value.getQueryOptions();

            Set<String> fieldsToSerialize = null;
            boolean hasSelectOption = false;
            if (options != null) {
                hasSelectOption = options.hasSelectOption();
                if (hasSelectOption) {
                    fieldsToSerialize = options.getSelectOption();
                }
            }
            // olingo @iot links
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, location.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, location.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, location.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, location.getDescription());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                // only write out encodingtype if there is a location present
                if (location.isSetGeometry()) {
                    gen.writeStringField(STAEntityDefinition.PROP_ENCODINGTYPE, ENCODINGTYPE_GEOJSON);
                }
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_LOCATION)) {
                gen.writeObjectFieldStart(STAEntityDefinition.PROP_LOCATION);
                gen.writeStringField("type", "Feature");
                gen.writeObjectFieldStart("geometry");
                gen.writeRaw(GEO_JSON_WRITER.write(location.getGeometryEntity().getGeometry()));
                gen.writeEndObject();
                gen.writeEndObject();
            }

            // navigation properties
            for (String navigationProperty : LocationEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, location.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }
    }

    public static class LocationDeserializer extends StdDeserializer<LocationEntity> {

        public LocationDeserializer() {
            super(LocationEntity.class);
        }

        @Override
        public LocationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONLocation.class).toEntity();
        }
    }

    public static class LocationPatchDeserializer extends StdDeserializer<LocationEntityPatch> {

        public LocationPatchDeserializer() {
            super(LocationEntityPatch.class);
        }

        @Override
        public LocationEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new LocationEntityPatch(p.readValueAs(JSONLocation.class).toEntity(false));
        }
    }
}
