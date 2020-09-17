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
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.sta.ObservationGroupEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.ObservationGroupEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONObservationGroup;
import org.n52.sta.serdes.util.ElementWithQueryOptions.ObservationGroupWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;

import java.io.IOException;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationGroupSerDes {

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ObservationGroupPatch extends ObservationGroupEntity
        implements EntityPatch<ObservationGroupEntity> {

        private static final long serialVersionUID = -2233037380407692718L;
        private final ObservationGroupEntity entity;

        ObservationGroupPatch(ObservationGroupEntity entity) {
            this.entity = entity;
        }

        public ObservationGroupEntity getEntity() {
            return entity;
        }
    }


    public static class ObservationGroupSerializer
        extends AbstractSTASerializer<ObservationGroupWithQueryOptions, ObservationGroupEntity> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ObservationGroupSerializer(String rootUrl, boolean implicitExpand, String... activeExtensions) {
            super(ObservationGroupWithQueryOptions.class, implicitExpand, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.OBSERVATION_GROUPS;
        }

        @Override
        public void serialize(ObservationGroupWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();
            value.unwrap(implicitSelect);
            ObservationGroupEntity obsGroup = value.getEntity();

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, obsGroup.getStaIdentifier());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, obsGroup.getStaIdentifier());
            }

            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, obsGroup.getName());
            }

            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, obsGroup.getDescription());
            }

            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                if (obsGroup.hasParameters()) {
                    gen.writeObjectFieldStart(STAEntityDefinition.PROP_PROPERTIES);
                    for (ParameterEntity<?> parameter : obsGroup.getParameters()) {
                        gen.writeObjectField(parameter.getName(), parameter.getValue());
                    }
                    gen.writeEndObject();
                } else {
                    gen.writeNullField(STAEntityDefinition.PROP_PROPERTIES);
                }
            }

            if (!value.hasSelectOption()
                || value.getFieldsToSerialize().contains(STAEntityDefinition.OBSERVATION_RELATIONS)) {
                if (!value.hasExpandOption()
                    || value.getFieldsToExpand().get(STAEntityDefinition.OBSERVATION_RELATIONS) == null) {
                    writeNavigationProp(gen,
                                        STAEntityDefinition.OBSERVATION_RELATIONS,
                                        obsGroup.getStaIdentifier());
                } else {
                    gen.writeFieldName(STAEntityDefinition.OBSERVATION_RELATIONS);
                    writeNestedCollection(obsGroup.getEntities(),
                                          value.getFieldsToExpand().get(STAEntityDefinition.OBSERVATION_RELATIONS),
                                          gen,
                                          serializers);
                }
            }
            gen.writeEndObject();
        }

    }


    public static class ObservationGroupDeserializer extends StdDeserializer<ObservationGroupEntity> {

        private static final long serialVersionUID = 3942005672394573517L;

        public ObservationGroupDeserializer() {
            super(ObservationGroupEntity.class);
        }

        @Override
        public ObservationGroupEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONObservationGroup.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class ObservationGroupPatchDeserializer
        extends StdDeserializer<ObservationGroupSerDes.ObservationGroupPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public ObservationGroupPatchDeserializer() {
            super(ObservationGroupPatch.class);
        }

        @Override
        public ObservationGroupSerDes.ObservationGroupPatch deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
            return new ObservationGroupSerDes.ObservationGroupPatch(p.readValueAs(JSONObservationGroup.class)
                                                                        .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
