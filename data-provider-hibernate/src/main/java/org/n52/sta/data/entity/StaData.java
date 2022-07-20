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

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.old.utils.TimeUtil;

public class StaData<T extends DescribableEntity> implements Identifiable {

    protected final T data;

    protected final EntityPropertyMapping propertyMapping;

    protected StaData(T dataEntity, EntityPropertyMapping propertyMapping) {
        Objects.requireNonNull(dataEntity, "dataEntity must not be null!");
        Objects.requireNonNull(propertyMapping, "propertyMapping must not be null");
        this.data = dataEntity;
        this.propertyMapping = propertyMapping;
    }
    
    public T getData() {
        return data;
    }

    @Override
    public String getId() {
        return data.getStaIdentifier();
    }

    protected Time toTime(Date time) {
        DateTime dateTime = TimeUtil.createDateTime(time);
        return TimeUtil.createTime(dateTime);
    }

    protected Time toTimeInterval(Date start, Date optionalEnd) {
        Optional<DateTime> begin = Optional.ofNullable(start)
                                           .map(TimeUtil::createDateTime);
        Optional<DateTime> end = Optional.ofNullable(optionalEnd)
                                         .map(TimeUtil::createDateTime);
        return begin.map(b -> TimeUtil.createTime(b, end.orElse(null)))
                    .orElse(null);
    }

    protected <E, U> Set<U> toSet(Collection<E> collection, Function<E, U> mapper) {
        return Streams.stream(collection)
                      .map(mapper)
                      .collect(Collectors.toSet());
    }

    protected Map<String, Object> toMap(Set<ParameterEntity< ? >> parameters) {
        return Streams.stream(parameters)
                      .collect(Collectors.toMap(ParameterEntity::getName, ParameterEntity::getValue));
    }

}
