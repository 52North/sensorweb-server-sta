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
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.*;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.dao.ThingDao;
import org.n52.sta.data.cloudnative.schema.tables.Format;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Platform;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ThingDaoImpl
        extends AbstractStaEntityDao<ThingDTO> implements ThingDao {
    private final String tableName = getEntityTable().getName();
    private final String parameterTableName = StaEntity.THING_PROPERTIES.getName();
    private final ThingQueryConditions tQC;
    private final LocationQueryConditions lQC;
    private final DatastreamQueryConditions dsQC;
    private final HistoricalLocationQueryConditions hlQC;

    public ThingDaoImpl(DSLContext ctx,
                        StaFirehoseClient firehoseClient,
                        ThingQueryConditions tQC,
                        LocationQueryConditions lQC,
                        DatastreamQueryConditions dsQC,
                        HistoricalLocationQueryConditions hlQC) {
        super(ctx, firehoseClient);
        this.tQC = tQC;
        this.lQC = lQC;
        this.dsQC = dsQC;
        this.hlQC = hlQC;
    }

    @Override
    public boolean existsByName(String name, Class<ThingDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.THING.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<ThingDTO> findByName(String name, Class<ThingDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.THING.NAME.eq(name);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, null).fetch();
        return Optional.of(mapResultToDTO(result).get(0));
    }

    @Override
    public Table<?> createJoinList(QueryOptions queryOptions, Table<?> table, List<Field<?>> select)
            throws STAInvalidQueryException {
        joins = new LinkedHashSet<>();

        joins.add(StaEntity.THING_PROPERTIES);
        table = table.leftJoin(StaEntity.THING_PROPERTIES)
                .on(StaEntity.THING_PROPERTIES.FK_PLATFORM_ID
                        .eq(StaEntity.THING.PLATFORM_ID));

        if (queryOptions == null ||
                queryOptions.getSelectFilter() == null) {
            select.addAll(getStaEntityFields(StaEntity.THING_PROPERTIES));
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

                        joins.add(StaEntity.HISTORICAL_LOCATION);
                        table = table.leftJoin(StaEntity.HISTORICAL_LOCATION)
                                .on(StaEntity.HISTORICAL_LOCATION.FK_PLATFORM_ID
                                        .eq(StaEntity.THING.PLATFORM_ID));

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
                    case STAEntityDefinition.DATASTREAMS:

                        joins.add(StaEntity.DATASTREAM);
                        table = table.leftJoin(StaEntity.DATASTREAM)
                                .on(StaEntity.DATASTREAM.FK_PLATFORM_ID
                                        .eq(StaEntity.THING.PLATFORM_ID));

                        Format DATASTREAM_FORMAT = StaEntity.FORMAT.as("DATASTREAM_FORMAT");
                        joins.add(DATASTREAM_FORMAT);
                        table = table.leftJoin(DATASTREAM_FORMAT)
                                .on(DATASTREAM_FORMAT.FORMAT_ID
                                        .eq(StaEntity.DATASTREAM.FK_FORMAT_ID));

                        joins.add(StaEntity.UNIT);
                        table = table.leftJoin(StaEntity.UNIT)
                                .on(StaEntity.DATASTREAM.FK_UNIT_ID
                                        .eq(StaEntity.UNIT.UNIT_ID));

                        if (expandItem.getQueryOptions() == null ||
                                expandItem.getQueryOptions().getSelectFilter() == null) {
                            select.addAll(getStaEntityFields(StaEntity.DATASTREAM));
                            select.addAll(getStaEntityFields(DATASTREAM_FORMAT));
                            select.addAll(getStaEntityFields(StaEntity.UNIT));
                        } else {
                            select.addAll(expandItem
                                    .getQueryOptions()
                                    .getSelectFilter()
                                    .getItems()
                                    .stream()
                                    .map(dsQC::checkPropertyName)
                                    .collect(Collectors.toList()));
                        }

                        break;
                    case STAEntityDefinition.LOCATIONS:
                        joins.add(StaEntity.THING_LOCATION);
                        table = table.leftJoin(StaEntity.THING_LOCATION)
                                .on(StaEntity.THING_LOCATION.FK_PLATFORM_ID
                                        .eq(StaEntity.THING.PLATFORM_ID));

                        joins.add(StaEntity.LOCATION);
                        table = table.leftJoin(StaEntity.LOCATION)
                                .on(StaEntity.LOCATION.LOCATION_ID
                                        .eq(StaEntity.THING_LOCATION.FK_LOCATION_ID));

                        if (expandItem.getQueryOptions() == null ||
                                expandItem.getQueryOptions().getSelectFilter() == null) {
                            select.addAll(getStaEntityFields(StaEntity.LOCATION));
                        } else {
                            select.addAll(expandItem
                                    .getQueryOptions()
                                    .getSelectFilter()
                                    .getItems()
                                    .stream()
                                    .map(lQC::checkPropertyName)
                                    .collect(Collectors.toList()));
                        }

                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                expandProperty,
                                StaConstants.THING));
                }
            }
        }
        return table;
    }

    @Override
    protected List<ThingDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, ThingDTO> thingMap = new TreeMap<>();
        for (Record record : result) {
            Long Id = (Long) record.get(StaEntity.alias(
                    StaEntity.THING,
                    StaEntity.THING.PLATFORM_ID));

            ThingDTO thing = thingMap.computeIfAbsent(Id, k -> record.map(new DTOMapper.ThingRecordMapper()));

            if (joins.contains(StaEntity.DATASTREAM)) {
                thing.setDatastreams(Optional.ofNullable(thing.getDatastreams()).orElseGet(HashSet::new));
                DatastreamDTO datastream = record.map(new DTOMapper.DatastreamRecordMapper());
                if (datastream.getId() != null) {
                    thing.getDatastreams().add(datastream);
                }
            }

            if (joins.contains(StaEntity.LOCATION)) {
                thing.setLocations(Optional.ofNullable(thing.getLocations()).orElseGet(HashSet::new));
                LocationDTO location = record.map(new DTOMapper.LocationRecordMapper());
                if (location.getId() != null) {
                    thing.getLocations().add(location);
                }
            }

            if (joins.contains(StaEntity.HISTORICAL_LOCATION)) {
                thing.setHistoricalLocations(Optional.ofNullable(thing.getHistoricalLocations())
                        .orElseGet(HashSet::new));
                HistoricalLocationDTO hloc = record.map(new DTOMapper.HistoricalLocationRecordMapper());
                if (hloc.getId() != null) {
                    thing.getHistoricalLocations().add(hloc);
                }
            }

            if (joins.contains(StaEntity.THING_PROPERTIES)) {
                ObjectNode properties = record.map(new DTOMapper.ThingRecordMapper.ThingParameterRecordMapper());
                if (properties != null) {
                    thing.setProperties(Optional.ofNullable(thing.getProperties())
                            .orElse(new ObjectMapper().createObjectNode()));
                    thing.getProperties().setAll(properties);
                }
            }

        }
        return new ArrayList<>(thingMap.values());
    }

    @Override
    public Field<?> checkPropertyName(String property) {
        return tQC.checkPropertyName(property);
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.THING.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.THING.PLATFORM_ID;
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.THING;
    }

    public void save(Platform thingPOJO) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(thingPOJO, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
    }

    public void update(Platform thingPOJO) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(thingPOJO, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.UPDATE);
    }

    @Override
    public void deleteByStaIdentifier(String staIdentifier) throws STACRUDException {
        // TODO: Firehose unstable
        firehoseClient.icebergDeleteById(StaEntity.THING.PLATFORM_ID.getName(),
                Long.parseLong(staIdentifier),
                tableName);
        //  firehoseClient.icebergDeleteByStaIdentifier(staIdentifier, tableName);
    }

    public void saveThingParameters(String id, ObjectNode parameters) throws STACRUDException {
        String foreignKey = StaEntity.THING_PROPERTIES.FK_PLATFORM_ID.getName();
        firehoseClient.icebergMergeParameters(parameters, parameterTableName, FirehoseConstants.INSERT, foreignKey, id);
    }

    public void deleteThingParameters(Long id) throws STACRUDException {
        String key = StaEntity.THING_PROPERTIES.FK_PLATFORM_ID.getName();
        firehoseClient.icebergDeleteById(key, id, parameterTableName);
    }
}
