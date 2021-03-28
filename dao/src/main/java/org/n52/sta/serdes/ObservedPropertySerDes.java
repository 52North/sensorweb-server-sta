/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.sta.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.shetland.ogc.sta.model.ObservedPropertyEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.EntityPatch;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONObservedProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class ObservedPropertySerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservedPropertySerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ObservedPropertyDTOPatch implements EntityPatch<ObservedPropertyDTO> {

        private static final long serialVersionUID = 2568180072428202569L;
        private final ObservedPropertyDTO entity;

        ObservedPropertyDTOPatch(ObservedPropertyDTO entity) {
            this.entity = entity;
        }

        public ObservedPropertyDTO getEntity() {
            return entity;
        }
    }


    public static class ObservedPropertySerializer extends AbstractSTASerializer<ObservedPropertyDTO> {

        private static final long serialVersionUID = -393434867481235299L;

        public ObservedPropertySerializer(String rootUrl, String... activeExtensions) {
            super(ObservedPropertyDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = ObservedPropertyEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(ObservedPropertyDTO value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, value.getId());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, value.getId());
            }

            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, value.getName());
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, value.getDescription());
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DEFINITION)) {
                gen.writeObjectField(STAEntityDefinition.PROP_DEFINITION, value.getDefinition());
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, value.getProperties());
                /*
                if (obsProp.hasParameters()) {
                    gen.writeObjectFieldStart(STAEntityDefinition.PROP_PROPERTIES);
                    for (ParameterEntity<?> parameter : obsProp.getParameters()) {
                        gen.writeObjectField(parameter.getName(), parameter.getValue());
                    }
                    gen.writeEndObject();
                }
                 */
            }

            // navigation properties
            for (String navigationProperty : ObservedPropertyEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!value.hasExpandOption() || value.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, value.getId());
                    } else {
                        switch (navigationProperty) {
                            case ObservedPropertyEntityDefinition.DATASTREAMS:
                                if (value.getDatastreams() == null) {
                                    writeNavigationProp(gen, navigationProperty, value.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(value.getDatastreams()),
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


    public static class ObservedPropertyDeserializer extends StdDeserializer<ObservedPropertyDTO> {

        private static final long serialVersionUID = -1880043109904466341L;

        public ObservedPropertyDeserializer() {
            super(ObservedPropertyDTO.class);
        }

        @Override
        public ObservedPropertyDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONObservedProperty.class).parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class ObservedPropertyPatchDeserializer extends StdDeserializer<ObservedPropertyDTOPatch> {

        private static final long serialVersionUID = 574088735297768175L;

        public ObservedPropertyPatchDeserializer() {
            super(ObservedPropertyDTOPatch.class);
        }

        @Override
        public ObservedPropertyDTOPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new ObservedPropertyDTOPatch(p.readValueAs(JSONObservedProperty.class)
                                                    .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
