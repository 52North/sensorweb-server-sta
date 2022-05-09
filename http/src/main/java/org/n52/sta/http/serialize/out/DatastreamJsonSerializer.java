package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Thing;

public class DatastreamJsonSerializer extends StaBaseSerializer<Datastream> {

    public DatastreamJsonSerializer(SerializationContext context) {
        super(context, StaConstants.DATASTREAMS, Datastream.class);
    }

    @Override
    public void serialize(Datastream value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        String id = value.getId();

        // entity properties
        
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, id));
        writeStringProperty(StaConstants.AT_IOT_SELFLINK, () -> createSelfLink(id), gen);
        writeStringProperty(StaConstants.PROP_NAME, value::getName, gen);
        writeStringProperty(StaConstants.PROP_DESCRIPTION, value::getDescription, gen);
        writeObjectProperty(StaConstants.PROP_PROPERTIES, value::getProperties, gen);

        // entity members
        String member = StaConstants.THING;
        writeMember(member, id, gen, ThingJsonSerializer::new, writer(value.getThing(), gen, serializers));
        
        // writeMemberArray(StaConstants.DATASTREAMS, name -> {
        //     gen.writeArrayFieldStart(name);
        //     for (Datastream datastream : value.getDatastreams()) {
        //         serializer.serialize(datastream, gen, serializers);
        //     }
        //     gen.writeEndArray();
        // });

        // Datastreams
        // ...

        gen.writeEndObject();
    }

    private ThrowingMemberWriter<Thing> writer(Thing thing, JsonGenerator gen, SerializerProvider serializers) {
        return serializer -> serializer.serialize(thing, gen, serializers);
    }

}
