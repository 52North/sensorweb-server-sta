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
import org.n52.series.db.beans.BlobDataEntity;
import org.n52.series.db.beans.BooleanDataEntity;
import org.n52.series.db.beans.CategoryDataEntity;
import org.n52.series.db.beans.ComplexDataEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataArrayDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.GeometryDataEntity;
import org.n52.series.db.beans.ProfileDataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ReferencedDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ObservationSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class StaDataEntityPatch extends StaDataEntity implements EntityPatch<DataEntity> {

        private static final long serialVersionUID = 7385044376634149048L;
        private final StaDataEntity entity;

        StaDataEntityPatch(StaDataEntity entity) {
            this.entity = entity;
        }

        public StaDataEntity getEntity() {
            return entity;
        }
    }


    public static class ObservationSerializer extends AbstractSTASerializer<ObservationWithQueryOptions> {

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
            StaDataEntity<?> observation = value.getEntity();
            QueryOptions options = value.getQueryOptions();

            Set<String> fieldsToSerialize = null;
            Map<String, QueryOptions> fieldsToExpand = new HashMap<>();
            boolean hasSelectOption = false;
            boolean hasExpandOption = false;
            if (options != null) {
                if (options.hasSelectOption()) {
                    hasSelectOption = true;
                    fieldsToSerialize = options.getSelectOption().getItems();
                }
                if (options.hasExpandOption()) {
                    hasExpandOption = true;
                    for (ExpandItem item : options.getExpandOption().getItems()) {
                        fieldsToExpand.put(item.getPath(), item.getQueryOptions());
                    }
                }
            }

            // olingo @iot links
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, observation.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, observation.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT)) {
                gen.writeStringField(STAEntityDefinition.PROP_RESULT, getResult(observation));
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_TIME)) {
                Date resultTime = observation.getResultTime();
                Date samplingTime = observation.getSamplingTimeEnd();
                if (!resultTime.equals(samplingTime)) {
                    gen.writeStringField(STAEntityDefinition.PROP_RESULT_TIME, resultTime.toInstant().toString());
                } else {
                    gen.writeFieldName(STAEntityDefinition.PROP_RESULT_TIME);
                    gen.writeNull();
                }
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
                String phenomenonTime = DateTimeHelper.format(createPhenomenonTime(observation));
                gen.writeStringField(STAEntityDefinition.PROP_PHENOMENON_TIME, phenomenonTime);
            }
            //            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_QUALITY)) {
            //                //TODO: implement
            //                //throw new NotImplementedException();
            //            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_VALID_TIME)) {
                if (observation.isSetValidTime()) {
                    gen.writeStringField(STAEntityDefinition.PROP_VALID_TIME,
                                         DateTimeHelper.format(createValidTime(observation)));
                }
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PARAMETERS)) {
                if (observation.hasParameters()) {
                    gen.writeArrayFieldStart(STAEntityDefinition.PROP_PARAMETERS);
                    for (ParameterEntity<?> parameter : observation.getParameters()) {
                        gen.writeStartObject();
                        gen.writeStringField("name", parameter.getName());
                        gen.writeStringField("value", parameter.getValueAsString());
                        gen.writeEndObject();
                    }
                    gen.writeEndArray();
                }
            }

            // navigation properties
            for (String navigationProperty : ObservationEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    if (!hasExpandOption || fieldsToExpand.get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, observation.getIdentifier());
                    } else {
                        gen.writeFieldName(navigationProperty);
                        switch (navigationProperty) {
                        case ObservationEntityDefinition.DATASTREAM:
                            writeNestedEntity(observation.getDatastream(),
                                              fieldsToExpand.get(navigationProperty),
                                              gen,
                                              serializers);
                            break;
                        case ObservationEntityDefinition.FEATURE_OF_INTEREST:
                            writeNestedEntity(observation.getFeatureOfInterest(),
                                              fieldsToExpand.get(navigationProperty),
                                              gen,
                                              serializers);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + navigationProperty);
                        }
                    }
                }
            }
            gen.writeEndObject();
        }

        private Time createPhenomenonTime(DataEntity<?> observation) {
            final DateTime start = new DateTime(observation.getSamplingTimeStart(), DateTimeZone.UTC);
            DateTime end;
            if (observation.getSamplingTimeEnd() != null) {
                end = new DateTime(observation.getSamplingTimeEnd(), DateTimeZone.UTC);
            } else {
                end = start;
            }
            return createTime(start, end);
        }

        private Time createValidTime(DataEntity<?> observation) {
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

        private String getResult(DataEntity o) {
            if (o instanceof QuantityDataEntity) {
                if ((((QuantityDataEntity) o).getValue().doubleValue() - ((QuantityDataEntity) o).getValue()
                                                                                                 .intValue()) == 0.0) {
                    return Integer.toString(((QuantityDataEntity) o).getValue().intValue());
                }
                return ((QuantityDataEntity) o).getValue().toString();
            } else if (o instanceof BlobDataEntity) {
                // TODO: check if Object.tostring is what we want here
                return o.getValue().toString();
            } else if (o instanceof BooleanDataEntity) {
                return ((BooleanDataEntity) o).getValue().toString();
            } else if (o instanceof CategoryDataEntity) {
                return ((CategoryDataEntity) o).getValue();
            } else if (o instanceof ComplexDataEntity) {

                // TODO: implement
                // return ((ComplexDataEntity)o).getValue();
                return null;

            } else if (o instanceof CountDataEntity) {
                return ((CountDataEntity) o).getValue().toString();
            } else if (o instanceof GeometryDataEntity) {

                // TODO: check if we want WKT here
                return ((GeometryDataEntity) o).getValue().getGeometry().toText();

            } else if (o instanceof TextDataEntity) {
                return ((TextDataEntity) o).getValue();
            } else if (o instanceof DataArrayDataEntity) {

                // TODO: implement
                // return ((DataArrayDataEntity)o).getValue();
                return null;

            } else if (o instanceof ProfileDataEntity) {

                // TODO: implement
                // return ((ProfileDataEntity)o).getValue();
                return null;

            } else if (o instanceof ReferencedDataEntity) {
                return ((ReferencedDataEntity) o).getValue();
            }
            return "";
        }

    }


    public static class ObservationDeserializer extends StdDeserializer<StaDataEntity> {

        private static final long serialVersionUID = 2731654401126762133L;

        public ObservationDeserializer() {
            super(StaDataEntity.class);
        }

        @Override
        public StaDataEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONObservation.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class ObservationPatchDeserializer extends StdDeserializer<StaDataEntityPatch> {

        private static final long serialVersionUID = 9042768872493184420L;

        public ObservationPatchDeserializer() {
            super(StaDataEntityPatch.class);
        }

        @Override
        public StaDataEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new StaDataEntityPatch(p.readValueAs(JSONObservation.class).toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
