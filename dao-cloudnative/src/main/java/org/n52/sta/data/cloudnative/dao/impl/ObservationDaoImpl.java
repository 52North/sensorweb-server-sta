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

import org.jooq.*;
import org.jooq.Record;

import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.ObservationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.ObservationDao;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ObservationDaoImpl extends AbstractStaEntityDao<ObservationDTO> implements ObservationDao {

    @Override
    public ObservationDTO findFirstByDatasetIdOrderBySamplingTimeStartAsc(Long datasetIdentifier,
                                                                          Class<ObservationDTO> entityClass) throws STAInvalidQueryException {

        Condition predicate = StaEntity.OBSERVATION.FK_DATASET_ID.eq(datasetIdentifier);
        Sort sort = Sort.by(Order.asc(StaEntity.OBSERVATION.SAMPLING_TIME_START.getName()));
        SelectSeekStepN<Record> query = (SelectSeekStepN<Record>) selectQueryBuilder(predicate,
                entityClass,
                sort,
                null);
        Result<Record> result = query.limit(1).fetch();
        return mapResultToDTO(result).get(0);
    }

    @Override
    public ObservationDTO findFirstByDatasetIdOrderBySamplingTimeEndDesc(Long datasetIdentifier,
                                                                         Class<ObservationDTO> entityClass) throws STAInvalidQueryException {

        Condition predicate = StaEntity.OBSERVATION.FK_DATASET_ID.eq(datasetIdentifier);
        Sort sort = Sort.by(Order.desc(StaEntity.OBSERVATION.SAMPLING_TIME_END.getName()));
        SelectSeekStepN<Record> query = (SelectSeekStepN<Record>) selectQueryBuilder(predicate,
                entityClass,
                sort,
                null);
        Result<Record> result = query.limit(1).fetch();
        return mapResultToDTO(result).get(0);
    }

    @Override
    public void deleteAllByDatasetIdIn(Set datasetId) {
        // TODO
    }

    @Override
    protected List<ObservationDTO> mapResultToDTO(Result<Record> result) {
        List<ObservationDTO> observations = new ArrayList<>();
        for (Record record : result) {
            ObservationDTO observation = record.map(new DTOMapper.ObservationRecordMapper());
            observation.setFeatureOfInterest(record.map(new DTOMapper.FeatureOfInterestRecordMapper()));
            observation.setDatastream(record.map(new DTOMapper.DatastreamRecordMapper()));
            observations.add(observation);
        }
        return observations;
    }

    @Override
    public Field checkPropertyName(String property) {
        return new ObservationQueryConditions().checkPropertyName(property);
    }

    @Override
    public List<Field<?>> getEntityTableFields() {
        return Arrays.asList(StaEntity.OBSERVATION.fields());
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
    public List<Table<?>> createJoinList(ExpandFilter expandOption)
            throws STAInvalidQueryException {
        List<Table<?>> joinList = new ArrayList<>();
        joinList.add(StaEntity.OBSERVATION_PARAMETERS);
        return joinList;
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.OBSERVATION;
    }
}
