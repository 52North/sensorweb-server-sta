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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.series.db.beans.sta.mapped.extension.Project;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.extension.JSONProject;
import org.n52.sta.serdes.util.ElementWithQueryOptions.ProjectWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ProjectSerDes {

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ProjectPatch extends Project implements EntityPatch<Project> {

        private static final long serialVersionUID = 742336485455358972L;

        private final Project entity;

        ProjectPatch(Project entity) {
            this.entity = entity;
        }

        public Project getEntity() {
            return entity;
        }
    }


    public static class ProjectSerializer
            extends AbstractSTASerializer<ProjectWithQueryOptions> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ProjectSerializer(String rootUrl) {
            super(ProjectWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.PROJECTS;
        }

        @Override
        public void serialize(ProjectWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();

            Project project = value.getEntity();
            QueryOptions options = value.getQueryOptions();

            Set<String> fieldsToSerialize = null;
            Map<String, QueryOptions> fieldsToExpand = new HashMap<>();
            boolean hasSelectOption = false;
            boolean hasExpandOption = false;
            if (options != null) {
                if (options.hasSelectFilter()) {
                    hasSelectOption = true;
                    fieldsToSerialize = options.getSelectFilter().getItems();
                }
                if (options.hasExpandFilter()) {
                    hasExpandOption = true;
                    for (ExpandItem item : options.getExpandFilter().getItems()) {
                        fieldsToExpand.put(item.getPath(), item.getQueryOptions());
                    }
                }
            }
            // olingo @iot links
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, project.getStaIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, project.getStaIdentifier());
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, project.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, project.getDescription());
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RUNTIME)) {
                String runtime = DateTimeHelper.format(createRuntime(project));
                gen.writeStringField(STAEntityDefinition.PROP_RUNTIME, runtime);
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.CSDATASTREAMS)) {
                if (!hasExpandOption || fieldsToExpand.get(STAEntityDefinition.CSDATASTREAMS) == null) {
                    writeNavigationProp(gen, STAEntityDefinition.CSDATASTREAMS, project.getStaIdentifier());
                } else {
                    gen.writeFieldName(STAEntityDefinition.CSDATASTREAMS);
                    writeNestedEntity(project.getDatastreams(),
                                      fieldsToExpand.get(STAEntityDefinition.CSDATASTREAMS),
                                      gen,
                                      serializers);
                }
            }
            gen.writeEndObject();
        }

        private Time createRuntime(Project project) {
            final DateTime start = new DateTime(project.getRuntimeStart(), DateTimeZone.UTC);
            if (project.getRuntimeEnd() != null) {
                return new TimePeriod(start, project.getRuntimeEnd());
            } else {
                return new TimeInstant(start);
            }
        }

    }


    public static class ProjectDeserializer extends StdDeserializer<Project> {

        private static final long serialVersionUID = 3942005672394573517L;

        public ProjectDeserializer() {
            super(Project.class);
        }

        @Override
        public Project deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONProject.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class ProjectPatchDeserializer
            extends StdDeserializer<ProjectSerDes.ProjectPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public ProjectPatchDeserializer() {
            super(ThingSerDes.PlatformEntityPatch.class);
        }

        @Override
        public ProjectSerDes.ProjectPatch deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new ProjectSerDes.ProjectPatch(p.readValueAs(JSONProject.class)
                                                   .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
