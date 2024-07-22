/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.cloudnative.condition;

import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.sta.StaConstants;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public interface EntityQueryConstants {

    String SENSOR = "Sensor";
    String OBSERVED_PROPERTY = "ObservedProperty";
    String THING = "Thing";
    String THINGS = "Things";
    String DATASTREAM = "Datastream";
    String DATASTREAMS = "Datastreams";
    String FEATUREOFINTEREST = "FeatureOfInterest";
    String LOCATIONS = "Locations";
    String HISTORICAL_LOCATIONS = "HistoricalLocations";
    String OBSERVATIONS = "Observations";


    String THING_TABLE = "platform";
    String THING_LOCATION_TABLE = "platform_location";
    String LOCATION_TABLE = "location";
    String DATASTREAM_TABLE = "dataset";
    String FEATURE_TABLE = "feature";
    String HISTORICAL_LOCATION_TABLE = "historical_location";
    String OBSERVATION_TABLE = "observation";
    String SENSOR_TABLE = "procedure";
    String FORMAT_TABLE = "format";
    String OBSERVED_PROPERTY_TABLE = "phenomenon";
    String LOCATION_HISTORICAL_LOCATION_TABLE = "location_historical_location";

    String OBSERVATION_PARAMETER_TABLE = "observation_parameter";
    String PHENOMENON_PARAMETER_TABLE = "phenomenon_parameter";
    String LOCATION_PARAMETER_TABLE = "location_parameter";
    String DATASTREAM_PARAMETER_TABLE = "dataset_parameter";
    String FEATURE_PARAMETER_TABLE = "feature_parameter";
    String SENSOR_PARAMETER_TABLE = "procedure_parameter";
    String THING_PARAMETER_TABLE = "platform_parameter";

    String STA_IDENTIFIER_FIELD = "sta_identifier";
    String STA_NAME_FIELD = "name";
    String STA_DESCRIPTION_FIELD = "description";
    String STA_PROPERTIES_FIELD = "properties";
    String STA_DEFINITION_FIELD = "definition";
    String STA_GEOM_FIELD = "geom";


    String PARAMETER_VALUE_COUNT = "value_count";
    String PARAMETER_VALUE_QUANTITY = "value_quantity";
    String PARAMETER_VALUE_CATEGORY = "value_category";
    String PARAMETER_VALUE_TEXT = "value_text";
    String PARAMETER_VALUE_BOOLEAN = "value_boolean";
    String SENSOR_METADATA_FIELD = "description_file";
    String HISTORICAL_LOCATION_TIME_FIELD = "time";
    String OBSERVATION_VALUE_COUNT_FIELD = PARAMETER_VALUE_COUNT;
    String OBSERVATION_VALUE_QUANTITY_FIELD = PARAMETER_VALUE_QUANTITY;
    String OBSERVATION_VALUE_CATEGORY_FIELD = PARAMETER_VALUE_CATEGORY;
    String OBSERVATION_VALUE_TEXT_FIELD = PARAMETER_VALUE_TEXT;
    String OBSERVATION_VALUE_BOOLEAN_FIELD = PARAMETER_VALUE_BOOLEAN;
    String OBSERVATION_RESULT_TIME_FIELD = "result_time";
    String OBSERVATION_SAMPLING_TIME_START_FIELD = "sampling_time_start";
    String OBSERVATION_SAMPLING_TIME_END_FIELD = "sampling_time_end";
    String LOCATION_GEOM_FIELD = STA_GEOM_FIELD;
    String FEATURE_GEOM_FIELD = STA_GEOM_FIELD;
    String DATASTREAM_PHENOMENONTIME_START_FIELD = "first_time";
    String DATASTREAM_PHENOMENONTIME_END_FIELD = "last_time";
    String DATASTREAM_RESULTTIME_START_FIELD = "result_time_start";

    String THING_ID_FIELD = "platform_id";
    String LOCATION_ID_FIELD = "location_id";
    String SENSOR_ID_FIELD = "procedure_id";
    String FORMAT_ID_FIELD = "format_id";
    String FEATURE_ID_FIELD = "feature_id";
    String OBSERVED_PROPERTY_ID_FIELD = "phenomenon_id";
    String HISTORICAL_LOCATION_ID_FIELD = "historical_location_id";
    String DATASTREAM_ID_FIELD = "dataset_id";
    String OBSERVATION_ID_FIELD = "observation_id";

    String FK_DATASTREAM_ID_FIELD = "fk_dataset_id";
    String FK_OBSERVATION_ID_FIELD = "fk_observation_id";
    String FK_FEATURE_ID_FIELD = "fk_feature_id";
    String FK_HISTORICAL_LOCATION_ID_FIELD = "fk_historical_location_id";
    String FK_LOCATION_ID_FIELD = "fk_location_id";
    String FK_THING_ID_FIELD = "fk_platform_id";
    String FK_SENSOR_ID_FIELD = "fk_procedure_id";
    String FK_FORMAT_ID_FIELD = "fk_format_id";
    String FK_OBSERVED_PROPERTY_ID_FIELD = "fk_phenomenon_id";
    String FK_AGGREGATE_ID_FIELD = "fk_aggregate_id";

    String COULD_NOT_FIND_RELATED_PROPERTY = "Could not find related property: ";
    String ERROR_GETTING_FILTER_NO_PROP = "Error getting filter for Property: '%s'. No such " +
            "property in Entity.";
    String ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE =
            "Error getting filter for Property: '%s'. No such property with type %s in Entity.";
    String ERROR_TEMPLATE = "Operator \"%s\" is not supported for given arguments.";
    String INVALID_DATATYPE_CANNOT_CAST = "Invalid Datatypes found. Cannot cast ";
    String ERROR_INVALID_PARAMETER_ENTITY_TYPE = "Error getting entity from '%s'. No such parameter entity found";


    default String getParameterTableName(ParameterFactory.EntityType entityType) {
        switch (entityType) {
            case PHENOMENON:
                return PHENOMENON_PARAMETER_TABLE;
            case PROCEDURE:
                return SENSOR_PARAMETER_TABLE;
            case PLATFORM:
                return THING_PARAMETER_TABLE;
            case DATASET:
                return DATASTREAM_PARAMETER_TABLE;
            case FEATURE:
                return FEATURE_PARAMETER_TABLE;
            case OBSERVATION:
                return OBSERVATION_PARAMETER_TABLE;
            case LOCATION:
                return LOCATION_PARAMETER_TABLE;
            default:
                return null;
        }
    }

    default String getEntityId(ParameterFactory.EntityType entityType) {
        switch (entityType) {
            case PHENOMENON:
                return OBSERVED_PROPERTY_ID_FIELD;
            case PROCEDURE:
                return SENSOR_ID_FIELD;
            case PLATFORM:
                return THING_ID_FIELD;
            case DATASET:
                return DATASTREAM_ID_FIELD;
            case FEATURE:
                return FEATURE_ID_FIELD;
            case OBSERVATION:
                return OBSERVATION_ID_FIELD;
            case LOCATION:
                return LOCATION_ID_FIELD;
            default:
                return null;
        }
    }

//    default String innerJoinKey(String entity1, String entity2) {
//        switch (entity1) {
//            case StaConstants.SENSOR:
//                switch (entity2) {
//                    case StaConstants.DATASTREAM:
//                        break;
//                }
//                break;
//            case StaConstants.DATASTREAM:
//                switch (entity2) {
//
//                }
//                break;
//            case StaConstants.THING:
//                switch (entity2) {
//                    case StaConstants.LOCATION:
//                        break;
//                    case StaConstants.HISTORICAL_LOCATION:
//                        break;
//                    case StaConstants.DATASTREAM:
//                        break;
//                }
//                break;
//            case StaConstants.OBSERVATION:
//                switch (entity2) {
//                    case StaConstants.DATASTREAM:
//                        break;
//                }
//                break;
//            case StaConstants.FEATURE_OF_INTEREST:
//                switch (entity2) {
//                    case StaConstants.DATASTREAM:
//                        break;
//                }
//                break;
//            case StaConstants.LOCATION:
//                switch (entity2) {
//                    case StaConstants.HISTORICAL_LOCATION:
//                        break;
//                    case StaConstants.THING:
//                        break;
//                }
//                break;
//            case StaConstants.HISTORICAL_LOCATION:
//                switch (entity2) {
//                    case StaConstants.LOCATION:
//                        break;
//                    case StaConstants.THING:
//                        break;
//                }
//                break;
//            case StaConstants.OBSERVED_PROPERTY:
//                switch (entity2) {
//                    case StaConstants.DATASTREAM:
//                }
//                break;
//        }
//    }
}
