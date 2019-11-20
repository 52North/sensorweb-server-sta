package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.data.serialization.ElementWithQueryOptions.FeatureOfInterestWithQueryOptions;
import org.n52.sta.data.serialization.STASerdesTypes.JSONwithIdNameDescription;
import org.n52.sta.edm.provider.entities.FeatureOfInterestEntityDefinition;
import org.n52.sta.edm.provider.entities.STAEntityDefinition;
import org.n52.sta.service.query.QueryOptions;

import java.io.IOException;
import java.util.Set;

public class FeatureOfInterestSerdes {

    public static class FeatureOfInterestSerializer extends AbstractSTASerializer<FeatureOfInterestWithQueryOptions> {

        private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

        public FeatureOfInterestSerializer(String rootUrl) {
            super(FeatureOfInterestWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = FeatureOfInterestEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(FeatureOfInterestWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            AbstractFeatureEntity feature = value.getEntity();
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
                writeId(gen, feature.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, feature.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, feature.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, feature.getDescription());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                // only write out encodingtype if there is a location present
                if (feature.isSetGeometry()) {
                    gen.writeStringField(STAEntityDefinition.PROP_ENCODINGTYPE, ENCODINGTYPE_GEOJSON);
                }
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_FEATURE)) {
                gen.writeStringField(STAEntityDefinition.PROP_FEATURE, feature.getGeometryEntity().toString());
            }


            // navigation properties
            for (String navigationProperty : FeatureOfInterestEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, feature.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }
    }

    public static class FeatureOfInterestDeserializer extends JsonDeserializer<FeatureEntity> {

        @Override
        public FeatureEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    class JSONFeatureOfInterest extends JSONwithIdNameDescription {

        public FeatureEntity toEntity() {
            throw new NotYetImplementedException();
        }
    }
}
