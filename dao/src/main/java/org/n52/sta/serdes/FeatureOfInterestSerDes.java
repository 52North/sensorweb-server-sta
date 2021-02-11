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
import org.n52.shetland.ogc.sta.model.FeatureOfInterestEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.EntityPatch;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONFeatureOfInterest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class FeatureOfInterestSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureOfInterestSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class FeatureOfInterestDTOPatch implements EntityPatch<FeatureOfInterestDTO> {

        private static final long serialVersionUID = 4488526324452194583L;
        private final FeatureOfInterestDTO entity;

        FeatureOfInterestDTOPatch(FeatureOfInterestDTO entity) {
            this.entity = entity;
        }

        @Override
        public FeatureOfInterestDTO getEntity() {
            return entity;
        }
    }


    public static class FeatureOfInterestSerializer
        extends AbstractSTASerializer<FeatureOfInterestDTO> {

        private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();
        private static final long serialVersionUID = -2476879916353087078L;

        public FeatureOfInterestSerializer(String rootUrl, String... activeExtensions) {
            super(FeatureOfInterestDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = FeatureOfInterestEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(FeatureOfInterestDTO feature,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            // olingo @iot links
            if (!feature.hasSelectOption() || feature.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, feature.getId());
            }
            if (!feature.hasSelectOption() ||
                feature.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, feature.getId());
            }

            // actual properties
            if (!feature.hasSelectOption() || feature.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, feature.getName());
            }
            if (!feature.hasSelectOption() ||
                feature.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, feature.getDescription());
            }
            if (!feature.hasSelectOption() ||
                feature.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                // only write out encodingtype if there is a location present
                if (feature.getFeature() != null) {
                    gen.writeStringField(STAEntityDefinition.PROP_ENCODINGTYPE, ENCODINGTYPE_GEOJSON);
                }
            }
            if (!feature.hasSelectOption() ||
                feature.getFieldsToSerialize().contains(STAEntityDefinition.PROP_FEATURE)) {
                gen.writeFieldName(STAEntityDefinition.PROP_FEATURE);
                gen.writeRawValue(GEO_JSON_WRITER.write(feature.getFeature()));
            }
            if (!feature.hasSelectOption() ||
                feature.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, feature.getProperties());
            }

            // navigation properties
            for (String navigationProperty : FeatureOfInterestEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!feature.hasSelectOption() || feature.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!feature.hasExpandOption() || feature.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, feature.getId());
                    } else {
                        switch (navigationProperty) {
                            case STAEntityDefinition.OBSERVATIONS:
                                if (feature.getObservations() == null) {
                                    writeNavigationProp(gen, navigationProperty, feature.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(feature.getObservations()),
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


    public static class FeatureOfInterestDeserializer extends StdDeserializer<FeatureOfInterestDTO> {

        private static final long serialVersionUID = 2394467109279839681L;

        public FeatureOfInterestDeserializer() {
            super(FeatureOfInterestDTO.class);
        }

        @Override
        public FeatureOfInterestDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONFeatureOfInterest.class).parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class FeatureOfInterestPatchDeserializer extends StdDeserializer<FeatureOfInterestDTOPatch> {

        private static final long serialVersionUID = 7273345348512569187L;

        public FeatureOfInterestPatchDeserializer() {
            super(FeatureOfInterestDTOPatch.class);
        }

        @Override
        public FeatureOfInterestDTOPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new FeatureOfInterestDTOPatch(p.readValueAs(JSONFeatureOfInterest.class)
                                                     .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
