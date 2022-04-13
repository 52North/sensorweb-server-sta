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

package org.n52.sta.api.serdes.plus;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.shetland.ogc.sta.model.DatastreamEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.EntityPatch;
import org.n52.sta.api.dto.plus.PlusDatastreamDTO;
import org.n52.sta.api.serdes.AbstractDatastreamSerializer;
import org.n52.sta.api.serdes.JSONBase;
import org.n52.sta.api.serdes.plus.json.JSONPlusDatastream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PlusDatastreamSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlusDatastreamSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class PlusDatastreamDTOPatch implements EntityPatch<PlusDatastreamDTO> {

        private final PlusDatastreamDTO entity;

        PlusDatastreamDTOPatch(PlusDatastreamDTO entity) {
            this.entity = entity;
        }

        @Override
        public PlusDatastreamDTO getEntity() {
            return entity;
        }
    }


    public static class PlusDatastreamSerializer extends AbstractDatastreamSerializer<PlusDatastreamDTO> {

        private static final long serialVersionUID = -6555417490577181829L;

        public PlusDatastreamSerializer(String rootUrl,
                                        String... activeExtensions) {
            super(PlusDatastreamDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = DatastreamEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(PlusDatastreamDTO datastream,
                              JsonGenerator gen,
                              SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();
            super.serialize(datastream, gen, serializers);

            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PARTY)) {
                if (datastream.getParty() == null) {
                    writeNavigationProp(gen, STAEntityDefinition.PARTY, datastream.getId());
                } else {
                    gen.writeFieldName(STAEntityDefinition.PARTY);
                    writeNestedEntity(datastream.getParty(),
                                      gen,
                                      serializers);
                }
            }
            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROJECT)) {
                if (datastream.getParty() == null) {
                    writeNavigationProp(gen, STAEntityDefinition.PROJECT, datastream.getId());
                } else {
                    gen.writeFieldName(STAEntityDefinition.PROJECT);
                    writeNestedEntity(datastream.getParty(),
                                      gen,
                                      serializers);
                }
            }

            // navigation properties
            for (String navigationProperty : DatastreamEntityDefinition.NAVIGATION_PROPERTIES) {
                if (STAEntityDefinition.PARTY.equals(navigationProperty)) {
                    if (!datastream.hasSelectOption() ||
                        datastream.getFieldsToSerialize().contains(navigationProperty)) {
                        if (!datastream.hasExpandOption() ||
                            datastream.getFieldsToExpand().get(navigationProperty) == null) {
                            writeNavigationProp(gen, navigationProperty, datastream.getId());
                        } else {
                            if (datastream.getParty() == null) {
                                writeNavigationProp(gen, navigationProperty, datastream.getId());
                            } else {
                                gen.writeFieldName(navigationProperty);
                                writeNestedEntity(datastream.getParty(),
                                                  gen,
                                                  serializers);
                            }
                        }
                    }
                }
            }

            gen.writeEndObject();
        }

    }


    public static class DatastreamDeserializer extends StdDeserializer<PlusDatastreamDTO> {

        private static final long serialVersionUID = 7491123624385588769L;

        public DatastreamDeserializer() {
            super(PlusDatastreamDTO.class);
        }

        @Override
        public PlusDatastreamDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return (PlusDatastreamDTO) p.readValueAs(JSONPlusDatastream.class)
                    .parseToDTO(JSONBase.EntityType.FULL);
        }
    }
}
