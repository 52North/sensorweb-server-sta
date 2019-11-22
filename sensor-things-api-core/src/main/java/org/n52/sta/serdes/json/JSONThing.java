package org.n52.sta.serdes.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.n52.series.db.beans.PlatformEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JSONThing extends JSONBase.JSONwithIdNameDescription implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public JsonNode properties;
    public JSONLocation[] Locations;
    public JSONDatastream[] Datastreams;

    public JSONThing() {
    }

    public PlatformEntity toEntity() {
        PlatformEntity thing = new PlatformEntity();

        // Check if Entity is only referenced via id and not provided fully
        // More complex since implementation allows custom setting of id by user
        if (!generatedId && name == null) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Locations, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Datastreams, INVALID_REFERENCED_ENTITY);

            thing.setIdentifier(identifier);
            return thing;
        } else {
            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");

            thing.setIdentifier(identifier);
            thing.setName(name);
            thing.setDescription(description);

            //TODO: check if this is correct
            thing.setProperties(properties.toString());

            if (Locations != null) {
                thing.setLocations(Arrays.stream(Locations)
                        .map(JSONLocation::toEntity)
                        .collect(Collectors.toSet()));
            }
            if (Datastreams != null) {
                thing.setDatastreams(Arrays.stream(Datastreams)
                        .map(JSONDatastream::toEntity)
                        .collect(Collectors.toSet()));
            }
            return thing;
        }
    }
}
