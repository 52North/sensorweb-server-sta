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

package org.n52.sta;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.LicenseEntityDefinition;
import org.n52.shetland.ogc.sta.model.ObservationGroupEntityDefinition;
import org.n52.shetland.ogc.sta.model.ObservationRelationEntityDefinition;
import org.n52.shetland.ogc.sta.model.PartyEntityDefinition;
import org.n52.shetland.ogc.sta.model.ProjectEntityDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@ActiveProfiles(StaConstants.CITSCIEXTENSION)
public class ITConformance1CitSci extends ITConformance1 {

    public ITConformance1CitSci(@Value("${server.rootUrl}") String rootUrl) throws Exception {
        super(rootUrl, false);

        postEntity(EntityType.THING, "{\n" +
            "    \"@iot.id\": \"DemoThing\",\n" +
            "    \"description\": \"This is the Lobby of the 52N HQ\",\n" +
            "    \"name\": \"52N HQ Lobby\",\n" +
            "    \"Locations\": [\n" +
            "        {\n" +
            "            \"@iot.id\": \"DemoLocation\",\n" +
            "            \"name\": \"Location of 52N HQ\",\n" +
            "            \"description\": \"Somewhere in the Loddenheide\",\n" +
            "            \"encodingType\": \"application/vnd.geo+json\",\n" +
            "            \"location\": {\n" +
            "                \"type\": \"Feature\",\n" +
            "                \"geometry\": {\n" +
            "                    \"type\": \"Point\",\n" +
            "                    \"coordinates\": [\n" +
            "                        52,\n" +
            "                        7\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    ],\n" +
            "    \"Datastreams\": [\n" +
            "        {\n" +
            "            \"@iot.id\": \"DemoCSDatastream\",\n" +
            "            \"name\": \"Air Temperature\",\n" +
            "            \"description\": \"This Datastreams measures Air Temperature\",\n" +
            "            \"unitOfMeasurement\": {\n" +
            "                \"name\": \"degree Celsius\",\n" +
            "                \"symbol\": \"°C\",\n" +
            "                \"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\n" +
            "            },\n" +
            "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2" +
            ".0/OM_Measurement\",\n" +
            "            \"phenomenonTime\": \"2009-01-11T16:22:25.00Z/2011-08-21T08:32:10.00Z\",\n" +
            "            \"ObservedProperty\": {\n" +
            "                \"@iot.id\": \"DemoAirTemp\",\n" +
            "                \"name\": \"Air Temperature\",\n" +
            "                \"definition\": \"http://sweet.jpl.nasa.gov/ontology/property.owl#AirTemperature\"," +
            "\n" +
            "                \"description\": \"The air temperature is the temperature of the air.\"\n" +
            "            },\n" +
            "            \"Sensor\": {\n" +
            "                \"@iot.id\": \"DemoDS18B2022\",\n" +
            "                \"name\": \"sensor 1\",\n" +
            "                \"description\": \"sensor 1\",\n" +
            "                \"encodingType\": \"application/pdf\",\n" +
            "                \"metadata\": \"http://datasheets.maxim-ic.com/en/ds/DS18B20.pdf\"\n" +
            "            },\n" +
            "            \"License\": {\n" +
            "                \"@iot.id\": \"DemoMIT\",\n" +
            "                \"name\": \"Demo MIT License\",\n" +
            "                \"definition\": \"https://opensource.org/licenses/MIT\"\n" +
            "            },\n" +
            "            \"Project\": {\n" +
            "                \"@iot.id\": \"DemoProject\",\n" +
            "                \"name\": \"Demo Project.\",\n" +
            "                \"description\": \"This is a demo project\",\n" +
            "                \"runtime\": \"2020-06-25T03:42:02-02:00\"\n" +
            "            },\n" +
            "            \"Party\": {\n" +
            "                \"@iot.id\": \"DemoParty\",\n" +
            "                \"nickName\": \"Demo Party nickName\",\n" +
            "                \"role\": \"individual\"\n" +
            "            },\n" +
            "            \"Observations\": [\n" +
            "                {\n" +
            "                    \"@iot.id\": \"DemoCSObservation\",\n" +
            "                    \"result\": \"52\",\n" +
            "                    \"phenomenonTime\": \"2099-03-11T17:55:09Z\",\n" +
            "                    \"FeatureOfInterest\": {\n" +
            "                        \"@iot.id\": \"DemoFOI\",\n" +
            "                        \"name\": \"DemoFeatureOfInterest\",\n" +
            "                        \"description\": \"DemoFeatureOfInterest\",\n" +
            "                        \"encodingType\": \"application/vnd.geo+json\",\n" +
            "                        \"feature\": {\n" +
            "                            \"type\": \"Feature\",\n" +
            "                            \"geometry\": {\n" +
            "                                \"type\": \"LineString\",\n" +
            "                                \"coordinates\": [\n" +
            "                                    [\n" +
            "                                        0,\n" +
            "                                        0.0\n" +
            "                                    ],\n" +
            "                                    [\n" +
            "                                        52,\n" +
            "                                        52\n" +
            "                                    ]\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        }\n" +
            "                    },\n" +
            "                    \"ObservationRelations\": [\n" +
            "                        {\n" +
            "                            \"@iot.id\": \"DemoRelation\",\n" +
            "                            \"type\": \"root\",\n" +
            "                            \"Group\": {\n" +
            "                                \"@iot.id\": \"DemoGroup\",\n" +
            "                                \"name\": \"Demo Group 1\",\n" +
            "                                \"description\": \"Demo Group 1 description\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}"
        );
    }

