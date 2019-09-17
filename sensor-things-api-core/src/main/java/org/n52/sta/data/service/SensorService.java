/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.SensorQuerySpecifications;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.FormatRepository;
import org.n52.sta.data.repositories.ProcedureHistoryRepository;
import org.n52.sta.data.repositories.ProcedureRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.edm.provider.entities.DatastreamEntityProvider;
import org.n52.sta.mapping.SensorMapper;
import org.n52.sta.service.query.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class SensorService extends AbstractSensorThingsEntityService<ProcedureRepository, ProcedureEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SensorService.class);

    private static final SensorQuerySpecifications sQS = new SensorQuerySpecifications();
    private static final DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();

    private final FormatRepository formatRepository;
    private final ProcedureHistoryRepository procedureHistoryRepository;
    private final DatastreamRepository datastreamRepository;
    private final String IOT_DATASTREAM = "iot.Datastream";

    private SensorMapper mapper;

    @Autowired
    public SensorService(ProcedureRepository repository,
                         SensorMapper mapper,
                         FormatRepository formatRepository,
                         ProcedureHistoryRepository procedureHistoryRepository,
                         DatastreamRepository datastreamRepository) {
        super(repository);
        this.mapper = mapper;
        this.formatRepository = formatRepository;
        this.procedureHistoryRepository = procedureHistoryRepository;
        this.datastreamRepository = datastreamRepository;
    }

    @Override
    public EntityTypes getType() {
        return EntityTypes.Sensor;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) throws ODataApplicationException {
        EntityCollection retEntitySet = new EntityCollection();
        Specification<ProcedureEntity> filter = getFilterPredicate(ProcedureEntity.class, queryOptions);
        getRepository().findAll(filter, createPageableRequest(queryOptions))
                .forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(String identifier) {
        Optional<ProcedureEntity> entity = getRepository().findByIdentifier(identifier);
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(String sourceId,
                                                       EdmEntityType sourceEntityType,
                                                       QueryOptions queryOptions) {
        return null;
    }

    @Override
    public boolean existsEntity(String id) {
        return getRepository().existsByIdentifier(id);
    }

    @Override
    public boolean existsRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.existsRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public boolean existsRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case IOT_DATASTREAM: {
                Specification<ProcedureEntity> filter = sQS.withDatastreamIdentifier(sourceId);
                if (targetId != null) {
                    filter = filter.and(sQS.withIdentifier(targetId));
                }
                return getRepository().count(filter) > 0;
            }
            default:
                return false;
        }
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<ProcedureEntity> sensor = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return sensor.map(procedureEntity -> Optional.of(procedureEntity.getIdentifier())).orElseGet(Optional::empty);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<ProcedureEntity> sensor = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return sensor.map(procedureEntity -> mapper.createEntity(procedureEntity)).orElse(null);
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            case "encodingType":
                return ProcedureEntity.PROPERTY_PROCEDURE_DESCRIPTION_FORMAT;
            case "metadata":
                // TODO: Add sorting by HistoricalLocation that replaces Description if it is not present
                return "descriptionFile";
            default:
                return super.checkPropertyName(property);
        }
    }

    /**
     * Retrieves Sensor Entity (aka Procedure Entity) with Relation to sourceEntity from Database. Returns
     * empty if Sensor is not found or Entities are not related.
     *
     * @param sourceId         Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId         Id of the Entity to be retrieved
     * @return Optional&lt;ProcedureEntity&gt; Requested Entity
     */
    private Optional<ProcedureEntity> getRelatedEntityRaw(String sourceId,
                                                          EdmEntityType sourceEntityType,
                                                          String targetId) {
        Specification<ProcedureEntity> filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case IOT_DATASTREAM: {
                filter = sQS.withDatastreamIdentifier(sourceId);
                break;
            }
            default:
                return Optional.empty();
        }

        if (targetId != null) {
            filter = filter.and(sQS.withIdentifier(targetId));
        }
        return getRepository().findOne(filter);
    }

    @Override
    public long getCount(QueryOptions queryOptions) throws ODataApplicationException {
        return getRepository().count(getFilterPredicate(ProcedureEntity.class, queryOptions));
    }

    @Override
    public ProcedureEntity create(ProcedureEntity sensor) throws ODataApplicationException {
        if (sensor.getIdentifier() != null && !sensor.isSetName()) {
            return getRepository().findByIdentifier(sensor.getIdentifier()).get();
        }
        if (sensor.getIdentifier() == null) {
            if (getRepository().existsByName(sensor.getName())) {
                Optional<ProcedureEntity> optional = getRepository()
                        .findOne(sQS.withIdentifier(sensor.getIdentifier()).or(sQS.withName(sensor.getName())));
                return optional.isPresent() ? optional.get() : null;
            } else {
                // Autogenerate Identifier
                sensor.setIdentifier(UUID.randomUUID().toString());
            }
        } else if (getRepository().existsByIdentifier(sensor.getIdentifier())) {
            throw new ODataApplicationException("Identifier already exists!",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
        checkFormat(sensor);
        ProcedureEntity procedure = getAsProcedureEntity(sensor);
        checkProcedureHistory(getRepository().save(procedure));
        return procedure;
    }

    @Override
    public ProcedureEntity update(ProcedureEntity entity, HttpMethod method) throws ODataApplicationException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            Optional<ProcedureEntity> existing = getRepository().findByIdentifier(entity.getIdentifier());
            if (existing.isPresent()) {
                ProcedureEntity merged = mapper.merge(existing.get(), entity);
                if (entity instanceof SensorEntity) {
                    // TODO insert datastream
                    logger.trace("TODO: insert datastream.");
                }
                return getRepository().save(getAsProcedureEntity(merged));
            }
            throw new ODataApplicationException(
                    "Unable to update. Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        throw new ODataApplicationException("Invalid http method for updating entity!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }

    @Override
    protected ProcedureEntity update(ProcedureEntity entity) throws ODataApplicationException {
        return getRepository().save(getAsProcedureEntity(entity));
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
    public void delete(String identifier) throws ODataApplicationException {
        if (getRepository().existsByIdentifier(identifier)) {
            // delete datastreams
            datastreamRepository.findAll(dQS.withSensorIdentifier(identifier)).forEach(d -> {
                try {
                    // TODO delete observation and datasets ...
                    getDatastreamService().delete(d.getIdentifier());
                } catch (ODataApplicationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            getRepository().deleteByIdentifier(identifier);
        } else {
            throw new ODataApplicationException(
                    "Unable to delete. Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        }
    }

    @Override
    protected void delete(ProcedureEntity entity) {
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected ProcedureEntity createOrUpdate(ProcedureEntity entity) throws ODataApplicationException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return update(entity, HttpMethod.PATCH);
        }
        return create(entity);
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
                    procedureHistory.setProcedure(sensor);
                    sensor.getProcedureHistory().add(procedureHistoryRepository.save(procedureHistory));
                }
            } else {
                sensor.setDescriptionFile(sensor.getProcedureHistory().iterator().next().getXml());
            }
        }
    }

    private ProcedureEntity getAsProcedureEntity(ProcedureEntity sensor) {
        return sensor instanceof SensorEntity
                ? ((SensorEntity) sensor).asProcedureEntity()
                : sensor;
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(
                EntityTypes.Datastream);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    @Override
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        SensorEntity entity = (SensorEntity) rawObject;

        if (entity.hasDatastreams()) {
            collections.put(DatastreamEntityProvider.ET_DATASTREAM_NAME,
                    entity.getDatastreams()
                            .stream()
                            .map(DatastreamEntity::getIdentifier)
                            .collect(Collectors.toSet()));
        }
        return collections;
    }
}
