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
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.shetland.ogc.sta.model.DatastreamEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONDatastream;
import org.n52.sta.serdes.util.ElementWithQueryOptions.DatastreamWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DatastreamSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class DatastreamEntityPatch extends DatastreamEntity implements EntityPatch<DatastreamEntity> {

        private static final long serialVersionUID = -8968753678464145994L;
        private final DatastreamEntity entity;

        DatastreamEntityPatch(DatastreamEntity entity) {
            this.entity = entity;
        }

        @Override
        public DatastreamEntity getEntity() {
            return entity;
        }
    }


    public static class DatastreamSerializer
            extends AbstractDatastreamSerializer<DatastreamWithQueryOptions> {

        public DatastreamSerializer(String rootUrl) {
            super(DatastreamWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = DatastreamEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(DatastreamWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            super.serialize(value, gen, serializers, new DatastreamEntityDefinition());
            gen.writeEndObject();
        }
    }


    public static class DatastreamDeserializer extends StdDeserializer<DatastreamEntity> {

        private static final long serialVersionUID = 7491123624385588769L;

        public DatastreamDeserializer() {
            super(DatastreamEntity.class);
        }

        @Override
        public DatastreamEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONDatastream.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class DatastreamPatchDeserializer extends StdDeserializer<DatastreamEntityPatch> {

        private static final long serialVersionUID = 6354638503794606750L;

        public DatastreamPatchDeserializer() {
            super(DatastreamEntityPatch.class);
        }

        @Override
        public DatastreamEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new DatastreamEntityPatch(p.readValueAs(JSONDatastream.class).toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
