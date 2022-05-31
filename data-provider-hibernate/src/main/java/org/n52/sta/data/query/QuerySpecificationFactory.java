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
package org.n52.sta.data.query;

import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.data.query.specifications.BaseQuerySpecifications;
import org.n52.sta.data.query.specifications.DatastreamQuerySpecification;
import org.n52.sta.data.query.specifications.FeatureOfInterestQuerySpecification;
import org.n52.sta.data.query.specifications.HistoricalLocationQuerySpecification;
import org.n52.sta.data.query.specifications.LocationQuerySpecification;
import org.n52.sta.data.query.specifications.ObservationQuerySpecification;
import org.n52.sta.data.query.specifications.ObservedPropertyQuerySpecification;
import org.n52.sta.data.query.specifications.SensorQuerySpecification;
import org.n52.sta.data.query.specifications.ThingQuerySpecification;

@SuppressWarnings("checkstyle:multiplestringliterals")
public class QuerySpecificationFactory {

    // public SpatialQuerySpecifications<?> createSpatialSpecification(String name)
    //          throws STAInvalidFilterExpressionException {
    //     switch (name) {
    //         // case "Location":
    //         // case "Locations":
    //         // case "LocationEntity": {
    //         //     return new LocationQuerySpecifications();
    //         // }
    //         // case "FeatureEntity":
    //         // case "AbstractFeatureEntity":
    //         // case "FeatureOfInterest":
    //         // case "FeaturesOfInterest": {
    //         //     return new FeatureOfInterestQuerySpecifications();
    //         // }
    //         default:
    //             throw new STAInvalidFilterExpressionException("Unknown spatial resource: " + name);
    //     }
    // }

    public static BaseQuerySpecifications<?> createSpecification(String name)
        throws STAInvalidFilterExpressionException {
        switch (name) {
            case "PlatformEntity":
            case "Thing":
            case "Things": {
                return new ThingQuerySpecification();
            }
             case "LocationEntity":
             case "Location":
             case "Locations": {
                 return new LocationQuerySpecification();
             }
             case "HistoricalLocationEntity":
             case "HistoricalLocation":
             case "HistoricalLocations": {
                 return new HistoricalLocationQuerySpecification();
             }
             case "AbstractDatasetEntity":
             case "DatasetEntity":
             case "DatasetAggregationEntity":
             case "Datastream":
             case "Datastreams": {
                 return new DatastreamQuerySpecification();
             }
             case "ProcedureEntity":
             case "Sensor":
             case "Sensors": {
                 return new SensorQuerySpecification();
             }
             case "ObservationEntity":
             case "DataEntity":
             case "Observation":
             case "Observations": {
                 return new ObservationQuerySpecification();
             }
             case "FeatureEntity":
             case "AbstractFeatureEntity":
             case "FeatureOfInterest":
             case "FeaturesOfInterest": {
                 return new FeatureOfInterestQuerySpecification();
             }
             case "PhenomenonEntity":
             case "ObservedProperty":
             case "ObservedProperties": {
                 return new ObservedPropertyQuerySpecification();
             }
            default:
                throw new STAInvalidFilterExpressionException("Unable to find QuerySpecification for type: " + name);
        }
    }

}
