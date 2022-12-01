/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.http.serialize.out;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.http.serialize.in.ThingNode;
import org.n52.svalbard.odata.core.QueryOptionsFactory;

public class ThingJsonSerializerTest {

    private ObjectMapper om;

    @BeforeEach
    void setUp() {
        this.om = new ObjectMapper();
    }

    @Test
    public void expectAvailabilityOfIotProperties() throws Exception {
        SerializationContext context = createEmptyContext();
        JsonNode parsedNode = serializeEntity(context, new ThingAdapter("foo"));

        assertTrue(parsedNode.has("@iot.id"));
        assertThat(parsedNode.get("@iot.id")
                             .asText(),
                   is("foo"));

        assertTrue(parsedNode.has("@iot.selfLink"));
        assertThat(parsedNode.get("@iot.selfLink")
                             .asText(),
                   is("http://localhost/v1.1/Things(foo)"));
    }

    @Test
    public void expectOnlySelectedPropertiesSerialized() throws Exception {
        SerializationContext context = createContext("$select=name");
        Thing thing = new ThingAdapter("foo", "bar", "baz");
        JsonNode parsedNode = serializeEntity(context, thing);

        assertTrue(parsedNode.has("name"));
        assertFalse(parsedNode.has("description"));

        assertThat(parsedNode.get("name")
                             .asText(),
                   is("bar"));
    }

    @Test
    public void expectEmptyPropertiesObject() throws Exception {
        SerializationContext context = createEmptyContext();
        JsonNode parsedNode = serializeEntity(context, new ThingAdapter("foo"));

        assertTrue(parsedNode.has("properties"));
        assertTrue(parsedNode.get("properties")
                             .isEmpty());
    }

    @Test
    public void expectPropertiesGetSerialized() throws Exception {
        SerializationContext context = createEmptyContext();
        ThingAdapter thing = new ThingAdapter("foo");
        Map<String, Object> properties = new HashMap<>();
        properties.put("foo", "bar");
        properties.put("bar", 42);
        thing.setProperties(properties);
        JsonNode parsedNode = serializeEntity(context, thing);

        assertTrue(parsedNode.has("properties"));
        JsonNode serializedProperties = parsedNode.get("properties");
        assertThat(serializedProperties.get("foo")
                                       .asText(),
                   is("bar"));
        assertThat(serializedProperties.get("bar")
                                       .asInt(),
                   is(42));
    }

    @Test
    public void expectNavLinkOnUnexpandedDatastream() throws Exception {
        SerializationContext context = createContext("$expand=Datastreams");
        Thing thing = new ThingNode(om.readTree(readFromFile("thingWithExpandedMembers.json")), om);
        JsonNode parsedNode = serializeEntity(context, thing);

        assertFalse(parsedNode.has("Datastreams@iot.navigationLink"));
        JsonNode datastreamsArray = parsedNode.get("Datastreams");
        assertTrue(datastreamsArray.isArray());
    }

    @Test
    public void expectExpandedDatastreamsWhenExpanded() throws Exception {
        SerializationContext context = createEmptyContext();
        Thing thing = new ThingNode(om.readTree(readFromFile("thingWithExpandedMembers.json")), om);
        JsonNode parsedNode = serializeEntity(context, thing);

        assertFalse(parsedNode.has("Datastreams"));
        assertTrue(parsedNode.has("Datastreams@iot.navigationLink"));
        JsonNode datastreamsLink = parsedNode.get("Datastreams@iot.navigationLink");
        assertThat(datastreamsLink.asText(),
                   is("http://localhost/v1.1/Things(06aac1af-d925-4a6a-9df3-aa40067a210e)/Datastreams"));
    }

    @Test
    public void expectIotCountOnExpandedDatastreamsWhenCountTrue() throws Exception {
        SerializationContext context = createEmptyContext();
        Thing thing = new ThingNode(om.readTree(readFromFile("thingWithExpandedMembers.json")), om);
        JsonNode parsedNode = serializeEntity(context, thing);

        assertFalse(parsedNode.has("Datastreams"));
        assertTrue(parsedNode.has("Datastreams@iot.navigationLink"));
        JsonNode datastreamsLink = parsedNode.get("Datastreams@iot.navigationLink");
        assertThat(datastreamsLink.asText(),
                   is("http://localhost/v1.1/Things(06aac1af-d925-4a6a-9df3-aa40067a210e)/Datastreams"));
    }

    private SerializationContext createEmptyContext() {
        return createContext("");
    }

    private SerializationContext createContext(String query) {
        QueryOptions options = createQueryOptions(query);
        return createContext(options);
    }

    private QueryOptions createQueryOptions(String options) {
        QueryOptions queryOptions = QueryOptionsFactory.createQueryOptions(options);
        return QueryOptionsFactory.createQueryOptions(queryOptions.getAllFilters());
    }

    private SerializationContext createContext(QueryOptions query) {
        ObjectMapper mapper = new ObjectMapper();
        SerializationContext context = new SerializationContext("http://localhost/v1.1", query, mapper);
        StaBaseSerializer<Thing> serializer = new ThingJsonSerializer(context);
        context.register(serializer);
        return context;
    }

    private InputStream readFromFile(String file) {
        return getClass().getClassLoader()
                         .getResourceAsStream(file);
    }

    private JsonNode serializeEntity(SerializationContext context, Thing thing) throws Exception {
        ObjectWriter writer = context.createWriter();
        return om.readTree(writer.writeValueAsString(thing));
    }

    private static class ThingAdapter implements Thing {

        private String id;
        private String name;
        private String description;
        private Map<String, Object> properties;
        private HashSet<Datastream> datastreams;

        private ThingAdapter(String id) {
            this(id, null, null);
        }

        private ThingAdapter(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.datastreams = new HashSet<>();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public Set<HistoricalLocation> getHistoricalLocations() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Set<Location> getLocations() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Set<Datastream> getDatastreams() {
            return this.datastreams;
        }

    }
}
