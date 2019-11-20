package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.sta.data.serialization.ElementWithQueryOptions.DatastreamWithQueryOptions;
import org.n52.sta.data.serialization.STASerdesTypes.JSONwithIdNameDescription;
import org.n52.sta.edm.provider.entities.DatastreamEntityDefinition;
import org.n52.sta.edm.provider.entities.STAEntityDefinition;
import org.n52.sta.service.query.QueryOptions;

import java.io.IOException;
import java.util.Set;

public class DatastreamSerdes {

    public static class DatastreamSerializer extends AbstractSTASerializer<DatastreamWithQueryOptions> {

        public DatastreamSerializer(String rootUrl) {
            super(DatastreamWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = DatastreamEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(DatastreamWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            DatastreamEntity datastream = value.getEntity();
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
                writeId(gen, datastream.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, datastream.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, datastream.getDescription());
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_OBSERVATION_TYPE)) {
                gen.writeObjectField(STAEntityDefinition.PROP_OBSERVATION_TYPE,
                        datastream.getObservationType().getFormat());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_UOM)) {
                gen.writeObjectFieldStart(STAEntityDefinition.PROP_UOM);
                gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getUnitOfMeasurement().getName());
                gen.writeStringField(STAEntityDefinition.PROP_SYMBOL, datastream.getUnitOfMeasurement().getSymbol());
                gen.writeStringField(STAEntityDefinition.PROP_DEFINITION, datastream.getUnitOfMeasurement().getLink());
                gen.writeEndObject();
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_OBSERVED_AREA)) {
                gen.writeStringField(STAEntityDefinition.PROP_OBSERVED_AREA, datastream.getGeometryEntity().toString());
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_TIME)) {
                //TODO: implement
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
                //TODO: implement
            }

            // navigation properties
            for (String navigationProperty : DatastreamEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, datastream.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }
    }

    public static class DatastreamDeserializer extends JsonDeserializer<DatastreamEntity> {

        @Override
        public DatastreamEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    class JSONDatastream extends JSONwithIdNameDescription {

        public DatastreamEntity toEntity() {
            throw new NotYetImplementedException();
        }
    }
}
