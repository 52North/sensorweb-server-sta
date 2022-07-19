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

import org.joda.time.DateTime;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.old.utils.TimeUtil;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ObservationData extends StaData<DataEntity<?>> implements Observation {

    public ObservationData(DataEntity<?> dataEntity, EntityPropertyMapping parameterProperties) {
        super(dataEntity, parameterProperties);
    }

    @Override
    public Time getPhenomenonTime() {
        Date samplingTimeStart = data.getSamplingTimeStart();
        Date samplingTimeEnd = data.getSamplingTimeEnd();
        Optional<DateTime> sStart = Optional.ofNullable(samplingTimeStart)
                                            .map(TimeUtil::createDateTime);
        Optional<DateTime> sEnd = Optional.ofNullable(samplingTimeEnd)
                                          .map(TimeUtil::createDateTime);
        return sStart.map(start -> TimeUtil.createTime(start, sEnd.orElse(null)))
                     .orElse(null);
    }

    @Override
    public Time getResultTime() {
        return toTime(data.getResultTime());
    }

    @Override
    public Object getResult() {
        Object value = data.getValue();
        Class< ? extends Object> type = value.getClass();
        if (Collection.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            Collection<DataEntity< ? >> items = (Collection<DataEntity< ? >>) value;
            return items.stream()
                        .map(v -> new ObservationData(v, propertyMapping))
                        .collect(Collectors.toSet());
        } else {
            return value;
        }
    }

    @Override
    public Object getResultQuality() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Time getValidTime() {
        Date samplingTimeStart = data.getSamplingTimeStart();
        Date samplingTimeEnd = data.getSamplingTimeEnd();
        return toTimeInterval(samplingTimeStart, samplingTimeEnd);
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = toMap(data.getParameters());
        String samplingGeometry = propertyMapping.getSamplingGeometry();
        if (samplingGeometry != null) {
            Optional<GeometryEntity> optionalSamplingGeometry = Optional.ofNullable(data.getGeometryEntity());
            optionalSamplingGeometry.ifPresent(entity -> parameters.put(samplingGeometry, entity.getGeometry()));
        }

        if ("profile".equals(getValueType())) {
            String verticalFrom = propertyMapping.getVerticalFrom();
            if (verticalFrom != null) {
                Optional.ofNullable(data.getVerticalFrom())
                        .ifPresent(entity -> parameters.put(verticalFrom, entity));
            }
            String verticalTo = propertyMapping.getVerticalTo();
            if (verticalTo != null) {
                Optional.ofNullable(data.getVerticalTo())
                        .ifPresent(entity -> parameters.put(verticalTo, entity));
            }
        }
        return parameters;
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return new FeatureOfInterestData(data.getFeature(), propertyMapping);
    }

    @Override
    public Datastream getDatastream() {
        return new DatastreamData(data.getDataset(), propertyMapping);
    }

    @Override
    public String getValueType() {
        return data.getValueType();
    }

}
