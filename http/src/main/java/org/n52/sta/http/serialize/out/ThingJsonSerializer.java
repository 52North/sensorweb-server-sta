package org.n52.sta.http.serialize.out;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Thing;

public class ThingJsonSerializer extends StaBaseSerializer<Thing> {

    public ThingJsonSerializer(SerializationContext context) {
        super(context, StaConstants.THINGS, Thing.class);
    }

    @Override
    public void serialize(Thing value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        String thingId = value.getId();

        // entity properties
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, thingId));
        String selfLink = createSelfLink(thingId);
        writeProperty(StaConstants.AT_IOT_SELFLINK, name -> gen.writeStringField(name, selfLink));
        writeProperty(StaConstants.PROP_NAME, name -> gen.writeStringField(name, value.getName()));
        writeProperty(StaConstants.PROP_DESCRIPTION, name -> gen.writeStringField(name, value.getDescription()));
        writeProperty(StaConstants.PROP_PROPERTIES, name -> gen.writeObjectField(name, value.getProperties()));

        // entity members
        String member = StaConstants.DATASTREAMS;
        Set<Datastream> collection = value.getDatastreams();
        writeMember(member, thingId, gen, DatastreamJsonSerializer::new, serializer -> {
            gen.writeArrayFieldStart(member);
            for (Datastream item : collection) {
                serializer.serialize(item, gen, serializers);
            }
            gen.writeEndArray();
        });
        
        gen.writeEndObject();
    }

}
