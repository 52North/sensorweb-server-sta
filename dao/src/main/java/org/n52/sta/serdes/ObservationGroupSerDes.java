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
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.EntityPatch;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONObservationGroup;

import java.io.IOException;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationGroupSerDes {

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ObservationGroupPatch implements EntityPatch<ObservationGroupDTO> {

        private static final long serialVersionUID = -2233037380407692718L;
        private final ObservationGroupDTO entity;

        ObservationGroupPatch(ObservationGroupDTO entity) {
            this.entity = entity;
        }

        public ObservationGroupDTO getEntity() {
            return entity;
        }
    }


    public static class ObservationGroupSerializer extends AbstractSTASerializer<ObservationGroupDTO> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ObservationGroupSerializer(String rootUrl, String... activeExtensions) {
            super(ObservationGroupDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.OBSERVATION_GROUPS;
        }

        @Override
        public void serialize(ObservationGroupDTO obsGroup, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!obsGroup.hasSelectOption() || obsGroup.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, obsGroup.getId());
            }
            if (!obsGroup.hasSelectOption() ||
                obsGroup.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, obsGroup.getId());
            }

            if (!obsGroup.hasSelectOption() ||
                obsGroup.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, obsGroup.getName());
            }

            if (!obsGroup.hasSelectOption() ||
                obsGroup.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, obsGroup.getDescription());
            }

            if (!obsGroup.hasSelectOption() ||
                obsGroup.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, obsGroup.getProperties());
            }

            if (!obsGroup.hasSelectOption()
                || obsGroup.getFieldsToSerialize().contains(STAEntityDefinition.OBSERVATION_RELATIONS)) {
                if (!obsGroup.hasExpandOption()
                    || obsGroup.getFieldsToExpand().get(STAEntityDefinition.OBSERVATION_RELATIONS) == null) {
                    writeNavigationProp(gen,
                                        STAEntityDefinition.OBSERVATION_RELATIONS,
                                        obsGroup.getId());
                } else {
                    gen.writeFieldName(STAEntityDefinition.OBSERVATION_RELATIONS);
                    writeNestedCollection(obsGroup.getObservationRelations(),
                                          gen,
                                          serializers);
                }
            }

            if (!obsGroup.hasSelectOption()
                || obsGroup.getFieldsToSerialize().contains(STAEntityDefinition.LICENSE)) {
                if (!obsGroup.hasExpandOption()
                    || obsGroup.getFieldsToExpand().get(STAEntityDefinition.LICENSE) == null) {
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
                || obsGroup.getFieldsToSerialize().contains(STAEntityDefinition.OBSERVATIONS)) {
                if (!obsGroup.hasExpandOption()
                    || obsGroup.getFieldsToExpand().get(STAEntityDefinition.OBSERVATIONS) == null) {
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


    public static class ObservationGroupDeserializer extends StdDeserializer<ObservationGroupDTO> {

        private static final long serialVersionUID = 3942005672394573517L;

        public ObservationGroupDeserializer() {
            super(ObservationGroupDTO.class);
        }

        @Override
        public ObservationGroupDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONObservationGroup.class).parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class ObservationGroupPatchDeserializer
        extends StdDeserializer<ObservationGroupPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public ObservationGroupPatchDeserializer() {
            super(ObservationGroupPatch.class);
        }

        @Override
        public ObservationGroupPatch deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
            return new ObservationGroupPatch(p.readValueAs(JSONObservationGroup.class)
                                                 .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
