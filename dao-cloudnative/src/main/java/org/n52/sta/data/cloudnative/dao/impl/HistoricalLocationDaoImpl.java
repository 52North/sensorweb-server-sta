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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.*;
import org.jooq.Record;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.*;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.HistoricalLocationDao;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.HistoricalLocation;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class HistoricalLocationDaoImpl
        extends AbstractStaEntityDao<HistoricalLocationDTO> implements HistoricalLocationDao {
    private final String tableName = getEntityTable().getName();
    private final LocationQueryConditions lQC;
    private final ThingQueryConditions tQC;
    private final HistoricalLocationQueryConditions hlQC;
    public HistoricalLocationDaoImpl(DSLContext ctx,
                                     StaFirehoseClient firehoseClient,
                                     LocationQueryConditions lQC,
                                     ThingQueryConditions tQC,
                                     HistoricalLocationQueryConditions hlQC) {
        super(ctx, firehoseClient);
        this.lQC = lQC;
        this.tQC = tQC;
        this.hlQC = hlQC;
    }

    @Override
    protected List<HistoricalLocationDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, HistoricalLocationDTO> historicalLocationMap = new TreeMap<>();
        for (Record record : result) {

            Long id = (Long) record.get(
                    StaEntity.alias(StaEntity.HISTORICAL_LOCATION,
                            StaEntity.HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID));

            HistoricalLocationDTO historicalLocation = historicalLocationMap.computeIfAbsent(id,
                    k -> record.map(new DTOMapper.HistoricalLocationRecordMapper()));

            if (joins.contains(StaEntity.LOCATION)) {
                historicalLocation.setLocations(Optional.ofNullable(historicalLocation.getLocations())
                        .orElse(new HashSet<>()));
                LocationDTO loc = record.map(new DTOMapper.LocationRecordMapper());
                if (loc.getId() != null) {
                    historicalLocation.getLocations().add(loc);
                }
            }
            if (joins.contains(StaEntity.THING)) {
                ThingDTO thing = record.map(new DTOMapper.ThingRecordMapper());
                if (thing.getId() != null) {
                    historicalLocation.setThing(thing);
                }
            }
        }
        return new ArrayList<>(historicalLocationMap.values());
    }

    @Override
    public Table<?> createJoinList(QueryOptions queryOptions, Table<?> table, List<Field<?>> select)
            throws STAInvalidQueryException {

        joins = new LinkedHashSet<>();

        if (queryOptions != null && queryOptions.getExpandFilter() != null) {
            for (ExpandItem expandItem : queryOptions.getExpandFilter().getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() ||
                        expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.LOCATIONS:

                        joins.add(StaEntity.LOCATION_HISTORICAL_LOCATION);
                        table = table.leftJoin(StaEntity.LOCATION_HISTORICAL_LOCATION)
                                        .on(StaEntity.LOCATION_HISTORICAL_LOCATION.FK_HISTORICAL_LOCATION_ID
                                                .eq(StaEntity.HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID));

                        joins.add(StaEntity.LOCATION);
                        table = table.leftJoin(StaEntity.LOCATION)
                                .on(StaEntity.LOCATION.LOCATION_ID
                                        .eq(StaEntity.LOCATION_HISTORICAL_LOCATION.FK_LOCATION_ID));

                        if (expandItem.getQueryOptions() == null ||
                                expandItem.getQueryOptions().getSelectFilter() == null) {
                            select.addAll(getStaEntityFields(StaEntity.LOCATION));
                        } else {
                            select.addAll(expandItem
                                    .getQueryOptions()
                                    .getSelectFilter()
                                    .getItems()
                                    .stream()
                                    .map(lQC::checkAliasedPropertyName)
                                    .collect(Collectors.toList()));
                        }

                        break;
                    case STAEntityDefinition.THING:
                        // fallthru
                        // The UML in Section 8.2 of the OGC STA v1.0 defines the relations as "Things"
                        // The Definition in Section 8.2.3 of the OGC STA v1.0 defines the relations as "Thing"
                        // We will allow both for now
                    case STAEntityDefinition.THINGS:

                        joins.add(StaEntity.THING);
                        table = table.leftJoin(StaEntity.THING)
                                .on(StaEntity.THING.PLATFORM_ID
                                        .eq(StaEntity.HISTORICAL_LOCATION.FK_PLATFORM_ID));

                        if (expandItem.getQueryOptions() == null ||
                                expandItem.getQueryOptions().getSelectFilter() == null) {
                            select.addAll(getStaEntityFields(StaEntity.THING));
                        } else {
                            select.addAll(expandItem
                                    .getQueryOptions()
                                    .getSelectFilter()
                                    .getItems()
                                    .stream()
                                    .map(tQC::checkAliasedPropertyName)
                                    .collect(Collectors.toList()));
                        }
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                expandProperty,
                                StaConstants.HISTORICAL_LOCATION));
                }
            }
        }
        return table;
    }

    @Override
    public Field<?> checkAliasedPropertyName(String property) {
        return hlQC.checkAliasedPropertyName(property);
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.HISTORICAL_LOCATION;
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.HISTORICAL_LOCATION.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID;
    }

    public void save(HistoricalLocation historicalLocation) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(historicalLocation, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
    }

    public void update(HistoricalLocation historicalLocation) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(historicalLocation, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.UPDATE);
    }

    @Override
    public void deleteByStaIdentifier(String staIdentifier) throws STACRUDException {
        // TODO: Firehose unstable
        firehoseClient.icebergDeleteById(StaEntity.HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID.getName(),
                Long.parseLong(staIdentifier),
                tableName);
        // firehoseClient.icebergDeleteByStaIdentifier(staIdentifier, tableName);
    }
}
