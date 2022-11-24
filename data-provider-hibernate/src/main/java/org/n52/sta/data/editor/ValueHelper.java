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

package org.n52.sta.data.editor;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.joda.time.DateTime;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.data.repositories.value.FormatRepository;
import org.n52.sta.data.repositories.value.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

public class ValueHelper {

    @Autowired
    private FormatRepository formatRepository;

    @Autowired
    private UnitRepository unitRepository;

    public void setStartTime(Consumer<Date> setter, Time time) {
        if (time == null) {
            return;
        } else if (time instanceof TimeInstant) {
            setTime(setter, (TimeInstant) time);
        } else {
            TimePeriod period = (TimePeriod) time;
            DateTime startTime = period.getStart();
            setter.accept(startTime.toDate());
        }
    }

    public void setEndTime(Consumer<Date> setter, Time time) {
        if (time == null) {
            return;
        } else if (time instanceof TimeInstant) {
            setTime(setter, (TimeInstant) time);
        } else {
            TimePeriod period = (TimePeriod) time;
            DateTime endTime = period.getEnd();
            setter.accept(endTime.toDate());
        }
    }

    public void setTime(Consumer<Date> setter, TimeInstant time) {
        DateTime startTime = time.getValue();
        setter.accept(startTime.toDate());
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public void setFormat(Consumer<FormatEntity> setter, String format) {
        Objects.requireNonNull(format, "format must not be null");
        Optional<FormatEntity> entity = formatRepository.findByFormat(format);
        entity.ifPresentOrElse(
                               setter::accept,
                               () -> {
                                   FormatEntity formatEntity = new FormatEntity();
                                   formatEntity.setFormat(format);
                                   FormatEntity savedEntity = formatRepository.save(formatEntity);
                                   setter.accept(savedEntity);
                               });
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public void setUnit(Consumer<UnitEntity> setter, Datastream.UnitOfMeasurement uom) {
        Objects.requireNonNull(uom, "uom must not be null");
        String symbol = uom.getSymbol();
        Optional<UnitEntity> entity = unitRepository.findBySymbol(symbol);
        entity.ifPresentOrElse(
                               setter::accept,
                               () -> {
                                   UnitEntity unitEntity = new UnitEntity();
                                   unitEntity.setLink(uom.getDefinition());
                                   unitEntity.setName(uom.getName());
                                   unitEntity.setSymbol(symbol);
                                   UnitEntity savedUnit = unitRepository.save(unitEntity);
                                   setter.accept(savedUnit);
                               });
    }

}
