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

package org.n52.sta.api.old.dto;

import java.util.Set;

import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.old.entity.HistoricalLocationDTO;
import org.n52.sta.api.old.entity.LocationDTO;
import org.n52.sta.api.old.entity.ThingDTO;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class HistoricalLocation extends DtoEntity implements HistoricalLocationDTO {

    private Time time;

    private Set<LocationDTO> locations;

    private ThingDTO thing;

    @Override
    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    @Override
    public Set<LocationDTO> getLocations() {
        return locations;
    }

    @Override
    public void setLocations(Set<LocationDTO> locations) {
        this.locations = locations;
    }

    @Override
    public ThingDTO getThing() {
        return thing;
    }

    public void setThing(ThingDTO thing) {
        this.thing = thing;
    }
}