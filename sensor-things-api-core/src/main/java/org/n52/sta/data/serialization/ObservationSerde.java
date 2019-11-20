package org.n52.sta.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.BlobDataEntity;
import org.n52.series.db.beans.BooleanDataEntity;
import org.n52.series.db.beans.CategoryDataEntity;
import org.n52.series.db.beans.ComplexDataEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataArrayDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.GeometryDataEntity;
import org.n52.series.db.beans.ProfileDataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ReferencedDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.sta.data.serialization.ElementWithQueryOptions.ObservationWithQueryOptions;
import org.n52.sta.data.serialization.STASerdesTypes.JSONwithIdNameDescription;
import org.n52.sta.edm.provider.entities.ObservationEntityDefinition;
import org.n52.sta.edm.provider.entities.STAEntityDefinition;
import org.n52.sta.service.query.QueryOptions;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

public class ObservationSerde {

    public static class ObservationSerializer extends AbstractSTASerializer<ObservationWithQueryOptions> {

        public ObservationSerializer(String rootUrl) {
            super(ObservationWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = ObservationEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(ObservationWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            DataEntity<?> observation = value.getEntity();
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
                writeId(gen, observation.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, observation.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT)) {
                gen.writeStringField(STAEntityDefinition.PROP_RESULT, getResult(observation));
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_TIME)) {
                Date resultTime = observation.getResultTime();
                Date samplingTime = observation.getSamplingTimeEnd();
                if (!resultTime.equals(samplingTime)) {
                    gen.writeStringField(STAEntityDefinition.PROP_RESULT_TIME, resultTime.toString());
                }
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
                //TODO: implement
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_QUALITY)) {
                //TODO: implement
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_VALID_TIME)) {
                //TODO: implement
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PARAMETERS)) {
                if (observation.hasParameters()) {
                    gen.writeArrayFieldStart(STAEntityDefinition.PROP_PARAMETERS);
                    for (ParameterEntity<?> parameter : observation.getParameters()) {
                        gen.writeStringField(parameter.getName(), parameter.getValueAsString());
                    }
                    gen.writeEndArray();
                }
            }

            // navigation properties
            for (String navigationProperty : ObservationEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, observation.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }
    }

    private static String getResult(DataEntity o) {
        if (o instanceof QuantityDataEntity) {
            if ((((QuantityDataEntity) o).getValue().doubleValue() - ((QuantityDataEntity) o).getValue()
                    .intValue()) == 0.0) {
                return Integer.toString(((QuantityDataEntity) o).getValue().intValue());
            }
            return ((QuantityDataEntity) o).getValue().toString();
        } else if (o instanceof BlobDataEntity) {
            // TODO: check if Object.tostring is what we want here
            return o.getValue().toString();
        } else if (o instanceof BooleanDataEntity) {
            return ((BooleanDataEntity) o).getValue().toString();
        } else if (o instanceof CategoryDataEntity) {
            return ((CategoryDataEntity) o).getValue();
        } else if (o instanceof ComplexDataEntity) {

            // TODO: implement
            // return ((ComplexDataEntity)o).getValue();
            return null;

        } else if (o instanceof CountDataEntity) {
            return ((CountDataEntity) o).getValue().toString();
        } else if (o instanceof GeometryDataEntity) {

            // TODO: check if we want WKT here
            return ((GeometryDataEntity) o).getValue().getGeometry().toText();

        } else if (o instanceof TextDataEntity) {
            return ((TextDataEntity) o).getValue();
        } else if (o instanceof DataArrayDataEntity) {

            // TODO: implement
            // return ((DataArrayDataEntity)o).getValue();
            return null;

        } else if (o instanceof ProfileDataEntity) {

            // TODO: implement
            // return ((ProfileDataEntity)o).getValue();
            return null;

        } else if (o instanceof ReferencedDataEntity) {
            return ((ReferencedDataEntity) o).getValue();
        }
        return "";
    }

    public static class ObservationDeserializer extends JsonDeserializer<DataEntity<?>> {

        @Override
        public DataEntity<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new NotYetImplementedException();
        }
    }

    class JSONObservation extends JSONwithIdNameDescription {

        public DataEntity<?> toEntity() {
            throw new NotYetImplementedException();
        }
    }
}
