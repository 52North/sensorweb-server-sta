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

package org.n52.sta.utils;

import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;

import java.util.regex.Pattern;

public interface CoreRequestUtils extends RequestUtils {

    String BASE_COLLECTION_REGEX_NAMED_GROUPS =
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

    String BASE_COLLECTION_REGEX =
        OBSERVATIONS + OR
            + DATASTREAMS + OR
            + THINGS + OR
            + SENSORS + OR
            + LOCATIONS + OR
            + HISTORICAL_LOCATIONS + OR
            + FEATURES_OF_INTEREST + OR
            + OBSERVED_PROPERTIES;

    String IDENTIFIED_BY_DATASTREAM_REGEX =
        DATASTREAMS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN
            + SENSOR + OR + OBSERVED_PROPERTY + OR + THING + OR + OBSERVATIONS + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_OBSERVATION_REGEX =
        OBSERVATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + DATASTREAM + OR + FEATURE_OF_INTEREST +
            ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX =
        HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + THING + OR + LOCATIONS +
            ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_THING_REGEX =
        THINGS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + DATASTREAMS
            + OR + HISTORICAL_LOCATIONS + OR + LOCATIONS + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_LOCATION_REGEX =
        LOCATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + THINGS + OR + HISTORICAL_LOCATIONS +
            ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_SENSOR_REGEX = SENSORS + IDENTIFIER_REGEX + SLASH + DATASTREAMS;

    String IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX =
        OBSERVED_PROPERTIES + IDENTIFIER_REGEX + SLASH + DATASTREAMS;

    String IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX =
        FEATURES_OF_INTEREST + IDENTIFIER_REGEX + SLASH + OBSERVATIONS;

    // /Datastreams(52)
    String ENTITY_IDENTIFIED_DIRECTLY =
        PATH_ENTITY + BASE_COLLECTION_REGEX + CURLY_BRACKET_CLOSE + PATH_ID + IDENTIFIER_REGEX + DOLLAR +
            CURLY_BRACKET_CLOSE;

    // /Datastreams(52)/Sensor
    // /Datastreams(52)/ObservedProperty
    // /Datastreams(52)/Thing
    String ENTITY_IDENTIFIED_BY_DATASTREAM =
        SOURCE_NAME_GROUP_START + DATASTREAMS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + SENSOR + OR + OBSERVED_PROPERTY + OR + THING + WANTED_NAME_GROUP_END;

    String ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE =
        PATH_ENTITY + DATASTREAMS + IDENTIFIER_REGEX
            + CURLY_BRACKET_CLOSE + PATH_TARGET + SENSOR + OR + OBSERVED_PROPERTY + OR + THING +
            CURLY_BRACKET_CLOSE;

    String ENTITY_PROPERTY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE =
        ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE + SLASH + PATH_PROPERTY;

    // /Observations(52)/Datastream
    // /Observations(52)/FeatureOfInterest
    String ENTITY_IDENTIFIED_BY_OBSERVATION =
        SOURCE_NAME_GROUP_START + OBSERVATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + DATASTREAM + OR + FEATURE_OF_INTEREST + WANTED_NAME_GROUP_END;

    String ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE =
        PATH_ENTITY + OBSERVATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET + DATASTREAM + OR +
            FEATURE_OF_INTEREST +
            CURLY_BRACKET_CLOSE;

    String ENTITY_PROPERTY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE =
        ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE + SLASH + PATH_PROPERTY;

    // /HistoricalLocations(52)/Thing
    String ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION =
        SOURCE_NAME_GROUP_START + HISTORICAL_LOCATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + THING + WANTED_NAME_GROUP_END;

    String ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE =
        PATH_ENTITY + HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET + THING +
            CURLY_BRACKET_CLOSE;

    String ENTITY_PROPERTY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE =
        ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE + SLASH + PATH_PROPERTY;

    // /Datastream(52)/Observations
    String COLLECTION_IDENTIFIED_BY_DATASTREAM =
        SOURCE_NAME_GROUP_START + DATASTREAMS + ROUND_BRACKET_CLOSE
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + OBSERVATIONS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE =
        PATH_ENTITY + DATASTREAMS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET + OBSERVATIONS +
            CURLY_BRACKET_CLOSE;

    // /Things(52)/Datastreams
    // /Things(52)/HistoricalLocations
    // /Things(52)/Locations
    String COLLECTION_IDENTIFIED_BY_THING =
        SOURCE_NAME_GROUP_START + THINGS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + DATASTREAMS + OR + HISTORICAL_LOCATIONS + OR + LOCATIONS +
            WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_THING_PATH_VARIABLE =
        PATH_ENTITY + THINGS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET
            + DATASTREAMS + OR + HISTORICAL_LOCATIONS + OR + LOCATIONS + CURLY_BRACKET_CLOSE;

    // /Locations(52)/Things
    // /Locations(52)/HistoricalLocations
    String COLLECTION_IDENTIFIED_BY_LOCATION =
        SOURCE_NAME_GROUP_START + LOCATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + THINGS + OR + HISTORICAL_LOCATIONS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_LOCATION_PATH_VARIABLE =
        PATH_ENTITY + LOCATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET + THINGS + OR +
            HISTORICAL_LOCATIONS +
            CURLY_BRACKET_CLOSE;

