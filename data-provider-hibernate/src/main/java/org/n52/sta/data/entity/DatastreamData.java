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

package org.n52.sta.data.entity;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.config.EntityPropertyMapping;

public class DatastreamData extends StaData<AbstractDatasetEntity> implements Datastream {

    public DatastreamData(AbstractDatasetEntity dataEntity, Optional<EntityPropertyMapping> propertyMapping) {
        super(dataEntity, propertyMapping);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public String getDescription() {
        return data.getDescription();
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(data.getParameters());
    }

    @Override
    public String getObservationType() {
        FormatEntity type = data.getOMObservationType();
        return type != null
                ? type.getFormat()
                : null;
    }

    @Override
    public UnitOfMeasurement getUnitOfMeasurement() {
        return createUom(data.getUnit()).orElse(null);
    }

    @Override
    public Geometry getObservedArea() {
        return data.getGeometry();
    }

    @Override
    public Time getPhenomenonTime() {
        Date samplingTimeStart = data.getSamplingTimeStart();
        Date samplingTimeEnd = data.getSamplingTimeEnd();
        return toTimeInterval(samplingTimeStart, samplingTimeEnd);
    }

    @Override
    public Time getResultTime() {
        Date resultTimeStart = data.getResultTimeStart();
        Date resultTimeEnd = data.getResultTimeEnd();
        return toTimeInterval(resultTimeStart, resultTimeEnd);
    }

    @Override
    public Thing getThing() {
        return new ThingData(data.getPlatform(), propertyMapping);
    }

    @Override
    public Sensor getSensor() {
        return new SensorData(data.getProcedure(), propertyMapping);
    }

    @Override
    public ObservedProperty getObservedProperty() {
        return new ObservedPropertyData(data.getObservableProperty(), propertyMapping);
    }

    @Override
    public Set<Observation> getObservations() {
        //@formatter:off
        return toSet(data.getObservations(), entity -> new ObservationData(entity, propertyMapping.orElseThrow(
        () -> new RuntimeException("no property mapping supplied!"))));
        //@formatter:on
    }

    private Optional<Datastream.UnitOfMeasurement> createUom(UnitEntity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        String symbol = entity.getSymbol();
        String name = entity.getName();
        String definition = entity.getLink();
        return Optional.of(new Datastream.UnitOfMeasurement(symbol, name, definition));
    }
}
