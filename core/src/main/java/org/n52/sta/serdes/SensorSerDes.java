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
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.ogc.sta.model.SensorEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONSensor;
import org.n52.sta.serdes.json.JSONSensorVariableEncoding;
import org.n52.sta.serdes.util.ElementWithQueryOptions.SensorWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class SensorSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ProcedureEntityPatch extends ProcedureEntity implements EntityPatch<ProcedureEntity> {

        private static final long serialVersionUID = 2966462269307558981L;
        private final ProcedureEntity entity;

        ProcedureEntityPatch(ProcedureEntity entity) {
            this.entity = entity;
        }

        public ProcedureEntity getEntity() {
            return entity;
        }
    }


    public static class SensorSerializer extends AbstractSTASerializer<SensorWithQueryOptions, ProcedureEntity> {

        private static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
        private static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";
        private static final long serialVersionUID = -2190624056257407974L;

        public SensorSerializer(String rootUrl, boolean implicitExpand, String... activeExtensions) {
            super(SensorWithQueryOptions.class, implicitExpand, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = SensorEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(SensorWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();
            value.unwrap(implicitSelect);
            ProcedureEntity sensor = value.getEntity();

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, sensor.getStaIdentifier());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, sensor.getStaIdentifier());
            }

            // actual properties
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, sensor.getName());
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, sensor.getDescription());
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                String format = sensor.getFormat().getFormat();
                if (format.equalsIgnoreCase(SENSORML_2)) {
                    format = STA_SENSORML_2;
                }
                gen.writeObjectField(STAEntityDefinition.PROP_ENCODINGTYPE, format);
            }

            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_METADATA)) {
                String metadata = "metadata";
                if (sensor.getDescriptionFile() != null && !sensor.getDescriptionFile().isEmpty()) {
                    metadata = sensor.getDescriptionFile();
                } else if (sensor.hasProcedureHistory()) {
                    Optional<ProcedureHistoryEntity> history =
                        sensor.getProcedureHistory().stream().filter(h -> h.getEndTime() == null).findFirst();
                    if (history.isPresent()) {
                        metadata = history.get().getXml();
                    }
                }
                gen.writeStringField(STAEntityDefinition.PROP_METADATA, metadata);
            }

            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                if (sensor.hasParameters()) {
                    gen.writeObjectFieldStart(STAEntityDefinition.PROP_PROPERTIES);
                    for (ParameterEntity<?> parameter : sensor.getParameters()) {
                        gen.writeObjectField(parameter.getName(), parameter.getValue());
                    }
                    gen.writeEndObject();
                } else {
                    gen.writeNullField(STAEntityDefinition.PROP_PROPERTIES);
                }
            }

            // navigation properties
            for (String navigationProperty : SensorEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!value.hasExpandOption() || value.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, sensor.getStaIdentifier());
                    } else {
                        if (SensorEntityDefinition.DATASTREAMS.equals(navigationProperty)) {
                            if (sensor.getDatasets() == null) {
                                writeNavigationProp(gen, navigationProperty, sensor.getStaIdentifier());
                            } else {
                                gen.writeFieldName(navigationProperty);
                                writeNestedCollectionOfType(Collections.unmodifiableSet(sensor.getDatasets()),
                                                            AbstractDatasetEntity.class,
                                                            value.getFieldsToExpand().get(navigationProperty),
                                                            gen,
                                                            serializers);
                            }
                        } else {
                            throw new IllegalStateException("Unexpected value: " + navigationProperty);
                        }
                    }
                }
            }
            gen.writeEndObject();
        }
    }


    public static class SensorDeserializer extends StdDeserializer<ProcedureEntity> {

        private static final long serialVersionUID = -6513819346703020350L;
        private final boolean variableEncodingType;

        public SensorDeserializer(boolean variableEncodingType) {
            super(ProcedureEntity.class);
            this.variableEncodingType = variableEncodingType;
        }

        @Override
        public ProcedureEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (variableEncodingType) {
                return p.readValueAs(JSONSensorVariableEncoding.class).toEntity(JSONBase.EntityType.FULL);
            } else {
                return p.readValueAs(JSONSensor.class).toEntity(JSONBase.EntityType.FULL);
            }
        }
    }


    public static class SensorPatchDeserializer extends StdDeserializer<ProcedureEntityPatch> {

        private static final long serialVersionUID = -6636765136530111251L;
        private final boolean variableEncodingType;

        public SensorPatchDeserializer(boolean variableEncodingType) {
            super(ProcedureEntityPatch.class);
            this.variableEncodingType = variableEncodingType;
        }

        @Override
        public ProcedureEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (variableEncodingType) {
                return new ProcedureEntityPatch(p.readValueAs(JSONSensorVariableEncoding.class)
                                                    .toEntity(JSONBase.EntityType.PATCH));
            } else {
                return new ProcedureEntityPatch(p.readValueAs(JSONSensor.class).toEntity(JSONBase.EntityType.PATCH));
            }
        }
    }
}
