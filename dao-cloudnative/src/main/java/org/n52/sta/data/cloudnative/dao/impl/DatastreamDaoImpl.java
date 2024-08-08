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

import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.jooq.*;
import org.jooq.Record;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.DatastreamDao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class DatastreamDaoImpl extends AbstractStaEntityDao<DatastreamDTO> implements DatastreamDao {
    @Override
    public List<DatastreamDTO> findAllByAggregationId(Long id, Class<DatastreamDTO> entityClass)
            throws STAInvalidQueryException {

        Condition predicate = StaEntity.DATASTREAM.FK_AGGREGATION_ID
                .eq(id);

        return findAll(predicate, null, entityClass);
    }

    @Override
    protected List<DatastreamDTO> mapResultToDTO(Result<Record> result) {
        ArrayList<DatastreamDTO> datastreams = new ArrayList<>();
        for (Record record : result) {
            DatastreamDTO datastream = record.map(new DTOMapper.DatastreamRecordMapper());
            datastream.setObservedProperty(record.map(new DTOMapper.ObservedPropertyRecordMapper()));
            datastream.setSensor(record.map(new DTOMapper.SensorRecordMapper()));
            datastream.setThing(record.map(new DTOMapper.ThingRecordMapper()));
            datastreams.add(datastream);
        }
        return datastreams;
    }

    @Override
    public boolean existsByName(String name, Class<DatastreamDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.DATASTREAM.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<DatastreamDTO> findByName(String name, Class<DatastreamDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.DATASTREAM.NAME.eq(name);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, null).fetch();
        return Optional.of(mapResultToDTO(result).get(0));
    }

    @Override
    public List<Table<?>> createJoinList(ExpandFilter expandOption) throws STAInvalidQueryException {
        List<Table<?>> joinList = new ArrayList<>();

        joinList.add(StaEntity.UNIT);
        joinList.add(StaEntity.DATASTREAM_PROPERTIES);

        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.SENSOR:
                        joinList.add(StaEntity.SENSOR);
                        break;
                    case STAEntityDefinition.THING:
                        joinList.add(StaEntity.THING);
                        break;
                    case STAEntityDefinition.OBSERVED_PROPERTY:
                        joinList.add(StaEntity.OBSERVED_PROPERTY);
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
        return joinList;
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
        return Arrays.asList(StaEntity.DATASTREAM.fields());
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.DATASTREAM.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.DATASTREAM.DATASET_ID;
    }
}
