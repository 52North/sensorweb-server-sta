/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.sta.serdes.json.JSONHistoricalLocation;
import org.n52.sta.serdes.model.ElementWithQueryOptions.HistoricalLocationWithQueryOptions;
import org.n52.sta.serdes.model.HistoricalLocationEntityDefinition;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.service.query.QueryOptions;

import java.io.IOException;
import java.util.Set;

public class HistoricalLocationSerde {

    public static class HistoricalLocationSerializer extends AbstractSTASerializer<HistoricalLocationWithQueryOptions> {

        public HistoricalLocationSerializer(String rootUrl) {
            super(HistoricalLocationWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = HistoricalLocationEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(HistoricalLocationWithQueryOptions value,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            HistoricalLocationEntity histLoc = value.getEntity();
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
                writeId(gen, histLoc.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, histLoc.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_TIME)) {
                gen.writeStringField(STAEntityDefinition.PROP_TIME, histLoc.getTime().toInstant().toString());
            }

            // navigation properties
            for (String navigationProperty : HistoricalLocationEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, histLoc.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }
    }

    public static class HistoricalLocationDeserializer extends JsonDeserializer<HistoricalLocationEntity> {

        @Override
        public HistoricalLocationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONHistoricalLocation.class).toEntity();
        }
    }
}
