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
import org.n52.series.db.beans.sta.mapped.extension.License;
import org.n52.series.db.beans.sta.mapped.extension.ObservationGroup;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.ObservationGroupQuerySpecifications;
import org.n52.sta.data.repositories.ObservationGroupRepository;
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
public class ObservationGroupService
        extends AbstractSensorThingsEntityServiceImpl<ObservationGroupRepository, ObservationGroup, ObservationGroup> {

    private static final ObservationGroupQuerySpecifications ogQS = new ObservationGroupQuerySpecifications();
    private static final String NOT_IMPLEMENTED = "not implemented yet!";

    public ObservationGroupService(ObservationGroupRepository repository) {
        super(repository, ObservationGroup.class);
    }

    @Override public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.ObservationGroup, EntityTypes.ObservationGroups};
    }

    @Override protected ObservationGroup fetchExpandEntities(ObservationGroup entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        return null;
    }

    @Override protected Specification<ObservationGroup> byRelatedEntityFilter(String relatedId,
                                                                              String relatedType,
                                                                              String ownId) {
        Specification<ObservationGroup> filter;
        switch (relatedType) {
        case STAEntityDefinition.OBSERVATION_RELATIONS:
            filter = ogQS.withRelationStaIdentifier(relatedId);
            break;
        default:
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(ogQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override public ObservationGroup createEntity(ObservationGroup entity) throws STACRUDException {
        ObservationGroup obsGroup = entity;
        if (!obsGroup.isProcessed()) {
            if (obsGroup.getStaIdentifier() != null && !obsGroup.isSetName()) {
                Optional<ObservationGroup> optionalEntity =
                        getRepository().findByStaIdentifier(obsGroup.getStaIdentifier());
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException("No ObservationGroup with id '"
                                                       + obsGroup.getStaIdentifier() + "' "
                                                       + "found");
                }
            } else if (obsGroup.getStaIdentifier() == null) {
                // Autogenerate Identifier
                String uuid = UUID.randomUUID().toString();
                obsGroup.setStaIdentifier(uuid);
            }
            synchronized (getLock(obsGroup.getStaIdentifier())) {
                if (getRepository().existsByStaIdentifier(obsGroup.getStaIdentifier())) {
                    throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
                } else {
                    obsGroup.setProcessed(true);
                    getRepository().save(obsGroup);
                }
            }
        }
        return obsGroup;
    }

    @Override protected ObservationGroup updateEntity(String id, ObservationGroup entity, HttpMethod method)
            throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<ObservationGroup> existing = getRepository().findByStaIdentifier(id);
                if (existing.isPresent()) {
                    ObservationGroup merged = merge(existing.get(), entity);
                    return getRepository().save(merged);
                }
                throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    @Override protected ObservationGroup updateEntity(ObservationGroup entity) throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override public ObservationGroup createOrUpdate(ObservationGroup entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override protected ObservationGroup merge(ObservationGroup existing, ObservationGroup toMerge)
            throws STACRUDException {
        
        if (toMerge.getStaIdentifier() != null) {
            existing.setStaIdentifier(toMerge.getStaIdentifier());
        }
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        mergeObservationRelations(existing, toMerge);
        return existing;
    }

    @Override protected void delete(ObservationGroup entity) throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override public void delete(String id) throws STACRUDException {
        getRepository().deleteByStaIdentifier(id);
    }

    private void mergeObservationRelations(ObservationGroup existing,
            ObservationGroup toMerge) {
        
        if (existing.getEntities() == null) {
            existing.setEntities(toMerge.getEntities());
        } else {
            if (toMerge.getEntities() != null) {
                existing.getEntities().addAll(toMerge.getEntities());
            }
        }
    }
}
