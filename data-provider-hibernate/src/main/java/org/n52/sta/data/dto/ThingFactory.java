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

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.api.dto.ThingDto;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public final class ThingFactory extends BaseDtoFactory<ThingDto, ThingFactory> {

    private ThingFactory(ThingDto dto) {
        super(dto);
    }

    public static Thing create(PlatformEntity entity) {
        ThingFactory factory = create();
        factory.withMetadata(entity);
        factory.setProperties(entity);
        factory.setDatastreams(entity);
        factory.setLocations(entity.getLocations());
        factory.setHistoricalLocations(entity.getHistoricalLocations());
        return factory.get();
    }

    public static ThingFactory create() {
        return new ThingFactory(new ThingDto());
    }

    private ThingFactory setDatastreams(PlatformEntity entity) {
        Set<AbstractDatasetEntity> datasets = entity.getDatasets();
        Streams.stream(datasets).forEach(this::addDatastream);
        return this;
    }

    private ThingFactory addDatastream(AbstractDatasetEntity entity) {
        get().addDatastream(DatastreamFactory.create(entity));
        return this;
    }

    public ThingFactory addDatastream(Datastream datastream) {
        get().addDatastream(datastream);
        return this;
    }

    private ThingFactory setLocations(Set<LocationEntity> locations) {
        Streams.stream(locations).forEach(this::addLocation);
        return this;
    }

    private ThingFactory addLocation(LocationEntity entity) {
        return addLocation(LocationFactory.create(entity));
    }

    public ThingFactory addLocation(Location location) {
        get().addLocation(location);
        return this;
    }

    private ThingFactory setHistoricalLocations(Set<HistoricalLocationEntity> locations) {
        Streams.stream(locations).forEach(this::addHistoricalLocation);
        return this;
    }

    private ThingFactory addHistoricalLocation(HistoricalLocationEntity entity) {
        return addHistoricalLocation(HistoricalLocationFactory.create(entity));
    }

    public ThingFactory addHistoricalLocation(HistoricalLocation location) {
        get().addHistoricalLocation(location);
        return this;
    }


}
