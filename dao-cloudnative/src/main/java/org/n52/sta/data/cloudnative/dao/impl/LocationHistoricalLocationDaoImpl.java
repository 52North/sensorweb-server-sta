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
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.LocationHistoricalLocationDao;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.LocationHistoricalLocation;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class LocationHistoricalLocationDaoImpl implements LocationHistoricalLocationDao {
    private final StaFirehoseClient firehoseClient;
    private final String tableName = "LOCATION_HISTORICAL_LOCATION";
    private final ObjectMapper mapper = new ObjectMapper();

    public LocationHistoricalLocationDaoImpl(StaFirehoseClient firehoseClient) {
        this.firehoseClient = firehoseClient;
    }

    @Override
    public void saveAll(Set<LocationHistoricalLocation> locationHistoricalLocations) throws STACRUDException {
        for (LocationHistoricalLocation locationHistoricalLocation : locationHistoricalLocations) {
            ObjectNode dataNode = mapper.convertValue(locationHistoricalLocation, ObjectNode.class);
            firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
        }
    }

    @Override
    public void deleteByLocationId(long locationId) throws STACRUDException {
        String key = StaEntity.LOCATION_HISTORICAL_LOCATION.FK_LOCATION_ID.getName();
        firehoseClient.icebergDeleteById(key, locationId, tableName);

    }

    @Override
    public void deleteByHistoricalLocationId(long historicalLocationId) throws STACRUDException {
        String key = StaEntity.LOCATION_HISTORICAL_LOCATION.FK_HISTORICAL_LOCATION_ID.getName();
        firehoseClient.icebergDeleteById(key, historicalLocationId, tableName);
    }
}
