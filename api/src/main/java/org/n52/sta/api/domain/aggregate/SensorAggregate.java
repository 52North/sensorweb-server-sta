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

import java.util.Map;
import java.util.Set;

import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Sensor;

public class SensorAggregate extends EntityAggregate<Sensor> implements Sensor {

    private final Sensor sensor;

    public SensorAggregate(Sensor sensor) {
        this(sensor, null);
    }

    public SensorAggregate(Sensor sensor, EntityEditor<Sensor> editor) {
        super(sensor, editor);
        this.sensor = sensor;
    }

    @Override
    public String getId() {
        return sensor.getId();
    }

    @Override
    public String getName() {
        return sensor.getName();
    }

    @Override
    public String getDescription() {
        return sensor.getDescription();
    }

    @Override
    public String getEncodingType() {
        return sensor.getEncodingType();
    }

    @Override
    public String getMetadata() {
        return sensor.getMetadata();
    }

    @Override
    public Map<String, Object> getProperties() {
        return sensor.getProperties();
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return sensor.getDatastreams();
    }
}
