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
import org.n52.shetland.ogc.sta.model.ObservationEntityDefinition;
import org.n52.sta.api.dto.CitSciObservationDTO;
import org.n52.sta.api.dto.EntityPatch;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONCitSciObservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CitSciObservationSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class CitSciObservationDTOPatch<T> implements EntityPatch<CitSciObservationDTO> {

        private static final long serialVersionUID = 7385044376634149048L;
        private final CitSciObservationDTO entity;

        CitSciObservationDTOPatch(CitSciObservationDTO entity) {
            this.entity = entity;
        }

        @Override
        public CitSciObservationDTO getEntity() {
            return entity;
        }
    }


    public static class CitSciObservationSerializer extends AbstractObservationSerializer<CitSciObservationDTO> {

        private static final long serialVersionUID = -4575044340713191285L;

        public CitSciObservationSerializer(String rootUrl, String... activeExtensions) {
            super(CitSciObservationDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = ObservationEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(CitSciObservationDTO observation, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();
            super.serialize(observation, gen, serializers);
            gen.writeEndObject();
        }
    }


    public static class CitSciObservationDeserializer extends StdDeserializer<CitSciObservationDTO> {

        private static final long serialVersionUID = 2731654401126762133L;

        public CitSciObservationDeserializer() {
            super(CitSciObservationDTO.class);
        }

        @Override
        public CitSciObservationDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONCitSciObservation.class)
                .parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class CitSciObservationPatchDeserializer extends StdDeserializer<CitSciObservationDTOPatch> {

        private static final long serialVersionUID = 9042768872493184420L;

        public CitSciObservationPatchDeserializer() {
            super(CitSciObservationDTOPatch.class);
        }

        @Override
        public CitSciObservationDTOPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new CitSciObservationDTOPatch(p.readValueAs(JSONCitSciObservation.class)
                                                     .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
