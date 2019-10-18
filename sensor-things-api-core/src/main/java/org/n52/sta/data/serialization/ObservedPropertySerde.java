package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.sta.data.serialization.SensorThingsSerde.JSONwithIdNameDescription;

import java.io.IOException;

public class ObservedPropertySerde {

    public class ObservedPropertySerializer extends JsonSerializer<PhenomenonEntity> {

        @Override
        public void serialize(PhenomenonEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    public class ObservedPropertyDeserializer extends JsonDeserializer<PhenomenonEntity> {

        @Override
        public PhenomenonEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    class JSONObservedProperty extends JSONwithIdNameDescription {

        public PhenomenonEntity toEntity() {
            throw new NotYetImplementedException();
        }
    }
}
