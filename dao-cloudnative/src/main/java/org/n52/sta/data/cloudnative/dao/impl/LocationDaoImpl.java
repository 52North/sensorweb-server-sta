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
import org.jooq.impl.DSL;
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
import org.n52.sta.data.cloudnative.condition.HistoricalLocationQueryConditions;
import org.n52.sta.data.cloudnative.condition.LocationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.condition.ThingQueryConditions;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.LocationDao;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Location;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class LocationDaoImpl extends AbstractStaEntityDao<LocationDTO> implements LocationDao {
    private final String tableName = getEntityTable().getName();
    private final String parameterTableName = StaEntity.FEATURE_PROPERTIES.getName();
    private final HistoricalLocationQueryConditions hlQC;
    private final LocationQueryConditions lQC;
    private final ThingQueryConditions tQC;
    public LocationDaoImpl(DSLContext ctx,
                           StaFirehoseClient firehoseClient,
                           HistoricalLocationQueryConditions hlQC,
                           LocationQueryConditions lQC,
                           ThingQueryConditions tQC) {
        super(ctx, firehoseClient);
        this.hlQC = hlQC;
        this.lQC = lQC;
        this.tQC = tQC;
    }

    @Override
    public List<LocationDTO> findAllByThingId(Long id, Class<LocationDTO> entityClass)
            throws STAInvalidQueryException {
        Condition predicate = DSL.exists(
                ctx.selectOne()
                        .from(StaEntity.THING_LOCATION)
                        .join(StaEntity.THING)
                        .onKey()
                        .where(StaEntity.THING.PLATFORM_ID.eq(id))
                        .and(StaEntity.THING_LOCATION.FK_LOCATION_ID.eq(StaEntity.LOCATION.LOCATION_ID))
        );
        return findAll(predicate, null, entityClass);
    }

    @Override
    protected List<LocationDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, LocationDTO> locationMap = new TreeMap<>();
        for (Record record : result) {
            Long Id = (Long) record.get(StaEntity.alias(
                    StaEntity.LOCATION,
                    StaEntity.LOCATION.LOCATION_ID));

            LocationDTO location = locationMap
                    .computeIfAbsent(Id, k -> record.map(new DTOMapper.LocationRecordMapper()));

            if (joins.contains(StaEntity.HISTORICAL_LOCATION)) {
                location.setHistoricalLocations(Optional.ofNullable(location.getHistoricalLocations())
                        .orElse(new HashSet<>()));
                HistoricalLocationDTO historicalLocation = record.map(new DTOMapper.HistoricalLocationRecordMapper());
                if (historicalLocation.getId() != null) {
                    location.getHistoricalLocations().add(historicalLocation);
                }
            }
            if (joins.contains(StaEntity.THING)) {
                location.setThings(Optional.ofNullable(location.getThings()).orElse(new HashSet<>()));
                ThingDTO thing = record.map(new DTOMapper.ThingRecordMapper());
                if(thing.getId() != null){
                    location.getThings().add(thing);
                }
            }
            if (joins.contains(StaEntity.LOCATION_PROPERTIES)) {
                ObjectNode properties = record.map(new DTOMapper.LocationRecordMapper.
                        LocationParameterRecordMapper());
                if (properties != null) {
                    location.setProperties(Optional.ofNullable(location.getProperties())
                            .orElse(new ObjectMapper().createObjectNode()));
                    location.getProperties().setAll(properties);
                }
            }

        }
        return new ArrayList<>(locationMap.values());
    }

    @Override
    public boolean existsByName(String name, Class<LocationDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.LOCATION.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<LocationDTO> findByName(String name, Class<LocationDTO> entityClass)
            throws STAInvalidQueryException {
        Condition predicate = StaEntity.LOCATION.NAME.eq(name);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, null).fetch();
        return Optional.of(mapResultToDTO(result).get(0));
    }

    @Override
    public Table<?> createJoinList(QueryOptions queryOptions, Table<?> table, List<Field<?>> select)
            throws STAInvalidQueryException {
        joins = new LinkedHashSet<>();

        joins.add(StaEntity.LOCATION_PROPERTIES);
        table = table.leftJoin(StaEntity.LOCATION_PROPERTIES)
                .on(StaEntity.LOCATION.LOCATION_ID
                        .eq(StaEntity.LOCATION_PROPERTIES.FK_LOCATION_ID));
        if (queryOptions == null ||
                queryOptions.getSelectFilter() == null) {
            select.addAll(getStaEntityFields(StaEntity.LOCATION_PROPERTIES));
        }

        if (queryOptions != null && queryOptions.getExpandFilter() != null) {
            for (ExpandItem expandItem : queryOptions.getExpandFilter().getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() ||
                        expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.HISTORICAL_LOCATIONS:

                        joins.add(StaEntity.LOCATION_HISTORICAL_LOCATION);
                        table = table.leftJoin(StaEntity.LOCATION_HISTORICAL_LOCATION)
                                        .on(StaEntity.LOCATION_HISTORICAL_LOCATION.FK_LOCATION_ID
                                                .eq(StaEntity.LOCATION.LOCATION_ID));

                        joins.add(StaEntity.HISTORICAL_LOCATION);
                        table = table.leftJoin(StaEntity.HISTORICAL_LOCATION)
                                .on(StaEntity.HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID
                                        .eq(StaEntity.LOCATION_HISTORICAL_LOCATION.FK_HISTORICAL_LOCATION_ID));

                        if (expandItem.getQueryOptions() == null ||
                                expandItem.getQueryOptions().getSelectFilter() == null) {
                            select.addAll(getStaEntityFields(StaEntity.HISTORICAL_LOCATION));
                        } else {
                            select.addAll(expandItem
                                    .getQueryOptions()
                                    .getSelectFilter()
                                    .getItems()
                                    .stream()
                                    .map(hlQC::checkPropertyName)
                                    .collect(Collectors.toList()));
                        }
                        break;
                    case STAEntityDefinition.THINGS:

                        joins.add(StaEntity.THING_LOCATION);
                        table = table.leftJoin(StaEntity.THING_LOCATION)
                                        .on(StaEntity.THING_LOCATION.FK_LOCATION_ID
                                                .eq(StaEntity.LOCATION.LOCATION_ID));

                        joins.add(StaEntity.THING);
                        table = table.leftJoin(StaEntity.THING)
                                .on(StaEntity.THING.PLATFORM_ID
                                        .eq(StaEntity.THING_LOCATION.FK_PLATFORM_ID));

                        if (expandItem.getQueryOptions() == null ||
                                expandItem.getQueryOptions().getSelectFilter() == null) {
                            select.addAll(getStaEntityFields(StaEntity.THING));
                        } else {
                            select.addAll(expandItem
                                    .getQueryOptions()
                                    .getSelectFilter()
                                    .getItems()
                                    .stream()
                                    .map(tQC::checkPropertyName)
                                    .collect(Collectors.toList()));
                        }
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(
                                INVALID_EXPAND_OPTION_SUPPLIED,
                                expandProperty,
                                StaConstants.LOCATION)
                        );
                }

            }
        }
        return table;
    }

    @Override
    public Field<?> checkPropertyName(String property) {
        return lQC.checkPropertyName(property);
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.LOCATION;
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.LOCATION.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.LOCATION.LOCATION_ID;
    }

    public void save(Location location) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(location, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
    }

    public void update(Location location) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(location, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.UPDATE);
    }

    @Override
    public void deleteByStaIdentifier(String staIdentifier) throws STACRUDException {
        // TODO: Firehose unstable
        firehoseClient.icebergDeleteById(StaEntity.LOCATION.LOCATION_ID.getName(),
                Long.parseLong(staIdentifier),
                tableName);
        // firehoseClient.icebergDeleteByStaIdentifier(staIdentifier, tableName);
    }

    public void saveLocationParameters(String id, ObjectNode parameters) throws STACRUDException {
        String foreignKey = StaEntity.LOCATION_PROPERTIES.FK_LOCATION_ID.getName();
        firehoseClient.icebergMergeParameters(parameters, parameterTableName, FirehoseConstants.INSERT, foreignKey, id);
    }

    public void deleteLocationParameters(Long id) throws STACRUDException {
        String key = StaEntity.LOCATION_PROPERTIES.FK_LOCATION_ID.getName();
        firehoseClient.icebergDeleteById(key, id, parameterTableName);
    }
}
