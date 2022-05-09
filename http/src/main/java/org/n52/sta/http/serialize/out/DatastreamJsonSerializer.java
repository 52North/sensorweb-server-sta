package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Observation;

public class DatastreamJsonSerializer extends StaBaseSerializer<Datastream> {

    public DatastreamJsonSerializer(SerializationContext context) {
        super(context, StaConstants.DATASTREAMS, Datastream.class);
    }

    @Override
    public void serialize(Datastream value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        String id = value.getId();

        // entity properties
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, id));
        writeStringProperty(StaConstants.AT_IOT_SELFLINK, () -> createSelfLink(id), gen);
        writeStringProperty(StaConstants.PROP_NAME, value::getName, gen);
        writeStringProperty(StaConstants.PROP_DESCRIPTION, value::getDescription, gen);
        writeStringProperty(StaConstants.PROP_OBSERVATION_TYPE, value::getObservationType, gen);
        
        writeObjectProperty(StaConstants.PROP_UOM, value::getUnitOfMeasurement, gen);
        writeObjectProperty(StaConstants.PROP_PROPERTIES, value::getProperties, gen);
        writeTimeProperty(StaConstants.PROP_RESULT_TIME, value::getResultTime, gen);
        writeTimeProperty(StaConstants.PROP_PHENOMENON_TIME, value::getPhenomenonTime, gen);
        writeGeometryAndEncodingType(StaConstants.PROP_OBSERVED_AREA, value::getObservedArea, gen);

        // entity members
        String observations = StaConstants.OBSERVATIONS;
        writeMemberCollection(observations, id, gen, ObservationJsonSerializer::new, serializer -> {
            for (Observation<?> item : value.getObservations()) {
                serializer.serialize(item, gen, serializers);
            }
        });

        writeMember(StaConstants.THING, id, gen, ThingJsonSerializer::new, 
                serializer -> serializer.serialize(value.getThing(), gen, serializers));
        
        writeMember(StaConstants.SENSOR, id, gen, SensorJsonSerializer::new,
                serializer -> serializer.serialize(value.getSensor(), gen, serializers));

        writeMember(StaConstants.OBSERVED_PROPERTY, id, gen, ObservedPropertyJsonSerializer::new,
                serializer -> serializer.serialize(value.getObservedProperty(), gen, serializers));

        gen.writeEndObject();
    }

}
