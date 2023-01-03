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

import javax.management.Query;

import org.n52.sensorweb.server.helgoland.adapters.connector.request.AbstractHereonRequest;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;

public class GetFeaturesRequest extends AbstractHereonRequest {

    public GetFeaturesRequest(QueryOptions queryOptions, String metadata_id) {
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
        withWhere(String.format("metadata_id = '%s'", metadata_id));
    }
}
