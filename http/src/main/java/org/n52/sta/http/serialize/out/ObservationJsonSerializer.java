package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Observation;

public class ObservationJsonSerializer extends StaBaseSerializer<Observation> {

    protected ObservationJsonSerializer(SerializationContext context) {
        super(context, StaConstants.OBSERVATIONS, Observation.class);
    }

    @Override
    public void serialize(Observation value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        String id = value.getId();

        // entity properties
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, id));
        writeStringProperty(StaConstants.AT_IOT_SELFLINK, () -> createSelfLink(id), gen);
        
        writeObjectProperty(StaConstants.PROP_RESULT_QUALITY, value::getResultQuality, gen);
        writeObjectProperty(StaConstants.PROP_PARAMETERS, value::getParameters, gen);
        writeObjectProperty(StaConstants.PROP_RESULT, value::getResult, gen);
        writeTimeProperty(StaConstants.PROP_RESULT_TIME, value::getResultTime, gen);
        writeTimeProperty(StaConstants.PROP_VALID_TIME, value::getValidTime, gen);
        writeTimeProperty(StaConstants.PROP_PHENOMENON_TIME, value::getPhenomenonTime, gen);

        // entity members
        writeMember(StaConstants.DATASTREAM, id, gen, DatastreamJsonSerializer::new,
                serializer -> serializer.serialize(value.getDatastream(), gen, serializers));

        writeMember(StaConstants.FEATURES_OF_INTEREST, id, gen, FeatureOfInterestJsonSerializer::new,
                serializer -> serializer.serialize(value.getFeatureOfInterest(), gen, serializers));

    }

}
