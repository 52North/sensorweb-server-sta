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
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.SensorQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.SensorDao;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Procedure;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class SensorDaoImpl extends AbstractStaEntityDao<SensorDTO> implements SensorDao {
    private Set<Table<?>> joins;
    private final String tableName = getEntityTable().getName();
    private final String parameterTableName = "PROCEDURE_PARAMETER";

    public SensorDaoImpl(DSLContext ctx, StaFirehoseClient firehoseClient) {
        super(ctx, firehoseClient);
    }

    @Override
    protected List<SensorDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, SensorDTO> sensorMap = new HashMap<Long, SensorDTO>();
        for (Record record : result) {
            Long Id = record.get(StaEntity.SENSOR.PROCEDURE_ID);
            SensorDTO sensor = sensorMap.computeIfAbsent(Id, k -> record.map(new DTOMapper.SensorRecordMapper()));

            if (joins.contains(StaEntity.DATASTREAM)) {
                sensor.setDatastreams(Optional.ofNullable(sensor.getDatastreams()).orElse(new HashSet<>()));
                sensor.getDatastreams().add(record.map(new DTOMapper.DatastreamRecordMapper()));
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
    public Set<Table<?>> createJoinList(QueryOptions queryOptions) throws STAInvalidQueryException {
        joins = new HashSet<>();
        joins.add(StaEntity.FORMAT);
        joins.add(StaEntity.SENSOR_PROPERTIES);
        if (queryOptions != null && queryOptions.getExpandFilter() != null) {
            for (ExpandItem expandItem : queryOptions.getExpandFilter().getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                if (SensorEntityDefinition.DATASTREAMS.equals(expandProperty)) {
                    joins.add(StaEntity.DATASTREAM);
                }
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                        expandProperty,
                        StaConstants.SENSOR));
            }
        }
        return joins;
    }

    @Override
    public Field checkPropertyName(String property) {
        return new SensorQueryConditions().checkPropertyName(property);
    }

    @Override
    public List<Field<?>> getEntityTableFields() {
        return Arrays.asList(StaEntity.SENSOR.fields());
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
        firehoseClient.icebergDeleteByStaIdentifier(staIdentifier, tableName);
    }

    public void saveSensorParameters(String id, ObjectNode parameters)
            throws STACRUDException {
        String foreignKey = StaEntity.SENSOR_PROPERTIES.FK_PROCEDURE_ID.getName();
        firehoseClient.icebergMergeParameters(parameters, parameterTableName, FirehoseConstants.INSERT, foreignKey, id);
    }

    public void deleteSensorParameters(String fkSensorStaIdentifier) throws STACRUDException {
        firehoseClient.icebergDeleteByStaIdentifier(fkSensorStaIdentifier, parameterTableName);
    }
}
