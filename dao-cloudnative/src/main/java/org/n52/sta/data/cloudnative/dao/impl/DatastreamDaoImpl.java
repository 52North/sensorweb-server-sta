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
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.*;
import org.n52.sta.data.cloudnative.dao.DatastreamDao;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.Format;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Dataset;
import org.n52.sta.data.cloudnative.schema.tables.records.DatasetRecord;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class DatastreamDaoImpl extends AbstractStaEntityDao<DatastreamDTO> implements DatastreamDao {
    private final String tableName = getEntityTable().getName();
    private final String parameterTableName = StaEntity.DATASTREAM_PROPERTIES.getName();
    private final DatastreamQueryConditions dsQC;
    private final ThingQueryConditions tQC;
    private final SensorQueryConditions sQC;
    private final ObservedPropertyQueryConditions opQC;
    public DatastreamDaoImpl(DSLContext ctx,
                             StaFirehoseClient firehoseClient,
                             DatastreamQueryConditions dsQC,
                             ThingQueryConditions tQC,
                             SensorQueryConditions sQC,
                             ObservedPropertyQueryConditions opQC) {
        super(ctx, firehoseClient);
        this.dsQC = dsQC;
        this.tQC = tQC;
        this.sQC = sQC;
        this.opQC = opQC;
    }


    @Override
    public Set<Dataset> findAllByAggregationIdPOJO(Long datasetId)
            throws STAInvalidQueryException {
        Condition predicate = StaEntity.DATASTREAM.FK_AGGREGATION_ID.eq(datasetId);
        Result<Record> records = selectQueryBuilder(predicate,
                DatastreamDTO.class,
                null,
                null)
                .fetch();
        return new HashSet<>(mapResultToPOJO(records));
    }

    public Dataset findByFeatureIdPOJO(Condition predicate)
            throws STAInvalidQueryException {
        Result<Record> records = selectQueryBuilder(predicate,
                DatastreamDTO.class,
                null,
                null)
                .fetch();
        return mapResultToPOJO(records).get(0);
    }

    public Dataset findByDatasetIdPOJO(Long datasetId)
            throws STAInvalidQueryException, STACRUDException {
        Condition predicate = StaEntity.DATASTREAM.DATASET_ID.eq(datasetId);
        Result<Record> records = selectQueryBuilder(predicate,
                DatastreamDTO.class,
                null,
                null)
                .fetch();

        return Optional.ofNullable(mapResultToPOJO(records).get(0))
                .orElseThrow(() -> new STACRUDException("Unable to find Datastream!"));
    }

    public Dataset findByStaIdentifierPOJO(String staIdentifier) throws STAInvalidQueryException, STACRUDException {
        Condition predicate = StaEntity.DATASTREAM.STA_IDENTIFIER.eq(staIdentifier);
        Result<Record> records = selectQueryBuilder(predicate,
                DatastreamDTO.class,
                null,
                null)
                .fetch();

        return Optional.ofNullable(mapResultToPOJO(records).get(0))
                .orElseThrow(() -> new STACRUDException("Unable to find Datastream!"));
    }

    public Result<Record> findRecordByStaIdentifier(Long datasetId) throws STAInvalidQueryException, STACRUDException {
        Condition predicate = StaEntity.DATASTREAM.DATASET_ID.eq(datasetId);
        return Optional.of(
                selectQueryBuilder(predicate, DatastreamDTO.class, null, null).fetch())
                .orElseThrow(() -> new STACRUDException("Unable to find Datastream!"));
    }

    public List<Dataset> mapResultToPOJO(Result<Record> result) {
        Map<Long, Dataset> pojoMap = new HashMap<>();
        for (Record record : result) {
            DatasetRecord datasetRecord = new DatasetRecord();
            DTOMapper.mapEntityRecord(DTOMapper.mapAliasedFields(record, StaEntity.DATASTREAM), datasetRecord);
            Long Id = datasetRecord.getDatasetId();
            Dataset dataset = pojoMap.computeIfAbsent(Id, k -> datasetRecord.into(Dataset.class));
        }
        return new ArrayList<>(pojoMap.values());
    }

    @Override
    public List<DatastreamDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, DatastreamDTO> datastreamMap = new TreeMap<>();
        for (Record record : result) {
            Long Id = (Long) record.get(StaEntity.alias(
                    StaEntity.DATASTREAM,
                    StaEntity.DATASTREAM.DATASET_ID));

            DatastreamDTO datastream = datastreamMap.computeIfAbsent(Id,
                    k -> record.map(new DTOMapper.DatastreamRecordMapper()));

            if (joins.contains(StaEntity.OBSERVED_PROPERTY)) {
                ObservedPropertyDTO observedProperty = record.map(new DTOMapper.ObservedPropertyRecordMapper());
                if (observedProperty.getId() != null) {
                    datastream.setObservedProperty(observedProperty);
                }
            }
            if (joins.contains(StaEntity.SENSOR)) {
                SensorDTO sensor = record.map(new DTOMapper.SensorRecordMapper());
                if (sensor.getId() != null) {
                    datastream.setSensor(sensor);
                }
            }
            if (joins.contains(StaEntity.THING)) {
                ThingDTO thing = record.map(new DTOMapper.ThingRecordMapper());
                if (thing.getId() != null) {
                    datastream.setThing(thing);
                }
            }
            if (joins.contains(StaEntity.DATASTREAM_PROPERTIES)) {
                ObjectNode properties = record.map(new DTOMapper.DatastreamRecordMapper
                        .DatastreamParameterRecordMapper());
                if (properties != null) {
                    datastream.setProperties(Optional.ofNullable(datastream.getProperties())
                            .orElse(new ObjectMapper().createObjectNode()));
                    datastream.getProperties().setAll(properties);
                }
            }
        }
        return new ArrayList<>(datastreamMap.values());
    }

    @Override
    public boolean existsByName(String name, Class<DatastreamDTO> entityClass)
            throws STAInvalidQueryException {

        Condition predicate = StaEntity.DATASTREAM.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<DatastreamDTO> findByName(String name, Class<DatastreamDTO> entityClass)
            throws STAInvalidQueryException {

        Condition predicate = StaEntity.DATASTREAM.NAME.eq(name);
        return findOne(predicate, null, entityClass);
    }

    @Override
    public Table<?> createJoinList(QueryOptions queryOptions, Table<?> table, List<Field<?>> select)
            throws STAInvalidQueryException {

        joins = new LinkedHashSet<>();

        joins.add(StaEntity.UNIT);
        table = table.leftJoin(StaEntity.UNIT)
                .on(StaEntity.UNIT.UNIT_ID
                        .eq(StaEntity.DATASTREAM.FK_UNIT_ID));

        Format DATASTREAM_FORMAT = StaEntity.FORMAT.as("DATASTREAM_FORMAT");
        joins.add(DATASTREAM_FORMAT);
        table = table.leftJoin(DATASTREAM_FORMAT)
                .on(DATASTREAM_FORMAT.FORMAT_ID
                        .eq(StaEntity.DATASTREAM.FK_FORMAT_ID));

        joins.add(StaEntity.DATASTREAM_PROPERTIES);
        table = table.leftJoin(StaEntity.DATASTREAM_PROPERTIES)
                .on(StaEntity.DATASTREAM_PROPERTIES.FK_DATASET_ID
                        .eq(StaEntity.DATASTREAM.DATASET_ID));

        if (queryOptions == null ||
                queryOptions.getSelectFilter() == null) {
            select.addAll(getStaEntityFields(StaEntity.UNIT));
            select.addAll(getStaEntityFields(DATASTREAM_FORMAT));
            select.addAll(getStaEntityFields(StaEntity.DATASTREAM_PROPERTIES));
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
                    case STAEntityDefinition.SENSOR:

                        joins.add(StaEntity.SENSOR);
                        table = table.leftJoin(StaEntity.SENSOR)
                                        .on(StaEntity.SENSOR.PROCEDURE_ID
                                                .eq(StaEntity.DATASTREAM.FK_PROCEDURE_ID));

                        Format SENSOR_FORMAT = StaEntity.FORMAT.as("SENSOR_FORMAT");
                        joins.add(SENSOR_FORMAT);
                        table = table.leftJoin(SENSOR_FORMAT)
                                .on(SENSOR_FORMAT.FORMAT_ID
                                        .eq(StaEntity.SENSOR.FK_FORMAT_ID));

                        if (expandItem.getQueryOptions() == null ||
                                expandItem.getQueryOptions().getSelectFilter() == null) {
                            select.addAll(getStaEntityFields(StaEntity.SENSOR));
                            select.addAll(getStaEntityFields(SENSOR_FORMAT)
                                    .stream()
                                    .map(e -> e.as("SENSOR_FORMAT_" + e.getName()))
                                    .collect(Collectors.toList()));
                        } else {
                            select.addAll(expandItem
                                    .getQueryOptions()
                                    .getSelectFilter()
                                    .getItems()
                                    .stream()
                                    .map(sQC::checkPropertyName)
                                    .collect(Collectors.toList()));
                        }
                        break;
                    case STAEntityDefinition.THING:
                        joins.add(StaEntity.THING);
                        table = table.leftJoin(StaEntity.THING)
                                .on(StaEntity.THING.PLATFORM_ID
                                        .eq(StaEntity.DATASTREAM.FK_PLATFORM_ID));

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
                    case STAEntityDefinition.OBSERVED_PROPERTY:
                        joins.add(StaEntity.OBSERVED_PROPERTY);
                        table = table.leftJoin(StaEntity.OBSERVED_PROPERTY)
                                .on(StaEntity.OBSERVED_PROPERTY.PHENOMENON_ID
                                        .eq(StaEntity.DATASTREAM.FK_PHENOMENON_ID));

                        if (expandItem.getQueryOptions() == null ||
                                expandItem.getQueryOptions().getSelectFilter() == null) {
                            select.addAll(getStaEntityFields(StaEntity.OBSERVED_PROPERTY));
                        } else {
                            select.addAll(expandItem
                                    .getQueryOptions()
                                    .getSelectFilter()
                                    .getItems()
                                    .stream()
                                    .map(opQC::checkPropertyName)
                                    .collect(Collectors.toList()));
                        }

                        break;
                    case STAEntityDefinition.OBSERVATIONS:
                        // handled separately
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                expandProperty,
                                StaConstants.DATASTREAM));
                }
            }
        }
        return table;
    }

    @Override
    public Field<?> checkPropertyName(String property) {
        return dsQC.checkPropertyName(property);
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.DATASTREAM;
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.DATASTREAM.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.DATASTREAM.DATASET_ID;
    }

    public void save(Dataset dataset) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(dataset, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
    }

    public void update(Dataset dataset) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(dataset, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.UPDATE);
    }

    @Override
    public void deleteByStaIdentifier(String staIdentifier) throws STACRUDException {
        // TODO: Firehose unstable
        deleteById(Long.valueOf(staIdentifier));
        // firehoseClient.icebergDeleteByStaIdentifier(staIdentifier, tableName);
    }

    public void deleteById(Long datasetId) throws STACRUDException {
        String key = StaEntity.DATASTREAM.DATASET_ID.getName();
        firehoseClient.icebergDeleteById(key, datasetId, tableName);
    }

    /*public void deleteByAggregationId(Long aggregationId) throws STACRUDException {
        String key = StaEntity.DATASTREAM.FK_AGGREGATION_ID.getName();
        firehoseClient.icebergDeleteById(key, aggregationId, tableName);
    }*/

    public void saveDatastreamParameters(String id, ObjectNode parameters) throws STACRUDException {
        String foreignKey = StaEntity.DATASTREAM_PROPERTIES.FK_DATASET_ID.getName();
        firehoseClient.icebergMergeParameters(parameters, parameterTableName, FirehoseConstants.INSERT, foreignKey, id);
    }

    public void deleteDatastreamParameters(Long Id) throws STACRUDException {
        String key = StaEntity.DATASTREAM_PROPERTIES.FK_DATASET_ID.getName();
        firehoseClient.icebergDeleteById(key, Id, parameterTableName);
    }
}
