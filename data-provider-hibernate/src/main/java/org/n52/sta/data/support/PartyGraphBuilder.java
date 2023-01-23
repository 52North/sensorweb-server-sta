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

import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;

public final class PartyGraphBuilder extends GraphBuilder<PartyEntity> {

    private PartyGraphBuilder() {
        super(PartyEntity.class);
    }

    private PartyGraphBuilder(QueryOptions queryOptions) {
        super(PartyEntity.class);
        addUnfilteredExpandItems(queryOptions);
    }

    public static PartyGraphBuilder createEmpty() {
        return new PartyGraphBuilder();
    }

    public static PartyGraphBuilder createWith(QueryOptions queryOptions) {
        return new PartyGraphBuilder(queryOptions);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        if (expandItem != null) {
            switch (expandItem.getPath()) {
                case StaConstants.THINGS:
                    addGraphText(GraphText.GRAPH_THINGS);
                    break;
                case StaConstants.DATASTREAMS:
                    addGraphText(GraphText.GRAPH_DATASTREAMS);
                    break;
                case StaConstants.GROUPS:
                    addGraphText(GraphText.GRAPH_GROUPS);
                    break;
                default:
                    // no expand
            }
        }
    }

}
