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
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.n52.shetland.ogc.sta.model.DatastreamEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.EntityPatch;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONDatastream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class DatastreamSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class DatastreamDTOPatch implements EntityPatch<DatastreamDTO> {

        private final DatastreamDTO entity;

        DatastreamDTOPatch(DatastreamDTO entity) {
            this.entity = entity;
        }

        @Override
        public DatastreamDTO getEntity() {
            return entity;
        }
    }


    public static class DatastreamSerializer
        extends AbstractSTASerializer<DatastreamDTO> {

        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();
        private static final long serialVersionUID = -6555417490577181829L;

        public DatastreamSerializer(String rootUrl,
                                    String... activeExtensions) {
            super(DatastreamDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = DatastreamEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(DatastreamDTO datastream,
                              JsonGenerator gen,
                              SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, datastream.getId());
            }
            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, datastream.getId());
            }

            // actual properties
            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getName());
            }
            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, datastream.getDescription());
            }

            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_OBSERVATION_TYPE)) {
                gen.writeObjectField(STAEntityDefinition.PROP_OBSERVATION_TYPE,
                                     datastream.getObservationType());
            }
            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_UOM)) {
                gen.writeObjectFieldStart(STAEntityDefinition.PROP_UOM);
                if (datastream.getUnitOfMeasurement() != null) {
                    gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getUnitOfMeasurement().getName());
                    gen.writeStringField(STAEntityDefinition.PROP_SYMBOL,
                                         datastream.getUnitOfMeasurement().getSymbol());
                    gen.writeStringField(STAEntityDefinition.PROP_DEFINITION,
                                         datastream.getUnitOfMeasurement().getDefinition());
                } else {
                    gen.writeNullField(STAEntityDefinition.PROP_NAME);
                    gen.writeNullField(STAEntityDefinition.PROP_SYMBOL);
                    gen.writeNullField(STAEntityDefinition.PROP_DEFINITION);
                }
                gen.writeEndObject();
            }

            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_OBSERVED_AREA)) {
                gen.writeFieldName(STAEntityDefinition.PROP_OBSERVED_AREA);
                if (datastream.getObservedArea() != null) {
                    gen.writeRawValue(GEO_JSON_WRITER.write(datastream.getObservedArea()));
                }
            }

            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_RESULT_TIME)) {
                if (datastream.getResultTime() != null) {
                    gen.writeStringField(STAEntityDefinition.PROP_RESULT_TIME,
                                         DateTimeHelper.format(datastream.getResultTime()));
                }
            }
            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
                if (datastream.getPhenomenonTime() != null) {
                    gen.writeStringField(STAEntityDefinition.PROP_PHENOMENON_TIME,
                                         DateTimeHelper.format(datastream.getPhenomenonTime()));
                }
            }

            if (!datastream.hasSelectOption() ||
                datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                //TODO: refactor to postgres-dao
                /*
                if (includeDatastreamCategory) {
                    // Add Category to parameters
                    gen.writeNumberField(categoryPrefix + "Id",
                                         datastream.getCategory().getId());
                    gen.writeStringField(categoryPrefix + "Name",
                                         datastream.getCategory().getName());
                    gen.writeStringField(categoryPrefix + "Description",
                                         datastream.getCategory().getDescription());
                }
                 */
                gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, datastream.getProperties());
            }

            // navigation properties
            for (String navigationProperty : DatastreamEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!datastream.hasSelectOption() || datastream.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!datastream.hasExpandOption() ||
                        datastream.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, datastream.getId());
                    } else {
                        switch (navigationProperty) {
                            case STAEntityDefinition.OBSERVATIONS:
                                if (datastream.getObservations() == null) {
                                    writeNavigationProp(gen, navigationProperty, datastream.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(datastream.getObservations()),
                                                          gen,
                                                          serializers);
                                }
                                break;
                            case STAEntityDefinition.OBSERVED_PROPERTY:
                                if (datastream.getObservedProperty() == null) {
                                    writeNavigationProp(gen, navigationProperty, datastream.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedEntity(datastream.getObservedProperty(),
                                                      gen,
                                                      serializers);
                                }
                                break;
                            case STAEntityDefinition.THING:
                                if (datastream.getThing() == null) {
                                    writeNavigationProp(gen, navigationProperty, datastream.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedEntity(datastream.getThing(),
                                                      gen,
                                                      serializers);
                                }
                                break;
                            case STAEntityDefinition.SENSOR:
                                if (datastream.getSensor() == null) {
                                    writeNavigationProp(gen, navigationProperty, datastream.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedEntity(datastream.getSensor(),
                                                      gen,
                                                      serializers);
                                }
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + navigationProperty);
                        }
                    }
                }
            }
            gen.writeEndObject();
        }
    }


    public static class DatastreamDeserializer extends StdDeserializer<DatastreamDTO> {

        private static final long serialVersionUID = 7491123624385588769L;

        public DatastreamDeserializer() {
            super(DatastreamDTO.class);
        }

        @Override
        public DatastreamDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONDatastream.class)
                .parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class DatastreamPatchDeserializer extends StdDeserializer<DatastreamDTOPatch> {

        private static final long serialVersionUID = 6354638503794606750L;

        public DatastreamPatchDeserializer() {
            super(DatastreamDTOPatch.class);
        }

        @Override
        public DatastreamDTOPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new DatastreamDTOPatch(p.readValueAs(JSONDatastream.class)
                                              .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
