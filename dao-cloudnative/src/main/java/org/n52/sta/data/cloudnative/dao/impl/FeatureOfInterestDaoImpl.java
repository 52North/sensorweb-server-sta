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
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.FeatureOfInterestQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FeatureOfInterestDao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class FeatureOfInterestDaoImpl extends AbstractStaEntityDao<FeatureOfInterestDTO> implements FeatureOfInterestDao {
    @Override
    protected List<FeatureOfInterestDTO> mapResultToDTO(Result<Record> result) {
        List<FeatureOfInterestDTO> featuresOfInterest = new ArrayList<FeatureOfInterestDTO>();
        for (Record record : result) {
            FeatureOfInterestDTO feature = record.map(new DTOMapper.FeatureOfInterestRecordMapper());
            featuresOfInterest.add(feature);
        }
        return featuresOfInterest;
    }

    @Override
    public boolean existsByName(String name, Class<FeatureOfInterestDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.FEATURE_OF_INTEREST.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<FeatureOfInterestDTO> findByName(String name, Class<FeatureOfInterestDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.FEATURE_OF_INTEREST.NAME.eq(name);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, null).fetch();
        return Optional.of(mapResultToDTO(result).get(0));
    }

    @Override
    public List<Table<?>> createJoinList(ExpandFilter expandOption) throws STAInvalidQueryException {
        List<Table<?>> joinList = new ArrayList<>();
        joinList.add(StaEntity.FORMAT);
        joinList.add(StaEntity.FEATURE_PROPERTIES);
        return joinList;
    }

    @Override
    public Field<?> checkPropertyName(String property) {
        return new FeatureOfInterestQueryConditions().checkPropertyName(property);
    }

    @Override
    public List<Field<?>> getEntityTableFields() {
        return Arrays.asList(StaEntity.FEATURE_OF_INTEREST.fields());
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.FEATURE_OF_INTEREST.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.FEATURE_OF_INTEREST.FEATURE_ID;
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.FEATURE_OF_INTEREST;
    }
}
