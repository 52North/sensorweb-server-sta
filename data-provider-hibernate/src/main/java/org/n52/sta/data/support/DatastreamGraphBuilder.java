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

package org.n52.sta.data.support;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;

public final class DatastreamGraphBuilder extends GraphBuilder<AbstractDatasetEntity> {

    private DatastreamGraphBuilder() {
        super(AbstractDatasetEntity.class);
    }

    private DatastreamGraphBuilder(QueryOptions queryOptions) {
        super(AbstractDatasetEntity.class);
        addGraphText(GraphText.GRAPH_PARAMETERS);
        addGraphText(GraphText.GRAPH_OM_OBS_TYPE);
        addGraphText(GraphText.GRAPH_UOM);
        addUnfilteredExpandItems(queryOptions);
    }

    public static DatastreamGraphBuilder createEmpty() {
        return new DatastreamGraphBuilder();
    }

    public static DatastreamGraphBuilder createWith(QueryOptions queryOptions) {
        return new DatastreamGraphBuilder(queryOptions);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        if (expandItem != null) {
            switch (expandItem.getPath()) {
                case StaConstants.SENSOR:
                    addGraphText(GraphText.GRAPH_PROCEDURE);
                    break;
                case StaConstants.THING:
                    addGraphText(GraphText.GRAPH_PLATFORM);
                    break;
                case StaConstants.OBSERVED_PROPERTY:
                    addGraphText(GraphText.GRAPH_PHENOMENON);
                    break;
                case StaConstants.OBSERVATIONS:
                default:
                    // no expand
            }
        }
    }
}