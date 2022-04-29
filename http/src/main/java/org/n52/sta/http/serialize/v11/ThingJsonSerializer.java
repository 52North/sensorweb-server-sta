package org.n52.sta.http.serialize.v11;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Optional;
import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.entity.Thing;

public class ThingJsonSerializer extends JsonSerializer<Thing> {

    private final Optional<SelectFilter> selectFilter;

    public ThingJsonSerializer(QueryOptions queryOptions) {
        this.selectFilter = queryOptions != null
                ? Optional.ofNullable(queryOptions.getSelectFilter())
                : Optional.empty();
    }

    @Override
    public void serialize(Thing value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    }


//
//    @Override
//    public void serialize(Thing value, JsonGenerator gen, SerializerProvider provider) throws IOException {
//        gen.writeStartObject();
//
//        gen.writeS
//
//        gen.writeEndObject();
//    }


}
