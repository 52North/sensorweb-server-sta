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
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.ObservedPropertyEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.ObservedPropertyQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.ObservedPropertyDao;
import org.n52.sta.data.cloudnative.dao.impl.DatastreamDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.ObservedPropertyDaoImpl;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Phenomenon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.n52.sta.data.cloudnative.dao.StaEntityDao.INVALID_EXPAND_OPTION_SUPPLIED;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class CloudNativeObservedPropertyService
        extends CloudNativeAbstractSensorThingsEntityServiceImpl<ObservedPropertyDao, ObservedPropertyDTO> {

    private static final Logger logger = LoggerFactory.getLogger(CloudNativeObservedPropertyService.class);

    private static DatastreamQueryConditions dsQC = new DatastreamQueryConditions();
    private static ObservedPropertyQueryConditions oQC = new ObservedPropertyQueryConditions();

    private final DatastreamDaoImpl datastreamDao;
    private final ObservedPropertyDaoImpl observedPropertyDao;
    private final AtomicLong TS = new AtomicLong();

    public CloudNativeObservedPropertyService(ObservedPropertyDaoImpl observedPropertyDao,
                                              DatastreamDaoImpl datastreamDao, MutexFactory lock) {
        super(observedPropertyDao, ObservedPropertyDTO.class, lock);
        this.datastreamDao = datastreamDao;
        this.observedPropertyDao = observedPropertyDao;
    }

    public static void setObservedPropertyQueryConditions(ObservedPropertyQueryConditions oQC) {
        CloudNativeObservedPropertyService.oQC = oQC;
    }

    public static void setDatastreamQueryConditions(DatastreamQueryConditions dsQC) {
        CloudNativeObservedPropertyService.dsQC = dsQC;
    }

    @Override
    public boolean existsEntityByRelatedEntity(String relatedId, String relatedType, String ownId)
            throws STAInvalidQueryException {
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS: {
                return observedPropertyDao
                        .findOne(byRelatedEntityFilter(relatedId, relatedType, ownId), null, entityClass)
                        .isPresent();
            }
            default:
                return false;
        }
    }

    @Override
    protected ObservedPropertyDTO fetchExpandEntitiesWithFilter(ObservedPropertyDTO entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            String expandProperty = expandItem.getPath();
            if (ObservedPropertyEntityDefinition.DATASTREAMS.equals(expandProperty)) {
                Page<DatastreamDTO> datastreams = getDatastreamService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getId(),
                                STAEntityDefinition.OBSERVED_PROPERTIES,
                                expandItem.getQueryOptions());
                entity.setDatastreams(datastreams.get().collect(Collectors.toSet()));
            } else {
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                        expandProperty,
                        StaConstants.OBSERVED_PROPERTY));
            }
        }
        return entity;
    }

    @Override
    public Condition byRelatedEntityFilter(String relatedId,
                                           String relatedType,
                                           String ownId) {
        Condition filter;
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS: {
                filter = oQC.withDatastreamStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }
        if (ownId != null) {
            filter = filter.and(oQC.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    protected ObservedPropertyDTO createOrfetch(ObservedPropertyDTO entity)
            throws STACRUDException, STAInvalidQueryException {

        if (entity.getId() != null && entity.getName() == null) {
            Optional<ObservedPropertyDTO> optionalEntity =
                    observedPropertyDao.findByStaIdentifier(entity.getId(), null, entityClass);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                        StaConstants.OBSERVED_PROPERTY,
                        entity.getId()));
            }
        }

        if (entity.getId() == null) {
            if (observedPropertyDao.existsByName(entity.getName(), entityClass)) {
                Optional<ObservedPropertyDTO> optional
                        = observedPropertyDao.findOne(oQC.withName(entity.getName()), null, entityClass);
                return optional.orElse(null);
            } else {
                entity.setId(NULL_ID_MASK);
            }
        }
        synchronized (getLock(entity.getId())) {
            // Check for duplicate definition
            if (observedPropertyDao.existsByDefinition(entity.getDefinition(), entityClass)) {
                throw new STACRUDException("Observed Property with given Definition already exists!",
                        HTTPStatus.CONFLICT);
            }
            if (!entity.getId().equals(NULL_ID_MASK) &&
                    observedPropertyDao.existsByStaIdentifier(entity.getId(), entityClass)) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            }
            // Autogenerate Identifier
            entity.setId(getUniqueTimestamp().toString());
            if (entity.getProperties() != null) {
                observedPropertyDao.saveObservedPropertyParameters(entity.getId(), entity.getProperties());
            }
            observedPropertyDao.save(POJOWrapper(entity));
            return entity;
        }
    }

    private Phenomenon POJOWrapper(ObservedPropertyDTO entity) {
        Phenomenon observedPropertyPOJO = new Phenomenon();
        observedPropertyPOJO.setDescription(entity.getDescription());
        observedPropertyPOJO.setName(entity.getName());
        observedPropertyPOJO.setIdentifier(entity.getDefinition());
        observedPropertyPOJO.setStaIdentifier(entity.getId());
        observedPropertyPOJO.setPhenomenonId(Long.valueOf(entity.getId()));
        return observedPropertyPOJO;
    }

    @Override
    protected ObservedPropertyDTO updateEntity(String id, ObservedPropertyDTO entity, HttpMethod method)
            throws STACRUDException, STAInvalidQueryException {

        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<ObservedPropertyDTO> existing = observedPropertyDao
                        .findByStaIdentifier(id, null, entityClass);
                if (existing.isPresent()) {
                    ObservedPropertyDTO merged = merge(existing.get(), entity);
                    observedPropertyDao.update(POJOWrapper(merged));
                    return merged;
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override
    protected ObservedPropertyDTO createOrUpdate(ObservedPropertyDTO entity)
            throws STACRUDException, STAInvalidQueryException {
        if (entity.getId() != null && observedPropertyDao.existsByStaIdentifier(entity.getId(), entityClass)) {
            return updateEntity(entity.getId(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override
    protected void deleteEntity(String id)
            throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(id)) {
            if (observedPropertyDao.existsByStaIdentifier(id, entityClass)) {
                ObservedPropertyDTO observedProperty = observedPropertyDao
                        .findByStaIdentifier(id, null, entityClass).get();
                // delete datastreams
                Condition predicate = dsQC.withObservedPropertyStaIdentifier(id).
                        and(StaEntity.DATASTREAM.STA_IDENTIFIER.isNotNull());
                List<DatastreamDTO> relatedDatastreams = datastreamDao.findAll(
                        predicate,
                        null,
                        DatastreamDTO.class);
                for (DatastreamDTO datastreamEntity: relatedDatastreams) {
                    getDatastreamService().delete(datastreamEntity.getId());
                }
                // delete parameters
                if (observedProperty.getProperties() != null) {
                    observedPropertyDao.deleteObservedPropertyParameters(Long.valueOf(observedProperty.getId()));
                }
                observedPropertyDao.deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    @Override
    protected ObservedPropertyDTO merge(ObservedPropertyDTO existing, ObservedPropertyDTO toMerge) {
        if(toMerge.getDefinition() != null) {
            existing.setDefinition(toMerge.getDefinition());
        }
        mergeNameDescription(existing, toMerge);

        return existing;
    }

    private void checkUpdate(ObservedPropertyDTO entity) throws STACRUDException {
        if (entity.getDatastreams() != null) {
            for (DatastreamDTO datastream : entity.getDatastreams()) {
                checkInlineDatastream(datastream);
            }
        }
    }

    @Override
    protected String checkPropertyName(String property) {
        return observedPropertyDao.checkPropertyName(property).getName();
    }

    @Override
    protected Field<String> getStaEntityId() {
        return StaEntity.OBSERVED_PROPERTY.STA_IDENTIFIER;
    }

    @Override
    protected AtomicLong getStaEntityTS() {
        return TS;
    }
}
