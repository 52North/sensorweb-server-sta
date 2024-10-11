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
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.FeatureOfInterestQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FeatureOfInterestDao;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.Format;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FeatureOfInterestDaoImpl
        extends AbstractStaEntityDao<FeatureOfInterestDTO> implements FeatureOfInterestDao {
    private final String tableName = getEntityTable().getName();
    private final String parameterTableName = StaEntity.FEATURE_PROPERTIES.getName();
    private final FeatureOfInterestQueryConditions fQC;

    public FeatureOfInterestDaoImpl(DSLContext ctx, StaFirehoseClient firehoseClient, FeatureOfInterestQueryConditions fQC) {
        super(ctx, firehoseClient);
        this.fQC = fQC;
    }

    @Override
    protected List<FeatureOfInterestDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, FeatureOfInterestDTO> featureMap = new TreeMap<>();
        for (Record record : result) {
            Long Id = record.get(StaEntity.FEATURE_OF_INTEREST.FEATURE_ID);
            FeatureOfInterestDTO feature = featureMap.computeIfAbsent(Id,
                    k -> record.map(new DTOMapper.FeatureOfInterestRecordMapper()));
            if (joins.contains(StaEntity.FEATURE_PROPERTIES)) {
                ObjectNode properties = record.map(new DTOMapper.FeatureOfInterestRecordMapper
                        .FeatureParameterRecordMapper());
                if (properties != null) {
                    feature.setProperties(Optional.ofNullable(feature.getProperties())
                            .orElse(new ObjectMapper().createObjectNode()));

                    feature.getProperties().setAll(properties);
                }
            }
        }
        return new ArrayList<>(featureMap.values());
    }

    @Override
    public boolean existsByName(String name, Class<FeatureOfInterestDTO> entityClass)
            throws STAInvalidQueryException {
        Condition predicate = StaEntity.FEATURE_OF_INTEREST.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<FeatureOfInterestDTO> findByName(String name, Class<FeatureOfInterestDTO> entityClass)
            throws STAInvalidQueryException {
        Condition predicate = StaEntity.FEATURE_OF_INTEREST.NAME.eq(name);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, null).fetch();
        return Optional.of(mapResultToDTO(result).get(0));
    }

    @Override
    public Table<?> createJoinList(QueryOptions queryOptions, Table<?> table, List<Field<?>> select)
            throws STAInvalidQueryException {
        joins = new LinkedHashSet<>();

        Format FEATURE_FORMAT = StaEntity.FORMAT.as("FEATURE_FORMAT");
        joins.add(FEATURE_FORMAT);
        table = table.leftJoin(FEATURE_FORMAT)
                        .on(FEATURE_FORMAT.FORMAT_ID
                                .eq(StaEntity.FEATURE_OF_INTEREST.FK_FORMAT_ID));

        joins.add(StaEntity.FEATURE_PROPERTIES);
        table = table.leftJoin(StaEntity.FEATURE_PROPERTIES)
                .on(StaEntity.FEATURE_PROPERTIES.FK_FEATURE_ID
                        .eq(StaEntity.FEATURE_OF_INTEREST.FEATURE_ID));

        if (queryOptions == null ||
                queryOptions.getSelectFilter() == null) {
            select.addAll(getStaEntityFields(FEATURE_FORMAT)
                    .stream()
                    .map(e -> e.as("FEATURE_FORMAT_" + e.getName()))
                    .collect(Collectors.toList()));
            select.addAll(getStaEntityFields(StaEntity.FEATURE_PROPERTIES));
        }

        return table;
    }

    @Override
    public Field<?> checkPropertyName(String property) {
        return fQC.checkPropertyName(property);
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

    public void save(Feature featureOfInterestPOJO) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(featureOfInterestPOJO, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
    }

    public void update(Feature featureOfInterestPOJO) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(featureOfInterestPOJO, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.UPDATE);
    }

    @Override
    public void deleteByStaIdentifier(String staIdentifier) throws STACRUDException {
        // TODO: Firehose unstable
        firehoseClient.icebergDeleteById(StaEntity.FEATURE_OF_INTEREST.FEATURE_ID.getName(),
                Long.parseLong(staIdentifier),
                tableName);
        // firehoseClient.icebergDeleteByStaIdentifier(staIdentifier, tableName);
    }

    public void saveFeatureParameters(String Id, ObjectNode parameters) throws STACRUDException {
        String foreignKey = StaEntity.FEATURE_PROPERTIES.FK_FEATURE_ID.getName();
        firehoseClient.icebergMergeParameters(parameters, parameterTableName, FirehoseConstants.INSERT, foreignKey, Id);
    }

    public void deleteFeatureParameters(Long featureId) throws STACRUDException {
        String key = StaEntity.FEATURE_PROPERTIES.FK_FEATURE_ID.getName();
        firehoseClient.icebergDeleteById(key, featureId, parameterTableName);
    }
}
