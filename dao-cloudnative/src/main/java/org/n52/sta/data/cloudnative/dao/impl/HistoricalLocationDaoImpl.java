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

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.HistoricalLocationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.AbstractStaEntityDao;
import org.n52.sta.data.cloudnative.dao.HistoricalLocationDao;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class HistoricalLocationDaoImpl extends AbstractStaEntityDao<HistoricalLocationDTO>
        implements HistoricalLocationDao {

    private Set<Table<?>> joins;

    @Override
    protected List<HistoricalLocationDTO> mapResultToDTO(Result<Record> result) {
        List<HistoricalLocationDTO> historicalLocations = new ArrayList<>();
        Map<Long, HistoricalLocationDTO> historicalLocationMap = new HashMap<>();
        for (Record record : result) {
            Long id = record.get(StaEntity.HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID);

            HistoricalLocationDTO historicalLocation = historicalLocationMap.computeIfAbsent(id,
                    k -> record.map(new DTOMapper.HistoricalLocationRecordMapper()));

            if (joins.contains(StaEntity.LOCATION)) {
                historicalLocation.setLocations(Optional.ofNullable(historicalLocation.getLocations()).orElse(new HashSet<>()));
                historicalLocation.getLocations().add(record.map(new DTOMapper.LocationRecordMapper()));
            }
            if (joins.contains(StaEntity.THING)) {
                historicalLocation.setThing(record.map(new DTOMapper.ThingRecordMapper()));
            }

            historicalLocations.add(historicalLocation);
        }
        return historicalLocations;
    }

    @Override
    public Set<Table<?>> createJoinList(QueryOptions queryOptions)
            throws STAInvalidQueryException {
        joins = new HashSet<>();
        if (queryOptions != null && queryOptions.getExpandFilter() != null) {
            for (ExpandItem expandItem : queryOptions.getExpandFilter().getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.LOCATIONS:
                        joins.add(StaEntity.LOCATION_HISTORICAL_LOCATION);
                        joins.add(StaEntity.LOCATION);
                        break;
                    case STAEntityDefinition.THING:
                        // fallthru
                        // The UML in Section 8.2 of the OGC STA v1.0 defines the relations as "Things"
                        // The Definition in Section 8.2.3 of the OGC STA v1.0 defines the relations as "Thing"
                        // We will allow both for now
                    case STAEntityDefinition.THINGS:
                        joins.add(StaEntity.THING);
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                expandProperty,
                                StaConstants.HISTORICAL_LOCATION));
                }
            }
        }
        return joins;
    }

    @Override
    public Field checkPropertyName(String property) {
        return new HistoricalLocationQueryConditions().checkPropertyName(property);
    }

    @Override
    public Table<?> getEntityTable() {
        return StaEntity.HISTORICAL_LOCATION;
    }

    @Override
    public List<Field<?>> getEntityTableFields() {
        return Arrays.asList(StaEntity.HISTORICAL_LOCATION.fields());
    }

    @Override
    public Field<String> getStaEntityId() {
        return StaEntity.HISTORICAL_LOCATION.STA_IDENTIFIER;
    }

    @Override
    public Field<Long> getEntityId() {
        return StaEntity.HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID;
    }
}
