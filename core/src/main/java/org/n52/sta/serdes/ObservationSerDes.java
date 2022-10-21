/*
 * Copyright (C) 2018-2022 52°North Initiative for Geospatial Open Source
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.ProfileDataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.SensorML20DataEntity;
import org.n52.series.db.beans.TrajectoryDataEntity;
import org.n52.series.db.beans.parameter.JsonParameterEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.ObservationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONObservation;
import org.n52.sta.serdes.util.ElementWithQueryOptions.ObservationWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ObservationSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class ObservationEntityPatch<T> extends DataEntity<T> implements EntityPatch<DataEntity<?>> {

        private static final long serialVersionUID = 7385044376634149048L;
        private final DataEntity<?> entity;

        ObservationEntityPatch(DataEntity<?> entity) {
            this.entity = entity;
        }

        @Override
        public DataEntity<?> getEntity() {
            return entity;
        }

        @Override public boolean isNoDataValue(Collection<String> noDataValues) {
            return false;
        }
    }


    public static class ObservationSerializer
        extends AbstractSTASerializer<ObservationWithQueryOptions, DataEntity<?>> {

        protected static final String VERTICAL = "vertical";
        private static final long serialVersionUID = -4575044340713191285L;
        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();

        public ObservationSerializer(String rootUrl, boolean implicitExpand, String... activeExtensions) {
            super(ObservationWithQueryOptions.class, implicitExpand, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = ObservationEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(ObservationWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();
            value.unwrap(implicitSelect);
            DataEntity<?> observation = value.getEntity();

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, observation.getStaIdentifier());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, observation.getStaIdentifier());
            }

            // actual properties
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_RESULT)) {
                gen.writeFieldName(STAEntityDefinition.PROP_RESULT);
                if (observation instanceof ProfileDataEntity) {
                    QueryOptions profileResultSchema =
                        new QueryOptions("",
                                         Collections.singleton(new SelectFilter(new HashSet<>(
                                             Arrays.asList(StaConstants.PROP_RESULT,
                                                           StaConstants.PROP_PARAMETERS,
                                                           VERTICAL)))));
                    writeNestedCollection(sortValuesByPhenomenonTime((Set<DataEntity<?>>) observation.getValue()),
                                          profileResultSchema,
                                          gen,
                                          serializers);
                } else if (observation instanceof TrajectoryDataEntity) {
                    QueryOptions trajectoryResultSchema =
                        new QueryOptions("",
                                         Collections.singleton(new SelectFilter(new HashSet<>(
                                             Arrays.asList(StaConstants.PROP_RESULT,
                                                           StaConstants.PROP_PARAMETERS,
                                                           StaConstants.PROP_PHENOMENON_TIME,
                                                           StaConstants.PROP_RESULT_TIME,
                                                           StaConstants.PROP_VALID_TIME)))));
                    writeNestedCollection(sortValuesByVerticalFrom((Set<DataEntity<?>>) observation.getValue()),
                                          trajectoryResultSchema,
                                          gen,
                                          serializers);
                } else if (observation instanceof QuantityDataEntity) {
                    gen.writeNumber(((QuantityDataEntity) observation).getValue());
                } else if (observation instanceof CountDataEntity) {
                    gen.writeNumber(((CountDataEntity) observation).getValue());
                } else if (observation instanceof SensorML20DataEntity) {
                    gen.writeRawValue(((SensorML20DataEntity) observation).getValue());
                } else {
                    gen.writeString(observation.getValue().toString());
                }
            }

            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_RESULT_TIME)) {
                if (observation.hasResultTime()) {
                    gen.writeStringField(STAEntityDefinition.PROP_RESULT_TIME,
                                         observation.getResultTime().toInstant().toString());
                } else {
                    // resultTime is mandatory (but null is allowed) so it must be serialized
                    gen.writeNullField(STAEntityDefinition.PROP_RESULT_TIME);
                }
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
                String phenomenonTime = DateTimeHelper.format(createPhenomenonTime(observation));
                gen.writeStringField(STAEntityDefinition.PROP_PHENOMENON_TIME, phenomenonTime);
            }

            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_RESULT_QUALITY)) {
                gen.writeNullField(STAEntityDefinition.PROP_RESULT_QUALITY);
            }

            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_VALID_TIME)) {
                if (observation.isSetValidTime()) {
                    gen.writeStringField(STAEntityDefinition.PROP_VALID_TIME,
                                         DateTimeHelper.format(createValidTime(observation)));
                } else {
                    gen.writeNullField(STAEntityDefinition.PROP_VALID_TIME);
                }
            }

            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PARAMETERS)) {
                if (observation.isSetGeometryEntity() ||
                    observation.hasParameters() ||
                    value.getFieldsToSerialize().contains(VERTICAL)) {
                    gen.writeObjectFieldStart(STAEntityDefinition.PROP_PARAMETERS);
                    if (value.getFieldsToSerialize().contains(VERTICAL)) {
                        gen.writeNumberField("verticalFrom", observation.getVerticalFrom());
                        gen.writeNumberField("verticalTo", observation.getVerticalTo());
                    }
                    if (observation.isSetGeometryEntity()) {
                        gen.writeFieldName("http://www.opengis.net/def/param-name/OGC-OM/2.0/samplingGeometry");
                        gen.writeRawValue(GEO_JSON_WRITER.write(observation.getGeometryEntity().getGeometry()));
                    }
                    if (observation.hasParameters()) {
                        for (ParameterEntity<?> parameter : observation.getParameters()) {
                            if (parameter instanceof JsonParameterEntity) {
                                ObjectMapper mapper = new ObjectMapper();
                                gen.writeObjectField(parameter.getName(),
                                                     mapper.readTree(parameter.getValueAsString()));
                            } else {
                                gen.writeStringField(parameter.getName(), parameter.getValueAsString());
                            }
                        }
                    }
                    gen.writeEndObject();
                } else {
                    gen.writeNullField(STAEntityDefinition.PROP_PARAMETERS);
                }
            }

            // navigation properties
            for (String navigationProperty : ObservationEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!value.hasExpandOption() || value.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, observation.getStaIdentifier());
                    } else {
                        switch (navigationProperty) {
                            case ObservationEntityDefinition.DATASTREAM:
                                if (observation.getDataset() == null) {
                                    writeNavigationProp(gen, navigationProperty, observation.getStaIdentifier());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedEntity(observation.getDataset(),
                                                      value.getFieldsToExpand().get(navigationProperty),
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
                                                      value.getFieldsToExpand().get(navigationProperty),
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

        private TreeSet sortValuesByPhenomenonTime(Set<DataEntity<?>> values) {
            ArrayList<DataEntity<?>> vals = new ArrayList<>(values);
            vals.sort(Comparator.comparing(HibernateRelations.HasPhenomenonTime::getPhenomenonTimeStart));
            return new TreeSet(vals);
        }

        private TreeSet sortValuesByVerticalFrom(Set<DataEntity<?>> values) {
            ArrayList<DataEntity<?>> vals = new ArrayList<>(values);
            vals.sort(Comparator.comparing(DataEntity::getVerticalFrom));
            return new TreeSet(vals);
        }
    }


    public static class ObservationDeserializer extends StdDeserializer<DataEntity<?>> {

        private static final long serialVersionUID = 2731654401126762133L;
        private final Map<String, String> parameterMapping;

        public ObservationDeserializer(Map<String, String> parameterMapping) {
            super(DataEntity.class);
            this.parameterMapping = parameterMapping;
        }

        @Override
        public DataEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONObservation.class)
                .parseParameters(parameterMapping)
                .toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class ObservationPatchDeserializer extends StdDeserializer<ObservationEntityPatch> {

        private static final long serialVersionUID = 9042768872493184420L;
        private final Map<String, String> parameterMapping;

        public ObservationPatchDeserializer(Map<String, String> parameterMapping) {
            super(ObservationEntityPatch.class);
            this.parameterMapping = parameterMapping;
        }

        @Override
        public ObservationEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new ObservationEntityPatch(p.readValueAs(JSONObservation.class)
                                                  .parseParameters(parameterMapping)
                                                  .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
