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
import org.n52.series.db.beans.sta.ObservationRelationEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONObservationRelation;
import org.n52.sta.serdes.util.ElementWithQueryOptions.ObservationRelationWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;

import java.io.IOException;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationRelationSerDes {

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ObservationRelationPatch extends ObservationRelationEntity
            implements EntityPatch<ObservationRelationEntity> {

        private static final long serialVersionUID = -2233037380407692718L;
        private final ObservationRelationEntity entity;

        ObservationRelationPatch(ObservationRelationEntity entity) {
            this.entity = entity;
        }

        public ObservationRelationEntity getEntity() {
            return entity;
        }
    }


    public static class ObservationRelationSerializer
            extends AbstractSTASerializer<ObservationRelationWithQueryOptions, ObservationRelationEntity> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ObservationRelationSerializer(String rootUrl, boolean implicitExpand, String... activeExtensions) {
            super(ObservationRelationWithQueryOptions.class, implicitExpand, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.OBSERVATION_RELATIONS;
        }

        @Override
        public void serialize(ObservationRelationWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            value.unwrap(implicitSelect);
            ObservationRelationEntity obsRelation = value.getEntity();

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, obsRelation.getStaIdentifier());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, obsRelation.getStaIdentifier());
            }

            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_TYPE)) {
                gen.writeStringField(STAEntityDefinition.PROP_TYPE, obsRelation.getType());
            }

            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.OBSERVATION)) {
                if (!value.hasExpandOption() ||
                        value.getFieldsToExpand().get(STAEntityDefinition.OBSERVATION) == null) {
                    writeNavigationProp(gen, STAEntityDefinition.OBSERVATION, obsRelation.getStaIdentifier());
                } else {
                    gen.writeFieldName(STAEntityDefinition.OBSERVATION);
                    writeNestedEntity(obsRelation.getObservation(),
                                      value.getFieldsToExpand().get(STAEntityDefinition.OBSERVATION),
                                      gen,
                                      serializers);
                }
            }

            if (!value.hasSelectOption() ||
                    value.getFieldsToSerialize().contains(STAEntityDefinition.OBSERVATION_GROUP)) {
                if (!value.hasExpandOption()
                        || value.getFieldsToExpand().get(STAEntityDefinition.OBSERVATION_GROUP) == null) {
                    writeNavigationProp(gen, STAEntityDefinition.OBSERVATION_GROUP, obsRelation.getStaIdentifier());
                } else {
                    gen.writeFieldName(STAEntityDefinition.OBSERVATION_GROUP);
                    writeNestedEntity(obsRelation.getGroup(),
                                      value.getFieldsToExpand().get(STAEntityDefinition.OBSERVATION_GROUP),
                                      gen,
                                      serializers);
                }
            }
            gen.writeEndObject();
        }

    }


    public static class ObservationRelationDeserializer extends StdDeserializer<ObservationRelationEntity> {

        private static final long serialVersionUID = 3942005672394573517L;

        public ObservationRelationDeserializer() {
            super(ObservationRelationEntity.class);
        }

        @Override
        public ObservationRelationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONObservationRelation.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class ObservationRelationPatchDeserializer
            extends StdDeserializer<ObservationRelationSerDes.ObservationRelationPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public ObservationRelationPatchDeserializer() {
            super(ThingSerDes.PlatformEntityPatch.class);
        }

        @Override
        public ObservationRelationSerDes.ObservationRelationPatch deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new ObservationRelationSerDes.ObservationRelationPatch(p.readValueAs(JSONObservationRelation.class)
                                                                           .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
