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

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservationEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.serdes.DatastreamSerDes;
import org.n52.sta.serdes.FeatureOfInterestSerDes;
import org.n52.sta.serdes.HistoricalLocationSerDes;
import org.n52.sta.serdes.LocationSerDes;
import org.n52.sta.serdes.ObservationSerDes;
import org.n52.sta.serdes.ObservedPropertySerDes;
import org.n52.sta.serdes.SensorSerDes;
import org.n52.sta.serdes.ThingSerDes;
import org.n52.svalbard.odata.core.QueryOptionsFactory;

import java.util.regex.Pattern;

public interface RequestUtils extends StaConstants {

    QueryOptionsFactory QUERY_OPTIONS_FACTORY = new QueryOptionsFactory();

    String INTERNAL_CLIENT_ID = "POC";

    // Used to store information about referenced entity during related POST
    String REFERENCED_FROM_TYPE = "referencedFromType";

    // Used to store information about referenced entity during related POST
    String REFERENCED_FROM_ID = "referencedFromID";

    // Used for identifying/referencing source Entity Type
    // e.g. "Datastreams" in /Datastreams(52)/Thing
    String GROUPNAME_SOURCE_NAME = "sourceName";

    // Used for identifying/referencing source Entity Id
    // e.g. "52" in /Datastreams(52)/Thing
    String GROUPNAME_SOURCE_IDENTIFIER = "sourceId";

    // Used for identifying/referencing requested Entity Type
    // e.g. "Thing" in /Datastreams(52)/Thing
    String GROUPNAME_WANTED_NAME = "wantedName";

    // Used for identifying/referencing requested Entity Type
    // e.g. "Thing" in /Datastreams(52)/Thing
    String GROUPNAME_WANTED_IDENTIFIER = "wantedId";

    // Used for identifying/referencing $select option
    // e.g. "name" in /Datastreams(52)/Thing$select=name
    String GROUPNAME_SELECT = "select";

    // Used for identifying/referencing source Entity Type
    // e.g. "name" in /Datastreams(52)/Thing/name
    String GROUPNAME_PROPERTY = "property";

    String MAPPING_PREFIX = "**/";
    String ID = "id";

    // Note: This is duplicated in LocationService to allow for non-standard 'updateFOI'-feature.
    String IDENTIFIER_REGEX = "(?:\\()[^)]+(?:\\))";

    String URL_INVALID = "Url is invalid. ";

    String PATH_ENTITY = "{entity:";
    String PATH_TARGET = "/{target:";
    String PATH_ID = "{id:";
    String PATH_PROPERTY = "{property:\\w+}";
    String SLASH = "/";
    String BACKSLASH = "\\";
    String OR = "|";
    String DOLLAR = "$";
    String QUESTIONMARK = "?";
    String PLUS = "+";
    String LESS_THAN = "<";
    String GREATER_THAN = ">";
    String ROUND_BRACKET_CLOSE = ")";
    String ROUND_BRACKET_OPEN = "(";
    String SQUARE_BRACKET_CLOSE = "]";
    String SQUARE_BRACKET_OPEN = "[";
    String CURLY_BRACKET_OPEN = "{";
    String CURLY_BRACKET_CLOSE = "}";
    String SLASHREF = SLASH + "$ref";
    String SLASHVALUE = SLASH + "$value";

    // Used to mark start and end of named capturing groups
    String SOURCE_NAME_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_SOURCE_NAME + GREATER_THAN;
    String SOURCE_NAME_GROUP_END = ROUND_BRACKET_CLOSE;
    String SOURCE_ID_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_SOURCE_IDENTIFIER + GREATER_THAN;
    String SOURCE_ID_GROUP_END = ROUND_BRACKET_CLOSE;
    String WANTED_NAME_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_WANTED_NAME + GREATER_THAN;
    String WANTED_NAME_GROUP_END = ROUND_BRACKET_CLOSE;
    String WANTED_ID_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_WANTED_IDENTIFIER + GREATER_THAN;
    String WANTED_ID_GROUP_END = ROUND_BRACKET_CLOSE;
    String SELECT_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_SELECT + GREATER_THAN;
    String SELECT_GROUP_END = ROUND_BRACKET_CLOSE;
    String PROPERTY_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_PROPERTY + GREATER_THAN;
    String PROPERTY_GROUP_END = ROUND_BRACKET_CLOSE;
    String SELECT_REGEX_NAMED_GROUPS =
            BACKSLASH + QUESTIONMARK + BACKSLASH + DOLLAR + "select=" +
                    SELECT_GROUP_START + SQUARE_BRACKET_OPEN + BACKSLASH + "w" + BACKSLASH + "," +
                    SQUARE_BRACKET_CLOSE + PLUS + SELECT_GROUP_END;

    String PROPERTY_REGEX_NAMED_GROUPS =
            SLASH + PROPERTY_GROUP_START + SQUARE_BRACKET_OPEN + "A-z," + SQUARE_BRACKET_CLOSE + PLUS +
                    PROPERTY_GROUP_END;

    default Class collectionNameToClass(String collectionName) throws STAInvalidUrlException {
        switch (collectionName) {
        case THINGS:
            return PlatformEntity.class;
        case LOCATIONS:
            return LocationEntity.class;
        case DATASTREAMS:
            return AbstractDatasetEntity.class;
        case HISTORICAL_LOCATIONS:
            return HistoricalLocationEntity.class;
        case SENSORS:
            return ProcedureEntity.class;
        case OBSERVATIONS:
            return ObservationEntity.class;
        case OBSERVED_PROPERTIES:
            return PhenomenonEntity.class;
        case FEATURES_OF_INTEREST:
            return AbstractFeatureEntity.class;
        default:
            throw new STAInvalidUrlException("Could not resolve CollectionName to Entity class!");
        }
    }

    default Class collectionNameToPatchClass(String collectionName) throws STAInvalidUrlException {
        switch (collectionName) {
        case THINGS:
        case THING:
            return ThingSerDes.PlatformEntityPatch.class;
        case LOCATIONS:
        case LOCATION:
            return LocationSerDes.LocationEntityPatch.class;
        case DATASTREAMS:
        case DATASTREAM:
            return DatastreamSerDes.DatastreamEntityPatch.class;
        case HISTORICAL_LOCATIONS:
        case HISTORICAL_LOCATION:
            return HistoricalLocationSerDes.HistoricalLocationEntityPatch.class;
        case SENSORS:
        case SENSOR:
            return SensorSerDes.ProcedureEntityPatch.class;
        case OBSERVATIONS:
        case OBSERVATION:
            return ObservationSerDes.ObservationEntityPatch.class;
        case OBSERVED_PROPERTIES:
        case OBSERVED_PROPERTY:
            return ObservedPropertySerDes.PhenomenonEntityPatch.class;
        case FEATURES_OF_INTEREST:
        case FEATURE_OF_INTEREST:
            return FeatureOfInterestSerDes.AbstractFeatureEntityPatch.class;
        default:
            throw new STAInvalidUrlException("Could not resolve CollectionName to PatchEntity class!");
        }
    }

    STAInvalidUrlException validateURISyntax(String[] uriResources);
}
