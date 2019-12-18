package org.n52.sta.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.sta.serdes.json.JSONObservedProperty;
import org.n52.sta.serdes.model.ElementWithQueryOptions.ObservedPropertyWithQueryOptions;
import org.n52.sta.serdes.model.ObservedPropertyEntityDefinition;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.utils.QueryOptions;

import java.io.IOException;
import java.util.Set;

public class ObservedPropertySerde {

    public static class PhenomenonEntityPatch extends PhenomenonEntity implements EntityPatch<PhenomenonEntity> {
        private final PhenomenonEntity entity;

        public PhenomenonEntityPatch (PhenomenonEntity entity) {
            this.entity = entity;
        }

        public PhenomenonEntity getEntity() {
            return entity;
        }
    }


    public static class ObservedPropertySerializer extends AbstractSTASerializer<ObservedPropertyWithQueryOptions> {

        private final String rootUrl;
        private final String entitySetName;

        public ObservedPropertySerializer(String rootUrl) {
            super(ObservedPropertyWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = ObservedPropertyEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(ObservedPropertyWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            PhenomenonEntity obsProp = value.getEntity();
            QueryOptions options = value .getQueryOptions();

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
                writeId(gen, obsProp.getStaIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, obsProp.getStaIdentifier());
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, obsProp.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, obsProp.getDescription());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DEFINITION)) {
                gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, obsProp.getIdentifier());
            }

            // navigation properties
            for (String navigationProperty : ObservedPropertyEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, obsProp.getStaIdentifier());
                }
            }
            //TODO: Deal with $expand

            gen.writeEndObject();
        }
    }

    public static class ObservedPropertyDeserializer extends StdDeserializer<PhenomenonEntity> {

        public ObservedPropertyDeserializer() {
            super(PhenomenonEntity.class);
        }

        @Override
        public PhenomenonEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONObservedProperty.class).toEntity();
        }
    }

    public static class ObservedPropertyPatchDeserializer extends StdDeserializer<PhenomenonEntityPatch> {

        public ObservedPropertyPatchDeserializer() {
            super(PhenomenonEntityPatch.class);
        }

        @Override
        public PhenomenonEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new PhenomenonEntityPatch(p.readValueAs(JSONObservedProperty.class).toEntity(false));
        }
    }
}
