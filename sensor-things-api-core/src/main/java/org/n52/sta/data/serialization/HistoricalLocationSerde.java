package org.n52.sta.data.serialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.data.serialization.LocationSerde.JSONLocation;
import org.n52.sta.data.serialization.SensorThingsSerde.JSONwithId;
import org.n52.sta.data.serialization.ThingSerde.JSONThing;
import org.n52.sta.exception.ParsingException;
import org.n52.sta.utils.TimeUtil;

import java.io.IOException;
import java.util.Date;

public class HistoricalLocationSerde {

    public class HistoricalLocationSerializer extends JsonSerializer<HistoricalLocationEntity> {

        @Override
        public void serialize(HistoricalLocationEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    public class HistoricalLocationDeserializer extends JsonDeserializer<HistoricalLocationEntity> {

        @Override
        public HistoricalLocationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            throw new NotYetImplementedException();
        }
    }

    class JSONHistoricalLocation extends JSONwithId {

        @JsonProperty("time")
        public Object rawTime;
        @JsonProperty("Things@iot.navigationLink")
        public JSONThing thing;
        @JsonProperty("Locations@iot.navigationLink")
        public JSONLocation[] locations;

        private Date date;

        /**
         * Wrapper around rawTime property called by jackson while deserializing.
         *
         * @param rawTime raw Time
         */
        public void setRawTime(Object rawTime) throws ParsingException {
            Time time = TimeUtil.parseTime(rawTime);
            if (time instanceof TimeInstant) {
                date = ((TimeInstant) time).getValue().toDate();
            } else if (time instanceof TimePeriod) {
                date = ((TimePeriod) time).getEnd().toDate();
            } else {
                //TODO: refine error message
                throw new ParsingException("Invalid time format.");
            }
        }

        public HistoricalLocationEntity toEntity() {
            HistoricalLocationEntity entity = new HistoricalLocationEntity();
            entity.setIdentifier(identifier);
            entity.setTime(date);
            entity.setThing(thing.toEntity());
            for (JSONLocation location : locations) {
                entity.addLocationEntity(location.toEntity());
            }
            return entity;
        }
    }
}
