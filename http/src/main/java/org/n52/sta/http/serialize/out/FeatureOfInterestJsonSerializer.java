package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;

public class FeatureOfInterestJsonSerializer extends StaBaseSerializer<FeatureOfInterest> {

    protected FeatureOfInterestJsonSerializer(SerializationContext context) {
        super(context, StaConstants.FEATURES_OF_INTEREST, FeatureOfInterest.class);
    }

    @Override
    public void serialize(FeatureOfInterest value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        String id = value.getId();

        // entity properties
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, id));
        writeStringProperty(StaConstants.AT_IOT_SELFLINK, () -> createSelfLink(id), gen);
        writeStringProperty(StaConstants.PROP_NAME, value::getName, gen);
        writeStringProperty(StaConstants.PROP_DESCRIPTION, value::getDescription, gen);
        writeObjectProperty(StaConstants.PROP_DESCRIPTION, value::getProperties, gen);
        writeGeometryAndEncodingType(StaConstants.PROP_FEATURE, value::getFeature, gen);
        
        // entity members
        String observations = StaConstants.OBSERVATIONS;
        writeMemberCollection(observations, id, gen, ObservationJsonSerializer::new, serializer -> {
            for (Observation<?> item : value.getObservations()) {
                serializer.serialize(item, gen, serializers);
            }
        });
    }

}
