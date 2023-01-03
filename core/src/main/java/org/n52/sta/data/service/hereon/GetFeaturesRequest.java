/*
 * Copyright (C) 2018-2022 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.data.service.hereon;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.management.Query;

import org.n52.sensorweb.server.helgoland.adapters.connector.HereonConstants;
import org.n52.sensorweb.server.helgoland.adapters.connector.hereon.HereonConfig;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Observation;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.AbstractHereonRequest;
import org.n52.shetland.filter.OrderProperty;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;

public class GetFeaturesRequest extends AbstractHereonRequest {

    private String orderByFields = null;

    public GetFeaturesRequest(QueryOptions queryOptions, String metadata_id, HereonConfig config)
            throws STACRUDException {
        //TODO: only request fields needed for serializing to reduce payload size
        withOutField("*");

        if (queryOptions.hasTopFilter()) {
            withResultRecordCount(queryOptions.getTopFilter().getValue());
        }

        if (queryOptions.hasSkipFilter()) {
            withResultOffset(queryOptions.getSkipFilter().getValue());
        }

        //TODO: construct via
        // https://github.com/52North/arctic-sea/blob/master/shetland/arcgis/src/main/java/org/n52/shetland/arcgis/service/feature/FeatureServiceConstants.java
        withWhere(String.format("%s = '%s'", MetadataFields.METADATA_ID, metadata_id));

        if (queryOptions.hasOrderByFilter()) {
            Observation observationMapping = config.getMapping().getObservation();
            this.orderByFields = constructOrderBy(queryOptions.getOrderByFilter().getSortProperties(),
                                                  observationMapping);
        }

    }

    private String constructOrderBy(List<OrderProperty> orderProperties, Observation observationMapping)
            throws STACRUDException {
        StringBuilder orderString = new StringBuilder();
        for (OrderProperty elem : orderProperties) {
            // Map STA property to Feature Service property
            switch (elem.getValueReference()) {
                case "result":
                    orderString.append(getOrError(observationMapping::getResult, elem.getValueReference()));
                    break;
                case "phenomenonTime":
                    orderString.append(getOrError(observationMapping::getPhenomenonTime, elem.getValueReference()));
                    break;
                case "resultTime":
                    orderString.append(getOrError(observationMapping::getResultTime, elem.getValueReference()));
                    break;
                case "validTime":
                    orderString.append(getOrError(observationMapping::getValidTime, elem.getValueReference()));
                    break;
                case "id":
                    orderString.append(DataFields.GLOBAL_ID);
                    break;
                case "resultQuality":
                    orderString.append(getOrError(observationMapping::getResultQuality, elem.getValueReference()));
                    break;
                default:
                    throw new STACRUDException(String.format("cannot order by %s. No such property!",
                                                             elem.getValueReference()));
            }
            orderString.append(" ");
            if (elem.isSetSortOrder()) {
                orderString.append(elem.getSortOrder().toString());
            } else {
                // Default to ascending as specified in STA v1.1 Section 9.3.3.1
                orderString.append("asc");
            }
        }

        return orderString.toString();
    }

    private String getOrError(Supplier<String> supplier, String property) throws STACRUDException {
        String value = supplier.get();
        if (value == null || value.equals("")) {
            throw new STACRUDException(String.format("cannot order by %s. property is not mapped!", property));
        } else {
            return value;
        }
    }

    @Override
    protected void addQueryParameters(Map<String, String> map) {
        super.addQueryParameters(map);
        if (orderByFields != null) {
            map.put(Parameter.ORDER_BY_FIELDS, orderByFields);
        }
    }
}
