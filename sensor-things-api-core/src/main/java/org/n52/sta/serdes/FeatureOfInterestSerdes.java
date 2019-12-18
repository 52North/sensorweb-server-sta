package org.n52.sta.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.serdes.json.JSONFeatureOfInterest;
import org.n52.sta.serdes.model.ElementWithQueryOptions.FeatureOfInterestWithQueryOptions;
import org.n52.sta.serdes.model.FeatureOfInterestEntityDefinition;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.utils.QueryOptions;

import java.io.IOException;
import java.util.Set;

public class FeatureOfInterestSerdes {

    public static class AbstractFeatureEntityPatch extends AbstractFeatureEntity implements EntityPatch<AbstractFeatureEntity> {
        private final AbstractFeatureEntity entity;

        public AbstractFeatureEntityPatch(FeatureEntity entity) {
            this.entity = entity;
        }

        public AbstractFeatureEntity getEntity() {
            return entity;
        }
    }

    public static class FeatureOfInterestSerializer extends AbstractSTASerializer<FeatureOfInterestWithQueryOptions> {

        private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();

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
                gen.writeObjectFieldStart(STAEntityDefinition.PROP_LOCATION);
                gen.writeStringField("type", "Feature");
                gen.writeObjectFieldStart("geometry");
                gen.writeRaw(GEO_JSON_WRITER.write(feature.getGeometryEntity().getGeometry()));
                gen.writeEndObject();
                gen.writeEndObject();
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

    public static class FeatureOfInterestDeserializer extends StdDeserializer<AbstractFeatureEntity<?>> {

        public FeatureOfInterestDeserializer() {
            super(AbstractFeatureEntity.class);
        }

        @Override
        public AbstractFeatureEntity<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONFeatureOfInterest.class).toEntity();
        }
    }

    public static class FeatureOfInterestPatchDeserializer extends StdDeserializer<AbstractFeatureEntityPatch> {

        public FeatureOfInterestPatchDeserializer() {
            super(AbstractFeatureEntityPatch.class);
        }

        @Override
        public AbstractFeatureEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new AbstractFeatureEntityPatch(p.readValueAs(JSONFeatureOfInterest.class)
                    .toEntity(false));
        }
    }
}
