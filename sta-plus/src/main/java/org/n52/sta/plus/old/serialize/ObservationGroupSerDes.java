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
import org.n52.sta.plus.old.entity.GroupDTO;
import org.n52.sta.plus.old.serialize.json.JSONGroup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationGroupSerDes {

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ObservationGroupDTOPatch implements EntityPatch<GroupDTO> {

        private static final long serialVersionUID = -2233037380407692718L;
        private final GroupDTO entity;

        ObservationGroupDTOPatch(GroupDTO entity) {
            this.entity = entity;
        }

        public GroupDTO getEntity() {
            return entity;
        }
    }

    public static class ObservationGroupSerializer extends AbstractSTASerializer<GroupDTO> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ObservationGroupSerializer(String rootUrl, String... activeExtensions) {
            super(GroupDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.GROUPS;
        }

        @Override
        public void serialize(GroupDTO obsGroup, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!obsGroup.hasSelectOption()
                    || obsGroup.getFieldsToSerialize()
                               .contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, obsGroup.getId());
            }
            if (!obsGroup.hasSelectOption()
                    ||
                    obsGroup.getFieldsToSerialize()
                            .contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, obsGroup.getId());
            }

            if (!obsGroup.hasSelectOption()
                    ||
                    obsGroup.getFieldsToSerialize()
                            .contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, obsGroup.getName());
            }

            if (!obsGroup.hasSelectOption()
                    ||
                    obsGroup.getFieldsToSerialize()
                            .contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, obsGroup.getDescription());
            }

            if (!obsGroup.hasSelectOption()
                    ||
                    obsGroup.getFieldsToSerialize()
                            .contains(STAEntityDefinition.PROP_PROPERTIES)) {
                gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, obsGroup.getProperties());
            }

            if (!obsGroup.hasSelectOption()
                    || obsGroup.getFieldsToSerialize()
                               .contains(STAEntityDefinition.RELATIONS)) {
                if (!obsGroup.hasExpandOption()
                        || obsGroup.getFieldsToExpand()
                                   .get(STAEntityDefinition.RELATIONS) == null) {
                    writeNavigationProp(gen,
                                        STAEntityDefinition.RELATIONS,
                                        obsGroup.getId());
                } else {
                    gen.writeFieldName(STAEntityDefinition.RELATIONS);
                    writeNestedCollection(obsGroup.getRelations(),
                                          gen,
                                          serializers);
                }
            }

            if (!obsGroup.hasSelectOption()
                    || obsGroup.getFieldsToSerialize()
                               .contains(STAEntityDefinition.LICENSE)) {
                if (!obsGroup.hasExpandOption()
                        || obsGroup.getFieldsToExpand()
                                   .get(STAEntityDefinition.LICENSE) == null) {
                    writeNavigationProp(gen,
                                        STAEntityDefinition.LICENSE,
                                        obsGroup.getId());
                } else {
                    gen.writeFieldName(STAEntityDefinition.LICENSE);
                    writeNestedEntity(obsGroup.getLicense(),
                                      gen,
                                      serializers);
                }
            }

            if (!obsGroup.hasSelectOption()
                    || obsGroup.getFieldsToSerialize()
                               .contains(STAEntityDefinition.OBSERVATIONS)) {
                if (!obsGroup.hasExpandOption()
                        || obsGroup.getFieldsToExpand()
                                   .get(STAEntityDefinition.OBSERVATIONS) == null) {
                    writeNavigationProp(gen,
                                        STAEntityDefinition.OBSERVATIONS,
                                        obsGroup.getId());
                } else {
                    gen.writeFieldName(STAEntityDefinition.OBSERVATIONS);
                    writeNestedCollection(obsGroup.getObservations(),
                                          gen,
                                          serializers);
                }
            }
            gen.writeEndObject();
        }

    }

    public static class ObservationGroupDeserializer extends StdDeserializer<GroupDTO> {

        private static final long serialVersionUID = 3942005672394573517L;

        public ObservationGroupDeserializer() {
            super(GroupDTO.class);
        }

        @Override
        public GroupDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONGroup.class)
                    .parseToDTO(JSONBase.EntityType.FULL);
        }
    }

    public static class ObservationGroupPatchDeserializer
            extends
            StdDeserializer<ObservationGroupDTOPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public ObservationGroupPatchDeserializer() {
            super(ObservationGroupDTOPatch.class);
        }

        @Override
        public ObservationGroupDTOPatch deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new ObservationGroupDTOPatch(p.readValueAs(JSONGroup.class)
                                                 .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
