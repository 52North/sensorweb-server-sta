package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.DataEntity;
import org.n52.sta.data.serialization.SensorThingsSerde.JSONwithIdNameDescription;

import java.io.IOException;

public class ObservationSerde {

    public class ObservationSerializer extends JsonSerializer<DataEntity<?>> {

        @Override
        public void serialize(DataEntity<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    public class ObservationDeserializer extends JsonDeserializer<DataEntity<?>> {

        @Override
        public DataEntity<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    class JSONObservation extends JSONwithIdNameDescription {

        public DataEntity<?> toEntity() {
            throw new NotYetImplementedException();
        }
    }
}
