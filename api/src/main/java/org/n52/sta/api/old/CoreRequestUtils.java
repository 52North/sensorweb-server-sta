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
package org.n52.sta.api.old;

import java.util.regex.Pattern;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;

/**
 * Constants used by STA Core for handling Requests.
 */
public interface CoreRequestUtils extends RequestUtils {

    String BASE_COLLECTION_REGEX_NAMED_GROUPS =
        WANTED_NAME_GROUP_START
            + StaConstants.OBSERVATIONS + OR
            + StaConstants.DATASTREAMS + OR
            + StaConstants.THINGS + OR
            + StaConstants.SENSORS + OR
            + StaConstants.LOCATIONS + OR
            + StaConstants.HISTORICAL_LOCATIONS + OR
            + StaConstants.FEATURES_OF_INTEREST + OR
            + StaConstants.OBSERVED_PROPERTIES
            + WANTED_NAME_GROUP_END;

    String BASE_COLLECTION_REGEX =
        StaConstants.OBSERVATIONS + OR
            + StaConstants.DATASTREAMS + OR
            + StaConstants.THINGS + OR
            + StaConstants.SENSORS + OR
            + StaConstants.LOCATIONS + OR
            + StaConstants.HISTORICAL_LOCATIONS + OR
            + StaConstants.FEATURES_OF_INTEREST + OR
            + StaConstants.OBSERVED_PROPERTIES;

    String IDENTIFIED_BY_DATASTREAM_REGEX =
        StaConstants.DATASTREAMS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN
            + StaConstants.SENSOR + OR + StaConstants.OBSERVED_PROPERTY + OR + StaConstants.THING + OR +
            StaConstants.OBSERVATIONS + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_OBSERVATION_REGEX =
        StaConstants.OBSERVATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + StaConstants.DATASTREAM + OR +
            StaConstants.FEATURE_OF_INTEREST +
            ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX =
        StaConstants.HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + StaConstants.THING + OR +
            StaConstants.LOCATIONS +
            ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_THING_REGEX =
        StaConstants.THINGS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + StaConstants.DATASTREAMS
            + OR + StaConstants.HISTORICAL_LOCATIONS + OR + StaConstants.LOCATIONS + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_LOCATION_REGEX =
        StaConstants.LOCATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + StaConstants.THINGS + OR +
            StaConstants.HISTORICAL_LOCATIONS +
            ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_SENSOR_REGEX = StaConstants.SENSORS + IDENTIFIER_REGEX + SLASH + StaConstants.DATASTREAMS;

    String IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX =
        StaConstants.OBSERVED_PROPERTIES + IDENTIFIER_REGEX + SLASH + StaConstants.DATASTREAMS;

    String IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX =
        StaConstants.FEATURES_OF_INTEREST + IDENTIFIER_REGEX + SLASH + StaConstants.OBSERVATIONS;

    // /Datastreams(52)
    String ENTITY_IDENTIFIED_DIRECTLY =
        PATH_ENTITY + BASE_COLLECTION_REGEX + CURLY_BRACKET_CLOSE + PATH_ID + IDENTIFIER_REGEX + DOLLAR +
            CURLY_BRACKET_CLOSE;

    // /Datastreams(52)/Sensor
    // /Datastreams(52)/ObservedProperty
    // /Datastreams(52)/Thing
    String ENTITY_IDENTIFIED_BY_DATASTREAM =
        SOURCE_NAME_GROUP_START + StaConstants.DATASTREAMS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.SENSOR + OR + StaConstants.OBSERVED_PROPERTY + OR +
            StaConstants.THING + WANTED_NAME_GROUP_END;

    String ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.DATASTREAMS + IDENTIFIER_REGEX
            + CURLY_BRACKET_CLOSE + PATH_TARGET + StaConstants.SENSOR + OR + StaConstants.OBSERVED_PROPERTY + OR +
            StaConstants.THING +
            CURLY_BRACKET_CLOSE;

    String ENTITY_PROPERTY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE =
        ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE + SLASH + PATH_PROPERTY;