    // /Sensors(52)/Datastreams
    String COLLECTION_IDENTIFIED_BY_SENSOR =
        SOURCE_NAME_GROUP_START + SENSORS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + DATASTREAMS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_SENSOR_PATH_VARIABLE =
        PATH_ENTITY + SENSORS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET + DATASTREAMS +
            CURLY_BRACKET_CLOSE;

    // /ObservedProperties(52)/Datastreams
    String COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY =
        SOURCE_NAME_GROUP_START + OBSERVED_PROPERTIES + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + DATASTREAMS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH_VARIABLE =
        PATH_ENTITY + OBSERVED_PROPERTIES + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET + DATASTREAMS +
            CURLY_BRACKET_CLOSE;

    // /FeaturesOfInterest(52)/Observations
    String COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST =
        SOURCE_NAME_GROUP_START + FEATURES_OF_INTEREST + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + OBSERVATIONS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH_VARIABLE =
        PATH_ENTITY + FEATURES_OF_INTEREST + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET + OBSERVATIONS +
            CURLY_BRACKET_CLOSE;

    // /HistoricalLocations(52)/Locations
    String COLLECTION_IDENTIFIED_BY_HIST_LOCATION =
        SOURCE_NAME_GROUP_START + HISTORICAL_LOCATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + LOCATIONS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_HIST_LOCATION_PATH_VARIABLE =
        PATH_ENTITY + HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET + LOCATIONS +
            CURLY_BRACKET_CLOSE;

