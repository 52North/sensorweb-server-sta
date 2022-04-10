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
package org.n52.sta.serdes.vanilla;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.ogc.sta.model.SensorEntityDefinition;
import org.n52.sta.api.dto.EntityPatch;
import org.n52.sta.api.dto.vanilla.SensorDTO;
import org.n52.sta.serdes.AbstractSTASerializer;
import org.n52.sta.serdes.JSONBase;
import org.n52.sta.serdes.vanilla.json.JSONSensor;
import org.n52.sta.serdes.vanilla.json.JSONSensorVariableEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class SensorSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class SensorDTOPatch implements EntityPatch<SensorDTO> {

        private static final long serialVersionUID = 2966462269307558981L;
        private final SensorDTO entity;

        SensorDTOPatch(SensorDTO entity) {
            this.entity = entity;
        }

        public SensorDTO getEntity() {
            return entity;
        }
    }


    public static class SensorSerializer extends AbstractSTASerializer<SensorDTO> {

        private static final long serialVersionUID = -2190624056257407974L;

        public SensorSerializer(String rootUrl, String... activeExtensions) {
            super(SensorDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = SensorEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(SensorDTO value, JsonGenerator gen, SerializerProvider serializers)
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
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                gen.writeStringField(STAEntityDefinition.PROP_ENCODINGTYPE, value.getEncodingType());
            }

            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_METADATA)) {
                gen.writeStringField(STAEntityDefinition.PROP_METADATA, value.getMetadata());
            }

            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                if (value.getProperties() != null) {
                    gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, value.getProperties());
                }
            }

            // navigation properties
            for (String navigationProperty : SensorEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!value.hasExpandOption() || value.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, value.getId());
                    } else {
                        if (SensorEntityDefinition.DATASTREAMS.equals(navigationProperty)) {
                            if (value.getDatastreams() == null) {
                                writeNavigationProp(gen, navigationProperty, value.getId());
                            } else {
                                gen.writeFieldName(navigationProperty);
                                writeNestedCollection(Collections.unmodifiableSet(value.getDatastreams()),
                                                      gen,
                                                      serializers);
                            }
                        } else {
                            throw new IllegalStateException("Unexpected value: " + navigationProperty);
                        }
                    }
                }
            }
            gen.writeEndObject();
        }
    }


    public static class SensorDeserializer extends StdDeserializer<SensorDTO> {

        private static final long serialVersionUID = -6513819346703020350L;
        private final boolean variableEncodingType;

        public SensorDeserializer(boolean variableEncodingType) {
            super(SensorDTO.class);
            this.variableEncodingType = variableEncodingType;
        }

        @Override
        public SensorDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (variableEncodingType) {
                return p.readValueAs(JSONSensorVariableEncoding.class)
                    .parseToDTO(JSONBase.EntityType.FULL);
            } else {
                return p.readValueAs(JSONSensor.class)
                    .parseToDTO(JSONBase.EntityType.FULL);
            }
        }
    }


    public static class SensorPatchDeserializer extends StdDeserializer<SensorDTOPatch> {

        private static final long serialVersionUID = -6636765136530111251L;
        private final boolean variableEncodingType;

        public SensorPatchDeserializer(boolean variableEncodingType) {
            super(SensorDTOPatch.class);
            this.variableEncodingType = variableEncodingType;
        }

        @Override
        public SensorDTOPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (variableEncodingType) {
                return new SensorDTOPatch(p.readValueAs(JSONSensorVariableEncoding.class)
                                              .parseToDTO(JSONBase.EntityType.PATCH));
            } else {
                return new SensorDTOPatch(p.readValueAs(JSONSensor.class)
                                              .parseToDTO(JSONBase.EntityType.PATCH));
            }
        }
    }
}
