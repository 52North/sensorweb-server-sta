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

import java.util.Optional;
import java.util.Set;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.sta.api.dto.FeatureOfInterestDto;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;

public final class FeatureOfInterestFactory extends BaseDtoFactory<FeatureOfInterestDto, FeatureOfInterestFactory> {

    public FeatureOfInterestFactory(FeatureOfInterestDto dto) {
        super(dto);
    }

    public static FeatureOfInterest create(AbstractFeatureEntity<?> entity) {
        Optional<StaFeatureEntity<?>> feature = tryToCast(entity);
        return feature.map(FeatureOfInterestFactory::create)
                .orElseThrow(() -> new IllegalStateException("No StaFeature"));
    }

    public static FeatureOfInterest create(StaFeatureEntity<?> entity) {
        FeatureOfInterestFactory factory = create();
        factory.withMetadata(entity);
        factory.setProperties(entity);
        factory.setObservations(entity);
        factory.setFeature(entity);
        return factory.get();
    }

    public static FeatureOfInterestFactory create() {
        return new FeatureOfInterestFactory(new FeatureOfInterestDto());
    }

    private FeatureOfInterestFactory setObservations(StaFeatureEntity<?> entity) {
        Set<DataEntity<?>> observations = entity.getObservations();
        Streams.stream(observations).forEach(this::addObservation);
        return this;
    }

    private FeatureOfInterestFactory addObservation(DataEntity<?> entity) {
        return addObservation(ObservationFactory.create(entity));
    }

    public FeatureOfInterestFactory addObservation(Observation<?> observation) {
        get().addObservation(observation);
        return this;
    }

    private FeatureOfInterestFactory setFeature(StaFeatureEntity<?> entity) {
        get().setFeature(entity.getGeometry());
        return this;
    }

    private static Optional<StaFeatureEntity<?>> tryToCast(AbstractFeatureEntity<?> entity) {
        return entity instanceof StaFeatureEntity
                ? Optional.of((StaFeatureEntity<?>) entity)
                : Optional.empty();
    }

}
