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
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.ogc.sta.model.ThingEntityDefinition;
import org.n52.sta.api.serdes.common.AbstractSTASerializer;
import org.n52.sta.api.serdes.common.JSONBase;
import org.n52.sta.api.serdes.json.JSONThing;

import java.io.IOException;
import java.util.Collections;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.api.dto.common.EntityPatch;

public class ThingSerDes {

    public static class ThingDTOPatch implements EntityPatch<ThingDTO> {

        private static final long serialVersionUID = -2233037380407692718L;
        private final ThingDTO entity;

        ThingDTOPatch(ThingDTO entity) {
            this.entity = entity;
        }

        public ThingDTO getEntity() {
            return entity;
        }
    }


    public static class ThingSerializer extends AbstractSTASerializer<ThingDTO> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ThingSerializer(String rootUrl, String... activeExtensions) {
            super(ThingDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = ThingEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(ThingDTO value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, value.getId());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, value.getId());
            }

            // actual properties
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, value.getName());
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, value.getDescription());
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, value.getProperties());
            }

            // navigation properties
            for (String navigationProperty : ThingEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!value.hasExpandOption() || value.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, value.getId());
                    } else {
                        switch (navigationProperty) {
                            case ThingEntityDefinition.DATASTREAMS:
                                if (value.getDatastream() == null) {
                                    writeNavigationProp(gen, navigationProperty, value.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(value.getDatastream()),
                                                          gen,
                                                          serializers);
                                }
                                break;
                            case ThingEntityDefinition.HISTORICAL_LOCATIONS:
                                if (value.getHistoricalLocations() == null) {
                                    writeNavigationProp(gen, navigationProperty, value.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(value.getHistoricalLocations()),
                                                          gen,
                                                          serializers);
                                }
                                break;
                            case ThingEntityDefinition.LOCATIONS:
                                if (value.getLocations() == null) {
                                    writeNavigationProp(gen, navigationProperty, value.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(value.getLocations()),
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


    public static class ThingDeserializer extends StdDeserializer<ThingDTO> {

        private static final long serialVersionUID = 3942005672394573517L;

        public ThingDeserializer() {
            super(ThingDTO.class);
        }

        @Override
        public ThingDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONThing.class)
                .parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class ThingPatchDeserializer extends StdDeserializer<ThingDTOPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public ThingPatchDeserializer() {
            super(ThingDTOPatch.class);
        }

        @Override
        public ThingDTOPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new ThingDTOPatch(p.readValueAs(JSONThing.class)
                                         .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
