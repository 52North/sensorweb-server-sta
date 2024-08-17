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
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.condition.ThingQueryConditions;
import org.n52.sta.data.cloudnative.dao.AbstractStaEntityDao;
import org.n52.sta.data.cloudnative.dao.ThingDao;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ThingDaoImpl extends AbstractStaEntityDao<ThingDTO> implements ThingDao {

    private Set<Table<?>> joins;

    @Override
    public boolean existsByName(String name, Class<ThingDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.THING.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<ThingDTO> findByName(String name, Class<ThingDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.THING.NAME.eq(name);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, null).fetch();
        return Optional.of(mapResultToDTO(result).get(0));
    }

    @Override
    public Set<Table<?>> createJoinList(QueryOptions queryOptions) throws STAInvalidQueryException {
        joins = new HashSet<>();
        joins.add(StaEntity.THING_PROPERTIES);
        if (queryOptions != null && queryOptions.getExpandFilter() != null) {
            for (ExpandItem expandItem : queryOptions.getExpandFilter().getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.HISTORICAL_LOCATIONS:
                        joins.add(StaEntity.HISTORICAL_LOCATION);
                        break;
                    case STAEntityDefinition.DATASTREAMS:
                        joins.add(StaEntity.DATASTREAM);
                        break;
                    case STAEntityDefinition.LOCATIONS:
                        joins.add(StaEntity.THING_LOCATION);
                        joins.add(StaEntity.LOCATION);
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                expandProperty,
                                StaConstants.THING));
                }
            }
        }
        return joins;
    }

    @Override
    protected List<ThingDTO> mapResultToDTO(Result<Record> result) {
        Map<Long, ThingDTO> thingMap = new HashMap<>();
        for (Record record : result) {
            Long Id = record.get(StaEntity.THING.PLATFORM_ID);
            ThingDTO thing = thingMap.computeIfAbsent(Id, k -> record.map(new DTOMapper.ThingRecordMapper()));

            if (joins.contains(StaEntity.DATASTREAM)) {
                thing.setDatastreams(Optional.ofNullable(thing.getDatastreams()).orElseGet(HashSet::new));
                thing.getDatastreams().add(record.map(new DTOMapper.DatastreamRecordMapper()));
            }

            if (joins.contains(StaEntity.LOCATION)) {
                thing.setLocations(Optional.ofNullable(thing.getLocations()).orElseGet(HashSet::new));
                thing.getLocations().add(record.map(new DTOMapper.LocationRecordMapper()));
            }

            if (joins.contains(StaEntity.HISTORICAL_LOCATION)) {
                thing.setHistoricalLocations(Optional.ofNullable(thing.getHistoricalLocations())
                        .orElseGet(HashSet::new));
                thing.getHistoricalLocations().add(record.map(new DTOMapper.HistoricalLocationRecordMapper()));
            }

            if (joins.contains(StaEntity.THING_PROPERTIES)) {
                thing.setProperties(Optional.ofNullable(thing.getProperties())
                        .orElse(new ObjectMapper().createObjectNode()));
                thing.getProperties().setAll(record.map(new DTOMapper.ThingRecordMapper.ThingParameterRecordMapper()));
            }

        }
        return new ArrayList<>(thingMap.values());
    }

    @Override
    public List<Field<?>> getEntityTableFields() {
        return new ArrayList<>(Arrays.asList(StaEntity.THING.fields()));
    }

    @Override
    public Field<?> checkPropertyName(String property) {
        return new ThingQueryConditions().checkPropertyName(property);
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.THING.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.THING.PLATFORM_ID;
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.THING;
    }
}
