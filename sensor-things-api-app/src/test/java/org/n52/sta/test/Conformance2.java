package org.n52.sta.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.n52.sta.test.TestUtil.compareJsonNodes;
import static org.n52.sta.test.TestUtil.compareJsonNodesTime;
import static org.n52.sta.test.TestUtil.getRelatedEntityEndpoint;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Profile("test")
public class Conformance2 {

    protected final static String jsonMimeType = "application/json";

    private static final Logger logger = LoggerFactory.getLogger(Conformance2.class);

    @Value("${server.rootUrl}")
    private String rootUrl;

    enum EntityType {
        THING,
        LOCATION,
        HISTORICAL_LOCATION,
        DATASTREAM,
        SENSOR,
        FEATURE_OF_INTEREST,
        OBSERVATION,
        OBSERVED_PROPERTY
    }

    private final HashMap<EntityType, String> endpoints;

    private ObjectMapper mapper = new ObjectMapper();

    public Conformance2() {
        HashMap<EntityType, String> map = new HashMap<>();
        map.put(EntityType.THING, "Things");
        map.put(EntityType.LOCATION, "Locations");
        map.put(EntityType.HISTORICAL_LOCATION, "HistoricalLocations");
        map.put(EntityType.DATASTREAM, "Datastreams");
        map.put(EntityType.SENSOR, "Sensors");
        map.put(EntityType.FEATURE_OF_INTEREST, "FeaturesOfInterest");
        map.put(EntityType.OBSERVATION, "Observations");
        map.put(EntityType.OBSERVED_PROPERTY, "ObservedProperties");
        endpoints = map;
    }

    private final String idKey = "@iot.id";

    /**
     * The list of ids for all the Things created during test procedure (will be
     * used for clean-up)
     */
    private Set<String> thingIds = new HashSet<>();
    /**
     * The list of ids for all the Locations created during test procedure (will
     * be used for clean-up)
     */
    private Set<String> locationIds = new HashSet<>();
    /**
     * The list of ids for all the HistoricalLocations created during test
     * procedure (will be used for clean-up)
     */
    private Set<String> historicalLocationIds = new HashSet<>();
    /**
     * The list of ids for all the Datastreams created during test procedure
     * (will be used for clean-up)
     */
    private Set<String> datastreamIds = new HashSet<>();
    /**
     * The list of ids for all the Observations created during test procedure
     * (will be used for clean-up)
     */
    private Set<String> observationIds = new HashSet<>();
    /**
     * The list of ids for all the Sensors created during test procedure (will
     * be used for clean-up)
     */
    private Set<String> sensorIds = new HashSet<>();
    /**
     * The list of ids for all the ObservedPropeties created during test
     * procedure (will be used for clean-up)
     */
    private Set<String> obsPropIds = new HashSet<>();
    /**
     * The list of ids for all the FeaturesOfInterest created during test
     * procedure (will be used for clean-up)
     */
    private Set<String> foiIds = new HashSet<>();

    @Test
    public void createEntities() throws IOException {
        /* Thing */
        String urlParameters = "{"
                + "\"name\":\"Test Thing\","
                + "\"description\":\"This is a Test Thing From TestNG\""
                + "}";
        JsonNode entity = postEntity(EntityType.THING, urlParameters);
        String thingId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);
        thingIds.add(thingId);

