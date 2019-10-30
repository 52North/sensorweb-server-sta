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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.n52.sta.exception.STACRUDException;
import org.n52.sta.mapping.SensorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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


    @Autowired
    public SensorService(ProcedureRepository repository,
                         SensorMapper mapper,
                         FormatRepository formatRepository,
                         ProcedureHistoryRepository procedureHistoryRepository,
                         DatastreamRepository datastreamRepository,
                         ObjectMapper SensorSerdes) {
        super(repository, ProcedureEntity.class);
        this.formatRepository = formatRepository;
        this.procedureHistoryRepository = procedureHistoryRepository;
        this.datastreamRepository = datastreamRepository;
    }

    /**
     * Returns the EntityType this Service handles
     *
     * @return EntityType this Service handles
     */
    @Override
    public EntityTypes getType() {
        return EntityTypes.Sensor;
    }

    @Override
    protected Specification<ProcedureEntity> byRelatedEntityFilter(String relatedId,
                                                                   String relatedType,
                                                                   String ownId) {
        Specification<ProcedureEntity> filter;
        switch (relatedType) {
            case IOT_DATASTREAM: {
                filter = sQS.withDatastreamIdentifier(relatedId);
                break;
            }
            default:
                return null;
        }

        if (ownId != null) {
            filter = filter.and(sQS.withIdentifier(ownId));
        }
        return filter;
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

    @Override
    public ProcedureEntity create(ProcedureEntity sensor) throws STACRUDException {
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
            throw new STACRUDException("Identifier already exists!",
                    HttpStatus.BAD_REQUEST);
        }
        checkFormat(sensor);
        ProcedureEntity procedure = getAsProcedureEntity(sensor);
        checkProcedureHistory(getRepository().save(procedure));
        return procedure;
    }

    @Override
    public ProcedureEntity update(ProcedureEntity entity, HttpMethod method) throws STACRUDException {
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
            throw new STACRUDException("Unable to update. Entity not found.", HttpStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HttpStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ProcedureEntity update(ProcedureEntity entity) {
        return getRepository().save(getAsProcedureEntity(entity));
    }

    private void checkUpdate(ProcedureEntity entity) throws STACRUDException {
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
    public void delete(String identifier) throws STACRUDException {
        if (getRepository().existsByIdentifier(identifier)) {
            // delete datastreams
            datastreamRepository.findAll(dQS.withSensorIdentifier(identifier)).forEach(d -> {
                try {
                    // TODO delete observation and datasets ...
                    getDatastreamService().delete(d.getIdentifier());
                } catch (STACRUDException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            getRepository().deleteByIdentifier(identifier);
        } else {
            throw new STACRUDException("Unable to delete. Entity not found.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    protected void delete(ProcedureEntity entity) {
        getRepository().deleteByIdentifier(entity.getIdentifier());

    }

    @Override
    protected ProcedureEntity createOrUpdate(ProcedureEntity entity) throws STACRUDException {
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
}
