package org.n52.sta.data.serialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.data.serialization.ElementWithQueryOptions.HistoricalLocationWithQueryOptions;
import org.n52.sta.data.serialization.LocationSerdes.JSONLocation;
import org.n52.sta.data.serialization.STASerdesTypes.JSONwithId;
import org.n52.sta.data.serialization.ThingSerdes.JSONThing;
import org.n52.sta.edm.provider.entities.HistoricalLocationEntityDefinition;
import org.n52.sta.edm.provider.entities.STAEntityDefinition;
import org.n52.sta.exception.ParsingException;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.utils.TimeUtil;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

public class HistoricalLocationSerde {

    public static class HistoricalLocationSerializer extends AbstractSTASerializer<HistoricalLocationWithQueryOptions> {

        public HistoricalLocationSerializer(String rootUrl) {
            super(HistoricalLocationWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = HistoricalLocationEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(HistoricalLocationWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            HistoricalLocationEntity histLoc = value.getEntity();
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
                writeId(gen, histLoc.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, histLoc.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_TIME)) {
                gen.writeStringField(STAEntityDefinition.PROP_TIME, histLoc.getTime().toString());
            }

            // navigation properties
            for (String navigationProperty : HistoricalLocationEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, histLoc.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }
    }

    public static class HistoricalLocationDeserializer extends JsonDeserializer<HistoricalLocationEntity> {

        @Override
        public HistoricalLocationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
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
