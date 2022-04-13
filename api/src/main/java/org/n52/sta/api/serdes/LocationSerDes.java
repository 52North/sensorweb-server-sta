/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.api.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.n52.shetland.ogc.sta.model.LocationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.serdes.common.AbstractSTASerializer;
import org.n52.sta.api.serdes.common.JSONBase;
import org.n52.sta.api.serdes.json.JSONLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import org.n52.sta.api.dto.common.EntityPatch;

public class LocationSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationSerDes.class);


    public static class LocationDTOPatch implements EntityPatch<LocationDTO> {

        private static final long serialVersionUID = -8421752856535036959L;
        private final LocationDTO entity;

        LocationDTOPatch(LocationDTO entity) {
            this.entity = entity;
        }

        public LocationDTO getEntity() {
            return entity;
        }
    }


    public static class LocationSerializer extends AbstractSTASerializer<LocationDTO> {

        private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();
        private static final long serialVersionUID = 5481294508394633788L;

        public LocationSerializer(String rootUrl, String... activeExtensions) {
            super(LocationDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = LocationEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(LocationDTO location, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!location.hasSelectOption() || location.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, location.getId());
            }
            if (!location.hasSelectOption() ||
                location.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, location.getId());
            }

            // actual properties
            if (!location.hasSelectOption() ||
                location.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, location.getName());
            }
            if (!location.hasSelectOption()
                || location.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, location.getDescription());
            }
            if (!location.hasSelectOption()
                || location.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                // only write out encodingtype if there is a location present
                if (location.getGeometry() != null) {
                    gen.writeStringField(STAEntityDefinition.PROP_ENCODINGTYPE, ENCODINGTYPE_GEOJSON);
                }
            }
            if (!location.hasSelectOption()
                || location.getFieldsToSerialize().contains(STAEntityDefinition.PROP_LOCATION)) {

                gen.writeFieldName(STAEntityDefinition.PROP_LOCATION);
                gen.writeRawValue(GEO_JSON_WRITER.write(location.getGeometry()));
            }
            if (!location.hasSelectOption() ||
                location.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                if (location.getProperties() != null) {
                    gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, location.getProperties());
                }
            }

            // navigation properties
            for (String navigationProperty : LocationEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!location.hasSelectOption() || location.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!location.hasExpandOption() || location.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, location.getId());
                    } else {
                        switch (navigationProperty) {
                            case LocationEntityDefinition.THINGS:
                                if (location.getThings() == null) {
                                    writeNavigationProp(gen, navigationProperty, location.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(location.getThings()),
                                                          gen,
                                                          serializers);
                                }
                                break;
                            case LocationEntityDefinition.HISTORICAL_LOCATIONS:
                                if (location.getHistoricalLocations() == null) {
                                    writeNavigationProp(gen, navigationProperty, location.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(
                                        Collections.unmodifiableSet(location.getHistoricalLocations()),
                                        gen,
                                        serializers);
                                }
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + navigationProperty);
                        }
                    }
                }
            }
            gen.writeEndObject();
        }
    }


    public static class LocationDeserializer extends StdDeserializer<LocationDTO> {

        private static final long serialVersionUID = -5978576540306282111L;

        public LocationDeserializer() {
            super(LocationDTO.class);
        }

        @Override
        public LocationDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONLocation.class)
                .parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class LocationPatchDeserializer extends StdDeserializer<LocationDTOPatch> {

        private static final long serialVersionUID = -4192059850856965261L;

        public LocationPatchDeserializer() {
            super(LocationDTOPatch.class);
        }

        @Override
        public LocationDTOPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new LocationDTOPatch(p.readValueAs(JSONLocation.class)
                                            .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
