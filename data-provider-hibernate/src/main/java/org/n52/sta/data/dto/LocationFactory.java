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
package org.n52.sta.data.dto;

import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.api.dto.LocationDto;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public final class LocationFactory extends BaseDtoFactory<LocationDto, LocationFactory> {

    private LocationFactory(LocationDto dto) {
        super(dto);
    }

    public static Location create(LocationEntity entity) {
        LocationFactory factory = create();
        factory.withMetadata(entity);
        factory.setProperties(entity);
        factory.setGeometry(entity.getGeometry());
        factory.setLocations(entity.getHistoricalLocations());
        factory.setThings(entity.getPlatforms());

        return factory.get();
    }

    public static LocationFactory create() {
        return new LocationFactory(new LocationDto());
    }

    private LocationFactory setLocations(Set<HistoricalLocationEntity> locations) {
        Streams.stream(locations).forEach(this::addLocation);
        return this;
    }

    private LocationFactory addLocation(HistoricalLocationEntity entity) {
        return addLocation(HistoricalLocationFactory.create(entity));
    }

    public LocationFactory addLocation(HistoricalLocation location) {
        get().addHistoricalLocation(location);
        return this;
    }

    private LocationFactory setThings(Set<PlatformEntity> platforms) {
        Streams.stream(platforms).forEach(this::addThing);
        return this;
    }

    private LocationFactory addThing(PlatformEntity entity) {
        return addThing(ThingFactory.create(entity));
    }

    private LocationFactory addThing(Thing thing) {
        get().addThing(thing);
        return this;
    }

    private void setGeometry(Geometry geometry) {
        get().setGeometry(geometry);
    }

}
