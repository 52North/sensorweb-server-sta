package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.sta.data.serialization.SensorThingsSerde.JSONwithIdNameDescription;

import java.io.IOException;

public class SensorSerde {

    public class SensorSerializer extends JsonSerializer<SensorEntity> {

        @Override
        public void serialize(SensorEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    public class SensorDeserializer extends JsonDeserializer<SensorEntity> {

        @Override
        public SensorEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    class JSONSensor extends JSONwithIdNameDescription {

        public SensorEntity toEntity() {
            throw new NotYetImplementedException();
        }
    }
}
