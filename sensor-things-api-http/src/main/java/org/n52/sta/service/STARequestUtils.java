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
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.DatastreamSerDes;
import org.n52.sta.serdes.FeatureOfInterestSerDes;
import org.n52.sta.serdes.HistoricalLocationSerDes;
import org.n52.sta.serdes.LocationSerDes;
import org.n52.sta.serdes.ObservationSerDes;
import org.n52.sta.serdes.ObservedPropertySerDes;
import org.n52.sta.serdes.SensorSerDes;
import org.n52.sta.serdes.ThingSerDes;
import org.n52.sta.utils.QueryOptions;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class STARequestUtils implements StaConstants {

    protected static final String MAPPING_PREFIX = "**/";
    protected static final String IDENTIFIER_REGEX = "(?:\\()['\\-0-9a-zA-Z]+(?:\\))";
    protected static final String PATH_ENTITY = "{entity:";
    protected static final String PATH_TARGET = "}/{target:";
    protected static final String SLASH_BRACKET = "/(";

    protected static final String COLLECTION_REGEX = OBSERVATIONS + "|" + DATASTREAMS + "|" + THINGS + "|" + SENSORS
            + "|" + LOCATIONS + "|" + HISTORICAL_LOCATIONS + "|" + FEATURES_OF_INTEREST + "|" + OBSERVED_PROPERTIES;

    protected static final String IDENTIFIED_BY_DATASTREAM_REGEX = DATASTREAMS + IDENTIFIER_REGEX + SLASH_BRACKET
            + SENSOR + "|" + OBSERVED_PROPERTY + "|" + THING + "|" + OBSERVATIONS + ")";

    protected static final String IDENTIFIED_BY_OBSERVATION_REGEX =
            OBSERVATIONS + IDENTIFIER_REGEX + SLASH_BRACKET + DATASTREAM + "|" + FEATURE_OF_INTEREST + ")";

    protected static final String IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX =
            HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + "/" + THING;

    protected static final String IDENTIFIED_BY_THING_REGEX = THINGS + IDENTIFIER_REGEX + SLASH_BRACKET + DATASTREAMS
            + "|" + HISTORICAL_LOCATIONS + "|" + LOCATIONS + ")";

    protected static final String IDENTIFIED_BY_LOCATION_REGEX =
            LOCATIONS + IDENTIFIER_REGEX + SLASH_BRACKET + THINGS + "|" + HISTORICAL_LOCATIONS + ")}";

    protected static final String IDENTIFIED_BY_SENSOR_REGEX = SENSORS + IDENTIFIER_REGEX + "/" + DATASTREAMS;

    protected static final String IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX =
            OBSERVED_PROPERTIES + IDENTIFIER_REGEX + "/" + DATASTREAMS;

    protected static final String IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX =
            FEATURES_OF_INTEREST + IDENTIFIER_REGEX + "/" + OBSERVATIONS;

    protected static final String ENTITY_IDENTIFIED_BY_DATASTREAM_PATH = PATH_ENTITY + DATASTREAMS + IDENTIFIER_REGEX
            + PATH_TARGET + SENSOR + "|" + OBSERVED_PROPERTY + "|" + THING + "}";

    protected static final String ENTITY_IDENTIFIED_BY_OBSERVATION_PATH =
            PATH_ENTITY + OBSERVATIONS + IDENTIFIER_REGEX + PATH_TARGET + DATASTREAM + "|" + FEATURE_OF_INTEREST + "}";

    protected static final String ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH =
            PATH_ENTITY + HISTORICAL_LOCATIONS + IDENTIFIER_REGEX + PATH_TARGET + THING + "}";

    protected static final String COLLECTION_IDENTIFIED_BY_DATASTREAM_PATH =
            PATH_ENTITY + DATASTREAMS + IDENTIFIER_REGEX + PATH_TARGET + OBSERVATIONS + "}";

    protected static final String COLLECTION_IDENTIFIED_BY_THING_PATH = PATH_ENTITY + THINGS + IDENTIFIER_REGEX
            + PATH_TARGET + DATASTREAMS + "|" + HISTORICAL_LOCATIONS + "|" + LOCATIONS + "}";

    protected static final String COLLECTION_IDENTIFIED_BY_LOCATION_PATH =
            PATH_ENTITY + LOCATIONS + IDENTIFIER_REGEX + PATH_TARGET + THINGS + "|" + HISTORICAL_LOCATIONS + "}";

    protected static final String COLLECTION_IDENTIFIED_BY_SENSOR_PATH =
            PATH_ENTITY + SENSORS + IDENTIFIER_REGEX + PATH_TARGET + DATASTREAMS + "}";

    protected static final String COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH =
            PATH_ENTITY + OBSERVED_PROPERTIES + IDENTIFIER_REGEX + PATH_TARGET + DATASTREAMS + "}";

    protected static final String COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH =
            PATH_ENTITY + FEATURES_OF_INTEREST + IDENTIFIER_REGEX + PATH_TARGET + OBSERVATIONS + "}";

    protected static Map<String, Class> collectionNameToClass;
    protected static Map<String, Class> collectionNameToPatchClass;

    private static final String URL_INVALID = "Url is invalid. ";

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

    protected final Pattern byIdPattern = Pattern.compile("(" + COLLECTION_REGEX + ")" + IDENTIFIER_REGEX);
    protected final Pattern byDatastreamPattern = Pattern.compile(IDENTIFIED_BY_DATASTREAM_REGEX);
    protected final Pattern byObservationPattern = Pattern.compile(IDENTIFIED_BY_OBSERVATION_REGEX);
    protected final Pattern byHistoricalLocationPattern = Pattern.compile(IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX);
    protected final Pattern byLocationPattern = Pattern.compile(IDENTIFIED_BY_LOCATION_REGEX);
    protected final Pattern byThingPattern = Pattern.compile(IDENTIFIED_BY_THING_REGEX);
    protected final Pattern bySensorsPattern = Pattern.compile(IDENTIFIED_BY_SENSOR_REGEX);
    protected final Pattern byObservedPropertiesPattern = Pattern.compile(IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX);
    protected final Pattern byFeaturesOfInterestPattern = Pattern.compile(IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX);

    protected STAInvalidUrlException validateURL(StringBuffer requestURL,
                       EntityServiceRepository serviceRepository,
                       int rootUrlLength) throws STAInvalidUrlException {
        String[] uriResources = requestURL.substring(rootUrlLength).split("/");

        STAInvalidUrlException ex;
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
     * @param uriResources URI of the Request split by "/"
     * @return STAInvalidUrlException if URI is malformed
     */
    private STAInvalidUrlException validateURISyntax(String[] uriResources) {
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
                        String resource = uriResources[i - 1] + "/" + uriResources[i];
                        if (!(byDatastreamPattern.matcher(resource).matches()
                                || byHistoricalLocationPattern.matcher(resource).matches()
                                || byLocationPattern.matcher(resource).matches()
                                || byThingPattern.matcher(resource).matches()
                                || byFeaturesOfInterestPattern.matcher(resource).matches()
                                || byObservationPattern.matcher(resource).matches()
                                || bySensorsPattern.matcher(resource).matches()
                                || byObservedPropertiesPattern.matcher(resource).matches())) {
                            return new STAInvalidUrlException(URL_INVALID
                                    + uriResources[i - 1]
                                    + "/" + uriResources[i]
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

    /**
     * This function validates a given URI semantically by checking if all Entities referenced in the navigation
     * exists. As URI is syntactically valid indices can be hard-coded.
     *
     * @param uriResources URI of the Request split by "/"
     * @return STAInvalidUrlException if URI is malformed
     */
    private STAInvalidUrlException validateURISemantic(String[] uriResources,
                                                         EntityServiceRepository serviceRepository) {
        // Check if this is Request to root collection. They are always valid
        if (uriResources.length == 1 && !uriResources[0].contains("(")) {
            return null;
        }
        // Parse first navigation Element
        String[] sourceEntity = splitId(uriResources[0]);
        String sourceId = sourceEntity[1].replace(")", "");
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
                targetId = targetEntity[1].replace(")", "");
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

    private STAInvalidUrlException createInvalidUrlExceptionNoEntit(String entity) {
        return new STAInvalidUrlException("No Entity: " + entity + " found!");
    }

    private STAInvalidUrlException createInvalidUrlExceptionNoEntitAssociated(String first, String last) {
        return createInvalidUrlExceptionNoEntit(first + " associated with " + last);
    }

    protected static String[] splitId(String entity) {
        return entity.split("\\(");
    }

    //TODO: actually implement parsing of Query options
    static QueryOptions createQueryOptions(Map<String, String> raw) {

        return new QueryOptions() {

            @Override
            public boolean hasCountOption() {
                return false;
            }

            @Override
            public boolean getCountOption() {
                return false;
            }

            @Override
            public int getTopOption() {
                return 0;
            }

            @Override
            public boolean hasSkipOption() {
                return false;
            }

            @Override
            public int getSkipOption() {
                return 0;
            }

            @Override
            public boolean hasOrderByOption() {
                return false;
            }

            @Override
            public String getOrderByOption() {
                return null;
            }

            @Override
            public boolean hasSelectOption() {
                return false;
            }

            @Override
            public Set<String> getSelectOption() {
                return null;
            }

            @Override
            public boolean hasExpandOption() {
                return false;
            }

            @Override
            public Set<String> getExpandOption() {
                return null;
            }

            @Override
            public boolean hasFilterOption() {
                return false;
            }

            @Override
            public Set<String> getFilterOption() {
                return null;
            }
        };
    }
}
