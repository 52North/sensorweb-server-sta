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

package org.n52.sta.service;

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlThrowable;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.DatastreamSerDes;
import org.n52.sta.serdes.FeatureOfInterestSerDes;
import org.n52.sta.serdes.HistoricalLocationSerDes;
import org.n52.sta.serdes.LocationSerDes;
import org.n52.sta.serdes.ObservationSerDes;
import org.n52.sta.serdes.ObservedPropertySerDes;
import org.n52.sta.serdes.SensorSerDes;
import org.n52.sta.serdes.ThingSerDes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class STARequestUtils implements StaConstants {

    // Used for identifying/referencing source Entity Type
    // e.g. "Datastreams" in /Datastreams(52)/Thing
    public static final String GROUPNAME_SOURCE_NAME = "sourceName";

    // Used for identifying/referencing source Entity Id
    // e.g. "52" in /Datastreams(52)/Thing
    public static final String GROUPNAME_SOURCE_IDENTIFIER = "sourceId";

    // Used for identifying/referencing requested Entity Type
    // e.g. "Thing" in /Datastreams(52)/Thing
    public static final String GROUPNAME_WANTED_NAME = "wantedName";

    // Used for identifying/referencing requested Entity Type
    // e.g. "Thing" in /Datastreams(52)/Thing
    public static final String GROUPNAME_WANTED_IDENTIFIER = "wantedId";

    // Used for identifying/referencing $select option
    // e.g. "name" in /Datastreams(52)/Thing$select=name
    public static final String GROUPNAME_SELECT = "select";

    // Used for identifying/referencing source Entity Type
    // e.g. "name" in /Datastreams(52)/Thing/name
    public static final String GROUPNAME_PROPERTY = "property";

    protected static Map<String, Class> collectionNameToClass;
    protected static Map<String, Class> collectionNameToPatchClass;


    static final String MAPPING_PREFIX = "**/";
    static final String IDENTIFIER_REGEX = "(?:\\()['\\-0-9a-zA-Z]+(?:\\))";

    private static final String URL_INVALID = "Url is invalid. ";

    private static final String PATH_ENTITY = "{entity:";
    private static final String PATH_TARGET = "}/{target:";
    private static final String SLASH = "/";
    private static final String BACKSLASH = "\\";
    private static final String OR = "|";
    private static final String DOLLAR = "$";
    private static final String QUESTIONMARK = "?";
    private static final String PLUS = "+";
    private static final String LESS_THAN = "<";
    private static final String GREATER_THAN = ">";
    private static final String ROUND_BRACKET_CLOSE = ")";
    private static final String ROUND_BRACKET_OPEN = "(";
    private static final String SQUARE_BRACKET_CLOSE = "]";
    private static final String SQUARE_BRACKET_OPEN = "[";
    private static final String CURLY_BRACKET_OPEN = "{";
    private static final String CURLY_BRACKET_CLOSE = "}";

    // Used to mark start and end of named capturing groups
    private static final String SOURCE_NAME_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_SOURCE_NAME + GREATER_THAN;
    private static final String SOURCE_NAME_GROUP_END = ROUND_BRACKET_CLOSE;
    private static final String SOURCE_ID_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_SOURCE_IDENTIFIER + GREATER_THAN;
    private static final String SOURCE_ID_GROUP_END = ROUND_BRACKET_CLOSE;
    private static final String WANTED_NAME_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_WANTED_NAME + GREATER_THAN;
    private static final String WANTED_NAME_GROUP_END = ROUND_BRACKET_CLOSE;
    private static final String WANTED_ID_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_WANTED_IDENTIFIER + GREATER_THAN;
    private static final String WANTED_ID_GROUP_END = ROUND_BRACKET_CLOSE;
    private static final String SELECT_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_SELECT + GREATER_THAN;
    private static final String SELECT_GROUP_END = ROUND_BRACKET_CLOSE;
    private static final String PROPERTY_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_PROPERTY + GREATER_THAN;
    private static final String PROPERTY_GROUP_END = ROUND_BRACKET_CLOSE;

    private static final String IDENTIFIED_BY_DATASTREAM_REGEX =
            DATASTREAMS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN
                    + SENSOR + OR + OBSERVED_PROPERTY + OR + THING + OR + OBSERVATIONS + ROUND_BRACKET_CLOSE;

    private static final String IDENTIFIED_BY_OBSERVATION_REGEX =
            OBSERVATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + DATASTREAM + OR + FEATURE_OF_INTEREST +
                    ROUND_BRACKET_CLOSE;

    private static final String IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX =
            HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + SLASH + THING;

    private static final String IDENTIFIED_BY_THING_REGEX =
            THINGS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + DATASTREAMS
                    + OR + HISTORICAL_LOCATIONS + OR + LOCATIONS + ROUND_BRACKET_CLOSE;

    private static final String IDENTIFIED_BY_LOCATION_REGEX =
            LOCATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + THINGS + OR + HISTORICAL_LOCATIONS +
                    ROUND_BRACKET_CLOSE + CURLY_BRACKET_CLOSE;

    private static final String IDENTIFIED_BY_SENSOR_REGEX = SENSORS + IDENTIFIER_REGEX + SLASH + DATASTREAMS;

    private static final String IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX =
            OBSERVED_PROPERTIES + IDENTIFIER_REGEX + SLASH + DATASTREAMS;

    private static final String IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX =
            FEATURES_OF_INTEREST + IDENTIFIER_REGEX + SLASH + OBSERVATIONS;

    // /Datastreams(52)/Sensor
    // /Datastreams(52)/ObservedProperty
    // /Datastreams(52)/Thing
    private static final String ENTITY_IDENTIFIED_BY_DATASTREAM =
            SOURCE_NAME_GROUP_START + DATASTREAMS + SOURCE_NAME_GROUP_END
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + SENSOR + OR + OBSERVED_PROPERTY + OR + THING + WANTED_NAME_GROUP_END;

    static final String ENTITY_IDENTIFIED_BY_DATASTREAM_PATHVARIABLE =
            PATH_ENTITY + DATASTREAMS + IDENTIFIER_REGEX
                    + PATH_TARGET + SENSOR + OR + OBSERVED_PROPERTY + OR + THING + CURLY_BRACKET_CLOSE;

    // /Observations(52)/Datastream
    // /Observations(52)/FeatureOfInterest
    private static final String ENTITY_IDENTIFIED_BY_OBSERVATION =
            SOURCE_NAME_GROUP_START + OBSERVATIONS + SOURCE_NAME_GROUP_END
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + DATASTREAM + OR + FEATURE_OF_INTEREST + WANTED_NAME_GROUP_END;

    static final String ENTITY_IDENTIFIED_BY_OBSERVATION_PATHVARIABLE =
            PATH_ENTITY + OBSERVATIONS + IDENTIFIER_REGEX + PATH_TARGET + DATASTREAM + OR + FEATURE_OF_INTEREST +
                    CURLY_BRACKET_CLOSE;

    // /HistoricalLocations(52)/Thing
    private static final String ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION =
            SOURCE_NAME_GROUP_START + HISTORICAL_LOCATIONS + SOURCE_NAME_GROUP_END
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + THING + WANTED_NAME_GROUP_END;

    static final String ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATHVARIABLE =
            PATH_ENTITY + HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + PATH_TARGET + THING + CURLY_BRACKET_CLOSE;

    // /Datastream(52)/Observations
    private static final String COLLECTION_IDENTIFIED_BY_DATASTREAM =
            SOURCE_NAME_GROUP_START + DATASTREAMS + ROUND_BRACKET_CLOSE
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + OBSERVATIONS + WANTED_NAME_GROUP_END;

    static final String COLLECTION_IDENTIFIED_BY_DATASTREAM_PATHVARIABLE =
            PATH_ENTITY + DATASTREAMS + IDENTIFIER_REGEX + PATH_TARGET + OBSERVATIONS + CURLY_BRACKET_CLOSE;

    // /Things(52)/Datastreams
    // /Things(52)/HistoricalLocations
    // /Things(52)/Locations
    private static final String COLLECTION_IDENTIFIED_BY_THING =
            SOURCE_NAME_GROUP_START + THINGS + SOURCE_NAME_GROUP_END
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + DATASTREAMS + OR + HISTORICAL_LOCATIONS + OR + LOCATIONS +
                    WANTED_NAME_GROUP_END;

    static final String COLLECTION_IDENTIFIED_BY_THING_PATHVARIABLE =
            PATH_ENTITY + THINGS + IDENTIFIER_REGEX + PATH_TARGET
                    + DATASTREAMS + OR + HISTORICAL_LOCATIONS + OR + LOCATIONS + CURLY_BRACKET_CLOSE;

    // /Locations(52)/Things
    // /Locations(52)/HistoricalLocations
    private static final String COLLECTION_IDENTIFIED_BY_LOCATION =
            SOURCE_NAME_GROUP_START + LOCATIONS + SOURCE_NAME_GROUP_END
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + THINGS + OR + HISTORICAL_LOCATIONS + WANTED_NAME_GROUP_END;

    static final String COLLECTION_IDENTIFIED_BY_LOCATION_PATHVARIABLE =
            PATH_ENTITY + LOCATIONS + IDENTIFIER_REGEX + PATH_TARGET + THINGS + OR + HISTORICAL_LOCATIONS +
                    CURLY_BRACKET_CLOSE;

    // /Sensors(52)/Datastreams
    private static final String COLLECTION_IDENTIFIED_BY_SENSOR =
            SOURCE_NAME_GROUP_START + SENSORS + SOURCE_NAME_GROUP_END
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + DATASTREAMS + WANTED_NAME_GROUP_END;

    static final String COLLECTION_IDENTIFIED_BY_SENSOR_PATHVARIABLE =
            PATH_ENTITY + SENSORS + IDENTIFIER_REGEX + PATH_TARGET + DATASTREAMS + CURLY_BRACKET_CLOSE;

    // /ObservedProperties(52)/Datastreams
    private static final String COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY =
            SOURCE_NAME_GROUP_START + OBSERVED_PROPERTIES + SOURCE_NAME_GROUP_END
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + DATASTREAMS + WANTED_NAME_GROUP_END;

    static final String COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATHVARIABLE =
            PATH_ENTITY + OBSERVED_PROPERTIES + IDENTIFIER_REGEX + PATH_TARGET + DATASTREAMS + CURLY_BRACKET_CLOSE;

    // /FeaturesOfInterest(52)/Observations
    private static final String COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST =
            SOURCE_NAME_GROUP_START + FEATURES_OF_INTEREST + SOURCE_NAME_GROUP_END
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + OBSERVATIONS + WANTED_NAME_GROUP_END;

    static final String COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST_PATHVARIABLE =
            PATH_ENTITY + FEATURES_OF_INTEREST + IDENTIFIER_REGEX + PATH_TARGET + OBSERVATIONS + CURLY_BRACKET_CLOSE;

    static final String BASE_COLLECTION_REGEX =
            WANTED_NAME_GROUP_START
                    + OBSERVATIONS + OR
                    + DATASTREAMS + OR
                    + THINGS + OR
                    + SENSORS + OR
                    + LOCATIONS + OR
                    + HISTORICAL_LOCATIONS + OR
                    + FEATURES_OF_INTEREST + OR
                    + OBSERVED_PROPERTIES
                    + WANTED_NAME_GROUP_END;

    private static final String SELECT_REGEX_NAMED_GROUPS =
            BACKSLASH + QUESTIONMARK + BACKSLASH + DOLLAR + "select=" +
                    SELECT_GROUP_START + SQUARE_BRACKET_OPEN + BACKSLASH + "w" + BACKSLASH + "," +
                    SQUARE_BRACKET_CLOSE + PLUS + SELECT_GROUP_END;

    private static final String PROPERTY_REGEX_NAMED_GROUPS =
            SLASH + PROPERTY_GROUP_START + SQUARE_BRACKET_OPEN + "A-z," + SQUARE_BRACKET_CLOSE + PLUS +
                    PROPERTY_GROUP_END;

    // Patterns used for matching Paths in mqtt with named groups
    // Java does not support duplicate names so patterns are handled separately
    // OGC-15-078r6 14.2.1
    private final Pattern collectionPattern1 = Pattern.compile(BASE_COLLECTION_REGEX + DOLLAR);
    private final Pattern collectionPattern2 = Pattern.compile(COLLECTION_IDENTIFIED_BY_DATASTREAM + DOLLAR);
    private final Pattern collectionPattern3 = Pattern.compile(COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST + DOLLAR);
    private final Pattern collectionPattern4 = Pattern.compile(COLLECTION_IDENTIFIED_BY_LOCATION + DOLLAR);
    private final Pattern collectionPattern5 = Pattern.compile(COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY + DOLLAR);
    private final Pattern collectionPattern6 = Pattern.compile(COLLECTION_IDENTIFIED_BY_SENSOR + DOLLAR);
    private final Pattern collectionPattern7 = Pattern.compile(COLLECTION_IDENTIFIED_BY_THING + DOLLAR);

    protected final Pattern[] namedCollectionPatterns =
            new Pattern[] {
                    collectionPattern1,
                    collectionPattern2,
                    collectionPattern3,
                    collectionPattern4,
                    collectionPattern5,
                    collectionPattern6,
                    collectionPattern7,
            };

    // OGC-15-078r6 14.2.2
    private final Pattern entityPattern1 = Pattern.compile(
            BASE_COLLECTION_REGEX + WANTED_ID_GROUP_START + IDENTIFIER_REGEX + WANTED_ID_GROUP_END + DOLLAR);
    private final Pattern entityPattern2 = Pattern.compile(ENTITY_IDENTIFIED_BY_DATASTREAM + DOLLAR);
    private final Pattern entityPattern3 = Pattern.compile(ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION + DOLLAR);
    private final Pattern entityPattern4 = Pattern.compile(ENTITY_IDENTIFIED_BY_OBSERVATION + DOLLAR);

    protected final Pattern[] namedEntityPatterns =
            new Pattern[] {
                    entityPattern1,
                    entityPattern2,
                    entityPattern3,
                    entityPattern4
            };

    // OGC-15-078r6 14.2.3
    private final Pattern propertyPattern1 = Pattern.compile(
            BASE_COLLECTION_REGEX + WANTED_ID_GROUP_START + IDENTIFIER_REGEX + WANTED_ID_GROUP_END +
                    PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);
    private final Pattern propertyPattern2 =
            Pattern.compile(ENTITY_IDENTIFIED_BY_DATASTREAM + PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);
    private final Pattern propertyPattern3 =
            Pattern.compile(ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION + PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);
    private final Pattern propertyPattern4 =
            Pattern.compile(ENTITY_IDENTIFIED_BY_OBSERVATION + PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);

    protected final Pattern[] namedPropertyPatterns =
            new Pattern[] {
                    propertyPattern1,
                    propertyPattern2,
                    propertyPattern3,
                    propertyPattern4
            };

    // OGC-15-078r6 14.2.4
    private final Pattern namedSelectPattern1 =
            Pattern.compile(BASE_COLLECTION_REGEX + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    private final Pattern namedSelectPattern2 =
            Pattern.compile(COLLECTION_IDENTIFIED_BY_DATASTREAM + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    private final Pattern namedSelectPattern3 =
            Pattern.compile(COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    private final Pattern namedSelectPattern4 =
            Pattern.compile(COLLECTION_IDENTIFIED_BY_LOCATION + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    private final Pattern namedSelectPattern5 =
            Pattern.compile(COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    private final Pattern namedSelectPattern6 =
            Pattern.compile(COLLECTION_IDENTIFIED_BY_SENSOR + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    private final Pattern namedSelectPattern7 =
            Pattern.compile(COLLECTION_IDENTIFIED_BY_THING + SELECT_REGEX_NAMED_GROUPS + DOLLAR);

    protected final Pattern[] namedSelectPatterns =
            new Pattern[] {
                    namedSelectPattern1,
                    namedSelectPattern2,
                    namedSelectPattern3,
                    namedSelectPattern4,
                    namedSelectPattern5,
                    namedSelectPattern6,
                    namedSelectPattern7,
            };

    static {
        HashMap<String, Class> map = new HashMap<>();
        map.put(THINGS, PlatformEntity.class);
        map.put(LOCATIONS, LocationEntity.class);
        map.put(DATASTREAMS, DatastreamEntity.class);
        map.put(HISTORICAL_LOCATIONS, HistoricalLocationEntity.class);
        map.put(SENSORS, SensorEntity.class);
        map.put(OBSERVATIONS, DataEntity.class);
        map.put(OBSERVED_PROPERTIES, PhenomenonEntity.class);
        map.put(FEATURES_OF_INTEREST, AbstractFeatureEntity.class);
        collectionNameToClass = Collections.unmodifiableMap(map);

        HashMap<String, Class> patchMap = new HashMap<>();
        patchMap.put(THINGS, ThingSerDes.PlatformEntityPatch.class);
        patchMap.put(LOCATIONS, LocationSerDes.LocationEntityPatch.class);
        patchMap.put(DATASTREAMS, DatastreamSerDes.DatastreamEntityPatch.class);
        patchMap.put(HISTORICAL_LOCATIONS, HistoricalLocationSerDes.HistoricalLocationEntityPatch.class);
        patchMap.put(SENSORS, SensorSerDes.SensorEntityPatch.class);
        patchMap.put(OBSERVATIONS, ObservationSerDes.StaDataEntityPatch.class);
        patchMap.put(OBSERVED_PROPERTIES, ObservedPropertySerDes.PhenomenonEntityPatch.class);
        patchMap.put(FEATURES_OF_INTEREST, FeatureOfInterestSerDes.AbstractFeatureEntityPatch.class);

        patchMap.put(THING, ThingSerDes.PlatformEntityPatch.class);
        patchMap.put(LOCATION, LocationSerDes.LocationEntityPatch.class);
        patchMap.put(DATASTREAM, DatastreamSerDes.DatastreamEntityPatch.class);
        patchMap.put(HISTORICAL_LOCATION, HistoricalLocationSerDes.HistoricalLocationEntityPatch.class);
        patchMap.put(SENSOR, SensorSerDes.SensorEntityPatch.class);
        patchMap.put(OBSERVATION, ObservationSerDes.StaDataEntityPatch.class);
        patchMap.put(OBSERVED_PROPERTY, ObservedPropertySerDes.PhenomenonEntityPatch.class);
        patchMap.put(FEATURE_OF_INTEREST, FeatureOfInterestSerDes.AbstractFeatureEntityPatch.class);
        collectionNameToPatchClass = Collections.unmodifiableMap(patchMap);
    }

    private final Pattern byIdPattern =
            Pattern.compile(ROUND_BRACKET_OPEN + BASE_COLLECTION_REGEX + ROUND_BRACKET_CLOSE + IDENTIFIER_REGEX);
    private final Pattern byDatastreamPattern = Pattern.compile(IDENTIFIED_BY_DATASTREAM_REGEX);
    private final Pattern byObservationPattern = Pattern.compile(IDENTIFIED_BY_OBSERVATION_REGEX);
    private final Pattern byHistoricalLocationPattern = Pattern.compile(IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX);
    private final Pattern byLocationPattern = Pattern.compile(IDENTIFIED_BY_LOCATION_REGEX);
    private final Pattern byThingPattern = Pattern.compile(IDENTIFIED_BY_THING_REGEX);
    private final Pattern bySensorsPattern = Pattern.compile(IDENTIFIED_BY_SENSOR_REGEX);
    private final Pattern byObservedPropertiesPattern = Pattern.compile(IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX);
    private final Pattern byFeaturesOfInterestPattern = Pattern.compile(IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX);

    public STAInvalidUrlThrowable validateURL(StringBuffer requestURL,
                                              EntityServiceRepository serviceRepository,
                                              int rootUrlLength) throws STAInvalidUrlThrowable {
        String[] uriResources = requestURL.substring(rootUrlLength).split(SLASH);

        STAInvalidUrlThrowable ex;
        ex = validateURISyntax(uriResources);
        if (ex != null) {
            throw ex;
        }
        ex = validateURISemantic(uriResources, serviceRepository);
        if (ex != null) {
            throw ex;
        }
        return null;
    }

    /**
     * Validates a given URI syntactically.
     *
     * @param uriResources URI of the Request split by SLASH
     * @return STAInvalidUrlException if URI is malformed
     */
    private STAInvalidUrlThrowable validateURISyntax(String[] uriResources) {
        // Validate URL syntax via Regex
        // Skip validation if no navigationPath is provided as Spring already validated syntax
        if (uriResources.length > 1) {
            // check iteratively and fail-fast
            for (int i = 0; i < uriResources.length; i++) {
                if (!byIdPattern.matcher(uriResources[i]).matches()) {
                    // Resource is addressed by relation to other entity
                    // e.g. Datastreams(1)/Thing
                    if (i > 0) {
                        // Look back at last resource and check if association is valid
                        String resource = uriResources[i - 1] + SLASH + uriResources[i];
                        if (!(byDatastreamPattern.matcher(resource).matches()
                                || byHistoricalLocationPattern.matcher(resource).matches()
                                || byLocationPattern.matcher(resource).matches()
                                || byThingPattern.matcher(resource).matches()
                                || byFeaturesOfInterestPattern.matcher(resource).matches()
                                || byObservationPattern.matcher(resource).matches()
                                || bySensorsPattern.matcher(resource).matches()
                                || byObservedPropertiesPattern.matcher(resource).matches())) {
                            return new STAInvalidUrlThrowable(URL_INVALID
                                                                      + uriResources[i - 1]
                                                                      + SLASH + uriResources[i]
                                                                      + " is not a valid resource path.");

                        }
                    } else {
                        return new STAInvalidUrlThrowable(URL_INVALID
                                                                  + uriResources[i]
                                                                  + " is not a valid resource.");
                    }
                }
                // Resource is adressed by Id
                // e.g. Things(1), no processing required
            }
        }
        return null;
    }

    /**
     * This function validates a given URI semantically by checking if all Entities referenced in the navigation
     * exists. As URI is syntactically valid indices can be hard-coded.
     *
     * @param uriResources URI of the Request split by SLASH
     * @return STAInvalidUrlException if URI is malformed
     */
    private STAInvalidUrlThrowable validateURISemantic(String[] uriResources,
                                                       EntityServiceRepository serviceRepository) {
        // Check if this is Request to root collection. They are always valid
        if (uriResources.length == 1 && !uriResources[0].contains(ROUND_BRACKET_OPEN)) {
            return null;
        }
        // Parse first navigation Element
        String[] sourceEntity = splitId(uriResources[0]);
        String sourceId = sourceEntity[1].replace(ROUND_BRACKET_CLOSE, "");
        String sourceType = sourceEntity[0];

        if (!serviceRepository.getEntityService(sourceType).existsEntity(sourceId)) {
            return createInvalidUrlExceptionNoEntit(uriResources[0]);
        }

        // Iterate over the rest of the uri validating each resource
        for (int i = 1, uriResourcesLength = uriResources.length; i < uriResourcesLength; i++) {
            String[] targetEntity = splitId(uriResources[i]);
            String targetType = targetEntity[0];
            String targetId = null;
            if (targetEntity.length == 1) {
                // Resource is addressed by related Entity
                // e.g. /Datastreams(1)/Thing/
                // Getting id directly as it is needed for next iteration
                targetId = serviceRepository.getEntityService(targetType)
                                            .getEntityIdByRelatedEntity(sourceId, sourceType);
                if (targetId == null) {
                    return createInvalidUrlExceptionNoEntitAssociated(uriResources[i], uriResources[i - 1]);
                }
            } else {
                // Resource is addressed by Id directly
                // e.g. /Things(1)/
                // Only checking exists as Id is already known
                targetId = targetEntity[1].replace(ROUND_BRACKET_CLOSE, "");
                if (!serviceRepository.getEntityService(targetType)
                                      .existsEntityByRelatedEntity(sourceId, sourceType, targetId)) {
                    return createInvalidUrlExceptionNoEntitAssociated(uriResources[i], uriResources[i - 1]);
                }
            }

            // Store target as source for next iteration
            sourceId = targetId;
            sourceType = targetType;
        }
        // As no error is thrown the uri is valid
        return null;
    }

    private STAInvalidUrlThrowable createInvalidUrlExceptionNoEntit(String entity) {
        return new STAInvalidUrlThrowable("No Entity: " + entity + " found!");
    }

    private STAInvalidUrlThrowable createInvalidUrlExceptionNoEntitAssociated(String first, String last) {
        return createInvalidUrlExceptionNoEntit(first + " associated with " + last);
    }

    protected static String[] splitId(String entity) {
        return entity.split("\\(");
    }

}
