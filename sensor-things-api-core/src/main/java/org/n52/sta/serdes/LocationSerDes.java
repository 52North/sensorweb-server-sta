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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONLocation;
import org.n52.sta.serdes.model.ElementWithQueryOptions.LocationWithQueryOptions;
import org.n52.sta.serdes.model.LocationEntityDefinition;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.utils.QueryOptions;

import java.io.IOException;
import java.util.Set;

public class LocationSerDes {

    public static class LocationEntityPatch extends LocationEntity implements EntityPatch<LocationEntity> {
        private final LocationEntity entity;

        public LocationEntityPatch (LocationEntity entity) {
            this.entity = entity;
        }

        public LocationEntity getEntity() {
            return entity;
        }
    }


    public static class LocationSerializer extends AbstractSTASerializer<LocationWithQueryOptions> {

        private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();

        public LocationSerializer(String rootUrl) {
            super(LocationWithQueryOptions.class);
            this.rootUrl = rootUrl;
            this.entitySetName = LocationEntityDefinition.entitySetName;
        }

        @Override
        public void serialize(LocationWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            LocationEntity location = value.getEntity();
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
                writeId(gen, location.getIdentifier());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, location.getIdentifier());
            }

            // actual properties
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, location.getName());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, location.getDescription());
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                // only write out encodingtype if there is a location present
                if (location.isSetGeometry()) {
                    gen.writeStringField(STAEntityDefinition.PROP_ENCODINGTYPE, ENCODINGTYPE_GEOJSON);
                }
            }
            if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_LOCATION)) {
                gen.writeFieldName(STAEntityDefinition.PROP_LOCATION);
                gen.writeRawValue(GEO_JSON_WRITER.write(location.getGeometryEntity().getGeometry()));
            }

            // navigation properties
            for (String navigationProperty : LocationEntityDefinition.navigationProperties) {
                if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                    writeNavigationProp(gen, navigationProperty, location.getIdentifier());
                }
            }
            //TODO: Deal with $expand
            gen.writeEndObject();
        }
    }

    public static class LocationDeserializer extends StdDeserializer<LocationEntity> {

        public LocationDeserializer() {
            super(LocationEntity.class);
        }

        @Override
        public LocationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONLocation.class).toEntity(JSONBase.EntityType.FULL);
        }
    }

    public static class LocationPatchDeserializer extends StdDeserializer<LocationEntityPatch> {

        public LocationPatchDeserializer() {
            super(LocationEntityPatch.class);
        }

        @Override
        public LocationEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new LocationEntityPatch(p.readValueAs(JSONLocation.class).toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