    // /Observations(52)/Datastream
    // /Observations(52)/FeatureOfInterest
    String ENTITY_IDENTIFIED_BY_OBSERVATION =
        SOURCE_NAME_GROUP_START + StaConstants.OBSERVATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.DATASTREAM + OR + StaConstants.FEATURE_OF_INTEREST +
            WANTED_NAME_GROUP_END;

    String ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.OBSERVATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            StaConstants.DATASTREAM + OR +
            StaConstants.FEATURE_OF_INTEREST +
            CURLY_BRACKET_CLOSE;

    String ENTITY_PROPERTY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE =
        ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE + SLASH + PATH_PROPERTY;

    // /HistoricalLocations(52)/Thing
    String ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION =
        SOURCE_NAME_GROUP_START + StaConstants.HISTORICAL_LOCATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.THING + WANTED_NAME_GROUP_END;

    String ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            StaConstants.THING +
            CURLY_BRACKET_CLOSE;

    String ENTITY_PROPERTY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE =
        ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE + SLASH + PATH_PROPERTY;

    // /Datastream(52)/Observations
    String COLLECTION_IDENTIFIED_BY_DATASTREAM =
        SOURCE_NAME_GROUP_START + StaConstants.DATASTREAMS + ROUND_BRACKET_CLOSE
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.OBSERVATIONS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.DATASTREAMS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            StaConstants.OBSERVATIONS +
            CURLY_BRACKET_CLOSE;

    // /Things(52)/Datastreams
    // /Things(52)/HistoricalLocations
    // /Things(52)/Locations
    String COLLECTION_IDENTIFIED_BY_THING =
        SOURCE_NAME_GROUP_START + StaConstants.THINGS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.DATASTREAMS + OR + StaConstants.HISTORICAL_LOCATIONS + OR +
            StaConstants.LOCATIONS +
            WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_THING_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.THINGS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET
            + StaConstants.DATASTREAMS + OR + StaConstants.HISTORICAL_LOCATIONS + OR + StaConstants.LOCATIONS +
            CURLY_BRACKET_CLOSE;

    // /Locations(52)/Things
    // /Locations(52)/HistoricalLocations
    String COLLECTION_IDENTIFIED_BY_LOCATION =
        SOURCE_NAME_GROUP_START + StaConstants.LOCATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.THINGS + OR + StaConstants.HISTORICAL_LOCATIONS +
            WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_LOCATION_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.LOCATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            StaConstants.THINGS + OR +
            StaConstants.HISTORICAL_LOCATIONS +
            CURLY_BRACKET_CLOSE;

    // /Sensors(52)/Datastreams
    String COLLECTION_IDENTIFIED_BY_SENSOR =
        SOURCE_NAME_GROUP_START + StaConstants.SENSORS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.DATASTREAMS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_SENSOR_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.SENSORS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            StaConstants.DATASTREAMS +
            CURLY_BRACKET_CLOSE;

    // /ObservedProperties(52)/Datastreams
    String COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY =
        SOURCE_NAME_GROUP_START + StaConstants.OBSERVED_PROPERTIES + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.DATASTREAMS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.OBSERVED_PROPERTIES + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            StaConstants.DATASTREAMS +
            CURLY_BRACKET_CLOSE;

    // /FeaturesOfInterest(52)/Observations
    String COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST =
        SOURCE_NAME_GROUP_START + StaConstants.FEATURES_OF_INTEREST + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.OBSERVATIONS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.FEATURES_OF_INTEREST + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            StaConstants.OBSERVATIONS +
            CURLY_BRACKET_CLOSE;

    // /HistoricalLocations(52)/Locations
    String COLLECTION_IDENTIFIED_BY_HIST_LOCATION =
        SOURCE_NAME_GROUP_START + StaConstants.HISTORICAL_LOCATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + StaConstants.LOCATIONS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_HIST_LOCATION_PATH_VARIABLE =
        PATH_ENTITY + StaConstants.HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            StaConstants.LOCATIONS +
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
