/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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
package org.n52.sta.plus.persistence.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.sta.plus.GroupEntity;
import org.n52.series.db.beans.sta.plus.RelationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.old.repositories.EntityGraphRepository;
import org.n52.sta.plus.old.entity.RelationDTO;
import org.n52.sta.plus.persistence.query.ObservationRelationQuerySpecifications;
import org.n52.sta.plus.persistence.repositories.ObservationRelationRepository;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({ "springApplicationContext" })
@Transactional
@Profile(StaConstants.STAPLUS)
public class RelationService
        extends CitSciSTAServiceImpl<ObservationRelationRepository, RelationDTO, RelationEntity> {

    private static final ObservationRelationQuerySpecifications orQS = new ObservationRelationQuerySpecifications();

    public RelationService(ObservationRelationRepository repository, EntityManager em) {
        super(repository, em, RelationEntity.class);
    }

    @Override
    protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
            throws STAInvalidQueryException {
        Set<EntityGraphRepository.FetchGraph> fetchGraphs = new HashSet<>();
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.GROUP:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_OBSERVATION_GROUP);
                        break;
                    case STAEntityDefinition.OBSERVATION:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_OBSERVATION);

                        break;
                    default:
                        throw new STAInvalidQueryException(
                                String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                        expandProperty,
                                        StaConstants.RELATION));
                }
            }
        }
        return fetchGraphs.toArray(new EntityGraphRepository.FetchGraph[0]);
    }

    @Override
    protected RelationEntity fetchExpandEntitiesWithFilter(RelationEntity entity,
            ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }

            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
                case STAEntityDefinition.GROUPS:
                    Page<GroupEntity> groups = getObservationGroupService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                    STAEntityDefinition.RELATIONS,
                                    expandItem.getQueryOptions());
                    entity.setGroups(groups.get().collect(Collectors.toSet()));
                    break;
                /*
                 * case STAEntityDefinition.SUBJECTS:
                 * entity.setSubject(getObservationService()
                 * .getEntityByIdRaw(entity.getSubject().getId(),
                 * expandItem.getQueryOptions()));
                 * break;
                 * case STAEntityDefinition.OBJECTS:
                 * entity.setObject(getObservationService()
                 * .getEntityByIdRaw(entity.getObject().getId(),
                 * expandItem.getQueryOptions()));
                 * break;
                 */
                default:
                    throw new STAInvalidQueryException(
                            String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                    expandProperty,
                                    StaConstants.RELATION));
            }
        }
        return entity;
    }

    @Override
    protected Specification<RelationEntity> byRelatedEntityFilter(String relatedId,
            String relatedType,
            String ownId) {
        Specification<RelationEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.GROUPS:
                filter = orQS.withGroupStaIdentifier(relatedId);
                break;
            case STAEntityDefinition.SUBJECTS:
                filter = orQS.withSubjectStaIdentifier(relatedId);
                break;
            case STAEntityDefinition.OBJECTS:
                filter = orQS.withObjectStaIdentifier(relatedId);
                break;
            default:
                throw new IllegalStateException(
                        String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE,
                                relatedType));
        }

        if (ownId != null) {
            filter = filter.and(orQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public RelationEntity createOrfetch(RelationEntity entity) throws STACRUDException {
        RelationEntity obsRel = entity;
        if (obsRel.getStaIdentifier() != null && obsRel.getRole() == null) {
            Optional<RelationEntity> optionalEntity = getRepository().findByStaIdentifier(obsRel.getStaIdentifier());
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                        StaConstants.RELATION,
                        obsRel.getStaIdentifier()));
            }
        } else if (obsRel.getStaIdentifier() == null) {
            // Autogenerate Identifier
            String uuid = UUID.randomUUID().toString();
            obsRel.setStaIdentifier(uuid);
        }
        synchronized (getLock(obsRel.getStaIdentifier())) {
            if (getRepository().existsByStaIdentifier(obsRel.getStaIdentifier())) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS,
                        HTTPStatus.CONFLICT);
            } else {
                /*
                 * GroupEntity group =
                 * getObservationGroupService().createOrfetch(obsRel.getGroup());
                 * obsRel.setGroup(group);
                 *
                 * DataEntity<?> obj =
                 * getObservationService().createOrfetch(obsRel.getObject());
                 * obsRel.setObject(obj);
                 *
                 * DataEntity<?> sub =
                 * getObservationService().createOrfetch(obsRel.getSubject());
                 * obsRel.setSubject(sub);
                 */
                return getRepository().save(obsRel);
            }
        }
    }

    @Override
    protected RelationEntity updateEntity(String id, RelationEntity entity, String method)
            throws STACRUDException {
        if (PATCH.equals(method)) {
            return updateEntity(id, entity);
        } else if (PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED,
                    HTTPStatus.NOT_IMPLEMENTED);
        } else {
            throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY,
                    HTTPStatus.BAD_REQUEST);
        }
    }

    private RelationEntity updateEntity(String id, RelationEntity entity) throws STACRUDException {
        synchronized (getLock(id)) {
            Optional<RelationEntity> existing = getRepository().findByStaIdentifier(id);
            if (existing.isPresent()) {
                RelationEntity merged = merge(existing.get(), entity);
                return getRepository().save(merged);
            }
            throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND,
                    HTTPStatus.NOT_FOUND);
        }
    }

    @Override
    public RelationEntity createOrUpdate(RelationEntity entity)
            throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity);
        }
        return createOrfetch(entity);
    }

    @Override
    public String checkPropertyName(String property) {
        return property;
    }

    @Override
    protected RelationEntity merge(RelationEntity existing, RelationEntity toMerge) {
        if (toMerge.getStaIdentifier() != null) {
            existing.setStaIdentifier(toMerge.getStaIdentifier());
        }
        if (toMerge.getRole() != null) {
            existing.setRole(toMerge.getRole());
        }
        return existing;
    }

    @Override
    public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND,
                        HTTPStatus.NOT_FOUND);
            }
        }
    }
}
