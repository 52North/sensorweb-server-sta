package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.data.serialization.SensorThingsSerde.JSONwithIdNameDescription;

import java.io.IOException;

public class FeatureOfInterestSerde {

    public class FeatureOfInterestServiceSerializer extends JsonSerializer<FeatureEntity> {

        @Override
        public void serialize(FeatureEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    public class FeatureOfInterestServiceDeserializer extends JsonDeserializer<FeatureEntity> {

        @Override
        public FeatureEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    class JSONFeatureOfInterestService extends JSONwithIdNameDescription {

        public FeatureEntity toEntity() {
            throw new NotYetImplementedException();
        }
    }
}
