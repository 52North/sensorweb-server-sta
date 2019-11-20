package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.sta.data.serialization.DatastreamSerdes.JSONDatastream;
import org.n52.sta.data.serialization.ElementWithQueryOptions.ThingWithQueryOptions;
import org.n52.sta.data.serialization.LocationSerdes.JSONLocation;
import org.n52.sta.data.serialization.STASerdesTypes.JSONwithIdNameDescription;
import org.n52.sta.edm.provider.entities.STAEntityDefinition;
import org.n52.sta.edm.provider.entities.ThingEntityDefinition;
import org.n52.sta.service.query.QueryOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ThingSerdes {

    public static class ThingSerializer extends AbstractSTASerializer<ThingWithQueryOptions> {

        public ThingSerializer(String rootUrl) {
            super(ThingWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = ThingEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(ThingWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            PlatformEntity thing = value.getEntity();
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
                writeId(gen, thing.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, thing.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, thing.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, thing.getDescription());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PROPERTIES)) {
                gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, thing.getProperties());
            }

            // navigation properties
            for (String navigationProperty : ThingEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, thing.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }

    }

    public static class ThingDeserializer extends StdDeserializer<PlatformEntity> {

        public ThingDeserializer() {
            super(PlatformEntity.class);
        }

        @Override
        public PlatformEntity deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            JSONThing node = p.readValueAs(JSONThing.class);
            return node.toEntity();
        }
    }

    static class JSONThing extends JSONwithIdNameDescription {
        public String properties;
        public JSONLocation[] locations;
        public JSONDatastream[] datastreams;

        public JSONThing() {}

        PlatformEntity toEntity() {
            PlatformEntity entity = new PlatformEntity();
            entity.setIdentifier(identifier);
            entity.setName(name);
            entity.setDescription(description);
            if (locations != null) {
                for (JSONLocation location : locations) {
                    entity.addLocationEntity(location.toEntity());
                }
            }
            if (datastreams != null) {
                entity.setDatastreams(Arrays.stream(datastreams)
                        .map(JSONDatastream::toEntity)
                        .collect(Collectors.toSet()));
            }
            entity.setProperties(properties);
            return entity;
        }
    }


}
