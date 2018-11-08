/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.data.service;

import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.FormatRepository;
import org.n52.series.db.ProcedureRepository;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.SensorQuerySpecifications;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.ProcedureHistoryRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.SensorMapper;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

/**
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class SensorService extends AbstractSensorThingsEntityService<ProcedureRepository, ProcedureEntity> {

    private SensorMapper mapper;
    
    @Autowired
    private FormatRepository formatRepository;
    
    @Autowired(required=false)
    private ProcedureHistoryRepository procedureHistoryRepository;
    
    @Autowired
    private DatastreamRepository datastreamRepository;

    private final static SensorQuerySpecifications sQS = new SensorQuerySpecifications();
    
    private final static DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();

    public SensorService(ProcedureRepository repository, SensorMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }
    
    @Override
    public EntityTypes getType() {
        return EntityTypes.Sensor;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) {
        EntityCollection retEntitySet = new EntityCollection();
        getRepository().findAll(createPageableRequest(queryOptions)).forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(Long id) {
        Optional<ProcedureEntity> entity = getRepository().findOne(byId(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType, QueryOptions queryOptions) {
        return null;
    }

    @Override
    public boolean existsEntity(Long id) {
        return getRepository().exists(byId(id));
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.existsRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        switch(sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.Datastream": {
            BooleanExpression filter = sQS.withDatastream(sourceId);
            if (targetId != null) {
                filter = filter.and(sQS.withId(targetId));
            }
            return getRepository().exists(filter);
        }
        default: return false;
        }
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<ProcedureEntity> sensor = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (sensor.isPresent()) {
            return OptionalLong.of(sensor.get().getId());
        } else {
            return OptionalLong.empty();
        }
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<ProcedureEntity> sensor = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (sensor.isPresent()) {
            return mapper.createEntity(sensor.get());
        } else {
            return null;
        }
    }

    /**
     * Retrieves Sensor Entity (aka Procedure Entity) with Relation to sourceEntity from Database.
     * Returns empty if Sensor is not found or Entities are not related.
     * 
     * @param sourceId Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId Id of the Entity to be retrieved
     * @return Optional<ProcedureEntity> Requested Entity
     */
    private Optional<ProcedureEntity> getRelatedEntityRaw(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        BooleanExpression filter;
        switch(sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.Datastream": {
            filter = sQS.withDatastream(sourceId);
            break;
        }
        default: return Optional.empty();
        }

        if (targetId != null) {
            filter = filter.and(sQS.withId(targetId));
        }
        return getRepository().findOne(filter);
    }

    /**
     * Constructs SQL Expression to request Entity by ID.
     * 
     * @param id id of the requested entity
     * @return BooleanExpression evaluating to true if Entity is found and valid
     */
    private BooleanExpression byId(Long id) {
        return sQS.withId(id);
    }

    @Override
    public ProcedureEntity create(ProcedureEntity sensor) throws ODataApplicationException {
        if (sensor.getId() != null && !sensor.isSetName()) {
            return getRepository().findOne(sQS.withId(sensor.getId())).get();
        }
        if (getRepository().exists(sQS.withIdentifier(sensor.getIdentifier()))
                || getRepository().exists(sQS.wihtName(sensor.getName()))) {
            Optional<ProcedureEntity> optional = getRepository()
                    .findOne(sQS.withIdentifier(sensor.getIdentifier()).or(sQS.wihtName(sensor.getName())));
            return optional.isPresent() ?  optional.get() : null;
        }
        checkFormat(sensor);
        ProcedureEntity procedure = getAsProcedureEntity(sensor);
        checkProcedureHistory(getRepository().save(getRepository().save(procedure)));
        return procedure;
    }

    @Override
    public ProcedureEntity update(ProcedureEntity entity, HttpMethod method) throws ODataApplicationException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            Optional<ProcedureEntity> existing = getRepository().findOne(sQS.withId(entity.getId()));
            if (existing.isPresent()) {
                ProcedureEntity merged = mapper.merge(existing.get(), entity);
                if (entity instanceof SensorEntity) {
                    // TODO insert datastream
                }
                return getRepository().save(getAsProcedureEntity(merged));
            }
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        throw new ODataApplicationException("Invalid http method for updating entity!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }

    private void checkUpdate(ProcedureEntity entity) throws ODataApplicationException {
        if (entity instanceof SensorEntity) {
            SensorEntity sensor = (SensorEntity) entity;
            if (sensor.hasDatastreams()) {
                for (DatastreamEntity datastream : sensor.getDatastreams()) {
                    checkInlineDatastream(datastream);
                }
            }
        }
    }
    
    @Override
    protected ProcedureEntity update(ProcedureEntity entity) throws ODataApplicationException {
        return getRepository().save(getAsProcedureEntity(entity));
    }

    @Override
    public void delete(Long id) throws ODataApplicationException {
        if (getRepository().existsById(id)) {
            // delete datastreams
            datastreamRepository.findAll(dQS.withSensor(id)).forEach(d -> {
                try {
                    // TODO delete observation and datasets ...
                    getDatastreamService().delete(d.getId());
                } catch (ODataApplicationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            getRepository().deleteById(id);
        } else {
            throw new ODataApplicationException("Entity not found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        }
    }

    @Override
    protected void delete(ProcedureEntity entity) throws ODataApplicationException {
        getRepository().deleteById(entity.getId());
    }

    private void checkFormat(ProcedureEntity sensor) {
        FormatEntity format;
        if (!formatRepository.existsByFormat(sensor.getFormat().getFormat())) {
            format = formatRepository.save(sensor.getFormat());
        } else {
            format = formatRepository.findByFormat(sensor.getFormat().getFormat());
        }
        sensor.setFormat(format);
        if (sensor.hasProcedureHistory()) {
            sensor.getProcedureHistory().forEach(pf -> pf.setFormat(format));
        }
    }

    private void checkProcedureHistory(ProcedureEntity sensor) {
        if (sensor.hasProcedureHistory()) {
            if (procedureHistoryRepository != null) {
                for (ProcedureHistoryEntity procedureHistory : sensor.getProcedureHistory()) {
                    sensor.getProcedureHistory().add(procedureHistoryRepository.save(procedureHistory));
                }
            } else {
                sensor.setDescriptionFile(sensor.getProcedureHistory().iterator().next().getXml());
            }
        }
    }
    
    private ProcedureEntity getAsProcedureEntity(ProcedureEntity sensor) {
        return  sensor instanceof ProcedureEntity
                ? ((SensorEntity) sensor).asProcedureEntity()
                : sensor;
    }
    
    private AbstractSensorThingsEntityService<?, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(
                EntityTypes.Datastream);
    }
}
