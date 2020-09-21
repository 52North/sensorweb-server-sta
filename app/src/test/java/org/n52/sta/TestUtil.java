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
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.jupiter.api.Assertions;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface TestUtil {

    String jsonMimeType = "application/json";
    String idKey = "@iot.id";
    String countKey = "@iot.count";
    String selfLinkKey = "@iot.selfLink";
    String value = "value";
    String DATASTREAMS = "Datastreams";
    String DATASTREAM = "Datastream";
    String THING = "Thing";
    String THINGS = "Things";
    String LOCATIONS = "Locations";
    String LOCATION = "Location";
    String SENSORS = "Sensors";
    String SENSOR = "Sensor";
    String HISTORICALLOCATIONS = "HistoricalLocations";
    String HISTORICALLOCATION = "HistoricalLocation";
    String OBSERVATIONS = "Observations";
    String OBSERVATION = "Observation";
    String FEATUREOFINTEREST = "FeatureOfInterest";
    String FEATURESOFINTEREST = "FeaturesOfInterest";
    String OBSERVEDPROPERTY = "ObservedProperty";
    String OBSERVEDPROPERTIES = "ObservedProperties";
    String OBSERVATIONRELATIONS = "ObservationRelations";
    String OBSERVATIONRELATION = "ObservationRelation";
    String OBSERVATIONGROUPS = "ObservationGroups";
    String OBSERVATIONGROUP = "ObservationGroup'";
    String LICENSES = "Licenses";
    String LICENSE = "License";
    String PROJECTS = "Projects";
    String PROJECT = "Project";
    String PARTIES = "Parties";
    String PARTY = "Party";
    GeometryFactory factory =
        new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    HashMap<String, String> relatedEntityEndpoints = new HashMap<String, String>() {{
        put(EntityType.THING.toString() + EntityType.DATASTREAM.toString(),
            "Things(%s)/Datastreams");
        put(EntityType.THING.toString() + EntityType.LOCATION.toString(),
            "Things(%s)/Locations");
        put(EntityType.THING.toString() + EntityType.HISTORICAL_LOCATION.toString(),
            "Things(%s)/HistoricalLocations");

        put(EntityType.LOCATION.toString() + EntityType.HISTORICAL_LOCATION.toString(),
            "Locations(%s)/HistoricalLocations");
        put(EntityType.LOCATION.toString() + EntityType.THING.toString(),
            "Locations(%s)/Things");

        put(EntityType.HISTORICAL_LOCATION.toString() + EntityType.THING.toString(),
            "HistoricalLocations(%s)/Thing");
        put(EntityType.HISTORICAL_LOCATION.toString() + EntityType.LOCATION.toString(),
            "HistoricalLocations(%s)/Locations");

        put(EntityType.DATASTREAM.toString() + EntityType.THING.toString(),
            "Datastreams(%s)/Thing");
        put(EntityType.DATASTREAM.toString() + EntityType.SENSOR.toString(),
            "Datastreams(%s)/Sensor");
        put(EntityType.DATASTREAM.toString() + EntityType.OBSERVED_PROPERTY.toString(),
            "Datastreams(%s)/ObservedProperty");
        put(EntityType.DATASTREAM.toString() + EntityType.OBSERVATION.toString(),
            "Datastreams(%s)/Observations");

        put(EntityType.SENSOR.toString() + EntityType.DATASTREAM.toString(),
            "Sensors(%s)/Datastreams");

        put(EntityType.OBSERVED_PROPERTY.toString() + EntityType.DATASTREAM.toString(),
            "ObservedProperties(%s)/Datastreams");

        put(EntityType.OBSERVATION.toString() + EntityType.DATASTREAM.toString(),
            "Observations(%s)/Datastream");
        put(EntityType.OBSERVATION.toString() + EntityType.FEATURE_OF_INTEREST.toString(),
            "Observations(%s)/FeatureOfInterest");

        put(EntityType.FEATURE_OF_INTEREST.toString() + EntityType.OBSERVATION.toString(),
            "FeaturesOfInterest(%s)/Observations");

    }};

    default Set<String> getRelatedEntityEndpointKeys(EntityType source) {
        return relatedEntityEndpoints.values()
            .stream()
            .filter((val) -> val.startsWith(source.val))
            .collect(Collectors.toSet());
    }

    default String getRelatedEntityEndpoint(EntityType source, EntityType target) {
        return relatedEntityEndpoints.get(source.toString() + target.toString());
    }

    default void compareJsonNodes(JsonNode reference, JsonNode actual) {
        for (Iterator<Map.Entry<String, JsonNode>> it = reference.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> next = it.next();
            switch (next.getKey()) {
                case "feature":
                case "location":
                    compareJsonNodesGeo(next.getKey(), next.getValue(), actual.get(next.getKey()));
                    break;
                case "validTime":
                case "phenomenonTime":
                case "resultTime":
                    compareJsonNodesTime(next.getKey(), next.getValue(), actual.get(next.getKey()));
                    break;
                default:
                    // Skip navigation links as they have different keys in response
                    // e.g. "Things" vs "Things@iot.navigationLink"
                    if (!Character.isUpperCase(next.getKey().charAt(0))) {
                        if (Objects.equals(next.getKey(), "result")) {
                            compareJsonNodesNumeric(next.getKey(), next.getValue(), actual.get(next.getKey()));
                        } else {
                            compareJsonNodesString(next.getKey(), next.getValue(), actual.get(next.getKey()));
                        }
                    }
                    break;
            }

        }
    }

    default void compareJsonNodesGeo(String fieldname, JsonNode referenceValue, JsonNode actualValue) {
        Assertions.assertNotNull(
            referenceValue,
            "Reference Entity is nonexistent!"
        );
        Assertions.assertNotNull(
            actualValue,
            "Actual Entity is nonexistent!"
        );
        // GeoJson comparison
        GeoJsonReader reader = new GeoJsonReader(factory);
        try {
            Geometry referenceGeo, actualGeo;
            if (referenceValue.get("type").asText().equals("Feature")) {
                referenceGeo = reader.read(referenceValue.get("geometry").toString());
            } else {
                referenceGeo = reader.read(referenceValue.toString());
            }
            if (actualValue.get("type").asText().equals("Feature")) {
                actualGeo = reader.read(actualValue.get("geometry").toString());
            } else {
                actualGeo = reader.read(actualValue.toString());
            }
            Assertions.assertTrue(
                referenceGeo.equals(actualGeo),
                "ERROR: Deep inserted " + fieldname + " is not created correctly."
                    + "Reference: " + referenceValue.toPrettyString()
                    + "Actual: " + actualValue.toPrettyString()
            );
        } catch (ParseException e) {
            Assertions.fail("Could not parse to GeoJSON. Error was:" + e.getMessage());
        }
    }

    default void compareJsonNodesTime(String fieldname, JsonNode reference, JsonNode actual) {
        Assertions.assertNotNull(
            reference,
            "Reference Entity is nonexistent!"
        );
        Assertions.assertNotNull(
            actual,
            "Actual Entity is nonexistent!"
        );
        String referenceTimeValue, actualTimeValue;
        // Time comparison
        referenceTimeValue = reference.toString().replace("\"", "");
        actualTimeValue = actual.toString().replace("\"", "");
        if (referenceTimeValue.equals("null")) {
            Assertions.assertEquals(
                referenceTimeValue,
                actualTimeValue,
                "The " + fieldname + " should have been \""
                    + "null"
                    + "\" but it is now \""
                    + actualTimeValue
                    + "\"."
            );
        } else {
            DateTime referenceTime = ISODateTimeFormat.dateTimeParser().parseDateTime(referenceTimeValue);
            DateTime actualTime = ISODateTimeFormat.dateTimeParser().parseDateTime(actualTimeValue);
            Assertions.assertEquals(
                actualTime,
                referenceTime,
                "The " + fieldname + " should have been \""
                    + referenceTime.toString()
                    + "\" but it is now \""
                    + actualTime.toString()
                    + "\"."
            );
        }
    }

    default void compareJsonNodesNumeric(String fieldname, JsonNode reference, JsonNode actual) {
        Assertions.assertNotNull(
            reference,
            "Reference Entity is nonexistent!"
        );
        Assertions.assertNotNull(
            actual,
            "Actual Entity is nonexistent!"
        );
        // Number comparison
        Double referenceValue, actualValue;
        referenceValue = reference.asDouble();
        actualValue = actual.asDouble();

        Assertions.assertEquals(referenceValue,
                                actualValue,
                                "ERROR: Deep inserted " + fieldname + " is not created correctly.");
    }

    default void compareJsonNodesString(String fieldname, JsonNode reference, JsonNode actual) {
        Assertions.assertNotNull(
            reference,
            "Reference Entity is nonexistent!"
        );
        Assertions.assertNotNull(
            actual,
            "Actual Entity is nonexistent!"
        );
        // Simple String comparison
        String referenceValue, actualValue;
        referenceValue = reference.toString().replace("\"", "");
        actualValue = actual.toString().replace("\"", "");

        Assertions.assertEquals(
            referenceValue,
            actualValue,
            "ERROR: Deep inserted " + fieldname + " is not created correctly."
        );
    }

    default String escape(String val) {
        return "\"" + val + "\"";
    }

    default void assertEmptyResponse(JsonNode response) {
        Assertions.assertTrue(
            0 == response.get("@iot.count").asDouble(),
            "Entity count is not zero although it should be"
        );
        Assertions.assertTrue(
            response.get("value").isEmpty(),
            "Entity is returned although it shouldn't"
        );
    }

    default void assertResponseCount(JsonNode response, int iotCount, int valueCount) {
        if (iotCount == 0 || valueCount == 0) {
            Assertions.fail("trying to test empty response with exact matcher");
        }
        Assertions.assertEquals(iotCount,
                                response.get("@iot.count").asDouble(),
                                "@iot.id count is not " + iotCount + " although it should be");
        Assertions.assertFalse(
            response.get("value").isEmpty(),
            "Entity is not returned although it should"
        );
        Assertions.assertEquals(response.get("value").size(),
                                valueCount,
                                "value has invalid number of elements! Expected: "
                                    + valueCount
                                    + ", Actual: "
                                    + response.get("value").size());
    }

    default void assertResponseCount(JsonNode response, int count) {
        assertResponseCount(response, count, count);
    }

    enum EntityType {
        THING("Things"),
        LOCATION("Locations"),
        HISTORICAL_LOCATION("HistoricalLocations"),
        DATASTREAM("Datastreams"),
        SENSOR("Sensors"),
        FEATURE_OF_INTEREST("FeaturesOfInterest"),
        OBSERVATION("Observations"),
        OBSERVED_PROPERTY("ObservedProperties"),
        OBSERVATIONRELATION("ObservationRelations"),
        OBSERVATIONGROUP("ObservationGroups");

        String val;

        EntityType(String value) {
            val = value;
        }

        public static EntityType getByVal(String value) {
            for (EntityType entityType : EntityType.values()) {
                if (entityType.val.equals(value)) {
                    return entityType;
                }
            }
            return null;
        }

        public String getVal() {
            return val;
        }
    }

}
