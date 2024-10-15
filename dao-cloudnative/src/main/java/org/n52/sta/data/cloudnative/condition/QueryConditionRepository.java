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

import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.data.cloudnative.SpringContext;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class QueryConditionRepository {
    public static EntityQueryConditions getCondition(String name)
            throws STAInvalidFilterExpressionException {
        switch (name) {
            case "ThingDTO":
            case "Thing":
            case "Things": {
                return SpringContext.getBean(ThingQueryConditions.class);
            }
            case "LocationDTO":
            case "Location":
            case "Locations": {
                return SpringContext.getBean(LocationQueryConditions.class);
            }
            case "HistoricalLocationDTO":
            case "HistoricalLocation":
            case "HistoricalLocations": {
                return SpringContext.getBean(HistoricalLocationQueryConditions.class);
            }
            case "DatastreamDTO":
            case "Datastream":
            case "Datastreams": {
                return SpringContext.getBean(DatastreamQueryConditions.class);
            }
            case "SensorDTO":
            case "Sensor":
            case "Sensors": {
                return SpringContext.getBean(SensorQueryConditions.class);
            }
            case "ObservationDTO":
            case "Observation":
            case "Observations": {
                return SpringContext.getBean(ObservationQueryConditions.class);
            }
            case "FeatureOfInterestDTO":
            case "FeatureOfInterest":
            case "FeaturesOfInterest": {
                return SpringContext.getBean(FeatureOfInterestQueryConditions.class);
            }
            case "ObservedPropertyDTO":
            case "ObservedProperty":
            case "ObservedProperties": {
                return SpringContext.getBean(ObservedPropertyQueryConditions.class);
            }
            default:
                throw new STAInvalidFilterExpressionException("Unable to find QueryCondition for type: " + name);
        }
    }

}
