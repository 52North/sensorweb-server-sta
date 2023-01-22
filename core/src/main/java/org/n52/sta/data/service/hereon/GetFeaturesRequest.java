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

import org.n52.sensorweb.server.helgoland.adapters.connector.hereon.HereonConfig;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Observation;
import org.n52.sensorweb.server.helgoland.adapters.connector.request.AbstractHereonRequest;
import org.n52.shetland.filter.OrderProperty;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.svalbard.odata.core.expr.Expr;

public class GetFeaturesRequest extends AbstractHereonRequest {

    private String orderByFields;
    private FilterExprVisitor.FeatureQuery filterQuery;

    public GetFeaturesRequest(QueryOptions queryOptions, String metadata_id, HereonConfig config)
            throws STACRUDException {
        Observation observationMapping = config.getMapping().getObservation();

        // only retrieve fields that are actually mapped
        withOutField(DataFields.GLOBAL_ID + "," + String.join(",", observationMapping.getFields()));
        // filter features by metadata_id (and possibly more restrictions)
        parseFilter(queryOptions, observationMapping, metadata_id);
        withWhere(filterQuery.getWhere());

        // limit number of results returned if $top is present
        if (queryOptions.hasTopFilter()) {
            withResultRecordCount(queryOptions.getTopFilter().getValue());
        }

        // offset result number if $skip is present
        if (queryOptions.hasSkipFilter()) {
            withResultOffset(queryOptions.getSkipFilter().getValue());
        }

        // add custom ordering
        if (queryOptions.hasOrderByFilter()) {
            this.orderByFields = constructOrderBy(queryOptions.getOrderByFilter().getSortProperties(),
                    observationMapping);
        }

    }

    private void parseFilter(QueryOptions queryOptions, Observation mapping, String metadata_id)
            throws STACRUDException {

        FilterExprVisitor.FeatureQuery query = new FilterExprVisitor.FeatureQuery(
                String.format("(%s = '%s')", MetadataFields.METADATA_ID, metadata_id));

        // We have $filter clause
        if (queryOptions.hasFilterFilter()) {
            Expr filter = (Expr) queryOptions.getFilterFilter().getFilter();
            try {
                FilterExprVisitor.FeatureQuery filterExpression = filter.accept(new FilterExprVisitor<>(mapping));
                query.and(filterExpression);
            } catch (STAInvalidQueryException e) {
                throw new STACRUDException("unable to parse $filter", e);
            }
        }
        this.filterQuery = query;
    }

    private String constructOrderBy(List<OrderProperty> orderProperties, Observation observationMapping)
            throws STACRUDException {
        StringBuilder orderString = new StringBuilder();
        for (OrderProperty elem : orderProperties) {
            // Map STA property to Feature Service property
            switch (elem.getValueReference()) {
                case StaConstants.PROP_RESULT:
                    orderString.append(getOrError(observationMapping::getResult, elem.getValueReference()));
                    break;
                case StaConstants.PROP_PHENOMENON_TIME:
                    orderString.append(getOrError(observationMapping::getPhenomenonTime, elem.getValueReference()));
                    break;
                case StaConstants.PROP_RESULT_TIME:
                    orderString.append(getOrError(observationMapping::getResultTime, elem.getValueReference()));
                    break;
                case StaConstants.PROP_VALID_TIME:
                    orderString.append(getOrError(observationMapping::getValidTime, elem.getValueReference()));
                    break;
                case StaConstants.PROP_ID:
                    orderString.append(DataFields.GLOBAL_ID);
                    break;
                case StaConstants.PROP_RESULT_QUALITY:
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

    private static String getOrError(Supplier<String> supplier, String property) throws STACRUDException {
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
        if (filterQuery.getGeometry() != null) {
            map.put(Parameter.GEOMETRY, filterQuery.getGeometry());
            map.put(Parameter.GEOMETRY_TYPE, filterQuery.getGeometryType());
            map.put(Parameter.SPATIAL_RELATIONSHIP, filterQuery.getSpatialRel());
        }
    }
}
