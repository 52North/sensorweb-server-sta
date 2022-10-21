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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.ogc.sta.model.HistoricalLocationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONHistoricalLocation;
import org.n52.sta.serdes.util.ElementWithQueryOptions.HistoricalLocationWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

@SuppressFBWarnings({"EI_EXPOSE_REP"})
public class HistoricalLocationSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalLocationSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class HistoricalLocationEntityPatch extends HistoricalLocationEntity
        implements EntityPatch<HistoricalLocationEntity> {

        private static final long serialVersionUID = -154825501303466727L;
        private final HistoricalLocationEntity entity;

        HistoricalLocationEntityPatch(HistoricalLocationEntity entity) {
            this.entity = entity;
        }

        public HistoricalLocationEntity getEntity() {
            return entity;
        }
    }


    public static class HistoricalLocationSerializer
        extends AbstractSTASerializer<HistoricalLocationWithQueryOptions, HistoricalLocationEntity> {

        private static final long serialVersionUID = 8651925159358792370L;

        public HistoricalLocationSerializer(String rootUrl, boolean implicitExpand, String... activeExtensions) {
            super(HistoricalLocationWithQueryOptions.class, implicitExpand, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = HistoricalLocationEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(HistoricalLocationWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            value.unwrap(implicitSelect);
            HistoricalLocationEntity histLoc = value.getEntity();
            // The UML in Section 8.2 of the OGC STA v1.0 defines the relations as "Things"
            // The Definition in Section 8.2.3 of the OGC STA v1.0 defines the relations as "Thing"
            // We will allow both for now
            if (value.hasSelectOption() && value.hasExpandOption() &&
                value.getFieldsToExpand().containsKey(STAEntityDefinition.THINGS)) {
                value.getFieldsToSerialize().add(STAEntityDefinition.THING);
                value.getFieldsToExpand()
                    .put(STAEntityDefinition.THING, value.getFieldsToExpand().get(STAEntityDefinition.THINGS));
            }

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, histLoc.getStaIdentifier());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, histLoc.getStaIdentifier());
            }

            // actual properties
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_TIME)) {
                gen.writeStringField(STAEntityDefinition.PROP_TIME, histLoc.getTime().toInstant().toString());
            }

            // navigation properties
            for (String navigationProperty : HistoricalLocationEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!value.hasExpandOption() || value.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, histLoc.getStaIdentifier());
                    } else {
                        switch (navigationProperty) {
                            case HistoricalLocationEntityDefinition.THING:
                                if (histLoc.getThing() == null) {
                                    writeNavigationProp(gen, navigationProperty, histLoc.getStaIdentifier());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedEntity(histLoc.getThing(),
                                                      value.getFieldsToExpand().get(navigationProperty),
                                                      gen,
                                                      serializers);
                                }
                                break;
                            case HistoricalLocationEntityDefinition.LOCATIONS:
                                if (histLoc.getLocations() == null) {
                                    writeNavigationProp(gen, navigationProperty, histLoc.getStaIdentifier());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(histLoc.getLocations()),
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


    public static class HistoricalLocationDeserializer extends StdDeserializer<HistoricalLocationEntity> {

        private static final long serialVersionUID = -7462598674289427663L;

        public HistoricalLocationDeserializer() {
            super(HistoricalLocationEntity.class);
        }

        @Override
        public HistoricalLocationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONHistoricalLocation.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class HistoricalLocationPatchDeserializer extends StdDeserializer<HistoricalLocationEntityPatch> {

        private static final long serialVersionUID = 8354140158937306874L;

        public HistoricalLocationPatchDeserializer() {
            super(HistoricalLocationEntityPatch.class);
        }

        @Override
        public HistoricalLocationEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new HistoricalLocationEntityPatch(p.readValueAs(JSONHistoricalLocation.class)
                                                         .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
