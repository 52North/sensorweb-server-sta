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

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.ObservationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.ObservationDao;

import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Observation;
import org.n52.sta.data.cloudnative.schema.tables.records.ObservationRecord;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.n52.sta.api.RequestUtils.QUERY_OPTIONS_FACTORY;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ObservationDaoImpl extends AbstractStaEntityDao<ObservationDTO> implements ObservationDao {
    private final String tableName = getEntityTable().getName();
    private final String parameterTableName = StaEntity.OBSERVATION_PARAMETERS.getName();
    private final DatastreamQueryConditions dsQC;
    private final ObservationQueryConditions oQC;
    public ObservationDaoImpl(DSLContext ctx,
                              StaFirehoseClient firehoseClient,
                              DatastreamQueryConditions dsQC,
                              ObservationQueryConditions oQC) {
        super(ctx, firehoseClient);
        this.dsQC = dsQC;
        this.oQC = oQC;
    }

    @Override
    public Observation findFirstByDatasetIdOrderBySamplingTimeStartAsc(Long datasetIdentifier,
                                                                          Class<ObservationDTO> entityClass)
            throws STAInvalidQueryException {

        Condition predicate = StaEntity.OBSERVATION.FK_DATASET_ID.eq(datasetIdentifier);
        Sort sort = Sort.by(Order.asc((StaEntity.OBSERVATION.SAMPLING_TIME_START.getName())));
        SelectSeekStepN<Record> query = (SelectSeekStepN<Record>) selectQueryBuilder(predicate,
                entityClass,
                sort,
                null);
        Result<ObservationRecord> result = query.limit(1).fetchInto(StaEntity.OBSERVATION);
        if (!result.isEmpty()) {
            return result.into(Observation.class).get(0);
        }
        return null;
    }

    @Override
    public Observation findFirstByDatasetIdOrderBySamplingTimeEndDesc(Long datasetIdentifier,
                                                                         Class<ObservationDTO> entityClass)
            throws STAInvalidQueryException {

        Condition predicate = StaEntity.OBSERVATION.FK_DATASET_ID.eq(datasetIdentifier);
        Sort sort = Sort.by(Order.desc(StaEntity.OBSERVATION.SAMPLING_TIME_END.getName()));
        SelectSeekStepN<Record> query = (SelectSeekStepN<Record>) selectQueryBuilder(predicate,
                entityClass,
                sort,
                null);
        Result<ObservationRecord> result = query.limit(1).fetchInto(StaEntity.OBSERVATION);
        if (!result.isEmpty()) {
            return result.into(Observation.class).get(0);
        }
        return null;
    }

    @Override
    protected List<ObservationDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, ObservationDTO> observationMap = new TreeMap<>();
        for (Record record : result) {
            Long Id = (Long) record.get(StaEntity.alias(
                    StaEntity.OBSERVATION,
                    StaEntity.OBSERVATION.OBSERVATION_ID));

            ObservationDTO observation = observationMap.computeIfAbsent(Id,
                    k -> record.map(new DTOMapper.ObservationRecordMapper()));

            if (joins.contains(StaEntity.FEATURE_OF_INTEREST)) {
                FeatureOfInterestDTO feature = record.map(new DTOMapper.FeatureOfInterestRecordMapper());
                if (feature.getId() != null) {
                    observation.setFeatureOfInterest(feature);
                }
            }
            if (joins.contains(StaEntity.DATASTREAM)) {
                DatastreamDTO datastream = record.map(new DTOMapper.DatastreamRecordMapper());
                if(datastream.getId() != null) {
                    observation.setDatastream(datastream);
                }
            }
            if (joins.contains(StaEntity.OBSERVATION_PARAMETERS)) {
                ObjectNode properties = record.map(new DTOMapper.ObservationRecordMapper
                        .ObservationParameterRecordMapper());
                if (properties != null) {
                    observation.setParameters(Optional.ofNullable(observation.getParameters())
                            .orElse(new ObjectMapper().createObjectNode()));
                    observation.getParameters().setAll(properties);
                }
            }

        }
        return new ArrayList<>(observationMap.values());
    }

    @Override
    public Field<?> checkAliasedPropertyName(String property) {
        return oQC.checkAliasedPropertyName(property);
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.OBSERVATION.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.OBSERVATION.OBSERVATION_ID;
    }

    @Override
    public Table<?> createJoinList(QueryOptions queryOptions, Table<?> table, List<Field<?>> select)
            throws STAInvalidQueryException {
        joins = new LinkedHashSet<>();

        joins.add(StaEntity.OBSERVATION_PARAMETERS);
        table = table.leftJoin(StaEntity.OBSERVATION_PARAMETERS)
                .on(StaEntity.OBSERVATION_PARAMETERS.FK_OBSERVATION_ID
                        .eq(StaEntity.OBSERVATION.OBSERVATION_ID));

        if (queryOptions == null ||
                queryOptions.getSelectFilter() == null) {
            select.addAll(getStaEntityFields(StaEntity.OBSERVATION_PARAMETERS));
        }
        return table;
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.OBSERVATION;
    }

    public void save(Observation observation) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(observation, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
    }

    public void update(Observation updatedObservation) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(updatedObservation, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.UPDATE);
    }

    @Override
    public void deleteByStaIdentifier(String staIdentifier) throws STACRUDException {
        // TODO: Firehose unstable
        // firehoseClient.icebergDeleteByStaIdentifier(staIdentifier, tableName);
        firehoseClient.icebergDeleteById(getEntityId().getName(), Long.parseLong(staIdentifier), tableName);
    }

    @Override
    public void deleteAllByDatasetIdIn(Set<Long> datasetId) throws STACRUDException, STAInvalidQueryException {
        // get list of observation_id where fk_dataset_id = datasetId
        // for each obs_id -> deleteById
        for (Long id : datasetId) {
            deleteByDatasetId(id);
        }
    }

    public void deleteByDatasetId(Long datasetId) throws STACRUDException, STAInvalidQueryException {
        String key = StaEntity.OBSERVATION.OBSERVATION_ID.getName();
        QueryOptions options = QUERY_OPTIONS_FACTORY.createQueryOptions("$select=id");
        Condition predicate = StaEntity.OBSERVATION.FK_DATASET_ID.eq(datasetId);
        List<ObservationDTO> observations = findAll(predicate, options, ObservationDTO.class);
        for (ObservationDTO observation : observations) {
            firehoseClient.icebergDeleteById(key, Long.parseLong(observation.getId()), tableName);
        }
    }

    public void saveObservationParameters(String id, ObjectNode parameters) throws STACRUDException {
        String foreignKey = StaEntity.OBSERVATION_PARAMETERS.FK_OBSERVATION_ID.getName();
        firehoseClient.icebergMergeParameters(parameters, parameterTableName, FirehoseConstants.INSERT, foreignKey, id);
    }

    public void deleteObservationParameters(Long Id) throws STACRUDException {
        String key = StaEntity.OBSERVATION_PARAMETERS.FK_OBSERVATION_ID.getName();
        firehoseClient.icebergDeleteById(key, Id, parameterTableName);
    }
}
