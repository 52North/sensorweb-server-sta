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
import org.n52.shetland.ogc.sta.model.ObservedPropertyEntityDefinition;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.ObservedPropertyQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.ObservedPropertyDao;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Phenomenon;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ObservedPropertyDaoImpl
        extends AbstractStaEntityDao<ObservedPropertyDTO> implements ObservedPropertyDao {
    private final String tableName = getEntityTable().getName();
    private final String parameterTableName = "PHENOMENON_PARAMETER";
    private Set<Table<?>> joins;

    public ObservedPropertyDaoImpl(DSLContext ctx, StaFirehoseClient firehoseClient) {
        super(ctx, firehoseClient);
    }

    @Override
    protected List<ObservedPropertyDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, ObservedPropertyDTO> observedPropertyMap = new HashMap<Long, ObservedPropertyDTO>();
        for (Record record : result) {
            Long Id = record.get(StaEntity.OBSERVED_PROPERTY.PHENOMENON_ID);
            ObservedPropertyDTO observedProperty = observedPropertyMap.computeIfAbsent(Id,
                    k -> record.map(new DTOMapper.ObservedPropertyRecordMapper()));

            if (joins.contains(StaEntity.DATASTREAM)) {
                observedProperty.setDatastreams(Optional.ofNullable(observedProperty.getDatastreams())
                        .orElse(new HashSet<>()));
                observedProperty.getDatastreams().add(record.map(new DTOMapper.DatastreamRecordMapper()));
            }
            if (joins.contains(StaEntity.OBSERVED_PROPERTY_PROPERTIES)) {
                ObjectNode properties = record.map(new DTOMapper.ObservedPropertyRecordMapper.
                        ObservedPropertyParameterRecordMapper());
                if (properties != null) {
                    observedProperty.setProperties(Optional.ofNullable(observedProperty.getProperties())
                            .orElse(new ObjectMapper().createObjectNode()));
                    observedProperty.getProperties().setAll(properties);
                }
            }
        }
        return new ArrayList<>(observedPropertyMap.values());
    }

    @Override
    public boolean existsByName(String name, Class<ObservedPropertyDTO> entityClass)
            throws STAInvalidQueryException {
        Condition predicate = StaEntity.OBSERVED_PROPERTY.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    public boolean existsByDefinition(String definition, Class<ObservedPropertyDTO> entityClass)
            throws STAInvalidQueryException {
        Condition predicate = StaEntity.OBSERVED_PROPERTY.IDENTIFIER.eq(definition);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<ObservedPropertyDTO> findByName(String name, Class<ObservedPropertyDTO> entityClass)
            throws STAInvalidQueryException {
        Condition predicate = StaEntity.OBSERVED_PROPERTY.NAME.eq(name);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, null).fetch();
        return Optional.of(mapResultToDTO(result).get(0));
    }

    @Override
    public Set<Table<?>> createJoinList(QueryOptions queryOptions) throws STAInvalidQueryException {
        joins = new HashSet<>();
        joins.add(StaEntity.OBSERVED_PROPERTY_PROPERTIES);
        if (queryOptions != null && queryOptions.getExpandFilter() != null) {
            for (ExpandItem expandItem : queryOptions.getExpandFilter().getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                if (ObservedPropertyEntityDefinition.DATASTREAMS.equals(expandProperty)) {
                    joins.add(StaEntity.DATASTREAM);
                }
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                        expandProperty,
                        StaConstants.OBSERVED_PROPERTY));
            }
        }
        return joins;
    }

    @Override
    public Field checkPropertyName(String property) {
        return new ObservedPropertyQueryConditions().checkPropertyName(property);
    }

    @Override
    public List<Field<?>> getEntityTableFields() {
        return Arrays.asList(StaEntity.OBSERVED_PROPERTY.fields());
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.OBSERVED_PROPERTY.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.OBSERVED_PROPERTY.PHENOMENON_ID;
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.OBSERVED_PROPERTY;
    }

    public void save(Phenomenon phenomenon) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(phenomenon, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.INSERT);
    }

    public void update(Phenomenon phenomenon) throws STACRUDException {
        ObjectNode dataNode = mapper.convertValue(phenomenon, ObjectNode.class);
        firehoseClient.icebergMerge(dataNode, tableName, FirehoseConstants.UPDATE);
    }

    @Override
    public void deleteByStaIdentifier(String staIdentifier) throws STACRUDException {
        // TODO: Firehose unstable
        firehoseClient.icebergDeleteById(StaEntity.OBSERVED_PROPERTY.PHENOMENON_ID.getName(),
                Long.parseLong(staIdentifier),
                FirehoseConstants.DELETE);
        // firehoseClient.icebergDeleteByStaIdentifier(staIdentifier, tableName);
    }

    public void saveObservedPropertyParameters(String id, ObjectNode parameters) throws STACRUDException {
        String foreignKey = StaEntity.OBSERVED_PROPERTY_PROPERTIES.FK_PHENOMENON_ID.getName();
        firehoseClient.icebergMergeParameters(parameters, parameterTableName, FirehoseConstants.INSERT, foreignKey, id);
    }

    public void deleteObservedPropertyParameters(Long Id) throws STACRUDException {
        String key = StaEntity.OBSERVED_PROPERTY_PROPERTIES.FK_PHENOMENON_ID.getName();
        firehoseClient.icebergDeleteById(key, Id, parameterTableName);
    }
}
