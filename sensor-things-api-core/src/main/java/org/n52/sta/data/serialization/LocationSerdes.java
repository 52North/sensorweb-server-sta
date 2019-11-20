package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.serialization.ElementWithQueryOptions.LocationWithQueryOptions;
import org.n52.sta.data.serialization.STASerdesTypes.JSONwithIdNameDescription;
import org.n52.sta.data.serialization.ThingSerdes.JSONThing;
import org.n52.sta.edm.provider.entities.LocationEntityDefinition;
import org.n52.sta.edm.provider.entities.STAEntityDefinition;
import org.n52.sta.service.query.QueryOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class LocationSerdes {

    public static class LocationSerializer extends AbstractSTASerializer<LocationWithQueryOptions> {

        private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

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
                gen.writeStringField(STAEntityDefinition.PROP_LOCATION, location.getGeometryEntity().toString());
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

    public static class LocationDeserializer extends JsonDeserializer<LocationEntity> {

        @Override
        public LocationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    class JSONLocation extends JSONwithIdNameDescription {
        public String encodingType;
        public JSONThing[] things;

        public LocationEntity toEntity() {
            LocationEntity entity = new LocationEntity();
            entity.setIdentifier(identifier);
            entity.setName(name);
            entity.setDescription(description);
            //TODO: add location aka geometry

            entity.setThings(Arrays.stream(things)
                    .map(JSONThing::toEntity)
                    .collect(Collectors.toSet()));
            return entity;
        }
    }

}
