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
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.ObservedPropertyEntityDefinition;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.data.cloudnative.DTOMapper;
import org.n52.sta.data.cloudnative.condition.ObservedPropertyQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.ObservedPropertyDao;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ObservedPropertyDaoImpl extends AbstractStaEntityDao<ObservedPropertyDTO> implements ObservedPropertyDao {
    @Override
    protected List<ObservedPropertyDTO> mapResultToDTO(Result<Record> result) {
        List<ObservedPropertyDTO> observedProperties = new ArrayList<ObservedPropertyDTO>();
        Map<Long, ObservedPropertyDTO> observedPropertyMap = new HashMap<Long, ObservedPropertyDTO>();
        for (Record record : result) {
            Long Id = record.get(StaEntity.OBSERVED_PROPERTY.PHENOMENON_ID);
            ObservedPropertyDTO observedProperty = observedPropertyMap.computeIfAbsent(Id,
                    k -> record.map(new DTOMapper.ObservedPropertyRecordMapper()));
            observedProperty.getDatastreams().add(record.map(new DTOMapper.DatastreamRecordMapper()));
            observedProperties.add(observedProperty);
        }
        return observedProperties;
    }

    @Override
    public boolean existsByName(String name, Class<ObservedPropertyDTO> entityClass)
            throws STAInvalidQueryException {
        Condition predicate = StaEntity.OBSERVED_PROPERTY.NAME.eq(name);
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
    public List<Table<?>> createJoinList(ExpandFilter expandOption) throws STAInvalidQueryException {
        List<Table<?>> joinList = new ArrayList<>();
        joinList.add(StaEntity.OBSERVED_PROPERTY_PROPERTIES);
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                if (ObservedPropertyEntityDefinition.DATASTREAMS.equals(expandProperty)) {
                    joinList.add(StaEntity.DATASTREAM);
                }
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                        expandProperty,
                        StaConstants.OBSERVED_PROPERTY));
            }
        }
        return joinList;
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
}
