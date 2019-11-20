package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.sta.data.serialization.ElementWithQueryOptions.SensorWithQueryOptions;
import org.n52.sta.data.serialization.STASerdesTypes.JSONwithIdNameDescription;
import org.n52.sta.edm.provider.entities.STAEntityDefinition;
import org.n52.sta.edm.provider.entities.SensorEntityDefinition;
import org.n52.sta.service.query.QueryOptions;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class SensorSerdes {

    public static class SensorSerializer extends AbstractSTASerializer<SensorWithQueryOptions> {

        private static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
        private static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";

        public SensorSerializer(String rootUrl) {
            super(SensorWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = SensorEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(SensorWithQueryOptions value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            ProcedureEntity sensor = value.getEntity();
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
                writeId(gen, sensor.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, sensor.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, sensor.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, sensor.getDescription());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_METADATA)) {
                String format = sensor.getFormat().getFormat();
                if (format.equalsIgnoreCase(SENSORML_2)) {
                    format = STA_SENSORML_2;
                }
                gen.writeObjectField(STAEntityDefinition.PROP_METADATA, format);
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                String metadata = "metadata";
                if (sensor.getDescriptionFile() != null && !sensor.getDescriptionFile().isEmpty()) {
                    metadata = sensor.getDescriptionFile();
                } else if (sensor.hasProcedureHistory()) {
                    Optional<ProcedureHistoryEntity> history =
                            sensor.getProcedureHistory().stream().filter(h -> h.getEndTime() == null).findFirst();
                    if (history.isPresent()) {
                        metadata = history.get().getXml();
                    }
                }
                gen.writeStringField(STAEntityDefinition.PROP_ENCODINGTYPE, metadata);
            }

            // navigation properties
            for (String navigationProperty : SensorEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, sensor.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }
    }

    public static class SensorDeserializer extends JsonDeserializer<SensorEntity> {

        @Override
        public SensorEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();

            //TODO: chekc sensorML2
        }
    }

    class JSONSensor extends JSONwithIdNameDescription {
        public String properties;
        public String encodingType;
        public String metadata;

        public SensorEntity toEntity() {


            throw new NotYetImplementedException();
        }
    }
}
