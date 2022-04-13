/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.api.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.shetland.ogc.sta.model.HistoricalLocationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.serdes.common.AbstractSTASerializer;
import org.n52.sta.api.serdes.common.JSONBase;
import org.n52.sta.api.serdes.json.JSONHistoricalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import org.n52.sta.api.dto.common.EntityPatch;

public class HistoricalLocationSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalLocationSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class HistoricalLocationDTOPatch implements EntityPatch<HistoricalLocationDTO> {

        private static final long serialVersionUID = -154825501303466727L;
        private final HistoricalLocationDTO entity;

        HistoricalLocationDTOPatch(HistoricalLocationDTO entity) {
            this.entity = entity;
        }

        public HistoricalLocationDTO getEntity() {
            return entity;
        }
    }


    public static class HistoricalLocationSerializer
        extends AbstractSTASerializer<HistoricalLocationDTO> {

        private static final long serialVersionUID = 8651925159358792370L;

        public HistoricalLocationSerializer(String rootUrl, String... activeExtensions) {
            super(HistoricalLocationDTO.class, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = HistoricalLocationEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(HistoricalLocationDTO histLoc,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            // The UML in Section 8.2 of the OGC STA v1.0 defines the relations as "Things"
            // The Definition in Section 8.2.3 of the OGC STA v1.0 defines the relations as "Thing"
            // We will allow both for now
            if (histLoc.hasSelectOption() && histLoc.hasExpandOption() &&
                histLoc.getFieldsToExpand().containsKey(STAEntityDefinition.THINGS)) {
                histLoc.getFieldsToSerialize().add(STAEntityDefinition.THING);
                histLoc.getFieldsToExpand()
                    .put(STAEntityDefinition.THING, histLoc.getFieldsToExpand().get(STAEntityDefinition.THINGS));
            }

            // olingo @iot links
            if (!histLoc.hasSelectOption() || histLoc.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, histLoc.getId());
            }
            if (!histLoc.hasSelectOption() ||
                histLoc.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, histLoc.getId());
            }

            // actual properties
            if (!histLoc.hasSelectOption() || histLoc.getFieldsToSerialize().contains(STAEntityDefinition.PROP_TIME)) {
                gen.writeStringField(STAEntityDefinition.PROP_TIME, DateTimeHelper.format(histLoc.getTime()));
            }

            // navigation properties
            for (String navigationProperty : HistoricalLocationEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!histLoc.hasSelectOption() || histLoc.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!histLoc.hasExpandOption() || histLoc.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, histLoc.getId());
                    } else {
                        switch (navigationProperty) {
                            case HistoricalLocationEntityDefinition.THING:
                                if (histLoc.getThing() == null) {
                                    writeNavigationProp(gen, navigationProperty, histLoc.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedEntity(histLoc.getThing(),
                                                      gen,
                                                      serializers);
                                }
                                break;
                            case HistoricalLocationEntityDefinition.LOCATIONS:
                                if (histLoc.getLocations() == null) {
                                    writeNavigationProp(gen, navigationProperty, histLoc.getId());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(histLoc.getLocations()),
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


    public static class HistoricalLocationDeserializer extends StdDeserializer<HistoricalLocationDTO> {

        private static final long serialVersionUID = -7462598674289427663L;

        public HistoricalLocationDeserializer() {
            super(HistoricalLocationDTO.class);
        }

        @Override
        public HistoricalLocationDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONHistoricalLocation.class).parseToDTO(JSONBase.EntityType.FULL);
        }
    }


    public static class HistoricalLocationPatchDeserializer extends StdDeserializer<HistoricalLocationDTOPatch> {

        private static final long serialVersionUID = 8354140158937306874L;

        public HistoricalLocationPatchDeserializer() {
            super(HistoricalLocationDTOPatch.class);
        }

        @Override
        public HistoricalLocationDTOPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new HistoricalLocationDTOPatch(p.readValueAs(JSONHistoricalLocation.class)
                                                      .parseToDTO(JSONBase.EntityType.PATCH));
        }
    }
}
