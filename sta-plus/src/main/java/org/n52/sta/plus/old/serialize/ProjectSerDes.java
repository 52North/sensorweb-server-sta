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
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.api.old.dto.common.EntityPatch;
import org.n52.sta.api.old.serialize.common.AbstractSTASerializer;
import org.n52.sta.api.old.serialize.common.JSONBase;
import org.n52.sta.plus.old.entity.ProjectDTO;
import org.n52.sta.plus.old.serialize.json.JSONProject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ProjectSerDes {

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ProjectDTOPatch implements EntityPatch<ProjectDTO> {

        private static final long serialVersionUID = 742336485455358972L;

        private final ProjectDTO entity;

        ProjectDTOPatch(ProjectDTO entity) {
            this.entity = entity;
        }

        public ProjectDTO getEntity() {
            return entity;
        }
    }

    public static class ProjectSerializer extends AbstractSTASerializer<ProjectDTO> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ProjectSerializer(String rootUrl, String... activeExtensions) {
            super(ProjectDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.PROJECTS;
        }

        @Override
        public void serialize(ProjectDTO project,
                              JsonGenerator gen,
                              SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!project.hasSelectOption()
                    || project.getFieldsToSerialize()
                              .contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, project.getId());
            }
            if (!project.hasSelectOption()
                    ||
                    project.getFieldsToSerialize()
                           .contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, project.getId());
            }

            if (!project.hasSelectOption()
                    || project.getFieldsToSerialize()
                              .contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, project.getName());
            }
            if (!project.hasSelectOption()
                    ||
                    project.getFieldsToSerialize()
                           .contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, project.getDescription());
            }
            if (!project.hasSelectOption()
                    ||
                    project.getFieldsToSerialize()
                           .contains(STAEntityDefinition.PROP_CLASSIFICATION)) {
                gen.writeStringField(STAEntityDefinition.PROP_CLASSIFICATION, project.getClassification());
            }
            if (!project.hasSelectOption()
                    ||
                    project.getFieldsToSerialize()
                           .contains(STAEntityDefinition.PROP_TERMS_OF_USE)) {
                gen.writeStringField(STAEntityDefinition.PROP_TERMS_OF_USE, project.getTermsOfUse());
            }
            if (!project.hasSelectOption()
                    ||
                    project.getFieldsToSerialize()
                           .contains(STAEntityDefinition.PROP_PRIVACY_POLICY)) {
                gen.writeStringField(STAEntityDefinition.PROP_PRIVACY_POLICY, project.getPrivacyPolicy());
            }
            if (!project.hasSelectOption()
                    ||
                    project.getFieldsToSerialize()
                           .contains(STAEntityDefinition.PROP_CREATION_TIME)) {
                if (project.getCreationTime() != null) {
                    gen.writeStringField(STAEntityDefinition.PROP_CREATION_TIME,
                                         DateTimeHelper.format(project.getCreationTime()));
                }
            }
            if (!project.hasSelectOption()
                    || project.getFieldsToSerialize()
                              .contains(StaConstants.PROP_RUNTIME)) {
                if (project.getStartTime() != null) {
                    gen.writeStringField(StaConstants.PROP_RUNTIME, DateTimeHelper.format(project.getStartTime()));
                }
            }

            if (!project.hasSelectOption()
                    ||
                    project.getFieldsToSerialize()
                           .contains(STAEntityDefinition.PROP_URL)) {
                gen.writeStringField(STAEntityDefinition.PROP_URL, project.getUrl());
            }

            if (!project.hasSelectOption()
                    ||
                    project.getFieldsToSerialize()
                           .contains(STAEntityDefinition.DATASTREAMS)) {
                if (!project.hasExpandOption()
                        ||
                        project.getFieldsToExpand()
                               .get(STAEntityDefinition.DATASTREAMS) == null) {
                    writeNavigationProp(gen, STAEntityDefinition.DATASTREAMS, project.getId());
                } else {
                    gen.writeFieldName(STAEntityDefinition.DATASTREAMS);
                    writeNestedCollection(project.getDatastreams(),
                                          gen,
                                          serializers);
                }
            }
            gen.writeEndObject();
        }
    }

    public static class ProjectDeserializer extends StdDeserializer<ProjectDTO> {

        private static final long serialVersionUID = 3942005672394573517L;

        public ProjectDeserializer() {
            super(ProjectDTO.class);
        }

        @Override
        public ProjectDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONProject.class)
                    .parseToDTO(JSONBase.EntityType.FULL);
        }
    }

    public static class ProjectPatchDeserializer
            extends
            StdDeserializer<ProjectDTOPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public ProjectPatchDeserializer() {
            super(ProjectDTOPatch.class);
        }

        @Override
        public ProjectDTOPatch deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new ProjectDTOPatch(p.readValueAs(JSONProject.class)
                                        .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
