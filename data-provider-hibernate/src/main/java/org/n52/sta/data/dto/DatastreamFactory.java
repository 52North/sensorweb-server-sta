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
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.dto.DatastreamDto;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Datastream.UnitOfMeasurement;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.old.utils.TimeUtil;

public final class DatastreamFactory extends BaseDtoFactory<DatastreamDto, DatastreamFactory> {

    public DatastreamFactory(DatastreamDto dto) {
        super(dto);
    }

    public static Datastream create(AbstractDatasetEntity entity) {
        DatastreamFactory factory = create();
        factory.withMetadata(entity);
        factory.setObservations(entity);
        factory.setObservationType(entity);
        factory.setObservedArea(entity.getGeometry());
        factory.setObservedProperty(entity.getPhenomenon());
        factory.setSensor(entity.getProcedure());
        factory.setThing(entity.getPlatform());

        // TODO includeDatastreamCategory
        factory.setProperties(entity);
        factory.setTime(entity);
        return factory.get();
    }

    public static DatastreamFactory create() {
        return new DatastreamFactory(new DatastreamDto());
    }

    private DatastreamFactory setThing(PlatformEntity entity) {
        return setThing(ThingFactory.create(entity));
    }

    public DatastreamFactory setThing(Thing thing) {
        get().setThing(thing);
        return this;
    }

    private DatastreamFactory setSensor(ProcedureEntity entity) {
        return setSensor(SensorFactory.create(entity));
    }

    public DatastreamFactory setSensor(Sensor sensor) {
        get().setSensor(sensor);
        return this;
    }

    private DatastreamFactory setObservedProperty(PhenomenonEntity entity) {
        return setObservedProperty(ObservedPropertyFactory.create(entity));
    }

    public DatastreamFactory setObservedProperty(ObservedProperty observedProperty) {
        get().setObservedProperty(observedProperty);
        return this;
    }

    private DatastreamFactory setObservationType(AbstractDatasetEntity entity) {
        FormatEntity type = entity.getOMObservationType();
        Optional<UnitOfMeasurement> unit = createUom(entity.getUnit());
        return setObservationType(type.getFormat(), unit.orElse(null));
    }

    public DatastreamFactory setObservationType(String type, Datastream.UnitOfMeasurement uom) {
        get().setObservationType(type);
        get().setUnitOfMeasurement(uom);
        return this;
    }

    public DatastreamFactory setObservedArea(Geometry geometry) {
        get().setObservedArea(geometry);
        return this;
    }

    private DatastreamFactory setTime(AbstractDatasetEntity entity) {
        Date samplingTimeStart = entity.getSamplingTimeStart();
        Date samplingTimeEnd = entity.getSamplingTimeEnd();
        Date resultTimeStart = entity.getResultTimeStart();
        Date resultTimeEnd = entity.getResultTimeEnd();

        Optional<DateTime> sStart = Optional.ofNullable(samplingTimeStart).map(TimeUtil::createDateTime);
        Optional<DateTime> sEnd = Optional.ofNullable(samplingTimeEnd).map(TimeUtil::createDateTime);
        Optional<DateTime> rStart = Optional.ofNullable(resultTimeStart).map(TimeUtil::createDateTime);
        Optional<DateTime> rEnd = Optional.ofNullable(resultTimeEnd).map(TimeUtil::createDateTime);

        Time phenomenonTime = sStart.map(start -> TimeUtil.createTime(start, sEnd.orElse(null))).orElse(null);
        Time resultTime = rStart.map(start -> TimeUtil.createTime(start, rEnd.orElse(null))).orElse(null);
        return setTime(phenomenonTime, resultTime);
    }

    public DatastreamFactory setTime(Time phenomenonTime, Time resultTime) {
        get().setPhenomenonTime(phenomenonTime);
        get().setResultTime(resultTime);
        return this;
    }

    public DatastreamFactory setObservations(AbstractDatasetEntity entity) {
        Set<DataEntity<?>> observations = entity.getObservations();
        Streams.stream(observations).forEach(this::addObservation);
        return this;
    }

    private DatastreamFactory addObservation(DataEntity<?> entity) {
        addObservation(ObservationFactory.create(entity));
        return this;
    }

    public DatastreamFactory addObservation(Observation<?> observation) {
        get().addObservation(observation);
        return this;
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
