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
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Record;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.dao.ThingLocationDao;
import org.n52.sta.data.cloudnative.schema.tables.pojos.PlatformLocation;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ThingLocationDaoImpl implements ThingLocationDao {

    private final StaFirehoseClient firehoseClient;
    private final String tableName = "PLATFORM_LOCATION";
    private final ObjectMapper mapper = new ObjectMapper();
    private final DSLContext ctx;

    public ThingLocationDaoImpl(StaFirehoseClient firehoseClient, DSLContext ctx) {
        this.firehoseClient = firehoseClient;
        this.ctx = ctx;
    }

    @Override
    public void saveAll(Set<PlatformLocation> thingLocations) throws STACRUDException {
        for (PlatformLocation thingLocation : thingLocations) {
            ObjectNode dataNode = mapper.convertValue(thingLocation, ObjectNode.class);
            firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
        }
    }

    @Override
    public void deleteByLocationId(long locationId) throws STACRUDException {
        // TODO: Firehose unstable
        Result<Record1<Long>> ret = ctx
                .select(StaEntity.THING_LOCATION.FK_PLATFORM_ID)
                .from(StaEntity.THING_LOCATION)
                .where(StaEntity.THING_LOCATION.FK_LOCATION_ID.eq(locationId)).fetch();
        for (Record1<Long> record : ret) {
            Long platformId = record.value1();
            if (platformId != null) {
                deleteByThingId(platformId);
            }
        }
        /*String key = StaEntity.THING_LOCATION.FK_LOCATION_ID.getName();
        firehoseClient.icebergDeleteById(key, locationId, tableName);*/

    }

    @Override
    public void deleteByThingId(long thingId) throws STACRUDException {
        String key = StaEntity.THING_LOCATION.FK_PLATFORM_ID.getName();
        firehoseClient.icebergDeleteById(key, thingId, tableName);
    }
}
