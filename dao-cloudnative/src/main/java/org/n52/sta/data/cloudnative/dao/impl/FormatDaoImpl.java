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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.*;
import org.jooq.Record;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class FormatDaoImpl {

    private final DSLContext ctx;
    private final String tableName = "FORMAT";
    private final ObjectMapper mapper = new ObjectMapper();
    private final StaFirehoseClient firehoseClient;

    @Autowired
    public FormatDaoImpl(DSLContext ctx, StaFirehoseClient firehoseClient) {
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
    }


    public boolean existsByFormat(String definition) {
        Condition predicate = StaEntity.FORMAT.DEFINITION.eq(definition);
        Table<?> table = StaEntity.FORMAT;
        Long count = ctx
                .selectCount()
                .from(table)
                .where(predicate)
                .fetchOne(0, long.class);

        return count != null;
    }

    public Optional<Format> findByFormat(String definition) {
        Condition predicate = StaEntity.FORMAT.DEFINITION.eq(definition);
        // definition must be unique
        Record result = ctx.select().from(StaEntity.UNIT).where(predicate).fetchOne();
        return Optional.of(mapResultToPOJO(result));
    }

    private Format mapResultToPOJO(Record result) {
        Format formatPOJO = new Format();
        formatPOJO.setFormatId(result.get(StaEntity.FORMAT.FORMAT_ID));
        formatPOJO.setDefinition(result.get(StaEntity.FORMAT.DEFINITION));
        return formatPOJO;
    }

    public void save(Format formatPOJO) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(formatPOJO, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
    }
}
