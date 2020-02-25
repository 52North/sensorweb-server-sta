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
import org.n52.series.db.beans.PlatformEntity;
import org.n52.shetland.filter.AbstractPathFilter;
import org.n52.shetland.filter.PathFilterItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.ogc.sta.model.ThingEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONThing;
import org.n52.sta.serdes.util.ElementWithQueryOptions.ThingWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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


    public static class ThingSerializer extends AbstractSTASerializer<ThingWithQueryOptions> {

        private static final long serialVersionUID = -1618289129123682794L;

        public ThingSerializer(String rootUrl) {
            super(ThingWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = ThingEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(ThingWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            PlatformEntity thing = value.getEntity();
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
                writeId(gen, thing.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, thing.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, thing.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, thing.getDescription());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PROPERTIES)) {
                gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, thing.getProperties());
            }

            // navigation properties
            for (String navigationProperty : ThingEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, thing.getIdentifier());
                }
            }
            //TODO: Deal with $expand
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
