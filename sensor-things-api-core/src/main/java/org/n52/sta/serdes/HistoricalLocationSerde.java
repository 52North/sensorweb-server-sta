package org.n52.sta.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.sta.serdes.json.JSONHistoricalLocation;
import org.n52.sta.serdes.model.HistoricalLocationEntityDefinition;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.serdes.model.ElementWithQueryOptions.HistoricalLocationWithQueryOptions;
import org.n52.sta.service.query.QueryOptions;

import java.io.IOException;
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
            return p.readValueAs(JSONHistoricalLocation.class).toEntity();
        }
    }
}
