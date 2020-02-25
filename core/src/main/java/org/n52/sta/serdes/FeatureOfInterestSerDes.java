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
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.shetland.filter.AbstractPathFilter;
import org.n52.shetland.filter.PathFilterItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.model.FeatureOfInterestEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONFeatureOfInterest;
import org.n52.sta.serdes.util.ElementWithQueryOptions.FeatureOfInterestWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FeatureOfInterestSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureOfInterestSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class AbstractFeatureEntityPatch extends AbstractFeatureEntity
            implements EntityPatch<AbstractFeatureEntity> {

        private static final long serialVersionUID = 4488526324452194583L;
        private final AbstractFeatureEntity entity;

        AbstractFeatureEntityPatch(FeatureEntity entity) {
            this.entity = entity;
        }

        @Override
        public AbstractFeatureEntity getEntity() {
            return entity;
        }
    }


    public static class FeatureOfInterestSerializer extends AbstractSTASerializer<FeatureOfInterestWithQueryOptions> {

        private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();
        private static final long serialVersionUID = -2476879916353087078L;

        public FeatureOfInterestSerializer(String rootUrl) {
            super(FeatureOfInterestWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = FeatureOfInterestEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(FeatureOfInterestWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            AbstractFeatureEntity feature = value.getEntity();
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
                writeId(gen, feature.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, feature.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, feature.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, feature.getDescription());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                // only write out encodingtype if there is a location present
                if (feature.isSetGeometry()) {
                    gen.writeStringField(STAEntityDefinition.PROP_ENCODINGTYPE, ENCODINGTYPE_GEOJSON);
                }
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_FEATURE)) {
                gen.writeFieldName(STAEntityDefinition.PROP_FEATURE);
                gen.writeRawValue(GEO_JSON_WRITER.write(feature.getGeometryEntity().getGeometry()));
            }

            // navigation properties
            for (String navigationProperty : FeatureOfInterestEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, feature.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }
    }


    public static class FeatureOfInterestDeserializer extends StdDeserializer<AbstractFeatureEntity<?>> {

        private static final long serialVersionUID = 2394467109279839681L;

        public FeatureOfInterestDeserializer() {
            super(AbstractFeatureEntity.class);
        }

        @Override
        public AbstractFeatureEntity<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONFeatureOfInterest.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class FeatureOfInterestPatchDeserializer extends StdDeserializer<AbstractFeatureEntityPatch> {

        private static final long serialVersionUID = 7273345348512569187L;

        public FeatureOfInterestPatchDeserializer() {
            super(AbstractFeatureEntityPatch.class);
        }

        @Override
        public AbstractFeatureEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new AbstractFeatureEntityPatch(p.readValueAs(JSONFeatureOfInterest.class)
                                                   .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
