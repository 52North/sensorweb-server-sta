/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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
package org.n52.sta.plus.old.serialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.old.dto.common.EntityPatch;
import org.n52.sta.api.old.serialize.common.AbstractSTASerializer;
import org.n52.sta.api.old.serialize.common.JSONBase;
import org.n52.sta.plus.data.entity.StaPlusParty;
import org.n52.sta.plus.old.dto.Party;
import org.n52.sta.plus.old.serialize.json.JSONParty;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class PartySerDes {

    private static final String PROP_AUTH_ID = "authId";


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class PartyDTOPatch implements EntityPatch<Party> {

        private static final long serialVersionUID = 742336485455358972L;

        private final Party entity;

        PartyDTOPatch(Party entity) {
            this.entity = entity;
        }

        public Party getEntity() {
            return entity;
        }
    }


    public static class PartySerializer extends AbstractSTASerializer<Party> {

        private static final long serialVersionUID = -1618289129123682794L;

        public PartySerializer(String rootUrl, String... activeExtensions) {
            super(Party.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.PARTIES;
        }

        @Override
        public void serialize(Party party,
                              JsonGenerator gen,
                              SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!party.hasSelectOption() || party.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, party.getId());
            }
            if (!party.hasSelectOption() || party.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, party.getId());
            }

            if (!party.hasSelectOption() || party.getFieldsToSerialize().contains(PROP_AUTH_ID)) {
                gen.writeStringField(PROP_AUTH_ID, party.getAuthId());
            }
            if (!party.hasSelectOption() ||
                party.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DISPLAY_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_DISPLAY_NAME, party.getDisplayName());
            }
            if (!party.hasSelectOption() ||
                party.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, party.getDescription());
            }
            if (!party.hasSelectOption() || party.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ROLE)) {
                gen.writeStringField(STAEntityDefinition.PROP_ROLE, party.getRole().name());
            }

            if (!party.hasSelectOption() || party.getFieldsToSerialize().contains(STAEntityDefinition.DATASTREAMS)) {
                if (!party.hasExpandOption() ||
                    party.getFieldsToExpand().get(STAEntityDefinition.DATASTREAMS) == null) {
                    writeNavigationProp(gen, STAEntityDefinition.DATASTREAMS, party.getId());
                } else {
                    gen.writeFieldName(STAEntityDefinition.DATASTREAMS);
                    writeNestedCollection(party.getDatastreams(),
                                          gen,
                                          serializers);
                }
            }

            if (!party.hasSelectOption() || party.getFieldsToSerialize().contains(STAEntityDefinition.DATASTREAMS)) {
                if (!party.hasExpandOption() ||
                    party.getFieldsToExpand().get(STAEntityDefinition.DATASTREAMS) == null) {
                    writeNavigationProp(gen, STAEntityDefinition.DATASTREAMS, party.getId());
                } else {
                    gen.writeFieldName(STAEntityDefinition.DATASTREAMS);
                    writeNestedCollection(party.getDatastreams(),
                                          gen,
                                          serializers);
                }
            }
            gen.writeEndObject();
        }

    }


    public static class PartyDeserializer extends StdDeserializer<Party> {

        private static final long serialVersionUID = 3942005672394573517L;

        public PartyDeserializer() {
            super(Party.class);
        }

        @Override
        public Party deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONParty.class).parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class PartyPatchDeserializer
        extends StdDeserializer<PartyDTOPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public PartyPatchDeserializer() {
            super(PartyDTOPatch.class);
        }

        @Override
        public PartyDTOPatch deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
            return new PartyDTOPatch(p.readValueAs(JSONParty.class)
                                         .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}