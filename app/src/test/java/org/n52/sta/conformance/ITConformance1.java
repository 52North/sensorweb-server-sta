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

package org.n52.sta.conformance;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Implements Conformance Tests according to Section A.1 in OGC SensorThings API Part 1: Sensing (15-078r6)
 * Adapted from the official Test Suite <a href="https://github.com/opengeospatial/ets-sta10/">ets-sta10</a>
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @see <a href="http://docs.opengeospatial.org/is/15-078r6/15-078r6.html#54"> OGC SensorThings API Part 1:
 *      Sensing (15-078r6)</a>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ITConformance1 extends ConformanceTests implements TestUtil {

    public ITConformance1(@Value("${server.config.service-root-url}") String rootUrl) throws Exception {
        super(rootUrl);
    }

    /**
     * This method is testing GET entities. It should return 200. Then the response entities are tested for
     * control information, mandatory properties, and mandatory related entities.
     */
    @Test
    public void testReadEntitiesAndCheckResponse() throws Exception {
        JsonNode collection;
        collection = getCollection(EntityType.THING);
        checkEntitiesAllAspectsForResponse(new ThingEntityDefinition(), collection);
        collection = getCollection(EntityType.LOCATION);
        checkEntitiesAllAspectsForResponse(new LocationEntityDefinition(), collection);
        collection = getCollection(EntityType.HISTORICAL_LOCATION);
        checkEntitiesAllAspectsForResponse(new HistoricalLocationEntityDefinition(), collection);
        collection = getCollection(EntityType.DATASTREAM);
        checkEntitiesAllAspectsForResponse(new DatastreamEntityDefinition(), collection);
        collection = getCollection(EntityType.SENSOR);
        checkEntitiesAllAspectsForResponse(new SensorEntityDefinition(), collection);
        collection = getCollection(EntityType.OBSERVATION);
        checkEntitiesAllAspectsForResponse(new ObservationEntityDefinition(), collection);
        collection = getCollection(EntityType.OBSERVED_PROPERTY);
        checkEntitiesAllAspectsForResponse(new ObservedPropertyEntityDefinition(), collection);
        collection = getCollection(EntityType.FEATURE_OF_INTEREST);
        checkEntitiesAllAspectsForResponse(new FeatureOfInterestEntityDefinition(), collection);
    }

    /**
     * This method is testing GET when requesting a nonexistent entity. The response should be 404.
     */
    @Test
    public void readNonexistentEntity() throws Exception {
        getNonExistentEntity(EntityType.THING);
        getNonExistentEntity(EntityType.LOCATION);
        getNonExistentEntity(EntityType.HISTORICAL_LOCATION);
        getNonExistentEntity(EntityType.DATASTREAM);
        getNonExistentEntity(EntityType.SENSOR);
        getNonExistentEntity(EntityType.OBSERVATION);
        getNonExistentEntity(EntityType.OBSERVED_PROPERTY);
        getNonExistentEntity(EntityType.FEATURE_OF_INTEREST);
    }

    /**
     * This method is testing GET for a specific entity with its id. It checks the control information,
     * mandatory properties and mandatory related entities for the response entity.
     */
    @Test
    public void readEntityAndCheckResponse() throws Exception {
        Set<JsonNode> response;
        response = readEntityWithEntityType(EntityType.THING);
        checkEntityAllAspectsForResponse(new ThingEntityDefinition(), response);
        response = readEntityWithEntityType(EntityType.LOCATION);
        checkEntityAllAspectsForResponse(new LocationEntityDefinition(), response);
        response = readEntityWithEntityType(EntityType.HISTORICAL_LOCATION);
        checkEntityAllAspectsForResponse(new HistoricalLocationEntityDefinition(), response);
        response = readEntityWithEntityType(EntityType.DATASTREAM);
        checkEntityAllAspectsForResponse(new DatastreamEntityDefinition(), response);
        response = readEntityWithEntityType(EntityType.SENSOR);
        checkEntityAllAspectsForResponse(new SensorEntityDefinition(), response);
        response = readEntityWithEntityType(EntityType.OBSERVATION);
        checkEntityAllAspectsForResponse(new ObservationEntityDefinition(), response);
        response = readEntityWithEntityType(EntityType.OBSERVED_PROPERTY);
        checkEntityAllAspectsForResponse(new ObservedPropertyEntityDefinition(), response);
        response = readEntityWithEntityType(EntityType.FEATURE_OF_INTEREST);
        checkEntityAllAspectsForResponse(new FeatureOfInterestEntityDefinition(), response);
    }

    /**
     * This method is testing GET for a property of an entity.
     */
    @Test
    public void readPropertyOfEntityAndCheckResponse() throws Exception {
        readPropertyOfEntityWithEntityType(EntityType.THING, new ThingEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.LOCATION, new LocationEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.HISTORICAL_LOCATION, new HistoricalLocationEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.DATASTREAM, new DatastreamEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.OBSERVED_PROPERTY, new ObservedPropertyEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.SENSOR, new SensorEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.OBSERVATION, new ObservationEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.FEATURE_OF_INTEREST, new FeatureOfInterestEntityDefinition());
    }

    /**
     * This method is testing the resource paths based on specification to the specified level.
     */
    @Test
    public void checkResourcePaths() throws Exception {
        Set<JsonNode> response;
        response = readEntityWithEntityType(EntityType.THING);
        checkRelatedEndpoints(EntityType.THING,
                              ((JsonNode) response.toArray()[0]).get(idKey)
                                                                .asText());
        response = readEntityWithEntityType(EntityType.LOCATION);
        checkRelatedEndpoints(EntityType.LOCATION,
                              ((JsonNode) response.toArray()[0]).get(idKey)
                                                                .asText());
        response = readEntityWithEntityType(EntityType.HISTORICAL_LOCATION);
        checkRelatedEndpoints(EntityType.HISTORICAL_LOCATION,
                              ((JsonNode) response.toArray()[0]).get(idKey)
                                                                .asText());
        response = readEntityWithEntityType(EntityType.DATASTREAM);
        checkRelatedEndpoints(EntityType.DATASTREAM,
                              ((JsonNode) response.toArray()[0]).get(idKey)
                                                                .asText());
        response = readEntityWithEntityType(EntityType.SENSOR);
        checkRelatedEndpoints(EntityType.SENSOR,
                              ((JsonNode) response.toArray()[0]).get(idKey)
                                                                .asText());
        response = readEntityWithEntityType(EntityType.OBSERVATION);
        checkRelatedEndpoints(EntityType.OBSERVATION,
                              ((JsonNode) response.toArray()[0]).get(idKey)
                                                                .asText());
        response = readEntityWithEntityType(EntityType.OBSERVED_PROPERTY);
        checkRelatedEndpoints(EntityType.OBSERVED_PROPERTY,
                              ((JsonNode) response.toArray()[0]).get(idKey)
                                                                .asText());
        response = readEntityWithEntityType(EntityType.FEATURE_OF_INTEREST);
        checkRelatedEndpoints(EntityType.FEATURE_OF_INTEREST,
                              ((JsonNode) response.toArray()[0]).get(idKey)
                                                                .asText());
    }

    /**
     * This method is testing the root URL of the service under test.
     */
    @Test()
    public void checkServiceRootUri() throws Exception {
        JsonNode response = getRootResponse();
        ArrayNode entities = (ArrayNode) response.get(value);

        final String ERROR = "The URL for %s in Service Root URI is not compliant to " + "SensorThings API.";
        Map<String, Boolean> addedLinks = new HashMap<>();
        for (EntityType entityType : EntityType.values()) {
            addedLinks.put(entityType.val, false);
        }
        for (int i = 0; i < entities.size(); i++) {
            JsonNode entity = entities.get(i);
            Assertions.assertNotNull(entity.get("name"));
            Assertions.assertNotNull(entity.get("url"));
            String name = entity.get("name")
                                .asText();
            String nameUrl = entity.get("url")
                                   .asText();

            if (EntityType.getByVal(name) == null) {
                Assertions.fail(
                                "There is a component in Service Root URI response that is not in SensorThings API : "
                                        + name);
            } else {
                Assertions.assertEquals(rootUrl + name,
                                        nameUrl,
                                        String.format(
                                                      "The URL for %s in Service Root URI is not compliant to SensorThings "
                                                              +
                                                              "API.",
                                                      rootUrl + name));
                addedLinks.put(name, true);
            }
        }
        for (String key : addedLinks.keySet()) {
            Assertions.assertTrue(addedLinks.get(key), "The Service Root URI response does not contain " + key);
        }
    }

    /**
     * This helper method is testing property and property/$value for single entity of a given entity type
     *
     * @param entityType
     *        Entity type from EntityType enum list
     */
    void readPropertyOfEntityWithEntityType(EntityType entityType, STAEntityDefinition definition)
            throws Exception {
        JsonNode collection = getCollection(entityType);
        Assertions.assertNotNull(collection.get(value),
                                 "Could not get collection for EntityType: " + entityType.name());
        for (JsonNode entity : collection.get(value)) {
            Assertions.assertNotNull(entity.get("@iot.id"),
                                     "Could not read @iot.id from entity:" + entity.toPrettyString());
            for (String mandatoryProp : this.getEntityPropsMandatory(definition)) {
                checkGetPropertyOfEntity(entityType,
                                         entity.get("@iot.id")
                                               .asText(),
                                         mandatoryProp);
                checkGetPropertyValueOfEntity(entityType,
                                              entity.get("@iot.id")
                                                    .asText(),
                                              mandatoryProp);
            }
        }
    }

    /**
     * This helper method sending GET request for requesting a property and check the response is 200.
     *
     * @param entityType
     *        Entity type from EntityType enum list
     * @param id
     *        The id of the entity
     * @param property
     *        The property to get requested
     */
    private void checkGetPropertyOfEntity(EntityType entityType, String id, String property) throws Exception {
        JsonNode entity = getEntityProperty(entityType, id, property);
        Assertions.assertNotNull(entity.get(property),
                                 "Reading property \"" + property + "\"of \"" + entityType + "\" fails.");
        Assertions.assertEquals(1,
                                entity.size(),
                                "The response for getting property "
                                        + property
                                        + " of Entity "
                                        + entityType
                                        +
                                        " returns more properties!");
    }

    /**
     * This helper method sending GET request for requesting a property $value and check the response is 200.
     *
     * @param entityType
     *        Entity type from EntityType enum list
     * @param id
     *        The id of the entity
     * @param property
     *        The property to get requested
     */
    private void checkGetPropertyValueOfEntity(EntityType entityType, String id, String property) throws Exception {
        String response = getEntityValue(entityType, id, property);
        Assertions.assertNotNull(response,
                                 "Reading property \"" + property + "\"of \"" + entityType + "\" fails.");
    }

    /**
     * Checks that all related Endpoints return HTTP 200 OK
     *
     * @param type
     *        Type of Source Entity
     * @param id
     *        Id of Source Entity
     * @throws IOException
     *         if an error occurred
     */
    void checkRelatedEndpoints(EntityType type, String id) throws IOException {
        Set<String> relatedEntityEndpointKeys = getRelatedEntityEndpointKeys(type);
        for (String relatedEntityEndpointKey : relatedEntityEndpointKeys) {
            getEntity(String.format(relatedEntityEndpointKey, id));
        }
    }

    /**
     * This method is checking the response for the request of Association Link. It confirms that it contains
     * a list of selfLinks.
     *
     * @param response
     *        The response for GET association link request
     * @param entityTypes
     *        List of entity type from EntityType enum list for the chain
     * @param ids
     *        List of ids for teh chain
     */
    private void checkAssociationLinks(String response, List<String> entityTypes, List<Long> ids) {

        // try {
        // Assert.assertTrue(response.indexOf(value) != -1,
        // "The GET entities Association Link response does not match SensorThings API :
        // missing " +
        // "\"value\" in response.: " +
        // entityTypes.toString() + ids.toString());
        // JSONArray value = new JSONObject(response.toString()).getJSONArray(value);
        // int count = 0;
        // for (int i = 0; i < value.length() && count < 2; i++) {
        // count++;
        // JSONObject obj = value.getJSONObject(i);
        // try {
        // Assert.assertNotNull(obj.get(ControlInformation.SELF_LINK),
        // "The Association Link does not contain self-links.: " +
        // entityTypes.toString() + ids.toString());
        // } catch (JSONException e) {
        // Assert.fail("The Association Link does not contain self-links.: " + entityTypes
        // .toString() +
        // ids.toString());
        // }
        // Assert.assertEquals(obj.length(), 1,
        // "The Association Link contains properties other than self-link.: " +
        // entityTypes.toString() + ids.toString());
        // }
        // } catch (JSONException e) {
        // e.printStackTrace();
        // Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        // }
    }

    /**
     * This method is reading a specific entity and return it as a string.
     *
     * @param entityType
     *        Entity type from EntityType enum list
     * @return The entity response as a string
     */
    Set<JsonNode> readEntityWithEntityType(EntityType entityType) throws Exception {
        HashSet<JsonNode> responses = new HashSet<>();
        JsonNode collection = getCollection(entityType);
        Assertions.assertNotNull(collection.get(value),
                                 "Could not get collection for EntityType: " + entityType.name());
        for (JsonNode entity : collection.get(value)) {
            Assertions.assertNotNull(entity.get("@iot.id"),
                                     "Could not read @iot.id from entity:" + entity.toPrettyString());
            responses.add(getEntity(entityType,
                                    entity.get("@iot.id")
                                          .asText()));
        }
        return responses;
    }

    /**
     * This helper method is the start point for checking the response for a collection in all aspects.
     *
     * @param entityType
     *        Entity type from EntityType enum list
     * @param entity
     *        The response of the GET request to be checked
     */
    void checkEntitiesAllAspectsForResponse(STAEntityDefinition entityType, JsonNode entity) {
        checkEntitiesControlInformation(entity);
        checkEntitiesProperties(this.getEntityPropsMandatory(entityType), entity);
        checkEntitiesProperties(annotateNavigationProperties(entityType.getNavPropsMandatory()), entity);
    }

    private Set<String> getEntityPropsMandatory(STAEntityDefinition entityType) {
        Set<String> entityPropsMandatoryByDefinition = entityType.getEntityPropsMandatory();
        Set<String> entityPropsMandatoryNoId = new HashSet<>();
        for (String s : entityPropsMandatoryByDefinition) {
            if (!s.equals(STAEntityDefinition.PROP_ID)) {
                entityPropsMandatoryNoId.add(s);
            }
        }
        return entityPropsMandatoryNoId;
    }

    private Set<String> annotateNavigationProperties(Set<String> properties) {
        HashSet<String> annotated = new HashSet<>();
        for (String property : properties) {
            annotated.add(property + "@iot.navigationLink");
        }
        return annotated;
    }

    /**
     * This helper method is the start point for checking the response for a specific entity in all aspects.
     *
     * @param entityType
     *        Entity type from EntityType enum list
     * @param entity
     *        The response of the GET request to be checked
     */
    private void checkEntityAllAspectsForResponse(STAEntityDefinition entityType, JsonNode entity) {
        checkEntityControlInformation(entity);
        checkEntityProperties(this.getEntityPropsMandatory(entityType), entity);
        checkEntityProperties(annotateNavigationProperties(entityType.getNavPropsMandatory()), entity);
    }

    /**
     * This helper method is the start point for checking the response for a specific entity in all aspects.
     *
     * @param entityType
     *        Entity type from EntityType enum list
     * @param entitySet
     *        The response of the GET request to be checked
     */
    private void checkEntityAllAspectsForResponse(STAEntityDefinition entityType, Set<JsonNode> entitySet) {
        for (JsonNode entity : entitySet) {
            checkEntityControlInformation(entity);
            checkEntityProperties(this.getEntityPropsMandatory(entityType), entity);
            checkEntityProperties(annotateNavigationProperties(entityType.getNavPropsMandatory()), entity);
        }
    }

    /**
     * This helper method is checking the control information of the response for a collection
     *
     * @param response
     *        The response of the GET request to be checked
     */
    private void checkEntitiesControlInformation(JsonNode response) {
        JsonNode entities = response.get(value);
        for (int i = 0; i < entities.size(); i++) {
            JsonNode entity = entities.get(i);
            checkEntityControlInformation(entity);
        }
    }

    /**
     * This helper method is checking the control information of the response for a specific entity
     *
     * @param entity
     *        The entity to be checked
     */
    private void checkEntityControlInformation(JsonNode entity) {
        Assertions.assertTrue(entity.has(StaConstants.AT_IOT_ID),
                              "The entity does not have mandatory control information :" + StaConstants.AT_IOT_ID);
        Assertions.assertNotNull(entity.get(StaConstants.AT_IOT_ID),
                                 "The entity does not have mandatory control information :" + StaConstants.AT_IOT_ID);

        Assertions.assertTrue(entity.has(StaConstants.AT_IOT_SELFLINK),
                              "The entity does not have mandatory control information :"
                                      +
                                      StaConstants.AT_IOT_SELFLINK);
    }

    /**
     * This helper method is checking the mandatory properties of the response for a collection
     *
     * @param mandatoryProperties
     *        List of mandatory properties
     * @param response
     *        The response of the GET request to be checked
     */
    private void checkEntitiesProperties(Set<String> mandatoryProperties, JsonNode response) {
        JsonNode entities = response.get(value);
        for (int i = 0; i < entities.size(); i++) {
            JsonNode entity = entities.get(i);
            checkEntityProperties(mandatoryProperties, entity);
        }

    }

}
