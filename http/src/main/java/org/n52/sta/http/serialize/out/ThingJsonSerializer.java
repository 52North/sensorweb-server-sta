package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Thing;

public class ThingJsonSerializer extends StaBaseSerializer<Thing> {

    public ThingJsonSerializer(QueryOptions queryOptions) {
        super(queryOptions, Thing.class);
    }

    @Override
    public void serialize(Thing value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        // entity properties
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, value.getId()));
        String selfLink = createSelfLink(value.getId());
        writeProperty(StaConstants.AT_IOT_SELFLINK, name -> gen.writeStringField(name, selfLink));
        writeProperty(StaConstants.PROP_NAME, name -> gen.writeStringField(name, value.getName()));
        writeProperty(StaConstants.PROP_DESCRIPTION, name -> gen.writeStringField(name, value.getDescription()));
        writeProperty(StaConstants.PROP_PROPERTIES, name -> gen.writeObjectField(name, value.getProperties()));

        // entity members


        // TODO refactor to base class
        
        String member = StaConstants.DATASTREAMS;
        writeMemberArray(member, expandOptions -> {
            if (expandOptions.isPresent()) {
                gen.writeArrayFieldStart(member);
                DatastreamJsonSerializer dsSerializer = new DatastreamJsonSerializer(expandOptions.get());
                for (Datastream datastream : value.getDatastreams()) {
                    dsSerializer.serialize(datastream, gen, serializers);
                }
                gen.writeEndArray();
            } else {
                String navLink = createNavigationLink(value.getId(), member);
                String iotNavLinkProperty = String.format("%s%s", member, StaConstants.AT_IOT_NAVIGATIONLINK);
                gen.writeStringField(iotNavLinkProperty, navLink);
            }
        });
        
        gen.writeEndObject();
    }

}
