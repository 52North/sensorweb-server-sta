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
import org.n52.series.db.beans.PlatformEntity;
import org.n52.sta.serdes.json.JSONThing;
import org.n52.sta.serdes.model.ElementWithQueryOptions.ThingWithQueryOptions;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.serdes.model.ThingEntityDefinition;
import org.n52.sta.utils.QueryOptions;

import java.io.IOException;
import java.util.Set;

public class ThingSerDes {

    public static class PlatformEntityPatch extends PlatformEntity implements EntityPatch<PlatformEntity> {
        private final PlatformEntity entity;

        public PlatformEntityPatch (PlatformEntity entity) {
            this.entity = entity;
        }

        public PlatformEntity getEntity() {
            return entity;
        }
    }

    public static class ThingSerializer extends AbstractSTASerializer<ThingWithQueryOptions> {

        public ThingSerializer(String rootUrl) {
            super(ThingWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = ThingEntityDefinition.entitySetName;
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
                hasSelectOption = options.hasSelectOption();
                if (hasSelectOption) {
                    fieldsToSerialize = options.getSelectOption();
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
            for (String navigationProperty : ThingEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, thing.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }

    }

    public static class ThingDeserializer extends StdDeserializer<PlatformEntity> {

        public ThingDeserializer() {
            super(PlatformEntity.class);
        }

        @Override
        public PlatformEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONThing.class).toEntity();
        }
    }

    public static class ThingPatchDeserializer extends StdDeserializer<PlatformEntityPatch> {

        public ThingPatchDeserializer() {
            super(PlatformEntityPatch.class);
        }

        @Override
        public PlatformEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new PlatformEntityPatch(p.readValueAs(JSONThing.class).toEntity(false));
        }
    }
}
