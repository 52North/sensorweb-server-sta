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
package org.n52.sta.data.repositories;

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.SpringApplicationContext;
import org.n52.sta.data.STAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class MessageBusRepository<T, I extends Serializable>
        extends SimpleJpaRepository<T, I> {

    private static final Logger logger = LoggerFactory.getLogger(MessageBusRepository.class);

    private final String DESCRIPTION = "description";
    private final String NAME = "name";
    private final String ENCODINGTYPE = "encodingType";
    private final String LOCATION = "location";
    private final String PHENOMENONTIME = "phenomenonTime";
    private final String RESULTTIME = "resultTime";
    private final String VALIDTIME = "validTime";

    private JpaEntityInformation entityInformation;
    private STAEventHandler mqttHandler;
    private EntityManager em;

    MessageBusRepository(JpaEntityInformation entityInformation,
                         EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.em = entityManager;
        this.entityInformation = entityInformation;

        this.mqttHandler = (STAEventHandler) SpringApplicationContext.getBean("mqttEventHandler");
        Assert.notNull(this.mqttHandler, "Could not autowire Mqtt handler!");
    }

    @Transactional
    @Override
    public <S extends T> S save(S newEntity) {
        boolean intercept = mqttHandler.getWatchedEntityTypes().contains(entityInformation.getJavaType().getName());

        if (entityInformation.isNew(newEntity)) {
            em.persist(newEntity);
            em.flush();
            if (intercept) {
                this.mqttHandler.handleEvent(newEntity, null);
            }
        } else {
            if (intercept) {
                S oldEntity = (S) em.find(newEntity.getClass(), entityInformation.getId(newEntity));
                S entity = em.merge(newEntity);
                em.flush();
                // Entity was saved multiple times without changes. As reference is the same
                if (oldEntity == entity) {
                    return entity;
                }
                this.mqttHandler.handleEvent(entity, computeDifferenceMap(oldEntity, entity));
            } else {
                return em.merge(newEntity);
            }
        }

        return newEntity;
    }


    /**
     * Saves an entity to the Datastore without intercepting for mqtt subscription checking.
     * Used when Entity is saved multiple times during creation
     *
     * @param entity Entity to be saved
     * @param <S>    raw entity type
     * @return saved entity.
     */
    @Transactional
    public <S extends T> S intermediateSave(S entity) {
        if (entityInformation.isNew(entity)) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }

    private Set<String> computeDifferenceMap(Object oldE, Object newE) {
        HashSet<String> map = new HashSet<>();
        try {
            switch (oldE.getClass().getSimpleName()) {
                case "ProcedureEntity":
                    ProcedureEntity oldProcedure = (ProcedureEntity) oldE;
                    ProcedureEntity newProcedure = (ProcedureEntity) newE;
                    if (oldProcedure.getDescription() != null &&
                            !oldProcedure.getDescription().equals(newProcedure.getDescription())) {
                        map.add(DESCRIPTION);
                    }
                    if (oldProcedure.getName() != null &&
                            !oldProcedure.getName().equals(newProcedure.getName())) {
                        map.add(NAME);
                    }
                    if (oldProcedure.getDescriptionFile() != null &&
                            !oldProcedure.getDescriptionFile().equals(newProcedure.getDescriptionFile())) {
                        map.add("metadata");
                    }
                    if (oldProcedure.getFormat() != null &&
                            !oldProcedure.getFormat().getFormat().equals(newProcedure.getFormat().getFormat())) {
                        map.add(ENCODINGTYPE);
                    }
                    return map;
                case "LocationEntity":
                    LocationEntity oldLocation = (LocationEntity) oldE;
                    LocationEntity newLocation = (LocationEntity) newE;
                    if (oldLocation.getDescription() != null &&
                            !oldLocation.getDescription().equals(newLocation.getDescription())) {
                        map.add(DESCRIPTION);
                    }
                    if (oldLocation.getName() != null &&
                            !oldLocation.getName().equals(newLocation.getName())) {
                        map.add(NAME);
                    }

                    if (oldLocation.getGeometryEntity() != null
                        && !oldLocation.getGeometryEntity().getGeometry()
                            .equals(newLocation.getGeometryEntity().getGeometry())) {
                        map.add(LOCATION);
                    }
                    if (oldLocation.getLocation() != null &&
                            !oldLocation.getLocation().equals(newLocation.getLocation())) {
                        map.add(LOCATION);
                    }
                    if (oldLocation.getLocationEncoding() != null &&
                            !oldLocation.getLocationEncoding().getEncodingType().equals(
                                    newLocation.getLocationEncoding().getEncodingType())) {
                        map.add(ENCODINGTYPE);
                    }
                    return map;
                case "PlatformEntity":
                    PlatformEntity oldThing = (PlatformEntity) oldE;
                    PlatformEntity newThing = (PlatformEntity) newE;
                    if (oldThing.getDescription() != null &&
                            !oldThing.getDescription().equals(newThing.getDescription())) {
                        map.add(DESCRIPTION);
                    }
                    if (oldThing.getName() != null &&
                            !oldThing.getName().equals(newThing.getName())) {
                        map.add(NAME);
                    }
                    if (oldThing.getProperties() != null &&
                            !oldThing.getProperties().equals(newThing.getProperties())) {
                        map.add("properties");
                    }
                    return map;
                case "DatastreamEntity":
                    DatastreamEntity oldDatastream = (DatastreamEntity) oldE;
                    DatastreamEntity newDatastream = (DatastreamEntity) newE;
                    if (oldDatastream.getDescription() != null &&
                            !oldDatastream.getDescription().equals(
                                    newDatastream.getDescription())) {
                        map.add(DESCRIPTION);
                    }
                    if (oldDatastream.getName() != null &&
                            !oldDatastream.getName().equals(newDatastream.getName())) {
                        map.add(NAME);
                    }
                    if (oldDatastream.getObservationType() != null &&
                            !oldDatastream.getObservationType().getFormat().equals(
                                    newDatastream.getObservationType().getFormat())) {
                        map.add("observationType");
                    }
                    if (oldDatastream.getUnitOfMeasurement() != null &&
                            !oldDatastream.getUnitOfMeasurement().equals(newDatastream.getUnitOfMeasurement())) {
                        map.add("unitOfMeasurement");
                    }
                    if (oldDatastream.getGeometryEntity() != null &&
                            !oldDatastream.getGeometryEntity().getGeometry()
                                    .equals(newDatastream.getGeometryEntity().getGeometry())) {
                        map.add("observedArea");
                    }
                    if (oldDatastream.getSamplingTimeStart() != null &&
                            !oldDatastream.getSamplingTimeStart().equals(newDatastream.getSamplingTimeStart())) {
                        map.add(PHENOMENONTIME);
                    }
                    if (oldDatastream.getSamplingTimeEnd() != null &&
                            !oldDatastream.getSamplingTimeEnd().equals(newDatastream.getSamplingTimeEnd())) {
                        map.add(PHENOMENONTIME);
                    }
                    if (oldDatastream.getResultTimeStart() != null &&
                            !oldDatastream.getResultTimeStart().equals(newDatastream.getResultTimeStart())) {
                        map.add(RESULTTIME);
                    }
                    if (oldDatastream.getResultTimeEnd() != null &&
                            !oldDatastream.getResultTimeEnd().equals(newDatastream.getResultTimeEnd())) {
                        map.add(RESULTTIME);
                    }
                    return map;
                case "HistoricalLocationEntity":
                    HistoricalLocationEntity oldHLocation = (HistoricalLocationEntity) oldE;
                    HistoricalLocationEntity newHLocation = (HistoricalLocationEntity) newE;
                    if (oldHLocation.getTime() != null &&
                            !oldHLocation.getTime().equals(
                                    newHLocation.getTime())) {
                        map.add("time");
                    }
                    return map;
                case "DataEntity":
                    DataEntity<?> oldData = (DataEntity<?>) oldE;
                    DataEntity<?> newData = (DataEntity<?>) newE;
                    if (oldData.getSamplingTimeStart() != null &&
                            !oldData.getSamplingTimeStart().equals(newData.getSamplingTimeStart())) {
                        map.add(PHENOMENONTIME);
                    }
                    if (oldData.getSamplingTimeEnd() != null &&
                            !oldData.getSamplingTimeEnd().equals(newData.getSamplingTimeEnd())) {
                        map.add(PHENOMENONTIME);
                    }
                    if (oldData.getResultTime() != null &&
                            !oldData.getResultTime().equals(newData.getResultTime())) {
                        map.add(RESULTTIME);
                    }
                    if (oldData.getValidTimeStart() != null &&
                            !oldData.getValidTimeStart().equals(newData.getValidTimeStart())) {
                        map.add(VALIDTIME);
                    }
                    if (oldData.getValidTimeEnd() != null &&
                            !oldData.getValidTimeEnd().equals(newData.getValidTimeEnd())) {
                        map.add(VALIDTIME);
                    }
                    //TODO: implement difference map for ::getParameters and ::getResult and "resultQuality"
                    return map;
                case "FeatureEntity":
                    FeatureEntity oldFeature = (FeatureEntity) oldE;
                    FeatureEntity newFeature = (FeatureEntity) newE;
                    if (oldFeature.getName() != null
                        && !oldFeature.getName().equals(newFeature.getName())) {
                        map.add(NAME);
                    }
                    if (oldFeature.getDescription() != null
                        && !oldFeature.getDescription().equals(newFeature.getDescription())) {
                        map.add(DESCRIPTION);
                    }
                    if (oldFeature.getGeometry() != null
                        && !oldFeature.getGeometry().equals(newFeature.getGeometry())) {
                        map.add("feature");
                    }
                    // There is only a single allowed encoding type so it cannot change
                    return map;
                case "PhenomenonEntity":
                    PhenomenonEntity oldPhenom = (PhenomenonEntity) oldE;
                    PhenomenonEntity newPhenom = (PhenomenonEntity) newE;
                    if (oldPhenom.getName() != null
                        && !oldPhenom.getName().equals(newPhenom.getName())) {
                        map.add(NAME);
                    }
                    if (oldPhenom.getDescription() != null
                        && !oldPhenom.getDescription().equals(newPhenom.getDescription())) {
                        map.add(DESCRIPTION);
                    }
                    if (oldPhenom.getIdentifier() != null
                        && oldPhenom.getIdentifier().equals(newPhenom.getIdentifier())) {
                        map.add("definition");
                    }
                    return map;
                default:
                    return map;
            }
        } catch (Exception e) {
            // Catch all errors to not interrupt processing
            logger.error("Error while computing difference map: {}, {}", e.getMessage(), e.getStackTrace());
            return map;
        }
    }

}
