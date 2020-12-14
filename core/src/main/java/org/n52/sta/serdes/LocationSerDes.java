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
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.sta.model.LocationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.serdes.json.JSONBase;
import org.n52.sta.serdes.json.JSONLocation;
import org.n52.sta.serdes.util.ElementWithQueryOptions.LocationWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class LocationSerDes {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationSerDes.class);


    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    public static class LocationEntityPatch extends LocationEntity implements EntityPatch<LocationEntity> {

        private static final long serialVersionUID = -8421752856535036959L;
        private final LocationEntity entity;

        LocationEntityPatch(LocationEntity entity) {
            this.entity = entity;
        }

        public LocationEntity getEntity() {
            return entity;
        }
    }


    public static class LocationSerializer extends AbstractSTASerializer<LocationWithQueryOptions, LocationEntity> {

        private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

        private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();
        private static final long serialVersionUID = 5481294508394633788L;

        public LocationSerializer(String rootUrl, boolean implicitExpand, String... activeExtensions) {
            super(LocationWithQueryOptions.class, implicitExpand, activeExtensions);
            this.rootUrl = rootUrl;
            this.entitySetName = LocationEntityDefinition.ENTITY_SET_NAME;
        }

        @Override
        public void serialize(LocationWithQueryOptions value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeStartObject();
            value.unwrap(implicitSelect);
            LocationEntity location = value.getEntity();

            // olingo @iot links
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
                writeId(gen, location.getStaIdentifier());
            }
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
                writeSelfLink(gen, location.getStaIdentifier());
            }

            // actual properties
            if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, location.getName());
            }
            if (!value.hasSelectOption()
                || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
                gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, location.getDescription());
            }
            if (!value.hasSelectOption()
                || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ENCODINGTYPE)) {
                // only write out encodingtype if there is a location present
                if (location.isSetGeometry()) {
                    gen.writeStringField(STAEntityDefinition.PROP_ENCODINGTYPE, ENCODINGTYPE_GEOJSON);
                }
            }
            if (!value.hasSelectOption()
                || value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_LOCATION)) {
                gen.writeFieldName(STAEntityDefinition.PROP_LOCATION);
                gen.writeRawValue(GEO_JSON_WRITER.write(location.getGeometryEntity().getGeometry()));
            }
            if (!value.hasSelectOption() ||
                value.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
                if (location.hasParameters()) {
                    gen.writeObjectFieldStart(STAEntityDefinition.PROP_PROPERTIES);
                    for (ParameterEntity<?> parameter : location.getParameters()) {
                        gen.writeObjectField(parameter.getName(), parameter.getValue());
                    }
                    gen.writeEndObject();
                }
            }

            // navigation properties
            for (String navigationProperty : LocationEntityDefinition.NAVIGATION_PROPERTIES) {
                if (!value.hasSelectOption() || value.getFieldsToSerialize().contains(navigationProperty)) {
                    if (!value.hasExpandOption() || value.getFieldsToExpand().get(navigationProperty) == null) {
                        writeNavigationProp(gen, navigationProperty, location.getStaIdentifier());
                    } else {
                        switch (navigationProperty) {
                            case LocationEntityDefinition.THINGS:
                                if (location.getThings() == null) {
                                    writeNavigationProp(gen, navigationProperty, location.getStaIdentifier());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(Collections.unmodifiableSet(location.getThings()),
                                                          value.getFieldsToExpand().get(navigationProperty),
                                                          gen,
                                                          serializers);
                                }
                                break;
                            case LocationEntityDefinition.HISTORICAL_LOCATIONS:
                                if (location.getHistoricalLocations() == null) {
                                    writeNavigationProp(gen, navigationProperty, location.getStaIdentifier());
                                } else {
                                    gen.writeFieldName(navigationProperty);
                                    writeNestedCollection(
                                        Collections.unmodifiableSet(location.getHistoricalLocations()),
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


    public static class LocationDeserializer extends StdDeserializer<LocationEntity> {

        private static final long serialVersionUID = -5978576540306282111L;

        public LocationDeserializer() {
            super(LocationEntity.class);
        }

        @Override
        public LocationEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(JSONLocation.class).toEntity(JSONBase.EntityType.FULL);
        }
    }


    public static class LocationPatchDeserializer extends StdDeserializer<LocationEntityPatch> {

        private static final long serialVersionUID = -4192059850856965261L;

        public LocationPatchDeserializer() {
            super(LocationEntityPatch.class);
        }

        @Override
        public LocationEntityPatch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new LocationEntityPatch(p.readValueAs(JSONLocation.class).toEntity(JSONBase.EntityType.PATCH));
        }
    }
}
