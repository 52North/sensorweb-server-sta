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
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.sta.api.dto.ObservationDto;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.old.utils.TimeUtil;

public final class ObservationFactory<T> {

    private final ObservationDto<T> dto;

    private ObservationFactory(ObservationDto<T> dto) {
        this.dto = dto;
    }

    public static <E> Observation<E> create(DataEntity<E> entity) {
        ObservationFactory<E> factory = create();
        factory.setIdentifier(entity.getStaIdentifier());
        factory.setResult(entity.getValue());
        factory.setFeatureOfInterest(entity.getFeature());

        // TODO handle special properties (verticalTo, etc.)
        factory.setParameters(entity);
        factory.setTime(entity);
        return factory.get();
    }

    public static <E> ObservationFactory<E> create() {
        return new ObservationFactory<>(new ObservationDto<>());
    }

    public ObservationFactory<T> setIdentifier(String staIdentifier) {
        dto.setId(staIdentifier);
        return this;
    }

    public ObservationFactory<T> setResult(T value) {
        dto.setResult(value);
        return this;
    }

    private ObservationFactory<T> setFeatureOfInterest(AbstractFeatureEntity<?> entity) {
        return setFeatureOfInterest(FeatureOfInterestFactory.create(entity));
    }

    public ObservationFactory<T> setFeatureOfInterest(FeatureOfInterest feature) {
        dto.setFeatureOfInterest(feature);
        return this;
    }

    private ObservationFactory<T> setParameters(DataEntity<?> entity) {
        Set<ParameterEntity<?>> parameters = entity.getParameters();
        Streams.stream(parameters).forEach(this::addParameter);
        return this;
    }

    private ObservationFactory<T> addParameter(ParameterEntity<?> entity) {
        addProperty(entity.getName(), entity.getValue());
        return this;
    }

    public ObservationFactory<T> addProperty(String key, Object value) {
        dto.addParameter(key, value);
        return this;
    }

    private ObservationFactory<T> setTime(DataEntity<?> entity) {
        Date samplingTimeStart = entity.getSamplingTimeStart();
        Date samplingTimeEnd = entity.getSamplingTimeEnd();
        Date validTimeStart = entity.getValidTimeStart();
        Date validTimeEnd = entity.getValidTimeEnd();

        Optional<DateTime> sStart = Optional.ofNullable(samplingTimeStart).map(TimeUtil::createDateTime);
        Optional<DateTime> sEnd = Optional.ofNullable(samplingTimeEnd).map(TimeUtil::createDateTime);
        Optional<DateTime> vStart = Optional.ofNullable(validTimeStart).map(TimeUtil::createDateTime);
        Optional<DateTime> vEnd = Optional.ofNullable(validTimeEnd).map(TimeUtil::createDateTime);

        Time phenomenonTime = sStart.map(start -> TimeUtil.createTime(start, sEnd.orElse(null))).orElse(null);
        Time validTime = vStart.map(start -> TimeUtil.createTime(start, vEnd.orElse(null))).orElse(null);
        TimeInstant resultTime = Optional.ofNullable(entity.getResultTime()).map(TimeInstant::new).orElse(null);
        return setTime(phenomenonTime, validTime, resultTime);
    }

    public ObservationFactory<T> setTime(Time phenomenonTime, Time validTime, TimeInstant resultTime) {
        dto.setValidTime(phenomenonTime);
        dto.setResultTime(resultTime);
        dto.setPhenomenonTime(validTime);
        return this;
    }

    public Observation<T> get() {
        return dto;
    }

}
