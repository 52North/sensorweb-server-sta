package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.n52.series.db.beans.PlatformEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
public class JSONThing extends JSONBase.JSONwithIdNameDescription<PlatformEntity> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public JsonNode properties;
    @JsonManagedReference
    public JSONLocation[] Locations;
    @JsonManagedReference
    public JSONDatastream[] Datastreams;

    public JSONThing() {
        self = new PlatformEntity();
    }

    public PlatformEntity toEntity() {
        self = new PlatformEntity();

        // Check if Entity is only referenced via id and not provided fully
        // More complex since implementation allows custom setting of id by user
        if (!generatedId && name == null) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(properties, INVALID_REFERENCED_ENTITY);

            Assert.isNull(Locations, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Datastreams, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        } else {
            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");

            self.setIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);

            //TODO: check if this is correct
            if (properties != null) {
                self.setProperties(properties.toString());
            }

            if (Locations != null) {
                self.setLocations(Arrays.stream(Locations)
                        .map(JSONLocation::toEntity)
                        .collect(Collectors.toSet()));
            }

            if (Datastreams != null) {
                self.setDatastreams(Arrays.stream(Datastreams)
                        .map(JSONDatastream::toEntity)
                        .collect(Collectors.toSet()));
            }

            // Deal with back reference during deep insert
            if (backReference != null) {
                if (backReference instanceof JSONLocation) {
                    self.addLocationEntity(((JSONLocation) backReference).getEntity());
                } else if (backReference instanceof JSONDatastream) {
                    if (self.getDatastreams() != null) {
                        self.getDatastreams().add(((JSONDatastream) backReference).getEntity());
                    } else {
                        self.setDatastreams(Collections.singleton(((JSONDatastream) backReference).getEntity()));
                    }
                } else {
                    self.addHistoricalLocation(((JSONHistoricalLocation) backReference).getEntity());
                }
            }

            return self;
        }
    }
}
