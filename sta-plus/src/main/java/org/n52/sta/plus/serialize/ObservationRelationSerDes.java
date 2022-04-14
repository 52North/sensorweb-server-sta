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
package org.n52.sta.plus.serialize;

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
import org.n52.sta.plus.entity.RelationDTO;
import org.n52.sta.plus.serialize.json.JSONRelation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationRelationSerDes {

    protected static final String NAMESPACE = "namespace";


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ObservationRelationDTOPatch implements EntityPatch<RelationDTO> {

        private static final long serialVersionUID = -2233037380407692718L;
        private final RelationDTO entity;

        ObservationRelationDTOPatch(RelationDTO entity) {
            this.entity = entity;
        }

        public RelationDTO getEntity() {
            return entity;
        }
    }


    public static class ObservationRelationSerializer
        extends AbstractSTASerializer<RelationDTO> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ObservationRelationSerializer(String rootUrl, String... activeExtensions) {
            super(RelationDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.RELATIONS;
        }

        @Override
        public void serialize(RelationDTO obsRelation,
                              JsonGenerator gen,
                              SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!obsRelation.hasSelectOption() ||
                obsRelation.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, obsRelation.getId());
            }
            if (!obsRelation.hasSelectOption() ||
                obsRelation.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, obsRelation.getId());
            }

            if (!obsRelation.hasSelectOption() ||
                obsRelation.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, obsRelation.getDescription());
            }
            if (!obsRelation.hasSelectOption() ||
                obsRelation.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ROLE)) {
                gen.writeStringField(STAEntityDefinition.PROP_ROLE, obsRelation.getRole());
            }

            if (!obsRelation.hasSelectOption() ||
                obsRelation.getFieldsToSerialize().contains(StaConstants.SUBJECT)) {
                if (!obsRelation.hasExpandOption() ||
                    obsRelation.getFieldsToExpand().get(StaConstants.SUBJECT) == null) {
                    writeNavigationProp(gen, StaConstants.SUBJECT, obsRelation.getId());
                } else {
                    gen.writeFieldName(StaConstants.SUBJECT);
                    writeNestedEntity(obsRelation.getSubject(),
                                      gen,
                                      serializers);
                }
            }
            if (!obsRelation.hasSelectOption() ||
                obsRelation.getFieldsToSerialize().contains(StaConstants.OBJECT)) {
                if (!obsRelation.hasExpandOption() ||
                    obsRelation.getFieldsToExpand().get(StaConstants.OBJECT) == null) {
                    writeNavigationProp(gen, StaConstants.OBJECT, obsRelation.getId());
                } else {
                    gen.writeFieldName(StaConstants.OBJECT);
                    writeNestedEntity(obsRelation.getObject(),
                                      gen,
                                      serializers);
                }
            }

            if (!obsRelation.hasSelectOption() ||
                obsRelation.getFieldsToSerialize().contains(STAEntityDefinition.GROUPS)) {
                if (!obsRelation.hasExpandOption()
                    || obsRelation.getFieldsToExpand().get(STAEntityDefinition.GROUPS) == null) {
                    writeNavigationProp(gen, STAEntityDefinition.GROUPS, obsRelation.getId());
                } else {
                    if (obsRelation.getObservationGroups() != null) {
                        gen.writeFieldName(STAEntityDefinition.GROUPS);
                        writeNestedCollection(obsRelation.getObservationGroups(),
                                              gen,
                                              serializers);
                    }
                }
            }
            gen.writeEndObject();
        }

    }


    public static class ObservationRelationDeserializer extends StdDeserializer<RelationDTO> {

        private static final long serialVersionUID = 3942005672394573517L;

        public ObservationRelationDeserializer() {
            super(RelationDTO.class);
        }

        @Override
        public RelationDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONRelation.class).parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class ObservationRelationPatchDeserializer
        extends StdDeserializer<ObservationRelationDTOPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public ObservationRelationPatchDeserializer() {
            super(ObservationRelationDTOPatch.class);
        }

        @Override
        public ObservationRelationDTOPatch deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
            return new ObservationRelationDTOPatch(p.readValueAs(JSONRelation.class)
                                                       .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
