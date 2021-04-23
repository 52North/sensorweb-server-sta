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
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.sta.ObservationGroupEntity;
import org.n52.series.db.beans.sta.ObservationRelationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.ObservationRelationQuerySpecifications;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.ObservationRelationRepository;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile(StaConstants.CITSCIEXTENSION)
public class ObservationRelationService
    extends AbstractSensorThingsEntityServiceImpl<ObservationRelationRepository, ObservationRelationEntity> {

    private static final ObservationRelationQuerySpecifications orQS = new ObservationRelationQuerySpecifications();

    public ObservationRelationService(ObservationRelationRepository repository, EntityManager em) {
        super(repository, em, ObservationRelationEntity.class);
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
        throws STAInvalidQueryException {
        Set<EntityGraphRepository.FetchGraph> fetchGraphs = new HashSet<>();
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.OBSERVATION_GROUP:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_OBSERVATION_GROUP);
                        break;
                    case STAEntityDefinition.OBSERVATION:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_OBSERVATION);

                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                         expandProperty,
                                                                         StaConstants.OBSERVATION_RELATION));
                }
            }
        }
        return fetchGraphs.toArray(new EntityGraphRepository.FetchGraph[0]);
    }

    @Override protected ObservationRelationEntity fetchExpandEntitiesWithFilter(ObservationRelationEntity entity,
                                                                                ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            
            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
                case STAEntityDefinition.OBSERVATION_GROUP:
                    entity.setGroup(getObservationGroupService()
                                        .getEntityByIdRaw(entity.getGroup().getId(), expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.OBSERVATION:
                    entity.setObservation(getObservationService()
                                              .getEntityByIdRaw(entity.getObservation().getId(),
                                                                expandItem.getQueryOptions()));
                    break;
                default:
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                     expandProperty,
                                                                     StaConstants.OBSERVATION_RELATION));
            }
        }
        return entity;
    }

    @Override protected Specification<ObservationRelationEntity> byRelatedEntityFilter(String relatedId,
                                                                                       String relatedType,
                                                                                       String ownId) {
        Specification<ObservationRelationEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.OBSERVATION_GROUPS:
                filter = orQS.withGroupStaIdentifier(relatedId);
                break;
            case STAEntityDefinition.OBSERVATIONS:
                filter = orQS.withObservationStaIdentifier(relatedId);
                break;
            default:
                throw new IllegalStateException(String.format(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE,
                                                                            relatedType)));
        }

        if (ownId != null) {
            filter = filter.and(orQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override public ObservationRelationEntity createOrfetch(ObservationRelationEntity entity) throws STACRUDException {
        ObservationRelationEntity obsRel = entity;
        if (obsRel.getStaIdentifier() != null && obsRel.getType() == null) {
            Optional<ObservationRelationEntity> optionalEntity =
                getRepository().findByStaIdentifier(obsRel.getStaIdentifier());
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                         StaConstants.OBSERVATION_RELATION,
                                                         obsRel.getStaIdentifier())
                );
            }
        } else if (obsRel.getStaIdentifier() == null) {
            // Autogenerate Identifier
            String uuid = UUID.randomUUID().toString();
            obsRel.setStaIdentifier(uuid);
        }
        synchronized (getLock(obsRel.getStaIdentifier())) {
            if (getRepository().existsByStaIdentifier(obsRel.getStaIdentifier())) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            } else {
                ObservationGroupEntity group = getObservationGroupService().createOrfetch(obsRel.getGroup());
                obsRel.setGroup(group);

                DataEntity<?> obs = getObservationService().createOrfetch(obsRel.getObservation());
                obsRel.setObservation(obs);
                return getRepository().save(obsRel);
            }
        }
    }

    @Override
    protected ObservationRelationEntity updateEntity(String id, ObservationRelationEntity entity, HttpMethod method)
        throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<ObservationRelationEntity> existing = getRepository().findByStaIdentifier(id);
                if (existing.isPresent()) {
                    ObservationRelationEntity merged = merge(existing.get(), entity);
                    return getRepository().save(merged);
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        } else {
            throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
        }
    }

    @Override public ObservationRelationEntity createOrUpdate(ObservationRelationEntity entity)
        throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override
    protected ObservationRelationEntity merge(ObservationRelationEntity existing, ObservationRelationEntity toMerge) {
        if (toMerge.getStaIdentifier() != null) {
            existing.setStaIdentifier(toMerge.getStaIdentifier());
        }
        if (toMerge.getType() != null) {
            existing.setType(toMerge.getType());
        }
        return existing;
    }

    @Override public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }
}
