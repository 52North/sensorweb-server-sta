/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.data.common.service;

import org.hibernate.Hibernate;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.Dataset;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.parameter.procedure.ProcedureParameterEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.ogc.sta.model.SensorEntityDefinition;
import org.n52.sta.data.common.CommonDatastreamService;
import org.n52.sta.data.common.query.DatastreamQuerySpecifications;
import org.n52.sta.data.common.query.SensorQuerySpecifications;
import org.n52.sta.data.common.repositories.DatastreamRepository;
import org.n52.sta.data.common.repositories.EntityGraphRepository;
import org.n52.sta.data.common.repositories.FormatRepository;
import org.n52.sta.data.common.repositories.ProcedureHistoryRepository;
import org.n52.sta.data.common.repositories.ProcedureParameterRepository;
import org.n52.sta.data.common.repositories.ProcedureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.n52.sta.api.dto.SensorDTO;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class SensorService
    extends
    org.n52.sta.data.common.CommonSTAServiceImpl<ProcedureRepository, SensorDTO, ProcedureEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorService.class);

    private static final SensorQuerySpecifications sQS = new SensorQuerySpecifications();
    private static final DatastreamQuerySpecifications<Dataset> dQS = new DatastreamQuerySpecifications<>();

    private final FormatRepository formatRepository;
    private final ProcedureHistoryRepository procedureHistoryRepository;
    private final DatastreamRepository datastreamRepository;
    private final ProcedureParameterRepository parameterRepository;

    @Autowired
    public SensorService(ProcedureRepository repository,
                         FormatRepository formatRepository,
                         ProcedureHistoryRepository procedureHistoryRepository,
                         DatastreamRepository datastreamRepository,
                         ProcedureParameterRepository parameterRepository,
                         EntityManager em) {
        super(repository, em, ProcedureEntity.class);
        this.formatRepository = formatRepository;
        this.procedureHistoryRepository = procedureHistoryRepository;
        this.datastreamRepository = datastreamRepository;
        this.parameterRepository = parameterRepository;
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
        throws STAInvalidQueryException {
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                if (SensorEntityDefinition.DATASTREAMS.equals(expandProperty)) {
                    return new EntityGraphRepository.FetchGraph[] {
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_FORMAT,
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_PROCEDUREHISTORY,
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS,
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS,
                    };
                }
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                 expandProperty,
                                                                 StaConstants.SENSOR));
            }
        }
        return new EntityGraphRepository.FetchGraph[] {
            EntityGraphRepository.FetchGraph.FETCHGRAPH_FORMAT,
            EntityGraphRepository.FetchGraph.FETCHGRAPH_PROCEDUREHISTORY,
            EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS,
        };
    }

    @Override protected ProcedureEntity fetchExpandEntitiesWithFilter(ProcedureEntity entity, ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            String expandProperty = expandItem.getPath();
            if (SensorEntityDefinition.DATASTREAMS.equals(expandProperty)) {
                Page<AbstractDatasetEntity> datastreams = getDatastreamService()
                    .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                           STAEntityDefinition.SENSORS,
                                                           expandItem.getQueryOptions());
                entity.setDatasets(datastreams.get().collect(Collectors.toSet()));
            } else {
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                 expandProperty,
                                                                 StaConstants.SENSOR));
            }
        }
        return entity;
    }

    @Override
    protected Specification<ProcedureEntity> byRelatedEntityFilter(String relatedId,
                                                                   String relatedType,
                                                                   String ownId) {
        Specification<ProcedureEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS: {
                filter = sQS.withDatastreamStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(sQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public ProcedureEntity createOrfetch(ProcedureEntity sensor) throws STACRUDException {
        if (sensor.getStaIdentifier() != null && !sensor.isSetName()) {
            Optional<ProcedureEntity> optionalEntity =
                getRepository().findByStaIdentifier(sensor.getStaIdentifier(),
                                                    EntityGraphRepository.FetchGraph.FETCHGRAPH_FORMAT);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                         StaConstants.SENSOR,
                                                         sensor.getStaIdentifier()));
            }
        }
        if (sensor.getStaIdentifier() == null) {
            if (getRepository().existsByName(sensor.getName())) {
                Optional<ProcedureEntity> optional = getRepository()
                    .findOne(sQS.withStaIdentifier(sensor.getStaIdentifier())
                                 .or(sQS.withName(sensor.getName())));
                return optional.isPresent() ? optional.get() : null;
            } else {
                // Autogenerate Identifier
                String uuid = UUID.randomUUID().toString();
                sensor.setIdentifier(uuid);
                sensor.setStaIdentifier(uuid);
            }
        }

        synchronized (getLock(sensor.getStaIdentifier())) {
            if (getRepository().existsByStaIdentifier(sensor.getStaIdentifier())) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            }
            checkFormat(sensor, sensor);
            // Intermediate save to allow DatastreamService->createOrUpdate to use this entity. Does not trigger
            // intercept handling (e.g. mqtt). Needed as Datastream<->Procedure connection is not yet set but
            // required by interceptors
            ProcedureEntity intermediateSave = getRepository().intermediateSave(sensor);
            checkProcedureHistory(sensor);
            if (sensor.hasDatastreams()) {
                for (AbstractDatasetEntity datastreamEntity : sensor.getDatasets()) {
                    try {
                        getDatastreamService().createOrUpdate((Dataset) datastreamEntity);
                    } catch (STACRUDException e) {
                        // Datastream might be currently processing.
                        //TODO: check if we need to do something here
                    }
                }
            }
            if (sensor.getParameters() != null) {
                parameterRepository.saveAll(sensor.getParameters()
                                                .stream()
                                                .filter(t -> t instanceof ProcedureParameterEntity)
                                                .map(t -> {
                                                    ((ProcedureParameterEntity) t).setProcedure(intermediateSave);
                                                    return (ProcedureParameterEntity) t;
                                                })
                                                .collect(Collectors.toSet()));
            }

            // Save with Interception as procedure is now linked to Datastream
            getRepository().save(sensor);
            return sensor;
        }
    }

    @Override
    public ProcedureEntity updateEntity(String id, ProcedureEntity entity, String method) throws
        STACRUDException {
        checkUpdate(entity);
        if ("PATCH".equals(method)) {
            return updateEntity(id, entity);
        } else if ("PUT".equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        } else {
            throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
        }

    }

    private ProcedureEntity updateEntity(String id, ProcedureEntity entity) throws
        STACRUDException {
        synchronized (getLock(id)) {
                Optional<ProcedureEntity> existing =
                    getRepository()
                        .findByStaIdentifier(id,
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_FORMAT,
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_PROCEDUREHISTORY);
                if (existing.isPresent()) {
                    ProcedureEntity merged = merge(existing.get(), entity);
                    if (entity != null) {
                        if (entity.hasDatastreams()) {
                            CommonDatastreamService dsService = getDatastreamService();
                            for (AbstractDatasetEntity datastreamEntity :
                                entity.getDatasets()) {
                                dsService.createOrUpdate(datastreamEntity);
                            }
                        }
                    }
                    checkFormat(merged, entity);
                    checkProcedureHistory(merged);
                    getRepository().save(merged);
                    Hibernate.initialize(merged.getParameters());
                    return merged;
                }
            }
            throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
    }

    @Override
    public ProcedureEntity createOrUpdate(ProcedureEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity);
        }
        return createOrfetch(entity);
    }

    @Override
    public String checkPropertyName(String property) {
        return sQS.checkPropertyName(property);
    }

    @Override
    protected ProcedureEntity merge(ProcedureEntity existing, ProcedureEntity toMerge) {
        mergeIdentifierNameDescription(existing, toMerge);
        if (toMerge.isSetDescriptionFile()) {
            existing.setDescriptionFile(toMerge.getDescriptionFile());
        }
        /*
        if (toMerge.isSetFormat()) {
            existing.setFormat(toMerge.getFormat());
        }
        */

        return existing;
    }

    @Override
    public void delete(String identifier) throws STACRUDException {
        synchronized (getLock(identifier)) {
            if (getRepository().existsByStaIdentifier(identifier)) {
                // delete datastreams
                for (AbstractDatasetEntity ds : datastreamRepository.findAll(dQS.withSensorStaIdentifier(identifier))) {
                    getDatastreamService().delete(ds.getStaIdentifier());
                }

                ProcedureEntity sensor = getRepository().findByStaIdentifier(identifier).get();
                if (sensor.hasParameters()) {
                    sensor.getParameters()
                        .forEach(entity -> parameterRepository.delete((ProcedureParameterEntity) entity));
                }
                getRepository().deleteByStaIdentifier(identifier);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    private void checkUpdate(ProcedureEntity entity) throws STACRUDException {
        if (entity instanceof ProcedureEntity) {
            ProcedureEntity sensor = (ProcedureEntity) entity;
            if (sensor.hasDatastreams()) {
                for (AbstractDatasetEntity datastream : sensor.getDatasets()) {
                    checkInlineDatastream(datastream);
                }
            }
        }
    }

    private void checkFormat(ProcedureEntity mergedSensor, ProcedureEntity newSensor) throws STACRUDException {
        FormatEntity format;
        synchronized (getLock(mergedSensor.getFormat().getFormat())) {
            if (newSensor.getFormat() != null) {
                if (!formatRepository.existsByFormat(newSensor.getFormat().getFormat())) {
                    format = formatRepository.save(newSensor.getFormat());
                } else {
                    format = formatRepository.findByFormat(newSensor.getFormat().getFormat());
                }
                mergedSensor.setFormat(format);
                if (mergedSensor.hasProcedureHistory()) {
                    mergedSensor.getProcedureHistory().forEach(pf -> pf.setFormat(format));
                }
            }
        }
    }

    private void checkProcedureHistory(ProcedureEntity sensor) {
        if (sensor.hasProcedureHistory()) {
            if (procedureHistoryRepository != null) {
                for (ProcedureHistoryEntity procedureHistory : sensor.getProcedureHistory()) {
                    procedureHistory.setProcedure(sensor);
                    sensor.getProcedureHistory().add(procedureHistoryRepository.save(procedureHistory));
                }
            } else {
                sensor.setDescriptionFile(sensor.getProcedureHistory().iterator().next().getXml());
            }
        }
    }
}
