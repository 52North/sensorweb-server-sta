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
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.shetland.filter.AbstractPathFilter;
import org.n52.shetland.filter.PathFilterItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.model.DatastreamEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONDatastream;
import org.n52.sta.serdes.util.ElementWithQueryOptions.DatastreamWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.n52.sta.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
            extends AbstractSTASerializer<DatastreamWithQueryOptions> {

        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();
        private static final long serialVersionUID = -6555417490577181829L;

        public DatastreamSerializer(String rootUrl) {
            super(DatastreamWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = DatastreamEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(DatastreamWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            DatastreamEntity datastream = value.getEntity();
            QueryOptions options = value.getQueryOptions();

            Set<String> fieldsToSerialize = null;
            boolean hasSelectOption = false;
            if (options != null) {
                if (options.hasSelectOption()) {
                    hasSelectOption = true;
                    fieldsToSerialize = ((AbstractPathFilter) options.getSelectOption())
                            .getItems()
                            .stream()
                            .map(PathFilterItem::getPath)
                            .collect(Collectors.toSet());
                }
            }
            // olingo @iot links
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, datastream.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, datastream.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, datastream.getDescription());
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_OBSERVATION_TYPE)) {
                gen.writeObjectField(STAEntityDefinition.PROP_OBSERVATION_TYPE,
                                     datastream.getObservationType().getFormat());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_UOM)) {
                gen.writeObjectFieldStart(STAEntityDefinition.PROP_UOM);
                gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getUnitOfMeasurement().getName());
                gen.writeStringField(STAEntityDefinition.PROP_SYMBOL, datastream.getUnitOfMeasurement().getSymbol());
                gen.writeStringField(STAEntityDefinition.PROP_DEFINITION, datastream.getUnitOfMeasurement().getLink());
                gen.writeEndObject();
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_OBSERVED_AREA)) {
                gen.writeFieldName(STAEntityDefinition.PROP_OBSERVED_AREA);
                if (datastream.getGeometryEntity() != null) {
                    gen.writeRawValue(GEO_JSON_WRITER.write(datastream.getGeometryEntity().getGeometry()));
                } else {
                    gen.writeNull();
                }
            }

            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_TIME)) {
                if (datastream.getResultTimeStart() != null) {
                    Time time = TimeUtil.createTime(TimeUtil.createDateTime(datastream.getResultTimeStart()),
                                                    TimeUtil.createDateTime(datastream.getResultTimeEnd()));
                    gen.writeStringField(STAEntityDefinition.PROP_RESULT_TIME,
                                         DateTimeHelper.format(time));
                }
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
                if (datastream.getSamplingTimeStart() != null) {
                    Time time = TimeUtil.createTime(TimeUtil.createDateTime(datastream.getSamplingTimeStart()),
                                                    TimeUtil.createDateTime(datastream.getSamplingTimeEnd()));
                    gen.writeStringField(STAEntityDefinition.PROP_PHENOMENON_TIME,
                                         DateTimeHelper.format(time));
                }
            }

            // navigation properties
            for (String navigationProperty : DatastreamEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, datastream.getIdentifier());
                }
            }
            //TODO: Deal with $expand
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
