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

import java.util.Date;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.dto.HistoricalLocationDto;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.old.utils.TimeUtil;

public final class HistoricalLocationFactory {

    private final HistoricalLocationDto dto;

    public HistoricalLocationFactory(HistoricalLocationDto dto) {
        this.dto = dto;
    }

    public static HistoricalLocation create(HistoricalLocationEntity entity) {
        HistoricalLocationFactory factory = create();
        factory.setIdentifier(entity.getStaIdentifier());
        factory.setThing(entity.getPlatform());
        factory.setTime(entity.getTime());
        factory.setLocations(entity);

        return factory.get();
    }

    public static HistoricalLocationFactory create() {
        return new HistoricalLocationFactory(new HistoricalLocationDto());
    }

    public HistoricalLocationFactory setIdentifier(String identifer) {
        dto.setId(identifer);
        return this;
    }

    private HistoricalLocationFactory setTime(Date time) {
        DateTime dateTime = TimeUtil.createDateTime(time);
        setTime(TimeUtil.createTime(dateTime));
        return this;
    }

    public HistoricalLocationFactory setTime(Time time) {
        dto.setTime(time);
        return this;
    }

    private HistoricalLocationFactory setLocations(HistoricalLocationEntity entity) {
        Set<LocationEntity> locations = entity.getLocations();
        Streams.stream(locations).forEach(this::addLocation);
        return this;
    }

    private HistoricalLocationFactory addLocation(LocationEntity entity) {
        dto.addLocation(LocationFactory.create(entity));
        return this;
    }

    private HistoricalLocationFactory setThing(PlatformEntity entity) {
        dto.setThing(ThingFactory.create(entity));
        return this;
    }

    public HistoricalLocation get() {
        return dto;
    }

}
