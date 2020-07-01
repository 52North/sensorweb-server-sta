/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.service.extension;

import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.sta.mapped.extension.ObservationGroup;
import org.n52.series.db.beans.sta.mapped.extension.ObservationRelation;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.ObservationRelationQuerySpecifications;
import org.n52.sta.data.repositories.ObservationRelationRepository;
import org.n52.sta.data.service.AbstractSensorThingsEntityServiceImpl;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile("citSciExtension")
public class ObservationRelationService
        extends AbstractSensorThingsEntityServiceImpl<ObservationRelationRepository, ObservationRelation,
        ObservationRelation> {

    private static final ObservationRelationQuerySpecifications orQS = new ObservationRelationQuerySpecifications();
    private static final String NOT_IMPLEMENTED = "not implemented yet";

    public ObservationRelationService(ObservationRelationRepository repository) {
        super(repository, ObservationRelation.class);
    }

    @Override public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.ObservationRelation, EntityTypes.ObservationRelations};
    }

    @Override protected ObservationRelation fetchExpandEntities(ObservationRelation entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override protected Specification<ObservationRelation> byRelatedEntityFilter(String relatedId,
                                                                                 String relatedType,
                                                                                 String ownId) {
        Specification<ObservationRelation> filter;
        switch (relatedType) {
        case STAEntityDefinition.OBSERVATION_GROUPS:
            filter = orQS.withGroupStaIdentifier(relatedId);
            break;
        case STAEntityDefinition.CSOBSERVATIONS:
            filter = orQS.withCSObservationStaIdentifier(relatedId);
            break;
        default:
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(orQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override protected ObservationRelation createEntity(ObservationRelation entity) throws STACRUDException {
        ObservationRelation obsRel = entity;
        if (obsRel.getIdentifier() != null && obsRel.getType() == null) {
            Optional<ObservationRelation> optionalEntity =
                    getRepository().findByStaIdentifier(obsRel.getStaIdentifier());
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException("No ObservationRelation with id '"
                                                   + obsRel.getStaIdentifier() + "' "
                                                   + "found");
            }
        } else if (obsRel.getStaIdentifier() == null) {
            // Autogenerate Identifier
            String uuid = UUID.randomUUID().toString();
            obsRel.setIdentifier(uuid);
            obsRel.setStaIdentifier(uuid);
        }
        synchronized (getLock(obsRel.getStaIdentifier())) {
            if (getRepository().existsByStaIdentifier(obsRel.getStaIdentifier())) {
                throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
            } else {

                ObservationGroup orUpdate = getObservationGroupService().createOrUpdate(obsRel.getGroup());
                obsRel.setGroup(orUpdate);
                return getRepository().save(obsRel);
            }
        }
    }

    @Override protected ObservationRelation updateEntity(String id, ObservationRelation entity, HttpMethod method)
            throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override protected ObservationRelation updateEntity(ObservationRelation entity) throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override public ObservationRelation createOrUpdate(ObservationRelation entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override protected ObservationRelation merge(ObservationRelation existing, ObservationRelation toMerge)
            throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override protected void delete(ObservationRelation entity) throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override public void delete(String id) throws STACRUDException {
        getRepository().deleteByIdentifier(id);
    }
}
