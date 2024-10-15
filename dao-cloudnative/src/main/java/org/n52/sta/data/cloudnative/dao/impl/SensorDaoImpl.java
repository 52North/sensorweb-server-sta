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
import org.n52.shetland.ogc.sta.model.SensorEntityDefinition;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.SensorQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.SensorDao;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.Format;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Procedure;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class SensorDaoImpl extends AbstractStaEntityDao<SensorDTO> implements SensorDao {
    private final String tableName = getEntityTable().getName();
    private final String parameterTableName = StaEntity.SENSOR_PROPERTIES.getName();
    private final DatastreamQueryConditions dsQC;
    private final SensorQueryConditions sQC;

    public SensorDaoImpl(DSLContext ctx,
                         StaFirehoseClient firehoseClient,
                         DatastreamQueryConditions dsQC,
                         SensorQueryConditions sQC) {
        super(ctx, firehoseClient);
        this.dsQC = dsQC;
        this.sQC = sQC;
    }

    @Override
    protected List<SensorDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, SensorDTO> sensorMap = new TreeMap<>();
        for (Record record : result) {
            Long Id = (Long) record.get(StaEntity.alias(
                    StaEntity.SENSOR,
                    StaEntity.SENSOR.PROCEDURE_ID));

            SensorDTO sensor = sensorMap.computeIfAbsent(Id, k -> record.map(new DTOMapper.SensorRecordMapper()));

            if (joins.contains(StaEntity.DATASTREAM)) {
                sensor.setDatastreams(Optional.ofNullable(sensor.getDatastreams()).orElse(new HashSet<>()));
                DatastreamDTO datastream = record.map(new DTOMapper.DatastreamRecordMapper());
                if (datastream.getId() != null) {
                    sensor.getDatastreams().add(datastream);
                }
            }
            if (joins.contains(StaEntity.SENSOR_PROPERTIES)) {
                ObjectNode properties = record.map(new DTOMapper
                        .SensorRecordMapper.SensorParameterRecordMapper());
                if (properties != null) {
                    sensor.setProperties(Optional.ofNullable(sensor.getProperties())
                            .orElse(new ObjectMapper().createObjectNode()));
                    sensor.getProperties().setAll(properties);
                }
            }
        }
        return new ArrayList<>(sensorMap.values());
    }

    @Override
    public boolean existsByName(String name, Class<SensorDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.SENSOR.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<SensorDTO> findByName(String name, Class<SensorDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.SENSOR.NAME.eq(name);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, null).fetch();
        return Optional.of(mapResultToDTO(result).get(0));
    }

    @Override
    public Table<?> createJoinList(QueryOptions queryOptions, Table<?> table, List<Field<?>> select)
            throws STAInvalidQueryException {
        joins = new LinkedHashSet<>();

        Format SENSOR_FORMAT = StaEntity.FORMAT.as("SENSOR_FORMAT");
        joins.add(SENSOR_FORMAT);
        table = table.leftJoin(SENSOR_FORMAT)
                .on(SENSOR_FORMAT.FORMAT_ID
                        .eq(StaEntity.SENSOR.FK_FORMAT_ID));

        joins.add(StaEntity.SENSOR_PROPERTIES);
        table = table.leftJoin(StaEntity.SENSOR_PROPERTIES)
                .on(StaEntity.SENSOR_PROPERTIES.FK_PROCEDURE_ID
                        .eq(StaEntity.SENSOR.PROCEDURE_ID));

        if (queryOptions == null ||
                queryOptions.getSelectFilter() == null) {
            select.addAll(getStaEntityFields(SENSOR_FORMAT));
            select.addAll(getStaEntityFields(StaEntity.SENSOR_PROPERTIES));
        }

        if (queryOptions != null && queryOptions.getExpandFilter() != null) {
            for (ExpandItem expandItem : queryOptions.getExpandFilter().getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() ||
                        expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                if (SensorEntityDefinition.DATASTREAMS.equals(expandProperty)) {

                    joins.add(StaEntity.DATASTREAM);
                    table = table.leftJoin(StaEntity.DATASTREAM)
                            .on(StaEntity.DATASTREAM.FK_PROCEDURE_ID
                                    .eq(StaEntity.SENSOR.PROCEDURE_ID));

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
                                .map(dsQC::checkAliasedPropertyName)
                                .collect(Collectors.toList()));
                    }
                }
                else {
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                            expandProperty,
                            StaConstants.SENSOR));
                }

            }
        }
        return table;
    }

    @Override
    public Field<?> checkAliasedPropertyName(String property) {
        return sQC.checkAliasedPropertyName(property);
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.SENSOR.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.SENSOR.PROCEDURE_ID;
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.SENSOR;
    }

    public void save(Procedure sensor) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(sensor, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
    }

    public void update(Procedure sensor) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(sensor, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.UPDATE);
    }

    @Override
    public void deleteByStaIdentifier(String staIdentifier)
            throws STACRUDException {
        // TODO: Firehose unstable
        firehoseClient.icebergDeleteById(StaEntity.SENSOR.PROCEDURE_ID.getName(),
                Long.parseLong(staIdentifier),
                tableName);
        // firehoseClient.icebergDeleteByStaIdentifier(staIdentifier, tableName);
    }

    public void saveSensorParameters(String id, ObjectNode parameters)
            throws STACRUDException {
        String foreignKey = StaEntity.SENSOR_PROPERTIES.FK_PROCEDURE_ID.getName();
        firehoseClient.icebergMergeParameters(parameters, parameterTableName, FirehoseConstants.INSERT, foreignKey, id);
    }

    public void deleteSensorParameters(Long fkSensorStaIdentifier) throws STACRUDException {
        String key = StaEntity.SENSOR_PROPERTIES.FK_PROCEDURE_ID.getName();
        firehoseClient.icebergDeleteById(key, fkSensorStaIdentifier, parameterTableName);
    }
}
