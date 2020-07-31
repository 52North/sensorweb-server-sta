/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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
import org.n52.series.db.beans.sta.mapped.extension.Party;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.extension.JSONParty;
import org.n52.sta.serdes.util.ElementWithQueryOptions.PartyWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;

import java.io.IOException;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class PartySerDes {

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class PartyPatch extends Party implements EntityPatch<Party> {

        private static final long serialVersionUID = 742336485455358972L;

        private final Party entity;

        PartyPatch(Party entity) {
            this.entity = entity;
        }

        public Party getEntity() {
            return entity;
        }
    }


    public static class PartySerializer
            extends AbstractSTASerializer<PartyWithQueryOptions, Party> {

        private static final long serialVersionUID = -1618289129123682794L;

        public PartySerializer(String rootUrl) {
            super(PartyWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.PARTIES;
        }

        @Override
        public void serialize(PartyWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            Party party = unwrap(value);

            // olingo @iot links
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, party.getStaIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, party.getStaIdentifier());
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ROLE)) {
                gen.writeStringField(STAEntityDefinition.PROP_ROLE, party.getRole().name());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NICKNAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NICKNAME, party.getNickname());
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.CSDATASTREAMS)) {
                if (!hasExpandOption || fieldsToExpand.get(STAEntityDefinition.CSDATASTREAMS) == null) {
                    writeNavigationProp(gen, STAEntityDefinition.CSDATASTREAMS, party.getStaIdentifier());
                } else {
                    gen.writeFieldName(STAEntityDefinition.CSDATASTREAMS);
                    writeNestedEntity(party.getDatastreams(),
                                      fieldsToExpand.get(STAEntityDefinition.CSDATASTREAMS),
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
            return p.readValueAs(JSONParty.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class PartyPatchDeserializer
            extends StdDeserializer<PartySerDes.PartyPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public PartyPatchDeserializer() {
            super(ThingSerDes.PlatformEntityPatch.class);
        }

        @Override
        public PartySerDes.PartyPatch deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new PartySerDes.PartyPatch(p.readValueAs(JSONParty.class)
                                               .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
