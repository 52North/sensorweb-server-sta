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
        QueryOptions options = createEmptyQueryOptions("http://localhost:8080/v1.1/Things");
        SerializationContext<Thing> context = createContext(options);
        JsonNode parsedNode = serializeEntity(context, new ThingAdapter("foo"));

        assertTrue(parsedNode.has("@iot.id"));
        assertThat(parsedNode.get("@iot.id").asText(), is("foo"));

        assertTrue(parsedNode.has("@iot.selfLink"));
        assertThat(parsedNode.get("@iot.selfLink").asText(), is("http://localhost:8080/v1.1/Things(foo)"));
    }

    @Test
    public void expectOnlySelectedPropertiesSerialized() throws Exception {
        SerializationContext<Thing> context = createContext("$select=name");
        Thing thing = new ThingAdapter("foo", "bar", "baz");
        JsonNode parsedNode = serializeEntity(context, thing);

        // id is always included
        assertTrue(parsedNode.has("@iot.id"));
        assertTrue(parsedNode.has("name"));
        assertFalse(parsedNode.has("description"));

        assertThat(parsedNode.get("@iot.id").asText(), is("foo"));
        assertThat(parsedNode.get("name").asText(), is("bar"));
    }

    @Test
    public void expectEmptyPropertiesObject() throws Exception {
        SerializationContext<Thing> context = createEmptyContext();
        JsonNode parsedNode = serializeEntity(context, new ThingAdapter("foo"));

        assertTrue(parsedNode.has("properties"));
        assertTrue(parsedNode.get("properties").isEmpty());
    }

    @Test
    public void expectPropertiesGetSerialized() throws Exception {
        SerializationContext<Thing> context = createEmptyContext();
        ThingAdapter thing = new ThingAdapter("foo");
        Map<String, Object> properties = new HashMap<>();
        properties.put("foo", "bar");
        properties.put("bar", 42);
        thing.setProperties(properties);
        JsonNode parsedNode = serializeEntity(context, thing);

        assertTrue(parsedNode.has("properties"));
        JsonNode serializedProperties = parsedNode.get("properties");
        assertThat(serializedProperties.get("foo").asText(), is("bar"));
        assertThat(serializedProperties.get("bar").asInt(), is(42));
    }

    @Test
    public void expectNavLinkOnUnexpandedDatastream() throws Exception {
        QueryOptions query = createQueryOptions("http://localhost:8080/v1.1/Things", "$expand=Datastreams");
        SerializationContext<Thing> context = createContext(query);
        Thing thing = new ThingNode(om.readTree(readFromFile("thingWithExpandedMembers.json")), om);
        JsonNode parsedNode = serializeEntity(context, thing);

        assertFalse(parsedNode.has("Datastreams@iot.navigationLink"));
        JsonNode datastreamsArray = parsedNode.get("Datastreams");
        assertTrue(datastreamsArray.isArray());
    }

    @Test
    public void expectExpandedDatastreamsWhenExpanded() throws Exception {
        QueryOptions options = createEmptyQueryOptions("http://localhost:8080/v1.1/Things");
        SerializationContext<Thing> context = createContext(options);
        Thing thing = new ThingNode(om.readTree(readFromFile("thingWithExpandedMembers.json")), om);
        JsonNode parsedNode = serializeEntity(context, thing);

        assertFalse(parsedNode.has("Datastreams"));
        assertTrue(parsedNode.has("Datastreams@iot.navigationLink"));
        JsonNode datastreamsLink = parsedNode.get("Datastreams@iot.navigationLink");
        assertThat(datastreamsLink.asText(),
                is("http://localhost:8080/v1.1/Things(06aac1af-d925-4a6a-9df3-aa40067a210e)/Datastreams"));
    }

    @Test
    public void expectIotCountOnExpandedDatastreamsWhenCountTrue() throws Exception {
        QueryOptions options = createQueryOptions("http://localhost:8080/v1.1/Things", "$count=true");
        SerializationContext<Thing> context = createContext(options);
        Thing thing = new ThingNode(om.readTree(readFromFile("thingWithExpandedMembers.json")), om);
        JsonNode parsedNode = serializeEntity(context, thing);

        assertFalse(parsedNode.has("Datastreams"));
        assertTrue(parsedNode.has("Datastreams@iot.navigationLink"));
        JsonNode datastreamsLink = parsedNode.get("Datastreams@iot.navigationLink");
        assertThat(datastreamsLink.asText(),
                is("http://localhost:8080/v1.1/Things(06aac1af-d925-4a6a-9df3-aa40067a210e)/Datastreams"));
    }

    private QueryOptions createEmptyQueryOptions(String baseUri) {
        return createQueryOptions(baseUri, "");
    }

    private QueryOptions createFromPlainQueryOptions(String options) {
        return createQueryOptions("", options);
    }

    private QueryOptions createQueryOptions(String baseUri, String options) {
        QueryOptions queryOptions = new QueryOptionsFactory().createQueryOptions(options);
        return new QueryOptions(baseUri, queryOptions.getAllFilters());
    }

    private SerializationContext<Thing> createEmptyContext() {
        return createContext("");
    }

    private SerializationContext<Thing> createContext(String query) {
        ObjectMapper mapper = new ObjectMapper();
        QueryOptions options = createFromPlainQueryOptions(query);
        StaSerializer<Thing> serializer = new ThingJsonSerializer(options);
        return new SerializationContext<>(options, mapper, serializer);
    }

    private SerializationContext<Thing> createContext(QueryOptions query) {
        ObjectMapper mapper = new ObjectMapper();
        StaSerializer<Thing> serializer = new ThingJsonSerializer(query);
        return new SerializationContext<>(query, mapper, serializer);
    }

    private InputStream readFromFile(String file) {
        return getClass().getClassLoader().getResourceAsStream(file);
    }

    private JsonNode serializeEntity(SerializationContext<Thing> context, Thing thing) throws Exception {
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
