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
package org.n52.sta.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class TestUtil {

    private static final GeometryFactory factory =
            new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    private static HashMap<String, String> relatedEntityEndpoints = new HashMap<String, String>() {{
        put(Conformance2.EntityType.THING.toString() + Conformance2.EntityType.DATASTREAM.toString(),
                "Things(%s)/Datastreams");
        put(Conformance2.EntityType.THING.toString() + Conformance2.EntityType.LOCATION.toString(),
                "Things(%s)/Locations");
        put(Conformance2.EntityType.THING.toString() + Conformance2.EntityType.HISTORICAL_LOCATION.toString(),
                "Things(%s)/HistoricalLocations");

        put(Conformance2.EntityType.LOCATION.toString() + Conformance2.EntityType.HISTORICAL_LOCATION.toString(),
                "Locations(%s)/HistoricalLocations");
        put(Conformance2.EntityType.LOCATION.toString() + Conformance2.EntityType.THING.toString(),
                "Locations(%s)/Things");

        put(Conformance2.EntityType.HISTORICAL_LOCATION.toString() + Conformance2.EntityType.THING.toString(),
                "HistoricalLocations(%s)/Thing");
        put(Conformance2.EntityType.HISTORICAL_LOCATION.toString() + Conformance2.EntityType.LOCATION.toString(),
                "HistoricalLocations(%s)/Locations");

        put(Conformance2.EntityType.DATASTREAM.toString() + Conformance2.EntityType.THING.toString(),
                "Datastreams(%s)/Thing");
        put(Conformance2.EntityType.DATASTREAM.toString() + Conformance2.EntityType.SENSOR.toString(),
                "Datastreams(%s)/Sensor");
        put(Conformance2.EntityType.DATASTREAM.toString() + Conformance2.EntityType.OBSERVED_PROPERTY.toString(),
                "Datastreams(%s)/ObservedProperty");
        put(Conformance2.EntityType.DATASTREAM.toString() + Conformance2.EntityType.OBSERVATION.toString(),
                "Datastreams(%s)/Observations");

        put(Conformance2.EntityType.SENSOR.toString() + Conformance2.EntityType.DATASTREAM.toString(),
                "Sensors(%s)/Datastreams");

        put(Conformance2.EntityType.OBSERVED_PROPERTY.toString() + Conformance2.EntityType.DATASTREAM.toString(),
                "ObservedProperties(%s)/Datastreams");

        put(Conformance2.EntityType.OBSERVATION.toString() + Conformance2.EntityType.DATASTREAM.toString(),
                "Observations(%s)/Datastream");
        put(Conformance2.EntityType.OBSERVATION.toString() + Conformance2.EntityType.FEATURE_OF_INTEREST.toString(),
                "Observations(%s)/FeatureOfInterest");

        put(Conformance2.EntityType.FEATURE_OF_INTEREST.toString() + Conformance2.EntityType.OBSERVATION.toString(),
                "FeaturesOfInterest(%s)/Observations");

    }};

    public static String getRelatedEntityEndpoint(Conformance2.EntityType source, Conformance2.EntityType target) {
        return relatedEntityEndpoints.get(source.toString() + target.toString());
    }

    public static void compareJsonNodes(JsonNode reference, JsonNode actual) {
        for (Iterator<Map.Entry<String, JsonNode>> it = reference.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> next = it.next();
            System.out.println(next.getKey());
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
                        compareJsonNodesString(next.getKey(), next.getValue(), actual.get(next.getKey()));
                    }
                    break;
            }

        }
    }

    public static void compareJsonNodesGeo(String fieldname, JsonNode referenceValue, JsonNode actualValue) {
        Assert.assertNotNull(
                "Reference Entity is nonexistent!",
                referenceValue);
        Assert.assertNotNull(
                "Actual Entity is nonexistent!",
                actualValue);
        // GeoJson comparison
        GeoJsonReader reader = new GeoJsonReader(factory);
        try {
            Geometry referenceGeo, actualGeo;
            referenceGeo = reader.read(referenceValue.toString());
            actualGeo = reader.read(actualValue.toString());
            Assert.assertTrue(
                    "ERROR: Deep inserted " + fieldname + " is not created correctly."
                            + "Reference: " + referenceValue.toPrettyString()
                            + "Actual: " + actualValue.toPrettyString(),
                    referenceGeo.equals(actualGeo));
        } catch (ParseException e) {
            org.springframework.util.Assert.notNull(null,
                    "Could not parse to GeoJSON. Error was:" + e.getMessage());
        }
    }

    public static void compareJsonNodesTime(String fieldname, JsonNode reference, JsonNode actual) {
        Assert.assertNotNull(
                "Reference Entity is nonexistent!",
                reference);
        Assert.assertNotNull(
                "Actual Entity is nonexistent!",
                actual);
        String referenceTimeValue, actualTimeValue;
        // Time comparison
        referenceTimeValue = reference.toString().replace("\"", "");
        actualTimeValue = actual.toString().replace("\"", "");
        if (referenceTimeValue.equals("null")) {
            Assert.assertEquals(
                    "The " + fieldname + " should have been \""
                            + "null"
                            + "\" but it is now \""
                            + actualTimeValue
                            + "\".",
                    "null",
                    actualTimeValue
                    );
        } else {
            DateTime referenceTime = ISODateTimeFormat.dateTimeParser().parseDateTime(referenceTimeValue);
            DateTime actualTime = ISODateTimeFormat.dateTimeParser().parseDateTime(actualTimeValue);
            Assert.assertEquals(
                    "The " + fieldname + " should have been \""
                            + referenceTime.toString()
                            + "\" but it is now \""
                            + actualTime.toString()
                            + "\".",
                    actualTime,
                    referenceTime);
        }
    }

    public static void compareJsonNodesString(String fieldname, JsonNode reference, JsonNode actual) {
        Assert.assertNotNull(
                "Reference Entity is nonexistent!",
                reference);
        Assert.assertNotNull(
                "Actual Entity is nonexistent!",
                actual);
        // Simple String comparison
        String referenceValue, actualValue;
        referenceValue = reference.toString().replace("\"", "");
        actualValue = actual.toString().replace("\"", "");
        Assert.assertEquals(
                "ERROR: Deep inserted " + fieldname + " is not created correctly.",
                referenceValue,
                actualValue);
    }
}
