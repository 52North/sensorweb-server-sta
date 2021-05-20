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
import org.n52.shetland.ogc.sta.model.DatastreamEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.CitSciDatastreamDTO;
import org.n52.sta.api.dto.EntityPatch;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONCitSciDatastream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CitSciDatastreamSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(CitSciDatastreamSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class CitSciDatastreamDTOPatch implements EntityPatch<CitSciDatastreamDTO> {

        private final CitSciDatastreamDTO entity;

        CitSciDatastreamDTOPatch(CitSciDatastreamDTO entity) {
            this.entity = entity;
        }

        @Override
        public CitSciDatastreamDTO getEntity() {
            return entity;
        }
    }


    public static class CitSciDatastreamSerializer extends AbstractDatastreamSerializer<CitSciDatastreamDTO> {

        private static final long serialVersionUID = -6555417490577181829L;

        public CitSciDatastreamSerializer(String rootUrl,
                                          String... activeExtensions) {
            super(CitSciDatastreamDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = DatastreamEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(CitSciDatastreamDTO datastream,
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
            gen.writeEndObject();
        }

    }


    public static class CitSciDatastreamDeserializer extends StdDeserializer<CitSciDatastreamDTO> {

        private static final long serialVersionUID = 7491123624385588769L;

        public CitSciDatastreamDeserializer() {
            super(CitSciDatastreamDTO.class);
        }

        @Override
        public CitSciDatastreamDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONCitSciDatastream.class)
                .parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class CitSciDatastreamPatchDeserializer extends StdDeserializer<CitSciDatastreamDTOPatch> {

        private static final long serialVersionUID = 6354638503794606750L;

        public CitSciDatastreamPatchDeserializer() {
            super(CitSciDatastreamDTOPatch.class);
        }

        @Override
        public CitSciDatastreamDTOPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new CitSciDatastreamDTOPatch(p.readValueAs(JSONCitSciDatastream.class)
                                                    .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
