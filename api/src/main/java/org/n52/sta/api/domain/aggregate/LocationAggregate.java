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

import org.locationtech.jts.geom.Geometry;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.domain.service.DomainService;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

import java.util.Map;
import java.util.Set;

public class LocationAggregate extends EntityAggregate<Location> implements Location {

    private final Location location;

    public LocationAggregate(Location location, DomainService<Location> domainService) {
        this(location, domainService, null);
    }

    public LocationAggregate(Location location, DomainService<Location> domainService, EntityEditor<Location> editor) {
        super(location, domainService, editor);
        this.location = location;
    }

    @Override
    public String getId() {
        return location.getId();
    }

    @Override
    public String getName() {
        return location.getName();
    }

    @Override
    public String getDescription() {
        return location.getDescription();
    }

    @Override
    public Map<String, Object> getProperties() {
        return location.getProperties();
    }

    @Override
    public String getEncodingType() {
        return location.getEncodingType();
    }

    @Override
    public Geometry getGeometry() {
        return location.getGeometry();
    }

    @Override
    public Set<HistoricalLocation> getHistoricalLocations() {
        return location.getHistoricalLocations();
    }

    @Override
    public Set<Thing> getThings() {
        return location.getThings();
    }
}
