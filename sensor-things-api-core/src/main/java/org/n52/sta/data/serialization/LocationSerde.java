package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.serialization.SensorThingsSerde.JSONwithIdNameDescription;
import org.n52.sta.data.serialization.ThingSerde.JSONThing;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LocationSerde {

    public class LocationSerializer extends JsonSerializer<LocationEntity> {

        @Override
        public void serialize(LocationEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    public class LocationDeserializer extends JsonDeserializer<LocationEntity> {

        @Override
        public LocationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
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
