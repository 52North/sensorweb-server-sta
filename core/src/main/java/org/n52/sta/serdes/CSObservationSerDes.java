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
import org.n52.series.db.beans.sta.mapped.extension.CSObservation;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.extension.JSONCSObservation;
import org.n52.sta.serdes.util.ElementWithQueryOptions.CSObservationWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;

import java.io.IOException;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class CSObservationSerDes {

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class CSObservationPatch extends CSObservation
            implements EntityPatch<CSObservation> {

        private static final long serialVersionUID = -2233037380407692718L;
        private final CSObservation entity;

        CSObservationPatch(CSObservation entity) {
            this.entity = entity;
        }

        public CSObservation getEntity() {
            return entity;
        }
    }


    public static class CSObservationSerializer
            extends AbstractObservationSerializer<CSObservationWithQueryOptions> {

        private static final long serialVersionUID = -1618289129123682794L;

        public CSObservationSerializer(String rootUrl) {
            super(CSObservationWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.CSOBSERVATIONS;
        }

        @Override
        public void serialize(CSObservationWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            super.serialize(value, gen, serializers);
            writeNavigationProp(gen, StaConstants.OBSERVATION_RELATIONS, value.getEntity().getStaIdentifier());
            gen.writeEndObject();
        }

    }


    public static class CSObservationDeserializer extends StdDeserializer<CSObservation> {

        private static final long serialVersionUID = 3942005672394573517L;

        public CSObservationDeserializer() {
            super(CSObservation.class);
        }

        @Override
        public CSObservation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONCSObservation.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class CSObservationPatchDeserializer
            extends StdDeserializer<CSObservationSerDes.CSObservationPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public CSObservationPatchDeserializer() {
            super(ThingSerDes.PlatformEntityPatch.class);
        }

        @Override
        public CSObservationSerDes.CSObservationPatch deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new CSObservationSerDes.CSObservationPatch(p.readValueAs(JSONCSObservation.class)
                                                               .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}