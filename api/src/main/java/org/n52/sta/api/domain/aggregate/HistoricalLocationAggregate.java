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
package org.n52.sta.api.domain.aggregate;

import java.util.Set;

import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.domain.service.DomainService;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class HistoricalLocationAggregate extends EntityAggregate<HistoricalLocation> implements HistoricalLocation {

    private final HistoricalLocation historicalLocation;

    public HistoricalLocationAggregate(HistoricalLocation historicalLocation,
            DomainService<HistoricalLocation> domainService) {
        this(historicalLocation, domainService, null);
    }

    public HistoricalLocationAggregate(HistoricalLocation historicalLocation,
            DomainService<HistoricalLocation> domainService, EntityEditor<HistoricalLocation> editor) {
        super(historicalLocation, domainService, editor);
        this.historicalLocation = historicalLocation;
    }

    @Override
    public String getId() {
        return historicalLocation.getId();
    }

    @Override
    public Time getTime() {
        return historicalLocation.getTime();
    }

    @Override
    public Set<Location> getLocations() {
        return historicalLocation.getLocations();
    }

    @Override
    public Thing getThing() {
        return historicalLocation.getThing();
    }
}
