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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterJsonEntity;
import org.n52.series.db.beans.sta.ObservationEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sta.model.ObservationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONObservation;
import org.n52.sta.serdes.util.ElementWithQueryOptions.ObservationWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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


    public static class ObservationSerializer extends AbstractSTASerializer<ObservationWithQueryOptions> {

        private static final long serialVersionUID = -4575044340713191285L;

        private static final String VALUE = "value";

        public ObservationSerializer(String rootUrl) {
            super(ObservationWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = ObservationEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(ObservationWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            ObservationEntity<?> observation = value.getEntity();
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
                writeId(gen, observation.getStaIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, observation.getStaIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT)) {
                gen.writeStringField(STAEntityDefinition.PROP_RESULT, observation.getValue().toString());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_TIME)) {
                if (observation.hasResultTime()) {
                    gen.writeStringField(STAEntityDefinition.PROP_RESULT_TIME,
                                         observation.getResultTime().toInstant().toString());
                } else {
                    gen.writeNullField(STAEntityDefinition.PROP_RESULT_TIME);
                }
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
                String phenomenonTime = DateTimeHelper.format(createPhenomenonTime(observation));
                gen.writeStringField(STAEntityDefinition.PROP_PHENOMENON_TIME, phenomenonTime);
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_QUALITY)) {
                gen.writeNullField(STAEntityDefinition.PROP_RESULT_QUALITY);
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_VALID_TIME)) {
                if (observation.isSetValidTime()) {
                    gen.writeStringField(STAEntityDefinition.PROP_VALID_TIME,
                                         DateTimeHelper.format(createValidTime(observation)));
                } else {
                    gen.writeNullField(STAEntityDefinition.PROP_VALID_TIME);
                }
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PARAMETERS)) {
                gen.writeArrayFieldStart(STAEntityDefinition.PROP_PARAMETERS);
                if (observation.hasParameters()) {
                    for (ParameterEntity<?> parameter : observation.getParameters()) {
                        gen.writeStartObject();
                        gen.writeStringField("name", parameter.getName());
                        if (parameter instanceof ParameterJsonEntity) {
                            ObjectMapper mapper = new ObjectMapper();
                            gen.writeObjectField(VALUE, mapper.readTree(parameter.getValueAsString()));
                        } else {
                            gen.writeStringField(VALUE, parameter.getValueAsString());
                        }
                        gen.writeEndObject();
                    }
                }
                gen.writeEndArray();
            }

            // navigation properties
            for (String navigationProperty : ObservationEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    if (!hasExpandOption || fieldsToExpand.get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, observation.getStaIdentifier());
                    } else {
                        switch (navigationProperty) {
                        case ObservationEntityDefinition.DATASTREAM:
                            if (observation.getDataset() == null) {
                                writeNavigationProp(gen, navigationProperty, observation.getStaIdentifier());
                            } else {
                                gen.writeFieldName(navigationProperty);
                                writeNestedEntity(observation.getDataset(),
                                                  fieldsToExpand.get(navigationProperty),
                                                  gen,
                                                  serializers);
                            }
                            break;
                        case ObservationEntityDefinition.FEATURE_OF_INTEREST:
                            if (observation.getFeature() == null) {
                                writeNavigationProp(gen, navigationProperty, observation.getStaIdentifier());
                            } else {
                                gen.writeFieldName(navigationProperty);
                                writeNestedEntity(observation.getFeature(),
                                                  fieldsToExpand.get(navigationProperty),
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

        private Time createPhenomenonTime(ObservationEntity<?> observation) {
            final DateTime start = new DateTime(observation.getSamplingTimeStart(), DateTimeZone.UTC);
            DateTime end;
            if (observation.getSamplingTimeEnd() != null) {
                end = new DateTime(observation.getSamplingTimeEnd(), DateTimeZone.UTC);
            } else {
                end = start;
            }
            return createTime(start, end);
        }

        private Time createValidTime(ObservationEntity<?> observation) {
            final DateTime start = new DateTime(observation.getValidTimeStart(), DateTimeZone.UTC);
            DateTime end;
            if (observation.getValidTimeEnd() != null) {
                end = new DateTime(observation.getValidTimeEnd(), DateTimeZone.UTC);
            } else {
                end = start;
            }
            return createTime(start, end);
        }

        private Time createTime(DateTime start, DateTime end) {
            if (start.equals(end)) {
                return new TimeInstant(start);
            } else {
                return new TimePeriod(start, end);
            }
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
