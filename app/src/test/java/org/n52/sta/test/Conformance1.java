///*
// * Copyright 2015-2020 52°North Initiative for Geospatial Open Source
// * Software GmbH
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.n52.sta.test;
//
//import org.json.JSONException;
//import org.junit.BeforeClass;
//import org.junit.jupiter.api.Test;
//import org.n52.janmayen.http.HTTPMethods;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
// */
//public class Conformance1 {
//
//    /**
//     * The variable that defines to which recursive level the resource path
//     * should be tested
//     */
//    private final int resourcePathLevel = 4;
//
//    /**
//     * This method is testing GET entities. It should return 200. Then the
//     * response entities are tested for control information, mandatory
//     * properties, and mandatory related entities.
//     */
//    @Test()
//    public void readEntitiesAndCheckResponse() {
//        String response = getEntities(TestUtil.EntityType.THING);
//        checkEntitiesAllAspectsForResponse(TestUtil.EntityType.THING, response);
//        response = getEntities(TestUtil.EntityType.LOCATION);
//        checkEntitiesAllAspectsForResponse(EntityType.LOCATION, response);
//        response = getEntities(EntityType.HISTORICAL_LOCATION);
//        checkEntitiesAllAspectsForResponse(EntityType.HISTORICAL_LOCATION, response);
//        response = getEntities(EntityType.DATASTREAM);
//        checkEntitiesAllAspectsForResponse(EntityType.DATASTREAM, response);
//        response = getEntities(EntityType.SENSOR);
//        checkEntitiesAllAspectsForResponse(EntityType.SENSOR, response);
//        response = getEntities(EntityType.OBSERVATION);
//        checkEntitiesAllAspectsForResponse(EntityType.OBSERVATION, response);
//        response = getEntities(EntityType.OBSERVED_PROPERTY);
//        checkEntitiesAllAspectsForResponse(EntityType.OBSERVED_PROPERTY, response);
//        response = getEntities(EntityType.FEATURE_OF_INTEREST);
//        checkEntitiesAllAspectsForResponse(EntityType.FEATURE_OF_INTEREST, response);
//    }
//
//    /**
//     * This method is testing GET when requesting a nonexistent entity. The
//     * response should be 404.
//     */
//    @Test()
//    public void readNonexistentEntity() {
//        readNonexistentEntityWithEntityType(EntityType.THING);
//        readNonexistentEntityWithEntityType(EntityType.LOCATION);
//        readNonexistentEntityWithEntityType(EntityType.HISTORICAL_LOCATION);
//        readNonexistentEntityWithEntityType(EntityType.DATASTREAM);
//        readNonexistentEntityWithEntityType(EntityType.SENSOR);
//        readNonexistentEntityWithEntityType(EntityType.OBSERVATION);
//        readNonexistentEntityWithEntityType(EntityType.OBSERVED_PROPERTY);
//        readNonexistentEntityWithEntityType(EntityType.FEATURE_OF_INTEREST);
//    }
//
//    /**
//     * This method is testing GET for a specific entity with its id. It checks
//     * the control information, mandatory properties and mandatory related
//     * entities for the response entity.
//     */
//    @Test(description = "GET Specific Entity", groups = "level-1")
//    public void readEntityAndCheckResponse() {
//        String response = readEntityWithEntityType(EntityType.THING);
//        checkEntityAllAspectsForResponse(EntityType.THING, response);
//        response = readEntityWithEntityType(EntityType.LOCATION);
//        checkEntityAllAspectsForResponse(EntityType.LOCATION, response);
//        response = readEntityWithEntityType(EntityType.HISTORICAL_LOCATION);
//        checkEntityAllAspectsForResponse(EntityType.HISTORICAL_LOCATION, response);
//        response = readEntityWithEntityType(EntityType.DATASTREAM);
//        checkEntityAllAspectsForResponse(EntityType.DATASTREAM, response);
//        response = readEntityWithEntityType(EntityType.SENSOR);
//        checkEntityAllAspectsForResponse(EntityType.SENSOR, response);
//        response = readEntityWithEntityType(EntityType.OBSERVATION);
//        checkEntityAllAspectsForResponse(EntityType.OBSERVATION, response);
//        response = readEntityWithEntityType(EntityType.OBSERVED_PROPERTY);
//        checkEntityAllAspectsForResponse(EntityType.OBSERVED_PROPERTY, response);
//        response = readEntityWithEntityType(EntityType.FEATURE_OF_INTEREST);
//        checkEntityAllAspectsForResponse(EntityType.FEATURE_OF_INTEREST, response);
//    }
//
//    /**
//     * This method is testing GET for a property of an entity.
//     */
//    @Test(description = "GET Property of an Entity", groups = "level-1")
//    public void readPropertyOfEntityAndCheckResponse() {
//        readPropertyOfEntityWithEntityType(EntityType.THING);
//        readPropertyOfEntityWithEntityType(EntityType.LOCATION);
//        readPropertyOfEntityWithEntityType(EntityType.HISTORICAL_LOCATION);
//        readPropertyOfEntityWithEntityType(EntityType.DATASTREAM);
//        readPropertyOfEntityWithEntityType(EntityType.OBSERVED_PROPERTY);
//        readPropertyOfEntityWithEntityType(EntityType.SENSOR);
//        readPropertyOfEntityWithEntityType(EntityType.OBSERVATION);
//        readPropertyOfEntityWithEntityType(EntityType.FEATURE_OF_INTEREST);
//    }
//
//    /**
//     * This helper method is testing property and property/$value for single
//     * entity of a given entity type
//     *
//     * @param entityType Entity type from EntityType enum list
//     */
//    private void readPropertyOfEntityWithEntityType(EntityType entityType) {
//        try {
//            String response = getEntities(entityType);
//            Long id = new JSONObject(response).getJSONArray("value").getJSONObject(0).getLong(ControlInformation.ID);
//            for (String property : EntityProperties.getPropertiesListFor(entityType)) {
//                checkGetPropertyOfEntity(entityType, id, property);
//                checkGetPropertyValueOfEntity(entityType, id, property);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//    }
//
//    /**
//     * This helper method sending GET request for requesting a property and
//     * check the response is 200.
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @param id         The id of the entity
//     * @param property   The property to get requested
//     */
//    private void checkGetPropertyOfEntity(EntityType entityType, long id, String property) {
//        try {
//            Map<String, Object> responseMap = getEntity(entityType, id, property);
//            int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
//            Assert.assertEquals(responseCode,
//                                200,
//                                "Reading property \"" + property + "\" of the existing " + entityType.name() +
//                                        " with id " + id + " failed.");
//            String response = responseMap.get("response").toString();
//            JSONObject entity = null;
//            entity = new JSONObject(response.toString());
//            try {
//                Assert.assertNotNull(entity.get(property),
//                                     "Reading property \"" + property + "\"of \"" + entityType + "\" fails.");
//            } catch (JSONException e) {
//                Assert.fail("Reading property \"" + property + "\"of \"" + entityType + "\" fails.");
//            }
//            Assert.assertEquals(entity.length(),
//                                1,
//                                "The response for getting property " + property + " of a " + entityType +
//                                        " returns more properties!");
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//    }
//
//    /**
//     * This helper method sending GET request for requesting a property $value
//     * and check the response is 200.
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @param id         The id of the entity
//     * @param property   The property to get requested
//     */
//    private void checkGetPropertyValueOfEntity(EntityType entityType, long id, String property) {
//        Map<String, Object> responseMap = getEntity(entityType, id, property + "/$value");
//        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
//        Assert.assertEquals(responseCode,
//                            200,
//                            "Reading property value of \"" + property + "\" of the exitixting " + entityType.name() +
//                                    " with id " + id + " failed.");
//        String response = responseMap.get("response").toString();
//        if (!property.equals("location") && !property.equals("feature") && !property.equals("unitOfMeasurement")) {
//            Assert.assertEquals(response.indexOf("{"),
//                                -1,
//                                "Reading property value of \"" + property + "\"of \"" + entityType + "\" fails.");
//        } else {
//            Assert.assertEquals(response.indexOf("{"),
//                                0,
//                                "Reading property value of \"" + property + "\"of \"" + entityType + "\" fails.");
//        }
//    }
//
//    /**
//     * This method is testing the resource paths based on specification to the
//     * specified level.
//     */
//    @Test(description = "Check Resource Path", groups = "level-1")
//    public void checkResourcePaths() {
//        readRelatedEntityOfEntityWithEntityType(EntityType.THING);
//        readRelatedEntityOfEntityWithEntityType(EntityType.LOCATION);
//        readRelatedEntityOfEntityWithEntityType(EntityType.HISTORICAL_LOCATION);
//        readRelatedEntityOfEntityWithEntityType(EntityType.DATASTREAM);
//        readRelatedEntityOfEntityWithEntityType(EntityType.OBSERVED_PROPERTY);
//        readRelatedEntityOfEntityWithEntityType(EntityType.SENSOR);
//        readRelatedEntityOfEntityWithEntityType(EntityType.OBSERVATION);
//        readRelatedEntityOfEntityWithEntityType(EntityType.FEATURE_OF_INTEREST);
//    }
//
//    /**
//     * This helper method is the start point for testing resource path. It adds
//     * the entity type to be tested to resource path chain and call the other
//     * method to test the chain.
//     *
//     * @param entityType Entity type from EntityType enum list
//     */
//    private void readRelatedEntityOfEntityWithEntityType(EntityType entityType) {
//        List<String> entityTypes = new ArrayList<>();
//        List<Long> ids = new ArrayList<>();
//        switch (entityType) {
//        case THING:
//            entityTypes.add("Things");
//            break;
//        case LOCATION:
//            entityTypes.add("Locations");
//            break;
//        case HISTORICAL_LOCATION:
//            entityTypes.add("HistoricalLocations");
//            break;
//        case DATASTREAM:
//            entityTypes.add("Datastreams");
//            break;
//        case SENSOR:
//            entityTypes.add("Sensors");
//            break;
//        case OBSERVATION:
//            entityTypes.add("Observations");
//            break;
//        case OBSERVED_PROPERTY:
//            entityTypes.add("ObservedProperties");
//            break;
//        case FEATURE_OF_INTEREST:
//            entityTypes.add("FeaturesOfInterest");
//            break;
//        default:
//            Assert.fail("Entity type is not recognized in SensorThings API : " + entityType);
//        }
//        readRelatedEntity(entityTypes, ids);
//    }
//
//    /**
//     * This helper method is testing the chain to the specified level. It
//     * confirms that the response is 200.
//     *
//     * @param entityTypes List of entity type from EntityType enum list for the
//     *                    chain
//     * @param ids         List of ids for teh chain
//     */
//    private void readRelatedEntity(List<String> entityTypes, List<Long> ids) {
//        if (entityTypes.size() > resourcePathLevel) {
//            return;
//        }
//        try {
//            String urlString = ServiceURLBuilder.buildURLString(rootUri, entityTypes, ids, null);
//            Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
//            Assert.assertEquals(responseMap.get("response-code"),
//                                200,
//                                "Reading relation of the entity failed: " + entityTypes.toString());
//            String response = responseMap.get("response").toString();
//            if (!entityTypes.get(entityTypes.size() - 1).toLowerCase().equals("featuresofinterest") &&
//                    !entityTypes.get(entityTypes.size() - 1).endsWith("s")) {
//                return;
//            }
//            Long id = new JSONObject(response.toString()).getJSONArray("value")
//                                                         .getJSONObject(0)
//                                                         .getLong(ControlInformation.ID);
//
//            //check $ref
//            urlString = ServiceURLBuilder.buildURLString(rootUri, entityTypes, ids, "$ref");
//            responseMap = HTTPMethods.doGet(urlString);
//            Assert.assertEquals(responseMap.get("response-code"),
//                                200,
//                                "Reading relation of the entity failed: " + entityTypes.toString());
//            response = responseMap.get("response").toString();
//            checkAssociationLinks(response, entityTypes, ids);
//
//            if (entityTypes.size() == resourcePathLevel) {
//                return;
//            }
//            ids.add(id);
//            for (String relation : EntityRelations.getRelationsListFor(entityTypes.get(entityTypes.size() - 1))) {
//                entityTypes.add(relation);
//                readRelatedEntity(entityTypes, ids);
//                entityTypes.remove(entityTypes.size() - 1);
//            }
//            ids.remove(ids.size() - 1);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//
//    }
//
//    /**
//     * This method is checking the response for the request of Association Link.
//     * It confirms that it contains a list of selfLinks.
//     *
//     * @param response    The response for GET association link request
//     * @param entityTypes List of entity type from EntityType enum list for the
//     *                    chain
//     * @param ids         List of ids for teh chain
//     */
//    private void checkAssociationLinks(String response, List<String> entityTypes, List<Long> ids) {
//
//        try {
//            Assert.assertTrue(response.indexOf("value") != -1,
//                              "The GET entities Association Link response does not match SensorThings API : missing " +
//                                      "\"value\" in response.: " +
//                                      entityTypes.toString() + ids.toString());
//            JSONArray value = new JSONObject(response.toString()).getJSONArray("value");
//            int count = 0;
//            for (int i = 0; i < value.length() && count < 2; i++) {
//                count++;
//                JSONObject obj = value.getJSONObject(i);
//                try {
//                    Assert.assertNotNull(obj.get(ControlInformation.SELF_LINK),
//                                         "The Association Link does not contain self-links.: " +
//                                                 entityTypes.toString() + ids.toString());
//                } catch (JSONException e) {
//                    Assert.fail("The Association Link does not contain self-links.: " + entityTypes.toString() +
//                                        ids.toString());
//                }
//                Assert.assertEquals(obj.length(), 1,
//                                    "The Association Link contains properties other than self-link.: " +
//                                            entityTypes.toString() + ids.toString());
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//    }
//
//    /**
//     * This method is reading a specific entity and return it as a string.
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @return The entity response as a string
//     */
//    private String readEntityWithEntityType(EntityType entityType) {
//        try {
//            String response = getEntities(entityType);
//            Long id = new JSONObject(response.toString()).getJSONArray("value")
//                                                         .getJSONObject(0)
//                                                         .getLong(ControlInformation.ID);
//            Map<String, Object> responseMap = getEntity(entityType, id, null);
//            int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
//            Assert.assertEquals(responseCode,
//                                200,
//                                "Reading existing " + entityType.name() + " with id " + id + " failed.");
//            response = responseMap.get("response").toString();
//            return response;
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * This method is check the response of sending a GET request to
//     * m=nonexistent entity is 404.
//     *
//     * @param entityType Entity type from EntityType enum list
//     */
//    private void readNonexistentEntityWithEntityType(EntityType entityType) {
//        long id = Long.MAX_VALUE;
//        int responseCode = Integer.parseInt(getEntity(entityType, id, null).get("response-code").toString());
//        Assert.assertEquals(responseCode,
//                            404,
//                            "Reading non-existing " + entityType.name() + " with id " + id + " failed.");
//    }
//
//    /**
//     * This method is testing the root URL of the service under test. It
//     * basically checks the first page.
//     */
//    @Test(description = "Check Service Root UI", groups = "level-1")
//    public void checkServiceRootUri() {
//        try {
//            String response = getEntities(null);
//            JSONObject jsonResponse = new JSONObject(response.toString());
//            JSONArray entities = jsonResponse.getJSONArray("value");
//            Map<String, Boolean> addedLinks = new HashMap<>();
//            addedLinks.put("Things", false);
//            addedLinks.put("Locations", false);
//            addedLinks.put("HistoricalLocations", false);
//            addedLinks.put("Datastreams", false);
//            addedLinks.put("Sensors", false);
//            addedLinks.put("Observations", false);
//            addedLinks.put("ObservedProperties", false);
//            addedLinks.put("FeaturesOfInterest", false);
//            for (int i = 0; i < entities.length(); i++) {
//                JSONObject entity = entities.getJSONObject(i);
//                try {
//                    Assert.assertNotNull(entity.get("name"));
//                    Assert.assertNotNull(entity.get("url"));
//                } catch (JSONException e) {
//                    Assert.fail("Service root URI does not have proper JSON keys: name and value.");
//                }
//                String name = entity.getString("name");
//                String nameUrl = entity.getString("url");
//                switch (name) {
//                case "Things":
//                    Assert.assertEquals(nameUrl,
//                                        rootUri + "/Things",
//                                        "The URL for Things in Service Root URI is not compliant to SensorThings API.");
//                    addedLinks.remove("Things");
//                    addedLinks.put(name, true);
//                    break;
//                case "Locations":
//                    Assert.assertEquals(nameUrl,
//                                        rootUri + "/Locations",
//                                        "The URL for Locations in Service Root URI is not compliant to SensorThings " +
//                                                "API.");
//                    addedLinks.remove("Locations");
//                    addedLinks.put(name, true);
//                    break;
//                case "HistoricalLocations":
//                    Assert.assertEquals(nameUrl,
//                                        rootUri + "/HistoricalLocations",
//                                        "The URL for HistoricalLocations in Service Root URI is not compliant to " +
//                                                "SensorThings API.");
//                    addedLinks.remove("HistoricalLocations");
//                    addedLinks.put(name, true);
//                    break;
//                case "Datastreams":
//                    Assert.assertEquals(nameUrl,
//                                        rootUri + "/Datastreams",
//                                        "The URL for Datastreams in Service Root URI is not compliant to SensorThings" +
//                                                " API.");
//                    addedLinks.remove("Datastreams");
//                    addedLinks.put(name, true);
//                    break;
//                case "Sensors":
//                    Assert.assertEquals(nameUrl,
//                                        rootUri + "/Sensors",
//                                        "The URL for Sensors in Service Root URI is not compliant to SensorThings API" +
//                                                ".");
//                    addedLinks.remove("Sensors");
//                    addedLinks.put(name, true);
//                    break;
//                case "Observations":
//                    Assert.assertEquals(nameUrl,
//                                        rootUri + "/Observations",
//                                        "The URL for Observations in Service Root URI is not compliant to " +
//                                                "SensorThings API.");
//                    addedLinks.remove("Observations");
//                    addedLinks.put(name, true);
//                    break;
//                case "ObservedProperties":
//                    Assert.assertEquals(nameUrl,
//                                        rootUri + "/ObservedProperties",
//                                        "The URL for ObservedProperties in Service Root URI is not compliant to " +
//                                                "SensorThings API.");
//                    addedLinks.remove("ObservedProperties");
//                    addedLinks.put(name, true);
//                    break;
//                case "FeaturesOfInterest":
//                    Assert.assertEquals(nameUrl,
//                                        rootUri + "/FeaturesOfInterest",
//                                        "The URL for FeaturesOfInterest in Service Root URI is not compliant to " +
//                                                "SensorThings API.");
//                    addedLinks.remove("FeaturesOfInterest");
//                    addedLinks.put(name, true);
//                    break;
//                case "MultiDatastreams":
//                    Assert.assertEquals(nameUrl,
//                                        rootUri + "/MultiDatastreams",
//                                        "The URL for MultiDatastreams in Service Root URI is not compliant to " +
//                                                "SensorThings API.");
//                    break;
//                default:
//                    Assert.fail("There is a component in Service Root URI response that is not in SensorThings API : " +
//                                        name);
//                    break;
//                }
//            }
//            for (String key : addedLinks.keySet()) {
//                Assert.assertTrue(addedLinks.get(key), "The Service Root URI response does not contain " + key);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//    }
//
//    /**
//     * This helper method is sending GET request to a collection of entities.
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @return The response of GET request in string format.
//     */
//    private String getEntities(EntityType entityType) {
//        String urlString = rootUri;
//        if (entityType != null) {
//            urlString = ServiceURLBuilder.buildURLString(rootUri, entityType, -1, null, null);
//        }
//        Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
//        String response = responseMap.get("response").toString();
//        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
//        Assert.assertEquals(responseCode,
//                            200,
//                            "Error during getting entities: " +
//                                    ((entityType != null) ? entityType.name() : "root URI"));
//        if (entityType != null) {
//            Assert.assertTrue(response.indexOf("value") != -1,
//                              "The GET entities response for entity type \"" + entityType +
//                                      "\" does not match SensorThings API : missing \"value\" in response.");
//        } else { // GET Service Base URI
//            Assert.assertTrue(response.indexOf("value") != -1, "The GET entities response for service root URI " +
//                    "does not match SensorThings API : missing \"value\" in response.");
//        }
//        return response.toString();
//    }
//
//    /**
//     * This helper method is sending Get request to a specific entity
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @param id         The if of the specific entity
//     * @param property   The requested property of the entity
//     * @return The response-code and response (body) of the request in Map
//     * format.
//     */
//    private Map<String, Object> getEntity(EntityType entityType, long id, String property) {
//        if (id == -1) {
//            return null;
//        }
//        String urlString = ServiceURLBuilder.buildURLString(rootUri, entityType, id, null, property);
//        return HTTPMethods.doGet(urlString);
//    }
//
//    /**
//     * This helper method is the start point for checking the response for a
//     * collection in all aspects.
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @param response   The response of the GET request to be checked
//     */
//    private void checkEntitiesAllAspectsForResponse(EntityType entityType, String response) {
//        checkEntitiesControlInformation(response);
//        checkEntitiesProperties(entityType, response);
//        checkEntitiesRelations(entityType, response);
//    }
//
//    /**
//     * This helper method is the start point for checking the response for a
//     * specific entity in all aspects.
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @param response   The response of the GET request to be checked
//     */
//    private void checkEntityAllAspectsForResponse(EntityType entityType, String response) {
//        checkEntityControlInformation(response);
//        checkEntityProperties(entityType, response);
//        checkEntityRelations(entityType, response);
//    }
//
//    /**
//     * This helper method is checking the control information of the response
//     * for a collection
//     *
//     * @param response The response of the GET request to be checked
//     */
//    private void checkEntitiesControlInformation(String response) {
//        try {
//            JSONObject jsonResponse = new JSONObject(response.toString());
//            JSONArray entities = jsonResponse.getJSONArray("value");
//            int count = 0;
//            for (int i = 0; i < entities.length() && count < 2; i++) {
//                count++;
//                JSONObject entity = entities.getJSONObject(i);
//                checkEntityControlInformation(entity);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//    }
//
//    /**
//     * This helper method is checking the control information of the response
//     * for a specific entity
//     *
//     * @param response The response of the GET request to be checked
//     */
//    private void checkEntityControlInformation(Object response) {
//        try {
//            JSONObject entity = new JSONObject(response.toString());
//            try {
//                Assert.assertNotNull(entity.get(ControlInformation.ID),
//                                     "The entity does not have mandatory control information : " +
//                                             ControlInformation.ID);
//            } catch (JSONException e) {
//                Assert.fail("The entity does not have mandatory control information : " + ControlInformation.ID);
//            }
//            try {
//                Assert.assertNotNull(entity.get(ControlInformation.SELF_LINK), "The entity does not have " +
//                        "mandatory control information : " + ControlInformation.SELF_LINK);
//            } catch (JSONException e) {
//                Assert.fail("The entity does not have mandatory control information : " + ControlInformation.SELF_LINK);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//    }
//
//    /**
//     * This helper method is checking the mandatory properties of the response
//     * for a collection
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @param response   The response of the GET request to be checked
//     */
//    private void checkEntitiesProperties(EntityType entityType, String response) {
//        try {
//            JSONObject jsonResponse = new JSONObject(response.toString());
//            JSONArray entities = jsonResponse.getJSONArray("value");
//            int count = 0;
//            for (int i = 0; i < entities.length() && count < 2; i++) {
//                count++;
//                JSONObject entity = entities.getJSONObject(i);
//                checkEntityProperties(entityType, entity);
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//
//    }
//
//    /**
//     * This helper method is checking the mandatory properties of the response
//     * for a specific entity
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @param response   The response of the GET request to be checked
//     */
//    private void checkEntityProperties(EntityType entityType, Object response) {
//        try {
//            JSONObject entity = new JSONObject(response.toString());
//            for (String property : EntityProperties.getPropertiesListFor(entityType)) {
//                try {
//                    Assert.assertNotNull(entity.get(property), "Entity type \"" + entityType + "\" does not have " +
//                            "mandatory property: \"" + property + "\".");
//                } catch (JSONException e) {
//                    Assert.fail("Entity type \"" + entityType + "\" does not have mandatory property: \"" + property +
//                                        "\".");
//                }
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//
//    }
//
//    /**
//     * This helper method is checking the mandatory relations of the response
//     * for a collection
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @param response   The response of the GET request to be checked
//     */
//    private void checkEntitiesRelations(EntityType entityType, String response) {
//        try {
//            JSONObject jsonResponse = new JSONObject(response);
//            JSONArray entities = jsonResponse.getJSONArray("value");
//            int count = 0;
//            for (int i = 0; i < entities.length() && count < 2; i++) {
//                count++;
//                JSONObject entity = entities.getJSONObject(i);
//                checkEntityRelations(entityType, entity);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//
//    }
//
//    /**
//     * This helper method is checking the mandatory relations of the response
//     * for a specific entity
//     *
//     * @param entityType Entity type from EntityType enum list
//     * @param response   The response of the GET request to be checked
//     */
//    private void checkEntityRelations(EntityType entityType, Object response) {
//        try {
//            JSONObject entity = new JSONObject(response.toString());
//            for (String relation : EntityRelations.getRelationsListFor(entityType)) {
//                try {
//                    Assert.assertNotNull(entity.get(relation + ControlInformation.NAVIGATION_LINK),
//                                         "Entity type \"" + entityType + "\" does not have mandatory relation: \"" +
//                                                 relation + "\".");
//                } catch (JSONException e) {
//                    Assert.fail("Entity type \"" + entityType + "\" does not have mandatory relation: \"" + relation +
//                                        "\".");
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
//        }
//    }
//
//}
//
//}
