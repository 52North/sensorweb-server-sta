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

package org.n52.sta.plus.http.serialize.out;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Project;
import org.n52.sta.http.serialize.out.DatastreamJsonSerializer;
import org.n52.sta.http.serialize.out.SerializationContext;
import org.n52.sta.http.serialize.out.StaBaseSerializer;

import java.io.IOException;

public class ProjectJsonSerializer extends StaBaseSerializer<Project> {

    public ProjectJsonSerializer(SerializationContext context) {
        super(context, StaConstants.PROJECTS, Project.class);
    }

    @Override
    public void serialize(Project value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        String id = value.getId();

        // entity properties
        writeProperty(StaConstants.PROP_ID, name -> gen.writeStringField(StaConstants.AT_IOT_ID, id));
        writeProperty(StaConstants.PROP_SELF_LINK,
                name -> gen.writeStringField(StaConstants.AT_IOT_SELFLINK, createSelfLink(id)));
        writeStringProperty(StaConstants.PROP_NAME, value::getName, gen);
        writeStringProperty(StaConstants.PROP_DESCRIPTION, value::getDescription, gen);
        writeStringProperty(StaConstants.PROP_CLASSIFICATION, () -> value.getClassification().orElse(null), gen);
        writeStringProperty(StaConstants.PROP_TERMS_OF_USE, value::getTermsOfUse, gen);
        writeStringProperty(StaConstants.PROP_PRIVACY_POLICY, () -> value.getPrivacyPolicy().orElse(null), gen);
        writeTimeProperty(StaConstants.PROP_CREATION_TIME, value::getCreationTime, gen);
        writeTimeProperty(StaConstants.PROP_RUNTIME, () -> value.getRunTime().orElse(null), gen);
        writeStringProperty(StaConstants.PROP_URL, () -> value.getUrl().orElse(null), gen);
        writeObjectProperty(StaConstants.PROP_PROPERTIES, value::getProperties, gen);

        // entity members
        writeMemberCollection(StaConstants.DATASTREAMS, id, gen, DatastreamJsonSerializer::new, serializer -> {
            for (Datastream item : value.getDatastreams()) {
                serializer.serialize(item, gen, serializers);
            }
        });

        gen.writeEndObject();
    }

}
