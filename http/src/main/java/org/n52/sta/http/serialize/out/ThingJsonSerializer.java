package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class ThingJsonSerializer extends StaBaseSerializer<Thing> {

    public ThingJsonSerializer(SerializationContext context) {
        super(context, StaConstants.THINGS, Thing.class);
    }

    @Override
    public void serialize(Thing value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        String id = value.getId();

        // entity properties
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, id));
        writeStringProperty(StaConstants.AT_IOT_SELFLINK, () -> createSelfLink(id), gen);
        writeStringProperty(StaConstants.PROP_NAME, value::getName, gen);
        writeStringProperty(StaConstants.PROP_DESCRIPTION, value::getDescription, gen);
        writeObjectProperty(StaConstants.PROP_PROPERTIES, value::getProperties, gen);

        // entity members
        String datastreams = StaConstants.DATASTREAMS;
        writeMemberCollection(datastreams, id, gen, DatastreamJsonSerializer::new, serializer -> {
            for (Datastream item : value.getDatastreams()) {
                serializer.serialize(item, gen, serializers);
            }
        });

        String locations = StaConstants.LOCATIONS;
        writeMemberCollection(locations, id, gen, LocationJsonSerializer::new, serializer -> {
            for (Location item : value.getLocations()) {
                serializer.serialize(item, gen, serializers);
            }
        });

        String historicalLocations = StaConstants.HISTORICAL_LOCATIONS;
        writeMemberCollection(historicalLocations, id, gen, HistoricalLocationJsonSerializer::new, serializer -> {
            for (HistoricalLocation item : value.getHistoricalLocations()) {
                serializer.serialize(item, gen, serializers);
            }
        });
        
        gen.writeEndObject();
    }

}
