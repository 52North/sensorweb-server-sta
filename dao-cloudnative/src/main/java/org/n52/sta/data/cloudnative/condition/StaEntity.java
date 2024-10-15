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
package org.n52.sta.data.cloudnative.condition;


import org.jooq.Field;
import org.jooq.Table;
import org.n52.sta.data.cloudnative.schema.tables.*;


/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public interface StaEntity {

    Dataset DATASTREAM = Dataset.DATASET;
    Procedure SENSOR = Procedure.PROCEDURE;
    Phenomenon OBSERVED_PROPERTY = Phenomenon.PHENOMENON;
    Platform THING = Platform.PLATFORM;
    Feature FEATURE_OF_INTEREST = Feature.FEATURE;
    Location LOCATION = Location.LOCATION;
    HistoricalLocation HISTORICAL_LOCATION = HistoricalLocation.HISTORICAL_LOCATION;
    Observation OBSERVATION = Observation.OBSERVATION;

    Unit UNIT = Unit.UNIT;
    Format FORMAT = Format.FORMAT;

    LocationHistoricalLocation LOCATION_HISTORICAL_LOCATION = LocationHistoricalLocation.LOCATION_HISTORICAL_LOCATION;
    PlatformLocation THING_LOCATION = PlatformLocation.PLATFORM_LOCATION;

    ObservationParameter OBSERVATION_PARAMETERS = ObservationParameter.OBSERVATION_PARAMETER;
    PhenomenonParameter OBSERVED_PROPERTY_PROPERTIES = PhenomenonParameter.PHENOMENON_PARAMETER;
    LocationParameter LOCATION_PROPERTIES = LocationParameter.LOCATION_PARAMETER;
    DatasetParameter DATASTREAM_PROPERTIES = DatasetParameter.DATASET_PARAMETER;
    FeatureParameter FEATURE_PROPERTIES = FeatureParameter.FEATURE_PARAMETER;
    ProcedureParameter SENSOR_PROPERTIES = ProcedureParameter.PROCEDURE_PARAMETER;
    PlatformParameter THING_PROPERTIES = PlatformParameter.PLATFORM_PARAMETER;

    static Field<?> alias(Table<?> table, Field<?> field) {
        return field.as(table.getName() + "_" + field.getName());
    }

    Long DATASET_AGGREGATION_MARKER = -1L;

}
