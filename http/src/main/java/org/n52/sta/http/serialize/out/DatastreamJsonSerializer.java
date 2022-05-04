package org.n52.sta.http.serialize.out;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;

public class DatastreamJsonSerializer extends StaBaseSerializer<Datastream> {

    public DatastreamJsonSerializer(QueryOptions queryOptions) {
        super(queryOptions, Datastream.class);
    }

    @Override
    public void serialize(Datastream value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        // entity properties
        gen.writeStringField(StaConstants.AT_IOT_ID, value.getId());
        String selfLink = createSelfLink(value.getId());
        writeProperty(StaConstants.AT_IOT_SELFLINK, name -> gen.writeStringField(name, selfLink));
        writeProperty(StaConstants.PROP_NAME, name -> gen.writeStringField(name, value.getName()));
        writeProperty(StaConstants.PROP_DESCRIPTION, name -> gen.writeStringField(name, value.getDescription()));
        writeProperty(StaConstants.PROP_PROPERTIES, name -> gen.writeObjectField(name, value.getProperties()));

        // entity members
        Optional<QueryOptions> expandedQueryOptions = getQueryOptionsForExpanded(StaConstants.THING);
        if (expandedQueryOptions.isPresent()) {
            QueryOptions queryOptions = expandedQueryOptions.get();
            ThingJsonSerializer thingSerializer = new ThingJsonSerializer(queryOptions);
            thingSerializer.serialize(value.getThing(), gen, serializers);
        }
        
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

}
