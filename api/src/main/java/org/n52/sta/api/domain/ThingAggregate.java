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
package org.n52.sta.api.domain;

import java.util.Map;
import java.util.Set;

import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class ThingAggregate extends EntityAggregate<Thing> implements Thing {

    private final Thing thing;

    public ThingAggregate(Thing thing, DomainService<Thing> domainService) {
        this(thing, domainService, null);
    }

    public ThingAggregate(Thing thing, DomainService<Thing> domainService, EntityEditor<Thing> editor) {
        super(thing, domainService, editor);
        this.thing = thing;
    }

    public String getId() {
        return thing.getId();
    }

    public String getName() {
        return thing.getName();
    }

    public String getDescription() {
        return thing.getDescription();
    }

    public Map<String, Object> getProperties() {
        return thing.getProperties();
    }

    public Set<HistoricalLocation> getHistoricalLocations() {
        return thing.getHistoricalLocations();
    }

    public Set<Location> getLocations() {
        return thing.getLocations();
    }

    public Set<Datastream> getDatastreams() {
        return thing.getDatastreams();
    }



}