        /* Location */
        urlParameters = "{\n"
                + "  \"name\": \"bow river\",\n"
                + "  \"description\": \"bow river\",\n"
                + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                + "  \"location\": { \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }\n"
                + "}";
        entity = postEntity(EntityType.LOCATION, urlParameters);
        String locationId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);
        locationIds.add(locationId);
        JsonNode locationEntity = entity;

        /* Sensor */
        urlParameters = "{\n"
                + "  \"name\": \"Fuguro Barometer\",\n"
                + "  \"description\": \"Fuguro Barometer\",\n"
                + "  \"encodingType\": \"application/pdf\",\n"
                + "  \"metadata\": \"Barometer\"\n"
                + "}";
        entity = postEntity(EntityType.SENSOR, urlParameters);
        String sensorId = entity.get(idKey).asText();
        sensorIds.add(sensorId);

        /* ObservedProperty */
        urlParameters = "{\n"
                + "  \"name\": \"DewPoint Temperature\",\n"
                + "  \"definition\": \"http://dbpedia.org/page/Dew_point\",\n"
                + "  \"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form. As the grass and other objects near the ground cool to the dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\"\n"
                + "}";
        entity = postEntity(EntityType.OBSERVED_PROPERTY, urlParameters);
        String obsPropId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);
        obsPropIds.add(obsPropId);

        /* FeatureOfInterest */
        urlParameters = "{\n"
                + "  \"name\": \"A weather station.\",\n"
                + "  \"description\": \"A weather station.\",\n"
                + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                + "  \"feature\": {\n"
                + "    \"type\": \"Point\",\n"
                + "    \"coordinates\": [\n"
                + "      10,\n"
                + "      10\n"
                + "    ]\n"
                + "  }\n"
                + "}";
        entity = postEntity(EntityType.FEATURE_OF_INTEREST, urlParameters);
        String foiId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);
        foiIds.add(foiId);

        /* Datastream */
        urlParameters = "{\n"
                + "  \"unitOfMeasurement\": {\n"
                + "    \"name\": \"Celsius\",\n"
                + "    \"symbol\": \"degC\",\n"
                + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                + "  },\n"
                + "  \"name\": \"test datastream.\",\n"
                + "  \"description\": \"test datastream.\",\n"
                + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
                + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "},\n"
                + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
                + "}";
        entity = postEntity(EntityType.DATASTREAM, urlParameters);
        String datastreamId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);
        datastreamIds.add(datastreamId);

        /* Observation */
        urlParameters = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
                + "  \"result\": 8,\n"
                + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "},\n"
                + "  \"FeatureOfInterest\": {\"@iot.id\": " + escape(foiId) + "}  \n"
                + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        String obsId1 = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);
        observationIds.add(obsId1);

        //POST Observation without FOI (Automatic creation of FOI)
        //Add location to the Thing
        urlParameters = "{\"Locations\":[{\"@iot.id\":" + escape(locationId) + "}]}";
        patchEntity(EntityType.THING, urlParameters, thingId);

        urlParameters = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T00:00:00.000Z\",\n"
                + "  \"resultTime\": \"2015-03-01T01:00:00.000Z\",\n"
                + "  \"result\": 100,\n"
                + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
                + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        compareJsonNodesTime(
                "resultTime",
                mapper.readTree("{\"resultTime\":\"2015-03-01T01:00:00.000Z\"}").get("resultTime"),
                entity.get("resultTime")
        );
        String obsId2 = entity.get(idKey).asText();
        observationIds.add(obsId2);
        String automatedFOIId = checkAutomaticInsertionOfFOI(obsId2, locationEntity, null);
        foiIds.add(automatedFOIId);

        //POST another Observation to make sure it is linked to the previously created FOI
        urlParameters = "{\n"
                + "  \"phenomenonTime\": \"2015-05-01T00:00:00.000Z\",\n"
                + "  \"result\": 105,\n"
                + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
                + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        compareJsonNodesTime(
                "resultTime",
                mapper.readTree("{\"resultTime\":\"null\"}").get("resultTime"),
                entity.get("resultTime")
        );
        String obsId3 = entity.get(idKey).asText();
        observationIds.add(obsId3);
        checkAutomaticInsertionOfFOI(obsId2, locationEntity, automatedFOIId);

        // Move the Thing to a new location, create a new observation
        // without FOI, check if a new FOI is created from this new location.
        /* Second Location */
        urlParameters = "{\n"
                + "  \"name\": \"spear river\",\n"
                + "  \"description\": \"spear river\",\n"
                + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                + "  \"location\": { \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }\n"
                + "}";
        entity = postEntity(EntityType.LOCATION, urlParameters);
        String location2Id = entity.get(idKey).asText();
        locationIds.add(location2Id);
        JsonNode location2Entity = entity;

        //Add second location to the Thing
        urlParameters = "{\"Locations\":[{\"@iot.id\":" + escape(location2Id) + "}]}";
        patchEntity(EntityType.THING, urlParameters, thingId);

        // Create a new Observation for Thing1 with no FoI.
        urlParameters = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T01:00:00.000Z\",\n"
                + "  \"resultTime\": \"2015-03-01T02:00:00.000Z\",\n"
                + "  \"result\": 200,\n"
                + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
                + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        String obsId4 = entity.get(idKey).asText();
        observationIds.add(obsId4);
        String automatedFOI2Id = checkAutomaticInsertionOfFOI(obsId4, location2Entity, null);
        foiIds.add(automatedFOI2Id);
        Assert.assertNotEquals(
                "A new FoI should have been created, since the Thing moved.",
                automatedFOIId,
                automatedFOI2Id
        );

        // Create a new Thing with the same Location, create a new
        // observation without FOI, check if the same FOI is used.
        /* Thing2 */
        urlParameters = "{"
                + "\"name\":\"Test Thing 2\","
                + "\"description\":\"This is a second Test Thing From TestNG\","
                + "\"Locations\":[{\"@iot.id\": " + escape(locationId) + "}]"
                + "}";
        entity = postEntity(EntityType.THING, urlParameters);
        String thing2Id = entity.get(idKey).asText();
        thingIds.add(thing2Id);

        /* Datastream2 */
        urlParameters = "{\n"
                + "  \"unitOfMeasurement\": {\n"
                + "    \"name\": \"Celsius\",\n"
                + "    \"symbol\": \"degC\",\n"
                + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                + "  },\n"
                + "  \"name\": \"test datastream 2.\",\n"
                + "  \"description\": \"test datastream 2.\",\n"
                + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "  \"Thing\": { \"@iot.id\": " + escape(thing2Id) + " },\n"
                + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "},\n"
                + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
                + "}";
        entity = postEntity(EntityType.DATASTREAM, urlParameters);
        String datastream2Id = entity.get(idKey).asText();
        datastreamIds.add(datastream2Id);

        /* Post new Observation without FoI */
        urlParameters = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T03:00:00.000Z\",\n"
                + "  \"resultTime\": \"2015-03-01T04:00:00.000Z\",\n"
                + "  \"result\": 300,\n"
                + "  \"Datastream\":{\"@iot.id\": " + escape(datastream2Id) + "}\n"
                + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        String obsId5 = entity.get(idKey).asText();
        observationIds.add(obsId5);
        String automatedFOI3Id = checkAutomaticInsertionOfFOI(obsId5, locationEntity, null);
        Assert.assertEquals(
                "The generated FoI should be the same as the first generated FoI, since Thing2 has the same Location.",
                automatedFOIId,
                automatedFOI3Id
        );

        /* HistoricalLocation */
        urlParameters = "{\n"
                + "  \"time\": \"2015-03-01T00:40:00.000Z\",\n"
                + "  \"Thing\":{\"@iot.id\": " + escape(thingId) + "},\n"
                + "  \"Locations\": [{\"@iot.id\": " + escape(locationId) + "}]  \n"
                + "}";
        entity = postEntity(EntityType.HISTORICAL_LOCATION, urlParameters);
        String histLocId = entity.get(idKey).asText();
        historicalLocationIds.add(histLocId);
    }

    /**
     * This method is testing create or POST in the form of Deep Insert. It
     * makes sure the response is 201. Also using GET requests, it makes sure
     * the entity and all its related entities are created and added to the
     * service.
     */
    @Test
    public void createEntitiesWithDeepInsert() throws IOException {
        /* Thing */
        String urlParameters = "{\n"
                + "  \"name\": \"Office Building\",\n"
                + "  \"description\": \"Office Building\",\n"
                + "  \"properties\": {\n"
                + "    \"reference\": \"Third Floor\"\n"
                + "  },\n"
                + "  \"Locations\": [\n"
                + "    {\n"
                + "      \"name\": \"West Roof\",\n"
                + "      \"description\": \"West Roof\",\n"
                + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
                + "      \"encodingType\": \"application/vnd.geo+json\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"Datastreams\": [\n"
                + "    {\n"
                + "      \"unitOfMeasurement\": {\n"
                + "        \"name\": \"Lumen\",\n"
                + "        \"symbol\": \"lm\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
                + "      },\n"
                + "      \"name\": \"Light exposure.\",\n"
                + "      \"description\": \"Light exposure.\",\n"
                + "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "      \"ObservedProperty\": {\n"
                + "        \"name\": \"Luminous Flux\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                + "      },\n"
                + "      \"Sensor\": {        \n"
                + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                + "        \"encodingType\": \"application/pdf\",\n"
                + "        \"metadata\": \"Light flux sensor\"\n"
                + "      }\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        JsonNode entity = postEntity(EntityType.THING, urlParameters);
        String thingId = entity.get(idKey).asText();
        //Check Datastream
        JsonNode deepInsertedObj = mapper.readTree("{\n"
                + "      \"unitOfMeasurement\": {\n"
                + "        \"name\": \"Lumen\",\n"
                + "        \"symbol\": \"lm\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
                + "      },\n"
                + "      \"name\": \"Light exposure.\",\n"
                + "      \"description\": \"Light exposure.\",\n"
                + "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\"\n"
                + "    }\n");
        String datastreamId = checkRelatedEntity(EntityType.THING, thingId, EntityType.DATASTREAM, deepInsertedObj);
        datastreamIds.add(datastreamId);
        //Check Location
        deepInsertedObj = mapper.readTree("{\n"
                + "      \"name\": \"West Roof\",\n"
                + "      \"description\": \"West Roof\",\n"
                + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
                + "      \"encodingType\": \"application/vnd.geo+json\"\n"
                + "    }\n");
        locationIds.add(checkRelatedEntity(EntityType.THING, thingId, EntityType.LOCATION, deepInsertedObj));
        //Check Sensor
        deepInsertedObj = mapper.readTree("{\n"
                + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                + "        \"encodingType\": \"application/pdf\",\n"
                + "        \"metadata\": \"Light flux sensor\"\n"
                + "      }\n");
        sensorIds.add(checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.SENSOR, deepInsertedObj));
        //Check ObservedProperty
        deepInsertedObj = mapper.readTree("{\n"
                + "        \"name\": \"Luminous Flux\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                + "      },\n");
        obsPropIds.add(checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.OBSERVED_PROPERTY, deepInsertedObj));
        thingIds.add(thingId);

        /* Datastream */
        urlParameters = "{\n"
                + "  \"unitOfMeasurement\": {\n"
                + "    \"name\": \"Celsius\",\n"
                + "    \"symbol\": \"degC\",\n"
                + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                + "  },\n"
                + "  \"name\": \"test datastream.\",\n"
                + "  \"description\": \"test datastream.\",\n"
                + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
                + "   \"ObservedProperty\": {\n"
                + "        \"name\": \"More Luminous Flux\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFluxWithMorePower\",\n"
                + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light. This has even more power than regular flux.\"\n"
                + "   },\n"
                + "   \"Sensor\": {        \n"
                + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                + "        \"encodingType\": \"application/pdf\",\n"
                + "        \"metadata\": \"Light flux sensor\"\n"
                + "   },\n"
                + "      \"Observations\": [\n"
                + "        {\n"
                + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
                + "          \"result\": 10\n"
                + "        }\n"
                + "      ]"
                + "}";
        entity = postEntity(EntityType.DATASTREAM, urlParameters);
        datastreamId = entity.get(idKey).asText();
        //Check Sensor
        deepInsertedObj = mapper.readTree("{\n"
                + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                + "        \"encodingType\": \"application/pdf\",\n"
                + "        \"metadata\": \"Light flux sensor\"\n"
                + "      }\n");
        sensorIds.add(checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.SENSOR, deepInsertedObj));
        //Check ObservedProperty
        deepInsertedObj = mapper.readTree("{\n"
                + "\"name\": \"More Luminous Flux\",\n"
                + "\"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFluxWithMorePower\",\n"
                + "\"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light. This has even more power than regular flux.\"\n"
                + "}\n");
        obsPropIds.add(checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.OBSERVED_PROPERTY, deepInsertedObj));
        //Check Observation
        deepInsertedObj = mapper.readTree("{\n"
                + "          \"phenomenonTime\": \"2015-03-01T00:10:00.000Z\",\n"
                + "          \"result\": 10\n"
                + "        }\n");
        observationIds.add(checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.OBSERVATION, deepInsertedObj));
        datastreamIds.add(datastreamId);

        /* Observation */
        urlParameters = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
                + "  \"result\": 100,\n"
                + "  \"FeatureOfInterest\": {\n"
                + "  \t\"name\": \"A weather station.\",\n"
                + "  \t\"description\": \"A weather station.\",\n"
                + "  \t\"encodingType\": \"application/vnd.geo+json\",\n"
                + "    \"feature\": {\n"
                + "      \"type\": \"Point\",\n"
                + "      \"coordinates\": [\n"
                + "        -114.05,\n"
                + "        51.05\n"
                + "      ]\n"
                + "    }\n"
                + "  },\n"
                + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
                + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        String obsId1 = entity.get(idKey).asText();
        //Check FeaturOfInterest
        deepInsertedObj = mapper.readTree("{\n"
                + "  \"name\": \"A weather station.\",\n"
                + "  \"description\": \"A weather station.\",\n"
                + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                + "    \"feature\": {\n"
                + "      \"type\": \"Point\",\n"
                + "      \"coordinates\": [\n"
                + "        -114.05,\n"
                + "        51.05\n"
                + "      ]\n"
                + "    }\n"
                + "  }\n");
        foiIds.add(checkRelatedEntity(EntityType.OBSERVATION, obsId1, EntityType.FEATURE_OF_INTEREST, deepInsertedObj));
        observationIds.add(obsId1);
    }

    private String escape(String val) {
        return "\"" + val + "\"";
    }

    private JsonNode postEntity(EntityType type, String body) throws IOException {
        HttpPost request = new HttpPost(rootUrl + endpoints.get(type));
        request.setEntity(new StringEntity(body));
        request.setHeader("Content-Type", "application/json");

        if (logger.isTraceEnabled()) {
            System.out.printf("POSTed to URL: %s\n", request.getURI());
            System.out.println(body);
        }

        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assert.assertEquals(
                "Response has invalid MIME Type",
                jsonMimeType,
                mimeType);

        return mapper.readTree(response.getEntity().getContent());
    }

    private JsonNode patchEntity(EntityType type, String body, String id) throws IOException {
        HttpPatch request = new HttpPatch(rootUrl
                + endpoints.get(type)
                + "("
                + id
                + ")");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(body));

        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        assertEquals(jsonMimeType, mimeType);
        return mapper.readTree(response.getEntity().getContent());
    }

    private JsonNode getEntity(String path) throws IOException {
        HttpGet request = new HttpGet(rootUrl + path);
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        assertEquals(jsonMimeType, mimeType);

        Assert.assertTrue(
                "ERROR: Did not receive 200 OK for path: " + path
                        + " Instead received Status Code: " + response.getStatusLine().getStatusCode(),
                response.getStatusLine().getStatusCode() == 200);
        return mapper.readTree(response.getEntity().getContent());
    }

    /**
     * Check the FeatureOfInterest is created automatically correctly if not
     * inserted in Observation
     *
     * @param obsId         The observation id
     * @param locationObj   The Location object that the FOI is supposed to be
     *                      created based on that
     * @param expectedFOIId The id of the FOI linked to the Observation
     * @return The id of FOI
     */
    private String checkAutomaticInsertionOfFOI(String obsId, JsonNode locationObj, String expectedFOIId) throws IOException {
        String urlString = "Observations(" + obsId + ")/FeatureOfInterest";
        JsonNode result = getEntity(urlString);
        String id = result.get(idKey).asText();
        if (expectedFOIId != null) {
            assertEquals(
                    "ERROR: the Observation should have linked to FeatureOfInterest with ID: "
                            + expectedFOIId
                            + " , but it is linked for FeatureOfInterest with Id: "
                            + id
                            + ".",
                    id,
                    expectedFOIId
            );
        }
        assertEquals(
                "ERROR: Automatic created FeatureOfInterest does not match last Location of that Thing.",
                result.get("feature").toString(),
                locationObj.get("location").toString()
        );
        return id;
    }

    /**
     * Check the related entity of a given entity
     *
     * @param sourceType The given entity type
     * @param sourceId   The given entity id
     * @param targetType The relation entity type
     * @param reference  The expected related entity object
     * @return The id of related object
     */
    private String checkRelatedEntity(EntityType sourceType,
                                      String sourceId,
                                      EntityType targetType,
                                      JsonNode reference) throws IOException {

        String url = String.format(getRelatedEntityEndpoint(sourceType, targetType), sourceId);
        JsonNode result = getEntity(url);
        if (result.isArray()) {
            result = result.get(0);
        }

        if (logger.isTraceEnabled()) {
            System.out.println("checkRelatedEntity:");
            System.out.println("Actual:");
            System.out.println(result.toPrettyString());

            System.out.println("Reference:");
            System.out.println(reference.toPrettyString());
        }

        compareJsonNodes(reference, result);
        return result.get(idKey).asText();
    }


}
