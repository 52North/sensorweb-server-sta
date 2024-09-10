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

package org.n52.sta.data.cloudnative.service;

import org.jooq.Condition;
import org.jooq.Field;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.ogc.sta.model.SensorEntityDefinition;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.SensorQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.SensorDao;
import org.n52.sta.data.cloudnative.dao.impl.DatastreamDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.FormatDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.SensorDaoImpl;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Format;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.n52.sta.data.cloudnative.dao.StaEntityDao.INVALID_EXPAND_OPTION_SUPPLIED;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class SensorService
        extends AbstractSensorThingsEntityServiceImpl<SensorDao, SensorDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorService.class);

    private static final SensorQueryConditions sQC = new SensorQueryConditions();
    private static final DatastreamQueryConditions dQC = new DatastreamQueryConditions();

    private final DatastreamDaoImpl datastreamDao;
    private final SensorDaoImpl sensorDao;
    private final FormatService formatService;
    private final AtomicLong TS = new AtomicLong();

    public SensorService(SensorDaoImpl sensorDao,
                         DatastreamDaoImpl datastreamDao,
                         FormatService formatService,
                         Class<SensorDTO> entityClass) {
        super(sensorDao, entityClass);
        this.datastreamDao = datastreamDao;
        this.sensorDao = sensorDao;
        this.formatService = formatService;
    }

    @Override
    protected SensorDTO fetchExpandEntitiesWithFilter(SensorDTO entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            String expandProperty = expandItem.getPath();
            if (SensorEntityDefinition.DATASTREAMS.equals(expandProperty)) {
                Page<DatastreamDTO> datastreams = getDatastreamService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getId(),
                                STAEntityDefinition.SENSORS,
                                expandItem.getQueryOptions());
                entity.setDatastreams(datastreams.get().collect(Collectors.toSet()));
            } else {
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                        expandProperty,
                        StaConstants.SENSOR));
            }
        }
        return entity;
    }

    @Override
    protected Condition byRelatedEntityFilter(String relatedId, String relatedType, String ownId)
            throws STAInvalidQueryException {
        Condition filter;
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS: {
                filter = sQC.withDatastreamStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(sQC.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    protected SensorDTO createOrfetch(SensorDTO entity)
            throws STACRUDException, STAInvalidQueryException {

        if (entity.getId() != null && entity.getName() == null) {
            Optional<SensorDTO> optionalEntity =
                    sensorDao.findByStaIdentifier(entity.getId(),null, entityClass);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                        StaConstants.SENSOR,
                        entity.getId()));
            }
        }
        if (entity.getId() == null) {
            if (sensorDao.existsByName(entity.getName(), entityClass)) {
                Optional<SensorDTO> optional = sensorDao
                        .findOne(sQC.withName(entity.getName()),
                                null,
                                entityClass);
                return optional.orElse(null);
            }
            else {
                entity.setId(NULL_ID_MASK);
            }
        }

        synchronized (getLock(entity.getId())) {
            if (!Objects.equals(entity.getId(), NULL_ID_MASK) &&
                    sensorDao.existsByStaIdentifier(entity.getId(), entityClass)) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            }

            entity.setId(getUniqueTimestamp().toString());
            // Intermediate save to allow DatastreamService->createOrUpdate to use this entity. Does not trigger
            // intercept handling (e.g. mqtt). Needed as Datastream<->Procedure connection is not yet set but
            // required by interceptors
            // ProcedureEntity intermediateSave = getRepository().intermediateSave(entity);
            if (entity.getProperties() != null) {
                sensorDao.saveSensorParameters(entity.getId(), entity.getProperties());
            }

            // Save with Interception as procedure is now linked to Datastream
            sensorDao.save(POJOWrapper(entity));

            if (entity.getDatastreams() != null) {
                for (DatastreamDTO datastreamEntity : entity.getDatastreams()) {
                    try {
                        getDatastreamService().createOrUpdate(datastreamEntity);
                    } catch (STACRUDException e) {
                        // Datastream might be currently processing.
                    }
                }
            }
            return entity;
        }
    }

    private Procedure POJOWrapper(SensorDTO entity) throws STACRUDException {
        Procedure sensorPOJO = new Procedure();
        sensorPOJO.setDescription(entity.getDescription());
        sensorPOJO.setIdentifier(entity.getId());
        sensorPOJO.setName(entity.getName());
        sensorPOJO.setStaIdentifier(entity.getId());
        sensorPOJO.setProcedureId(Long.valueOf(entity.getId()));
        sensorPOJO.setDescriptionFile(entity.getMetadata());
        Format formatPOJO = formatService.createOrFetchFormat(entity.getEncodingType());
        sensorPOJO.setFkFormatId(formatPOJO.getFormatId());
        return sensorPOJO;
    }

    @Override
    protected SensorDTO updateEntity(String id, SensorDTO entity, HttpMethod method)
            throws STACRUDException, STAInvalidQueryException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<SensorDTO> existing = sensorDao.findByStaIdentifier(id, null, entityClass);
                if (existing.isPresent() && entity != null) {
                    SensorDTO merged = merge(existing.get(), entity);
                    if (entity.getDatastreams() != null) {
                        for (DatastreamDTO datastreamEntity : entity.getDatastreams()) {
                            getDatastreamService().createOrUpdate(datastreamEntity);
                        }
                    }
                    sensorDao.update(POJOWrapper(merged));
                    return merged;
                }
            }
            throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    private void checkUpdate(SensorDTO entity) throws STACRUDException {
        if (entity.getDatastreams() != null) {
            for (DatastreamDTO datastream : entity.getDatastreams()) {
                checkInlineDatastream(datastream);
            }
        }
    }

    @Override
    protected SensorDTO createOrUpdate(SensorDTO entity)
            throws STACRUDException, STAInvalidQueryException {
        if (entity.getId() != null && sensorDao.existsByStaIdentifier(entity.getId(), entityClass)) {
            return updateEntity(entity.getId(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override
    protected void deleteEntity(String staIdentifier)
            throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(staIdentifier)) {
            if (sensorDao.existsByStaIdentifier(staIdentifier, entityClass)) {
                // delete datastreams
                for (DatastreamDTO ds : datastreamDao.findAll(dQC.withSensorStaIdentifier(staIdentifier),
                        null,
                        DatastreamDTO.class)) {
                    getDatastreamService().delete(ds.getId());
                }

                SensorDTO sensor = sensorDao.findByStaIdentifier(staIdentifier, null, entityClass).get();
                if (sensor.getProperties() != null) {
                    sensorDao.deleteSensorParameters(staIdentifier);
                }
                sensorDao.deleteByStaIdentifier(staIdentifier);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    @Override
    protected String checkPropertyName(String property) {
        return sensorDao.checkPropertyName(property).getName();
    }

    @Override
    protected SensorDTO merge(SensorDTO existing, SensorDTO toMerge)
            throws STACRUDException {

        mergeNameDescription(existing, toMerge);

        if (toMerge.getEncodingType() != null) {
            existing.setEncodingType(toMerge.getEncodingType());
        }
        if(toMerge.getMetadata() != null) {
            existing.setMetadata(toMerge.getMetadata());
        }

        return existing;
    }

    @Override
    Field<String> getStaEntityId() {
        return StaEntity.SENSOR.STA_IDENTIFIER;
    }

    @Override
    AtomicLong getStaEntityTS() {
        return TS;
    }
}
