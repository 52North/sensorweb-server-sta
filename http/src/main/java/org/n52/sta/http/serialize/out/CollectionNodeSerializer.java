package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.n52.shetland.ogc.sta.StaConstants;

public class CollectionNodeSerializer extends StdSerializer<CollectionNode> {

    private SerializationContext context;

    public CollectionNodeSerializer(SerializationContext context) {
        super(CollectionNode.class);
        this.context = context;
    }

    @Override
    public void serialize(CollectionNode value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        if (context.isCounted()) {
            gen.writeNumberField(StaConstants.AT_IOT_COUNT, value.getTotalEntityCount());
        }

        gen.writeArrayFieldStart("value");
        provider.defaultSerializeValue(value.getEntities(), gen);
        gen.writeEndArray();

        gen.writeEndObject();
    }
}
