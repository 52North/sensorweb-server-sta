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
import org.n52.series.db.beans.sta.mapped.ObservationEntity;
import org.n52.shetland.ogc.sta.model.ObservationEntityDefinition;
import org.n52.shetland.ogc.sta.model.extension.CSObservationEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONObservation;
import org.n52.sta.serdes.util.ElementWithQueryOptions.ObservationWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ObservationSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ObservationEntityPatch extends ObservationEntity implements EntityPatch<ObservationEntity> {

        private static final long serialVersionUID = 7385044376634149048L;
        private final ObservationEntity entity;

        ObservationEntityPatch(ObservationEntity entity) {
            this.entity = entity;
        }

        @Override
        public ObservationEntity getEntity() {
            return entity;
        }
    }


    public static class ObservationSerializer extends AbstractObservationSerializer<ObservationWithQueryOptions> {

        private static final long serialVersionUID = -4575044340713191285L;

        public ObservationSerializer(String rootUrl) {
            super(ObservationWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = ObservationEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(ObservationWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            super.serialize(value, gen, serializers, new ObservationEntityDefinition());
            gen.writeEndObject();
        }
    }


    public static class ObservationDeserializer extends StdDeserializer<ObservationEntity> {

        private static final long serialVersionUID = 2731654401126762133L;

        public ObservationDeserializer() {
            super(ObservationEntity.class);
        }

        @Override
        public ObservationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONObservation.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class ObservationPatchDeserializer extends StdDeserializer<ObservationEntityPatch> {

        private static final long serialVersionUID = 9042768872493184420L;

        public ObservationPatchDeserializer() {
            super(ObservationEntityPatch.class);
        }

        @Override
        public ObservationEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new ObservationEntityPatch(p.readValueAs(JSONObservation.class)
                                               .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
