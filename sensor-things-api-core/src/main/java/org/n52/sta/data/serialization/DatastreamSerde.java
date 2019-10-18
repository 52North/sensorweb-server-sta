package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.sta.data.serialization.SensorThingsSerde.JSONwithIdNameDescription;

import java.io.IOException;

public class DatastreamSerde {

    public class DatastreamSerializer extends JsonSerializer<DatastreamEntity> {

        @Override
        public void serialize(DatastreamEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    public class DatastreamDeserializer extends JsonDeserializer<DatastreamEntity> {

        @Override
        public DatastreamEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    class JSONDatastream extends JSONwithIdNameDescription {

        public DatastreamEntity toEntity() {
            throw new NotYetImplementedException();
        }
    }
}
