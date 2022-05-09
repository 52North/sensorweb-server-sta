package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.HistoricalLocation;

public class HistoricalLocationJsonSerializer extends StaBaseSerializer<HistoricalLocation> {

    public HistoricalLocationJsonSerializer(SerializationContext context) {
        super(context, StaConstants.HISTORICAL_LOCATIONS, HistoricalLocation.class);
    }

    @Override
    public void serialize(HistoricalLocation value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        String id = value.getId();

        // entity properties
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, id));
        writeStringProperty(StaConstants.AT_IOT_SELFLINK, () -> createSelfLink(id), gen);
        writeTimeProperty(StaConstants.PROP_TIME, value::getTime, gen);

        gen.writeEndObject();
        
    }
}