    // Patterns used for matching Paths in mqtt with named groups
    // Java does not support duplicate names so patterns are handled separately
    // OGC-15-078r6 14.2.1
    Pattern CP_BASE = Pattern.compile(BASE_COLLECTION_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern CP_IDENT_BY_DATASTREAM = Pattern.compile(COLLECTION_IDENTIFIED_BY_DATASTREAM + DOLLAR);
    Pattern CP_IDENT_BY_FOI = Pattern.compile(COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST + DOLLAR);
    Pattern CP_IDENT_BY_LOCATION = Pattern.compile(COLLECTION_IDENTIFIED_BY_LOCATION + DOLLAR);
    Pattern CP_IDENT_BY_OBS_PROP = Pattern.compile(COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY + DOLLAR);
    Pattern CP_IDENT_BY_SENSOR = Pattern.compile(COLLECTION_IDENTIFIED_BY_SENSOR + DOLLAR);
    Pattern CP_IDENT_BY_THING = Pattern.compile(COLLECTION_IDENTIFIED_BY_THING + DOLLAR);
    Pattern CP_IDENT_BY_HIST_LOCATION = Pattern.compile(COLLECTION_IDENTIFIED_BY_HIST_LOCATION + DOLLAR);

    Pattern[] NAMED_COLL_PATTERNS =
        new Pattern[] {
            CP_BASE,
            CP_IDENT_BY_DATASTREAM,
            CP_IDENT_BY_FOI,
            CP_IDENT_BY_LOCATION,
            CP_IDENT_BY_OBS_PROP,
            CP_IDENT_BY_SENSOR,
            CP_IDENT_BY_THING,
            CP_IDENT_BY_HIST_LOCATION,
        };

    // OGC-15-078r6 14.2.2
    Pattern EP_BASE = Pattern.compile(
        BASE_COLLECTION_REGEX_NAMED_GROUPS + WANTED_ID_GROUP_START + IDENTIFIER_REGEX + WANTED_ID_GROUP_END +
            DOLLAR);
    Pattern EP_IDENT_BY_DATASTREAM = Pattern.compile(ENTITY_IDENTIFIED_BY_DATASTREAM + DOLLAR);
    Pattern EP_IDENT_BY_HIST_LOC = Pattern.compile(ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION + DOLLAR);
    Pattern EP_IDENT_BY_OBSERVATION = Pattern.compile(ENTITY_IDENTIFIED_BY_OBSERVATION + DOLLAR);

    Pattern[] NAMED_ENTITY_PATTERNS =
        new Pattern[] {
            EP_BASE,
            EP_IDENT_BY_DATASTREAM,
            EP_IDENT_BY_HIST_LOC,
            EP_IDENT_BY_OBSERVATION,
        };

    // OGC-15-078r6 14.2.3
    Pattern PROP_PATTERN_BASE = Pattern.compile(
        BASE_COLLECTION_REGEX_NAMED_GROUPS + WANTED_ID_GROUP_START + IDENTIFIER_REGEX + WANTED_ID_GROUP_END +
            PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern PROP_PATTERN_IDENT_BY_DATASTREAM =
        Pattern.compile(ENTITY_IDENTIFIED_BY_DATASTREAM + PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern PROP_PATTERN_IDENT_BY_HIST_LOC =
        Pattern.compile(ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION + PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern PROP_PATTERN_IDENT_BY_OBSERVATION =
        Pattern.compile(ENTITY_IDENTIFIED_BY_OBSERVATION + PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);

    Pattern[] NAMED_PROP_PATTERNS =
        new Pattern[] {
            PROP_PATTERN_BASE,
            PROP_PATTERN_IDENT_BY_DATASTREAM,
            PROP_PATTERN_IDENT_BY_HIST_LOC,
            PROP_PATTERN_IDENT_BY_OBSERVATION,
        };

    // OGC-15-078r6 14.2.4
    Pattern NAMED_SELECT_PATTER_BASE =
        Pattern.compile(BASE_COLLECTION_REGEX_NAMED_GROUPS + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern NAMED_SELECT_PATTER_IDENT_BY_DATASTREAM =
        Pattern.compile(COLLECTION_IDENTIFIED_BY_DATASTREAM + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern NAMED_SELECT_PATTER_IDENT_BY_FOI =
        Pattern.compile(COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern NAMED_SELECT_PATTER_IDENT_BY_LOCATION =
        Pattern.compile(COLLECTION_IDENTIFIED_BY_LOCATION + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern NAMED_SELECT_PATTER_IDENT_BY_OBS_PROP =
        Pattern.compile(COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern NAMED_SELECT_PATTER_IDENT_BY_SENSOR =
        Pattern.compile(COLLECTION_IDENTIFIED_BY_SENSOR + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern NAMED_SELECT_PATTER_IDENT_BY_THING =
        Pattern.compile(COLLECTION_IDENTIFIED_BY_THING + SELECT_REGEX_NAMED_GROUPS + DOLLAR);

    Pattern[] NAMED_SELECT_PATTERNS =
        new Pattern[] {
            NAMED_SELECT_PATTER_BASE,
            NAMED_SELECT_PATTER_IDENT_BY_DATASTREAM,
            NAMED_SELECT_PATTER_IDENT_BY_FOI,
            NAMED_SELECT_PATTER_IDENT_BY_LOCATION,
            NAMED_SELECT_PATTER_IDENT_BY_OBS_PROP,
            NAMED_SELECT_PATTER_IDENT_BY_SENSOR,
            NAMED_SELECT_PATTER_IDENT_BY_THING,
        };

    Pattern BY_ID_PATTERN =
        Pattern.compile(
            ROUND_BRACKET_OPEN + BASE_COLLECTION_REGEX_NAMED_GROUPS + ROUND_BRACKET_CLOSE + IDENTIFIER_REGEX);
    Pattern BY_DATASTREAM_PATTERN = Pattern.compile(IDENTIFIED_BY_DATASTREAM_REGEX);
    Pattern BY_OBSERVATION_PATTERN = Pattern.compile(IDENTIFIED_BY_OBSERVATION_REGEX);
    Pattern BY_HIST_LOC_PATTERN = Pattern.compile(IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX);
    Pattern BY_LOCATION_PATTERN = Pattern.compile(IDENTIFIED_BY_LOCATION_REGEX);
    Pattern BY_THING_PATTERN = Pattern.compile(IDENTIFIED_BY_THING_REGEX);
    Pattern BY_SENSOR_PATTERN = Pattern.compile(IDENTIFIED_BY_SENSOR_REGEX);
    Pattern BY_OBSER_PROP_PATTERN = Pattern.compile(IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX);
    Pattern BY_FOI_PATTERN = Pattern.compile(IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX);

    /**
     * Validates a given URI syntactically.
     *
     * @param uriResources URI of the Request split by SLASH
     * @return STAInvalidUrlException if URI is malformed
     */
    default STAInvalidUrlException validateURISyntax(String[] uriResources) {
        // Validate URL syntax via Regex
        // Skip validation if no navigationPath is provided as Spring already validated syntax
        if (uriResources.length > 1) {
            // check iteratively and fail-fast
            for (int i = 0; i < uriResources.length; i++) {
                if (!BY_ID_PATTERN.matcher(uriResources[i]).matches()) {
                    // Resource is addressed by relation to other entity
                    // e.g. Datastreams(1)/Thing
                    if (i > 0) {
                        // Look back at last resource and check if association is valid
                        String resource = uriResources[i - 1] + SLASH + uriResources[i];
                        if (!(BY_DATASTREAM_PATTERN.matcher(resource).matches()
                            || BY_HIST_LOC_PATTERN.matcher(resource).matches()
                            || BY_LOCATION_PATTERN.matcher(resource).matches()
                            || BY_THING_PATTERN.matcher(resource).matches()
                            || BY_FOI_PATTERN.matcher(resource).matches()
                            || BY_OBSERVATION_PATTERN.matcher(resource).matches()
                            || BY_OBSER_PROP_PATTERN.matcher(resource).matches()
                            || BY_SENSOR_PATTERN.matcher(resource).matches()
                            || BY_OBSER_PROP_PATTERN.matcher(resource).matches())) {
                            return new STAInvalidUrlException(URL_INVALID
                                                                  + uriResources[i - 1]
                                                                  + SLASH + uriResources[i]
                                                                  + " is not a valid resource path.");

                        }
                    } else {
                        return new STAInvalidUrlException(URL_INVALID
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
}
