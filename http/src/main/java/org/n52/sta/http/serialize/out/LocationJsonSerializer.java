package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class LocationJsonSerializer extends StaBaseSerializer<Location> {

    public LocationJsonSerializer(SerializationContext context) {
        super(context, StaConstants.LOCATIONS, Location.class);
    }

    @Override
    public void serialize(Location value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        String id = value.getId();

        // entity properties
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, id));
        writeStringProperty(StaConstants.AT_IOT_SELFLINK, () -> createSelfLink(id), gen);
        writeStringProperty(StaConstants.PROP_NAME, value::getName, gen);
        writeStringProperty(StaConstants.PROP_DESCRIPTION, value::getDescription, gen);
        writeObjectProperty(StaConstants.PROP_PROPERTIES, value::getProperties, gen);

        Geometry geometry = value.getGeometry();
        writeGeometry(StaConstants.PROP_LOCATION, value::getGeometry, gen);
        if (geometry != null) {
            // only write out encodingtype if there is a location present
            writeStringProperty(StaConstants.PROP_ENCODINGTYPE, () -> ENCODINGTYPE_GEOJSON, gen);
        }

        // entity members
        String things = StaConstants.LOCATIONS;
        writeMemberCollection(things, id, gen, ThingJsonSerializer::new, serializer -> {
            for (Thing item : value.getThings()) {
                serializer.serialize(item, gen, serializers);
            }
        });

        String hitoricalLocations = StaConstants.HISTORICAL_LOCATIONS;
        writeMemberCollection(hitoricalLocations, id, gen, HistoricalLocationJsonSerializer::new,
                serializer -> {
                    for (HistoricalLocation item : value.getHistoricalLocations()) {
                        serializer.serialize(item, gen, serializers);
                    }
                });

        gen.writeEndObject();
    }

}
