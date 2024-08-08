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
import org.jooq.impl.DSL;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.LocationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.LocationDao;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class LocationDaoImpl extends AbstractStaEntityDao<LocationDTO> implements LocationDao {

    @Override
    public List<LocationDTO> findAllByThingId(Long id, Class<LocationDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = DSL.exists(
                ctx.select()
                        .from(StaEntity.LOCATION)
                        .join(StaEntity.THING_LOCATION)
                        .onKey()
                        .join(StaEntity.THING)
                        .onKey()
                        .where(StaEntity.THING.PLATFORM_ID.eq(id))
        );
        return findAll(predicate, null, entityClass);
    }

    @Override
    protected List<LocationDTO> mapResultToDTO(Result<Record> result) {
        List<LocationDTO> locations = new ArrayList<>();
        Map<Long, LocationDTO> map = new HashMap<>();
        for (Record record : result) {
            Long Id = record.get(StaEntity.LOCATION.LOCATION_ID);
            LocationDTO location = map.computeIfAbsent(Id, k -> record.map(new DTOMapper.LocationRecordMapper()));
            location.getHistoricalLocations().add(record.map(new DTOMapper.HistoricalLocationRecordMapper()));
            location.getThings().add(record.map(new DTOMapper.ThingRecordMapper()));
            location.getHistoricalLocations().add(record.map(new DTOMapper.HistoricalLocationRecordMapper()));
            locations.add(location);
        }
        return locations;
    }

    @Override
    public boolean existsByName(String name, Class<LocationDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.LOCATION.NAME.eq(name);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public Optional<LocationDTO> findByName(String name, Class<LocationDTO> entityClass) throws STAInvalidQueryException {
        Condition predicate = StaEntity.LOCATION.NAME.eq(name);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, null).fetch();
        return Optional.of(mapResultToDTO(result).get(0));
    }

    @Override
    public List<Table<?>> createJoinList(ExpandFilter expandOption) throws STAInvalidQueryException {
        List<Table<?>> joinList = new ArrayList<>();
        joinList.add(StaEntity.LOCATION_PROPERTIES);
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.HISTORICAL_LOCATIONS:
                        joinList.add(StaEntity.LOCATION_HISTORICAL_LOCATION);
                        joinList.add(StaEntity.HISTORICAL_LOCATION);
                        break;
                    case STAEntityDefinition.THINGS:
                        joinList.add(StaEntity.THING_LOCATION);
                        joinList.add(StaEntity.THING);
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED, expandProperty,
                                StaConstants.LOCATION));
                }
            }
        }
        return joinList;
    }

    @Override
    public Field checkPropertyName(String property) {
        return new LocationQueryConditions().checkPropertyName(property);
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.LOCATION;
    }

    @Override
    public List<Field<?>> getEntityTableFields() {
        return Arrays.asList(StaEntity.LOCATION.fields());
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.LOCATION.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.LOCATION.LOCATION_ID;
    }
}
