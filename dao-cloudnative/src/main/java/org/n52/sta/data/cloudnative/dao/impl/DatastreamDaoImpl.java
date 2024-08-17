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
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.AbstractStaEntityDao;
import org.n52.sta.data.cloudnative.dao.DatastreamDao;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class DatastreamDaoImpl extends AbstractStaEntityDao<DatastreamDTO> implements DatastreamDao {

    private Set<Table<?>> joins;

    @Override
    public List<DatastreamDTO> findAllByAggregationId(Long id, Class<DatastreamDTO> entityClass)
            throws STAInvalidQueryException {

        Condition predicate = StaEntity.DATASTREAM.FK_AGGREGATION_ID.eq(id);
        return findAll(predicate, null, entityClass);
    }

    @Override
    public List<DatastreamDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, DatastreamDTO> datastreamMap = new HashMap<>();
        for (Record record : result) {
            Long Id = record.get(StaEntity.DATASTREAM.DATASET_ID);

            DatastreamDTO datastream = datastreamMap.computeIfAbsent(Id,
                    k -> record.map(new DTOMapper.DatastreamRecordMapper()));

            if (joins.contains(StaEntity.OBSERVED_PROPERTY)) {
                datastream.setObservedProperty(record.map(new DTOMapper.ObservedPropertyRecordMapper()));
            }
            if (joins.contains(StaEntity.SENSOR)) {
                datastream.setSensor(record.map(new DTOMapper.SensorRecordMapper()));
            }
            if (joins.contains(StaEntity.THING)) {
                datastream.setThing(record.map(new DTOMapper.ThingRecordMapper()));
            }
            if (joins.contains(StaEntity.DATASTREAM_PROPERTIES)) {
                datastream.setProperties(Optional.ofNullable(datastream.getProperties())
                        .orElse(new ObjectMapper().createObjectNode()));
                datastream.getProperties().setAll(record.map(new DTOMapper.DatastreamRecordMapper
                        .DatastreamParameterRecordMapper()));
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
    public Set<Table<?>> createJoinList(QueryOptions queryOptions) throws STAInvalidQueryException {

        joins = new HashSet<>();
        joins.add(StaEntity.UNIT);
        joins.add(StaEntity.DATASTREAM_PROPERTIES);

        if (queryOptions != null && queryOptions.getExpandFilter() != null) {
            for (ExpandItem expandItem : queryOptions.getExpandFilter().getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.SENSOR:
                        joins.add(StaEntity.SENSOR);
                        break;
                    case STAEntityDefinition.THING:
                        joins.add(StaEntity.THING);
                        break;
                    case STAEntityDefinition.OBSERVED_PROPERTY:
                        joins.add(StaEntity.OBSERVED_PROPERTY);
                        break;
                    case STAEntityDefinition.OBSERVATIONS:
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                expandProperty,
                                StaConstants.DATASTREAM));
                }
            }
        }
        return joins;
    }

    @Override
    public Field<?> checkPropertyName(String property) {
        return new DatastreamQueryConditions().checkPropertyName(property);
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.DATASTREAM;
    }

    @Override
    public List<Field<?>> getEntityTableFields() {
        return Arrays.stream(StaEntity.DATASTREAM.fields()).map(field -> {
            if (field.getName().equals("OBSERVED_AREA")) {
                return DSL.function("ST_AsText",
                                String.class,
                                DSL.function("ST_GeomFromWKB", byte[].class, field))
                        .as(field.getName());
            }
            return field;
        }).collect(Collectors.toList());
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.DATASTREAM.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.DATASTREAM.DATASET_ID;
    }

    public void save(DatastreamDTO merged) {
        // TODO
    }

    public void update(String id, DatastreamDTO merged) {
        // TODO
    }

    public void deleteById(Long Id) {
        // TODO
    }
}