    @Test
    public void testReadEntitiesAndCheckResponse() throws Exception {
        JsonNode collection;
        // CitSci Extension
        collection = getCollection(EntityType.LICENSE);
        checkEntitiesAllAspectsForResponse(new LicenseEntityDefinition(), collection);
        collection = getCollection(EntityType.PARTY);
        checkEntitiesAllAspectsForResponse(new PartyEntityDefinition(), collection);
        collection = getCollection(EntityType.PROJECT);
        checkEntitiesAllAspectsForResponse(new ProjectEntityDefinition(), collection);
        collection = getCollection(EntityType.OBSERVATIONGROUP);
        checkEntitiesAllAspectsForResponse(new ObservationGroupEntityDefinition(), collection);
        collection = getCollection(EntityType.OBSERVATIONRELATION);
        checkEntitiesAllAspectsForResponse(new ObservationRelationEntityDefinition(), collection);
    }

    /**
     * This method is testing GET when requesting a nonexistent entity. The
     * response should be 404.
     */
    @Test
    public void readNonexistentEntity() throws Exception {
        getNonExistentEntity(EntityType.LICENSE);
        getNonExistentEntity(EntityType.PARTY);
        getNonExistentEntity(EntityType.PROJECT);
        getNonExistentEntity(EntityType.OBSERVATIONGROUP);
        getNonExistentEntity(EntityType.OBSERVATIONRELATION);
    }

    /**
     * This method is testing GET for a property of an entity.
     */
    @Test
    public void readPropertyOfEntityAndCheckResponse() throws Exception {
        readPropertyOfEntityWithEntityType(EntityType.LICENSE, new LicenseEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.PARTY, new PartyEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.PROJECT, new ProjectEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.OBSERVATIONGROUP, new ObservationGroupEntityDefinition());
        readPropertyOfEntityWithEntityType(EntityType.OBSERVATIONRELATION, new ObservationRelationEntityDefinition());
    }

    /**
     * This method is testing the resource paths based on specification to the
     * specified level.
     */
    @Test
    public void checkResourcePaths() throws Exception {
        Set<JsonNode> response;
        response = readEntityWithEntityType(EntityType.LICENSE);
        checkRelatedEndpoints(EntityType.LICENSE, ((JsonNode) response.toArray()[0]).get(idKey).asText());
        response = readEntityWithEntityType(EntityType.PARTY);
        checkRelatedEndpoints(EntityType.PARTY, ((JsonNode) response.toArray()[0]).get(idKey).asText());
        response = readEntityWithEntityType(EntityType.PROJECT);
        checkRelatedEndpoints(EntityType.PROJECT, ((JsonNode) response.toArray()[0]).get(idKey).asText());
        response = readEntityWithEntityType(EntityType.OBSERVATIONRELATION);
        checkRelatedEndpoints(EntityType.OBSERVATIONRELATION, ((JsonNode) response.toArray()[0]).get(idKey).asText());
        response = readEntityWithEntityType(EntityType.OBSERVATIONGROUP);
        checkRelatedEndpoints(EntityType.OBSERVATIONGROUP, ((JsonNode) response.toArray()[0]).get(idKey).asText());
    }
}
