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
import org.jooq.impl.DSL;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.LocationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.AbstractStaEntityDao;
import org.n52.sta.data.cloudnative.dao.LocationDao;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Location;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class LocationDaoImpl extends AbstractStaEntityDao<LocationDTO> implements LocationDao {

    private Set<Table<?>> joins;

    @Override
    public List<LocationDTO> findAllByThingId(Long id, Class<LocationDTO> entityClass)
            throws STAInvalidQueryException {
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
        Map<Long, LocationDTO> locationMap = new HashMap<>();
        for (Record record : result) {
            Long Id = record.get(StaEntity.LOCATION.LOCATION_ID);

            LocationDTO location = locationMap.computeIfAbsent(Id, k -> record.map(new DTOMapper.LocationRecordMapper()));

            if (joins.contains(StaEntity.HISTORICAL_LOCATION)) {
                location.setHistoricalLocations(Optional.ofNullable(location.getHistoricalLocations()).orElse(new HashSet<>()));
                location.getHistoricalLocations().add(record.map(new DTOMapper.HistoricalLocationRecordMapper()));
            }
            if (joins.contains(StaEntity.THING)) {
                location.setThings(Optional.ofNullable(location.getThings()).orElse(new HashSet<>()));
                location.getThings().add(record.map(new DTOMapper.ThingRecordMapper()));
            }
            if (joins.contains(StaEntity.LOCATION_PROPERTIES)) {
                location.setProperties(Optional.ofNullable(location.getProperties())
                        .orElse(new ObjectMapper().createObjectNode()));
                location.getProperties().setAll(record.map(new DTOMapper.LocationRecordMapper.
                        LocationParameterRecordMapper()));
            }

        }
        return new ArrayList<>(locationMap.values());
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
    public Set<Table<?>> createJoinList(QueryOptions queryOptions) throws STAInvalidQueryException {
        joins = new HashSet<>();
        joins.add(StaEntity.LOCATION_PROPERTIES);
        if (queryOptions != null && queryOptions.getExpandFilter() != null) {
            for (ExpandItem expandItem : queryOptions.getExpandFilter().getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.HISTORICAL_LOCATIONS:
                        joins.add(StaEntity.LOCATION_HISTORICAL_LOCATION);
                        joins.add(StaEntity.HISTORICAL_LOCATION);
                        break;
                    case STAEntityDefinition.THINGS:
                        joins.add(StaEntity.THING_LOCATION);
                        joins.add(StaEntity.THING);
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED, expandProperty,
                                StaConstants.LOCATION));
                }
            }
        }
        return joins;
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
        return Arrays.stream(StaEntity.LOCATION.fields()).map(field -> {
            if (field.getName().equals("GEOM")) {
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
        return StaEntity.LOCATION.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.LOCATION.LOCATION_ID;
    }

    @Override
    public void deleteByStaIdentifier(String identifier, Class<LocationDTO> entityClass) {
        // TODO
    }

    public void saveLocationParameters(String id, ObjectNode properties) {
        // TODO
    }

    public void save(Location location) {
        // TODO
    }

    public void update(Location location) {
        // TODO
    }

    public void deleteLocationParameters(String id, ObjectNode properties) {
        // TODO
    }
}
