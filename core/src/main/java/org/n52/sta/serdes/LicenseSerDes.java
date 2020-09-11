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
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONLicense;
import org.n52.sta.serdes.util.ElementWithQueryOptions.LicenseWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;

import java.io.IOException;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class LicenseSerDes {

    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class LicensePatch extends LicenseEntity implements EntityPatch<LicenseEntity> {

        private static final long serialVersionUID = 742336485455358972L;

        private final LicenseEntity entity;

        LicensePatch(LicenseEntity entity) {
            this.entity = entity;
        }

        public LicenseEntity getEntity() {
            return entity;
        }
    }


    public static class LicenseSerializer
            extends AbstractSTASerializer<LicenseWithQueryOptions, LicenseEntity> {

        private static final long serialVersionUID = -1618289129123682794L;

        public LicenseSerializer(String rootUrl, boolean implicitExpand, String... activeExtensions) {
            super(LicenseWithQueryOptions.class, implicitExpand, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = StaConstants.LICENSES;
        }

        @Override
        public void serialize(LicenseWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            value.unwrap(implicitSelect);
            LicenseEntity license = value.getEntity();

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, license.getStaIdentifier());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, license.getStaIdentifier());
            }

            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, license.getName());
            }
            if (!value.hasSelectOption() ||
                    value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DEFINITION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DEFINITION, license.getDefinition());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_LOGO)) {
                gen.writeStringField(STAEntityDefinition.PROP_LOGO, license.getLogo());
            }

            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.DATASTREAMS)) {
                if (!value.hasExpandOption() ||
                        value.getFieldsToExpand().get(STAEntityDefinition.DATASTREAMS) == null) {
                    writeNavigationProp(gen, STAEntityDefinition.DATASTREAMS, license.getStaIdentifier());
                } else {
                    gen.writeFieldName(STAEntityDefinition.DATASTREAMS);
                    writeNestedCollection(license.getDatasets(),
                                          value.getFieldsToExpand().get(STAEntityDefinition.DATASTREAMS),
                                          gen,
                                          serializers);
                }
            }
            gen.writeEndObject();
        }

    }


    public static class LicenseDeserializer extends StdDeserializer<LicenseEntity> {

        private static final long serialVersionUID = 3942005672394573517L;

        public LicenseDeserializer() {
            super(LicenseEntity.class);
        }

        @Override
        public LicenseEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONLicense.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class LicensePatchDeserializer
            extends StdDeserializer<LicenseSerDes.LicensePatch> {

        private static final long serialVersionUID = -6355786322787893665L;

        public LicensePatchDeserializer() {
            super(ThingSerDes.PlatformEntityPatch.class);
        }

        @Override
        public LicenseSerDes.LicensePatch deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new LicenseSerDes.LicensePatch(p.readValueAs(JSONLicense.class)
                                                   .toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
