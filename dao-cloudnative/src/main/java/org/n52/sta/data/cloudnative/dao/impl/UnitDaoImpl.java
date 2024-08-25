/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.sta.data.cloudnative.dao.impl;

import org.jooq.*;
import org.jooq.Record;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class UnitDaoImpl {
    private DSLContext ctx;

    @Autowired
    public UnitDaoImpl(DSLContext ctx) {
        this.ctx = ctx;
    }

    public boolean existsBySymbol(String symbol) {
        Condition predicate = StaEntity.UNIT.SYMBOL.eq(symbol);
        Table<?> table = StaEntity.UNIT;
        Long count = ctx
                .selectCount()
                .from(table)
                .where(predicate)
                .fetchOne(0, long.class);

        return count != null;
    }

    public void save(Unit unitPOJO) {
        // TODO
    }

    public Optional<Unit> findBySymbol(String symbol) {
        Condition predicate = StaEntity.UNIT.SYMBOL.eq(symbol);
        // symbol must be unique
        Record result = ctx.select().from(StaEntity.UNIT).where(predicate).fetchOne();
        return Optional.of(mapResultToPOJO(result));
    }

    private Unit mapResultToPOJO(Record result) {
        Unit unitPOJO = new Unit();
        unitPOJO.setSymbol(result.getValue(StaEntity.UNIT.SYMBOL));
        unitPOJO.setUnitId(result.getValue(StaEntity.UNIT.UNIT_ID));
        unitPOJO.setName(result.getValue(StaEntity.UNIT.NAME));
        unitPOJO.setLink(result.getValue(StaEntity.UNIT.LINK));
        return unitPOJO;
    }

}
