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
package org.n52.sta.cndao.condition;


import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class QueryConditionRepository {
    public static EntityQueryConditions getCondition(String name)
            throws STAInvalidFilterExpressionException {
        switch (name) {
            case "PlatformEntity":
            case "Thing":
            case "Things": {
                return new ThingQueryConditions();
            }
            case "LocationEntity":
            case "Location":
            case "Locations": {
                return new LocationQueryConditions();
            }
            case "HistoricalLocationEntity":
            case "HistoricalLocation":
            case "HistoricalLocations": {
                return new HistoricalLocationQueryConditions();
            }
            case "AbstractDatasetEntity":
            case "DatasetEntity":
            case "DatasetAggregationEntity":
            case "Datastream":
            case "Datastreams": {
                return new DatastreamQueryConditions();
            }
            case "ProcedureEntity":
            case "Sensor":
            case "Sensors": {
                return new SensorQueryConditions();
            }
            case "ObservationEntity":
            case "DataEntity":
            case "Observation":
            case "Observations": {
                return new ObservationQueryConditions();
            }
            case "FeatureEntity":
            case "AbstractFeatureEntity":
            case "FeatureOfInterest":
            case "FeaturesOfInterest": {
                return new FeatureOfInterestQueryConditions();
            }
            case "PhenomenonEntity":
            case "ObservedProperty":
            case "ObservedProperties": {
                return new ObservedPropertyQueryConditions();
            }
            default:
                throw new STAInvalidFilterExpressionException("Unable to find QueryCondition for type: " + name);
        }
    }

}
