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

import org.n52.series.db.beans.sta.mapped.extension.ObservationGroup;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.shetland.ogc.sta.exception.STANotFoundException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.ObservationGroupSerDes;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface CitSciExtensionRequestUtils extends RequestUtils {

    String OBSERVATIONGROUP = "ObservationGroup";
    String OBSERVATIONGROUPS = "ObservationGroups";

    String BASE_COLLECTION_REGEX_NAMED_GROUPS =
            WANTED_NAME_GROUP_START
                    + OBSERVATIONGROUPS
                    + WANTED_NAME_GROUP_END;

    String BASE_COLLECTION_REGEX = OBSERVATIONGROUPS;

    String IDENTIFIED_BY_OBSERVATION_REGEX =
            OBSERVATIONS + IDENTIFIER_REGEX + SLASH + ROUND_BRACKET_OPEN + OBSERVATIONGROUPS + ROUND_BRACKET_CLOSE;

    // /ObservationGroups(52)
    String ENTITY_IDENTIFIED_DIRECTLY =
            PATH_ENTITY + BASE_COLLECTION_REGEX + CURLY_BRACKET_CLOSE + PATH_ID + IDENTIFIER_REGEX + DOLLAR +
                    CURLY_BRACKET_CLOSE;

    // /Locations(52)/Things
    // /Locations(52)/HistoricalLocations
    String COLLECTION_IDENTIFIED_BY_OBSERVATION =
            SOURCE_NAME_GROUP_START + OBSERVATIONS + SOURCE_NAME_GROUP_END
                    + SOURCE_ID_GROUP_START + IDENTIFIER_REGEX + SOURCE_ID_GROUP_END
                    + SLASH
                    + WANTED_NAME_GROUP_START + OBSERVATIONGROUPS + WANTED_NAME_GROUP_END;

    String COLLECTION_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE =
            PATH_ENTITY + OBSERVATIONS + IDENTIFIER_REGEX + CURLY_BRACKET_CLOSE + PATH_TARGET + OBSERVATIONGROUPS +
                    CURLY_BRACKET_CLOSE;

    // Patterns used for matching Paths in mqtt with named groups
    // Java does not support duplicate names so patterns are handled separately
    // OGC-15-078r6 14.2.1
    Pattern CP_BASE = Pattern.compile(BASE_COLLECTION_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern CP_IDENT_BY_OBSERVATION = Pattern.compile(COLLECTION_IDENTIFIED_BY_OBSERVATION + DOLLAR);

    Pattern[] NAMED_COLL_PATTERNS =
            new Pattern[] {
                    CP_BASE,
                    CP_IDENT_BY_OBSERVATION
            };

    // OGC-15-078r6 14.2.3
    Pattern PROP_PATTERN_BASE = Pattern.compile(
            BASE_COLLECTION_REGEX_NAMED_GROUPS + WANTED_ID_GROUP_START + IDENTIFIER_REGEX + WANTED_ID_GROUP_END +
                    PROPERTY_REGEX_NAMED_GROUPS + DOLLAR);

    Pattern[] NAMED_PROP_PATTERNS =
            new Pattern[] {
                    PROP_PATTERN_BASE
            };

    // OGC-15-078r6 14.2.4
    Pattern NAMED_SELECT_PATTER_BASE =
            Pattern.compile(BASE_COLLECTION_REGEX_NAMED_GROUPS + SELECT_REGEX_NAMED_GROUPS + DOLLAR);
    Pattern NAMED_SELECT_PATTER_IDENT_BY_OBSERVATION =
            Pattern.compile(COLLECTION_IDENTIFIED_BY_OBSERVATION + SELECT_REGEX_NAMED_GROUPS + DOLLAR);

    Pattern[] NAMED_SELECT_PATTERNS =
            new Pattern[] {
                    NAMED_SELECT_PATTER_BASE,
                    NAMED_SELECT_PATTER_IDENT_BY_OBSERVATION
            };

    Pattern BY_ID_PATTERN = Pattern.compile(
            ROUND_BRACKET_OPEN + BASE_COLLECTION_REGEX_NAMED_GROUPS + ROUND_BRACKET_CLOSE + IDENTIFIER_REGEX);
    Pattern BY_OBSERVATION_PATTERN = Pattern.compile(IDENTIFIED_BY_OBSERVATION_REGEX);

    default Class collectionNameToClass(String collectionName) throws STAInvalidUrlException {
        switch (collectionName) {
        case OBSERVATIONGROUP:
            return ObservationGroup.class;
        default:
            throw new STAInvalidUrlException("Could not resolve CollectionName to Entity class!");
        }
    }

    default Class collectionNameToPatchClass(String collectionName) {
        switch (collectionName) {
        case OBSERVATIONGROUP:
        case OBSERVATIONGROUPS:
            return ObservationGroupSerDes.ObservationGroupPatch.class;
        default:
            return null;
        }
    }

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
                        if (!BY_OBSERVATION_PATTERN.matcher(resource).matches()) {
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
