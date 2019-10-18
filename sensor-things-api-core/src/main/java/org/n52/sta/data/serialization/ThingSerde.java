package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.sta.data.serialization.DatastreamSerde.JSONDatastream;
import org.n52.sta.data.serialization.LocationSerde.JSONLocation;
import org.n52.sta.data.serialization.SensorThingsSerde.JSONwithIdNameDescription;
import org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ThingSerde {

    public class ThingSerializer extends JsonSerializer<PlatformEntity> {

        @Override
        public void serialize(PlatformEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField(AbstractSensorThingsEntityProvider.PROP_ID, value.getIdentifier());
            gen.writeStringField(AbstractSensorThingsEntityProvider.PROP_DESCRIPTION, value.getDescription());
            gen.writeStringField(AbstractSensorThingsEntityProvider.PROP_NAME, value.getName());
            //TODO: what happens if properties is null?
            gen.writeStringField(AbstractSensorThingsEntityProvider.PROP_PROPERTIES, value.getProperties());
            gen.writeEndObject();
        }
    }

    public class ThingDeserializer extends JsonDeserializer<PlatformEntity> {

        @Override
        public PlatformEntity deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            JSONThing node = p.readValueAs(JSONThing.class);
            return node.toEntity();
        }
    }

    class JSONThing extends JSONwithIdNameDescription {
        public String properties;
        public JSONLocation[] locations;
        public JSONDatastream[] datastreams;

        PlatformEntity toEntity() {
            PlatformEntity entity = new PlatformEntity();
            entity.setIdentifier(identifier);
            entity.setName(name);
            entity.setDescription(description);
            for (JSONLocation location : locations) {
                entity.addLocationEntity(location.toEntity());
            }

            entity.setDatastreams(Arrays.stream(datastreams)
                                        .map(JSONDatastream::toEntity)
                                        .collect(Collectors.toSet()));
            entity.setProperties(properties);
            return entity;
        }
    }


}
