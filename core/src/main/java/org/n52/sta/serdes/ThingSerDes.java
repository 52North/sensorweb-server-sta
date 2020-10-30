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
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.ogc.sta.model.ThingEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONThing;
import org.n52.sta.serdes.util.ElementWithQueryOptions.ThingWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class ThingSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class PlatformEntityPatch extends PlatformEntity implements EntityPatch<PlatformEntity> {

        private static final long serialVersionUID = -2233037380407692718L;
        private final PlatformEntity entity;

        PlatformEntityPatch(PlatformEntity entity) {
            this.entity = entity;
        }

        public PlatformEntity getEntity() {
            return entity;
        }
    }


    public static class ThingSerializer extends AbstractSTASerializer<ThingWithQueryOptions, PlatformEntity> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ThingSerializer(String rootUrl, boolean implicitExpand, String... activeExtensions) {
            super(ThingWithQueryOptions.class, implicitExpand, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = ThingEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(ThingWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();
            value.unwrap(implicitSelect);
            PlatformEntity thing = value.getEntity();

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, thing.getStaIdentifier());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, thing.getStaIdentifier());
            }

            // actual properties
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, thing.getName());
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, thing.getDescription());
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                if (thing.hasParameters()) {
                    gen.writeObjectFieldStart(STAEntityDefinition.PROP_PROPERTIES);
                    for (ParameterEntity<?> parameter : thing.getParameters()) {
                        gen.writeObjectField(parameter.getName(), parameter.getValue());
                    }
                    gen.writeEndObject();
                } else {
                    gen.writeNullField(STAEntityDefinition.PROP_PROPERTIES);
                }
            }

            // navigation properties
            for (String navigationProperty : ThingEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!value.hasExpandOption() || value.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, thing.getStaIdentifier());
                    } else {
                        switch (navigationProperty) {
                            case ThingEntityDefinition.DATASTREAMS:
                                if (thing.getDatasets() == null) {
                                    writeNavigationProp(gen, navigationProperty, thing.getStaIdentifier());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollectionOfType(Collections.unmodifiableSet(thing.getDatasets()),
                                                                AbstractDatasetEntity.class,
                                                                value.getFieldsToExpand().get(navigationProperty),
                                                                gen,
                                                                serializers);
                                }
                                break;
                            case ThingEntityDefinition.HISTORICAL_LOCATIONS:
                                if (thing.getHistoricalLocations() == null) {
                                    writeNavigationProp(gen, navigationProperty, thing.getStaIdentifier());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(thing.getHistoricalLocations()),
                                                          value.getFieldsToExpand().get(navigationProperty),
                                                          gen,
                                                          serializers);
                                }
                                break;
                            case ThingEntityDefinition.LOCATIONS:
                                if (thing.getLocations() == null) {
                                    writeNavigationProp(gen, navigationProperty, thing.getStaIdentifier());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(thing.getLocations()),
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

    }


    public static class ThingDeserializer extends StdDeserializer<PlatformEntity> {

        private static final long serialVersionUID = 3942005672394573517L;

        public ThingDeserializer() {
            super(PlatformEntity.class);
        }

        @Override
        public PlatformEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONThing.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class ThingPatchDeserializer extends StdDeserializer<PlatformEntityPatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public ThingPatchDeserializer() {
            super(PlatformEntityPatch.class);
        }

        @Override
        public PlatformEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new PlatformEntityPatch(p.readValueAs(JSONThing.class).toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
