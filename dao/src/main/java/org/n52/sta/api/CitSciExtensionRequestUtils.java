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

package org.n52.sta.api;

import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface CitSciExtensionRequestUtils extends RequestUtils {

    String BASE_COLLECTION_REGEX =
        OBSERVATION_GROUPS
            + OR + OBSERVATION_RELATIONS
            + OR + PARTIES
            + OR + PROJECTS
            + OR + LICENSES;

    String BASE_COLLECTION_REGEX_NAMED_GROUPS =
        WANTED_NAME_GROUP_START + BASE_COLLECTION_REGEX + OR + DATASTREAMS + OR + OBSERVATIONS +
            WANTED_NAME_GROUP_END;

    String IDENTIFIED_BY_OBSERVATION_REGEX =
        OBSERVATIONS + IDENTIFIER_REGEX + SLASH
            + ROUND_BRACKET_OPEN
            + NAV_SUBJECTS
            + OR + NAV_OBJECTS
            + OR + OBSERVATION_GROUPS
            + OR + LICENSE
            + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_LICENSE_REGEX =
        LICENSES + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN
            + OBSERVATION_GROUPS
            + OR + OBSERVATIONS
            + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_PARTY_REGEX =
        PARTIES + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN
            + DATASTREAMS
            + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_PROJECT_REGEX =
        PROJECTS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN
            + DATASTREAMS
            + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_OBSERVATION_GROUP_REGEX =
        OBSERVATION_GROUPS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN
            + OBSERVATION_RELATIONS
            + OR + OBSERVATIONS
            + OR + LICENSE
            + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_OBSERVATION_RELATION_REGEX =
        OBSERVATION_RELATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN
            + NAV_SUBJECT
            + OR + NAV_OBJECT
            + OR + OBSERVATION_GROUPS
            + ROUND_BRACKET_CLOSE;

    String IDENTIFIED_BY_DATASTREAM_REGEX =
        DATASTREAMS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN
            + PROJECT + OR + PARTY
            + ROUND_BRACKET_CLOSE;

    // /ObservationRelations(52)/ObservationGroup
    // /ObservationRelations(52)/Observation
    String COLLECTION_IDENTIFIED_BY_OBSERVATIONRELATION =
        SOURCE_NAME_GROUP_START + OBSERVATION_RELATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + OBSERVATION_GROUPS
            + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_OBSERVATIONRELATION_PATH_VARIABLE =
        PATH_ENTITY + OBSERVATION_RELATIONS + IDENTIFIER_REGEX
            + CURLY_BRACKET_CLOSE + PATH_TARGET + OBSERVATION_GROUPS +
            CURLY_BRACKET_CLOSE;

    // /Observations(52)/ObservationRelations
    String COLLECTION_IDENTIFIED_BY_OBSERVATION =
        SOURCE_NAME_GROUP_START + OBSERVATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + NAV_OBJECTS
            + OR + NAV_SUBJECTS
            + OR + OBSERVATION_GROUPS
            + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE =
        PATH_ENTITY + OBSERVATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            NAV_OBJECTS + OR + NAV_SUBJECTS + OR + OBSERVATION_GROUPS +
            CURLY_BRACKET_CLOSE;

    // /ObservationGroups(52)/ObservationRelations
    String COLLECTION_IDENTIFIED_BY_OBSERVATION_GROUP =
        SOURCE_NAME_GROUP_START + OBSERVATION_GROUPS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + LICENSE + OR + OBSERVATION_RELATIONS + OR + OBSERVATIONS
            + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_OBSERVATION_GROUP_PATH_VARIABLE =
        PATH_ENTITY + OBSERVATION_GROUPS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET +
            LICENSE + OR + OBSERVATION_RELATIONS + OR + OBSERVATIONS +
            CURLY_BRACKET_CLOSE;

    // /Parties(52)/Datastreams
    String COLLECTION_IDENTIFIED_BY_PARTY =
        SOURCE_NAME_GROUP_START + PARTIES + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + DATASTREAMS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_PARTY_PATH_VARIABLE =
        PATH_ENTITY + PARTIES + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE
            + PATH_TARGET + DATASTREAMS + CURLY_BRACKET_CLOSE;

    // /Licenses(52)/Datastreams
    String COLLECTION_IDENTIFIED_BY_LICENSE =
        SOURCE_NAME_GROUP_START + LICENSES + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + OBSERVATION_GROUPS + OR + OBSERVATIONS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_LICENSE_PATH_VARIABLE =
        PATH_ENTITY + LICENSES + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE
            + PATH_TARGET + OBSERVATION_GROUPS + OR + OBSERVATIONS + CURLY_BRACKET_CLOSE;

    // /Projects(52)/Datastreams
    String COLLECTION_IDENTIFIED_BY_PROJECT =
        SOURCE_NAME_GROUP_START + PROJECTS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + DATASTREAMS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_PROJECT_PATH_VARIABLE =
        PATH_ENTITY + PROJECTS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET
            + DATASTREAMS +
            CURLY_BRACKET_CLOSE;

    // /ObservationGroups(52)
    String ENTITY_IDENTIFIED_DIRECTLY =
        PATH_ENTITY + BASE_COLLECTION_REGEX + CURLY_BRACKET_CLOSE + PATH_ID + IDENTIFIER_REGEX + DOLLAR +
            CURLY_BRACKET_CLOSE;

    // /ObservationRelations(52)/ObservationGroup
    // /ObservationRelations(52)/Observation
    String ENTITY_IDENTIFIED_BY_OBSERVATIONRELATION =
        SOURCE_NAME_GROUP_START + OBSERVATION_RELATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
            + SLASH
            + WANTED_NAME_GROUP_START + NAV_SUBJECT + OR + NAV_OBJECT +
            WANTED_NAME_GROUP_END;

    String ENTITY_IDENTIFIED_BY_OBSERVATIONRELATION_PATH_VARIABLE =
        PATH_ENTITY + OBSERVATION_RELATIONS + IDENTIFIER_REGEX
            + CURLY_BRACKET_CLOSE + PATH_TARGET + NAV_SUBJECT + OR + NAV_OBJECT +
            CURLY_BRACKET_CLOSE;

    String ENTITY_PROPERTY_IDENTIFIED_BY_OBSERVATIONRELATION_PATH_VARIABLE =
        ENTITY_IDENTIFIED_BY_OBSERVATIONRELATION_PATH_VARIABLE + SLASH + PATH_PROPERTY;

    // /Datastreams(52)/Party
    // /Datastreams(52)/Project
    String ENTITY_IDENTIFIED_BY_DATASTREAM =
        SOURCE_NAME_GROUP_START + DATASTREAMS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END + SLASH + WANTED_NAME_GROUP_START
            + PROJECT
            + OR + PARTY
            + WANTED_NAME_GROUP_END;

    String ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE =
        PATH_ENTITY + DATASTREAMS + IDENTIFIER_REGEX
            + CURLY_BRACKET_CLOSE + PATH_TARGET
            + PROJECT
            + OR + PARTY
            + CURLY_BRACKET_CLOSE;

    String ENTITY_PROPERTY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE =
        ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE + SLASH + PATH_PROPERTY;

    // /Observations(52)/License
    String ENTITY_IDENTIFIED_BY_OBSERVATION =
        SOURCE_NAME_GROUP_START + OBSERVATIONS + SOURCE_NAME_GROUP_END
            + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END + SLASH + WANTED_NAME_GROUP_START
            + LICENSE
            + WANTED_NAME_GROUP_END;

    String ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE =
        PATH_ENTITY + OBSERVATIONS + IDENTIFIER_REGEX
            + CURLY_BRACKET_CLOSE + PATH_TARGET
            + LICENSE
            + CURLY_BRACKET_CLOSE;

    // Patterns used for matching Paths in mqtt with named groups
    // Java does not support duplicate names so patterns are handled separately
    // OGC-15-078r6 14.2.1
    Pattern CP_BASE = Pattern.compile(BASE_COLLECTION_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern CP_IDENT_BY_OBSERVATION_GROUP = Pattern.compile(COLLECTION_IDENTIFIED_BY_OBSERVATION_GROUP + DOLLAR);
    Pattern CP_IDENT_BY_OBSERVATION_RELATION = Pattern.compile(COLLECTION_IDENTIFIED_BY_OBSERVATIONRELATION + DOLLAR);
    Pattern CP_IDENT_BY_LICENSE = Pattern.compile(COLLECTION_IDENTIFIED_BY_LICENSE + DOLLAR);
    Pattern CP_IDENT_BY_PARTY = Pattern.compile(COLLECTION_IDENTIFIED_BY_PARTY + DOLLAR);
    Pattern CP_IDENT_BY_PROJECT = Pattern.compile(COLLECTION_IDENTIFIED_BY_PROJECT + DOLLAR);

    Pattern[] NAMED_COLL_PATTERNS =
        new Pattern[] {
            CP_BASE,
            CP_IDENT_BY_OBSERVATION_GROUP,
            CP_IDENT_BY_OBSERVATION_RELATION,
            CP_IDENT_BY_LICENSE,
            CP_IDENT_BY_PARTY,
            CP_IDENT_BY_PROJECT,
        };

    // OGC-15-078r6 14.2.3
    Pattern PROP_PATTERN_BASE = Pattern.compile(
        BASE_COLLECTION_REGEX_NAMED_GROUPS + WANTED_ID_GROUP_START + IDENTIFIER_REGEX + WANTED_ID_GROUP_END +
            PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);

    Pattern[] NAMED_PROP_PATTERNS =
        new Pattern[] {
            PROP_PATTERN_BASE,
        };

    Pattern EP_BASE = Pattern.compile(
        BASE_COLLECTION_REGEX_NAMED_GROUPS + WANTED_ID_GROUP_START + IDENTIFIER_REGEX + WANTED_ID_GROUP_END +
            DOLLAR);
    Pattern EP_IDENT_BY_OBSERVATIONRELATION = Pattern.compile(ENTITY_IDENTIFIED_BY_OBSERVATIONRELATION + DOLLAR);
    Pattern EP_IDENT_BY_DATASTREAM = Pattern.compile(ENTITY_IDENTIFIED_BY_DATASTREAM + DOLLAR);

    Pattern[] NAMED_ENTITY_PATTERNS =
        new Pattern[] {
            EP_BASE,
            EP_IDENT_BY_OBSERVATIONRELATION,
            EP_IDENT_BY_DATASTREAM,
        };

    // OGC-15-078r6 14.2.4
    Pattern NAMED_SELECT_PATTER_BASE =
        Pattern.compile(BASE_COLLECTION_REGEX_NAMED_GROUPS + SELECT_REGEX_NAMED_GROUPS + DOLLAR);

    Pattern[] NAMED_SELECT_PATTERNS =
        new Pattern[] {
            NAMED_SELECT_PATTER_BASE,
        };

    Pattern BY_ID_PATTERN = Pattern.compile(
        ROUND_BRACKET_OPEN + BASE_COLLECTION_REGEX_NAMED_GROUPS + ROUND_BRACKET_CLOSE + IDENTIFIER_REGEX);
    Pattern BY_OBSERVATION_GROUP_PATTERN = Pattern.compile(IDENTIFIED_BY_OBSERVATION_GROUP_REGEX);
    Pattern BY_OBSERVATION_RELATION_PATTERN = Pattern.compile(IDENTIFIED_BY_OBSERVATION_RELATION_REGEX);
    Pattern BY_LICENSE_PATTERN = Pattern.compile(IDENTIFIED_BY_LICENSE_REGEX);
    Pattern BY_PARTY_PATTERN = Pattern.compile(IDENTIFIED_BY_PARTY_REGEX);
    Pattern BY_PROJECT_PATTERN = Pattern.compile(IDENTIFIED_BY_PROJECT_REGEX);
    Pattern BY_DATASTREAM_PATTERN = Pattern.compile(IDENTIFIED_BY_DATASTREAM_REGEX);
    Pattern BY_OBSERVATION_PATTERN = Pattern.compile(IDENTIFIED_BY_OBSERVATION_REGEX);

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
                        if (!(BY_OBSERVATION_GROUP_PATTERN.matcher(resource).matches()
                            || BY_LICENSE_PATTERN.matcher(resource).matches()
                            || BY_PARTY_PATTERN.matcher(resource).matches()
                            || BY_PROJECT_PATTERN.matcher(resource).matches()
                            || BY_DATASTREAM_PATTERN.matcher(resource).matches()
                            || BY_OBSERVATION_PATTERN.matcher(resource).matches()
                            || BY_OBSERVATION_RELATION_PATTERN.matcher(resource).matches())) {
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
