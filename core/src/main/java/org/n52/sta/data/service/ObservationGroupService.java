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

package org.n52.sta.data.service;

import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.sta.ObservationGroupEntity;
import org.n52.series.db.beans.sta.ObservationRelationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.ObservationGroupEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.ObservationGroupQuerySpecifications;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.ObservationGroupRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile(StaConstants.CITSCIEXTENSION)
public class ObservationGroupService
        extends AbstractSensorThingsEntityServiceImpl<ObservationGroupRepository, ObservationGroupEntity,
        ObservationGroupEntity> {

    private static final ObservationGroupQuerySpecifications ogQS = new ObservationGroupQuerySpecifications();

    public ObservationGroupService(ObservationGroupRepository repository) {
        super(repository, ObservationGroupEntity.class);
    }

    @Override public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.ObservationGroup, EntityTypes.ObservationGroups};
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
            throws STAInvalidQueryException {
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                String expandProperty = expandItem.getPath();
                if (ObservationGroupEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                    return new EntityGraphRepository.FetchGraph[] {
                            EntityGraphRepository.FetchGraph.FETCHGRAPH_OBSERVATION_RELATIONS
                    };
                } else {
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                     expandProperty,
                                                                     StaConstants.OBSERVATION_GROUP));
                }
            }
        }
        return new EntityGraphRepository.FetchGraph[0];
    }

    @Override protected ObservationGroupEntity fetchExpandEntitiesWithFilter(ObservationGroupEntity entity,
                                                                             ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (ObservationGroupEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                Page<ObservationRelationEntity> obsRelations = getObservationRelationService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                               STAEntityDefinition.OBSERVATION_GROUPS,
                                                               expandItem.getQueryOptions());
                entity.setEntities(obsRelations.get().collect(Collectors.toSet()));
                break;
            } else {
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                 expandProperty,
                                                                 StaConstants.OBSERVATION_GROUP));
            }
        }
        return entity;
    }

    @Override protected Specification<ObservationGroupEntity> byRelatedEntityFilter(String relatedId,
                                                                                    String relatedType,
                                                                                    String ownId) {
        Specification<ObservationGroupEntity> filter;
        switch (relatedType) {
        case STAEntityDefinition.OBSERVATION_RELATIONS:
            filter = ogQS.withRelationStaIdentifier(relatedId);
            break;
        default:
            throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(ogQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override public ObservationGroupEntity createOrfetch(ObservationGroupEntity entity) throws STACRUDException {
        ObservationGroupEntity obsGroup = entity;
        if (!obsGroup.isProcessed()) {
            if (obsGroup.getStaIdentifier() != null && !obsGroup.isSetName()) {
                Optional<ObservationGroupEntity> optionalEntity =
                        getRepository().findByStaIdentifier(obsGroup.getStaIdentifier());
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                             StaConstants.OBSERVATION_GROUP,
                                                             obsGroup.getStaIdentifier()));
                }
            } else if (obsGroup.getStaIdentifier() == null) {
                // Autogenerate Identifier
                String uuid = UUID.randomUUID().toString();
                obsGroup.setStaIdentifier(uuid);
            }
            synchronized (getLock(obsGroup.getStaIdentifier())) {
                if (getRepository().existsByStaIdentifier(obsGroup.getStaIdentifier())) {
                    throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
                } else {
                    obsGroup.setProcessed(true);
                    getRepository().save(obsGroup);
                }
            }
        }
        return obsGroup;
    }

    @Override protected ObservationGroupEntity updateEntity(String id, ObservationGroupEntity entity, HttpMethod method)
            throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<ObservationGroupEntity> existing = getRepository().findByStaIdentifier(id);
                if (existing.isPresent()) {
                    ObservationGroupEntity merged = merge(existing.get(), entity);
                    return getRepository().save(merged);
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override public ObservationGroupEntity createOrUpdate(ObservationGroupEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override protected ObservationGroupEntity merge(ObservationGroupEntity existing, ObservationGroupEntity toMerge) {
        if (toMerge.getStaIdentifier() != null) {
            existing.setStaIdentifier(toMerge.getStaIdentifier());
        }
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        mergeObservationRelations(existing, toMerge);
        return existing;
    }

    @Override protected void delete(ObservationGroupEntity entity) throws STACRUDException {
        getRepository().save(entity);
    }

    @Override public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                // delete related relations
                // observations are not deleted!
                getObservationRelationService().getRepository().deleteAllByGroupStaIdentifier(id);
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    private void mergeObservationRelations(ObservationGroupEntity existing,
                                           ObservationGroupEntity toMerge) {

        if (existing.getEntities() == null) {
            existing.setEntities(toMerge.getEntities());
        } else {
            if (toMerge.getEntities() != null) {
                existing.getEntities().addAll(toMerge.getEntities());
            }
        }
    }
}
