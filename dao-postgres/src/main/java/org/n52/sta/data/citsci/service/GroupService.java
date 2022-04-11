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
package org.n52.sta.data.citsci.service;

import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.parameter.observationgroup.ObservationGroupParameterEntity;
import org.n52.series.db.beans.sta.plus.GroupEntity;
import org.n52.series.db.beans.sta.plus.RelationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.GroupEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.plus.GroupDTO;
import org.n52.sta.data.citsci.query.ObservationGroupQuerySpecifications;
import org.n52.sta.data.citsci.repositories.GroupParameterRepository;
import org.n52.sta.data.citsci.repositories.GroupRepository;
import org.n52.sta.data.common.CommonSTAServiceImpl;
import org.n52.sta.data.vanilla.repositories.EntityGraphRepository;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile(StaConstants.STAPLUS)
public class GroupService
    extends CitSciSTAServiceImpl<GroupRepository, GroupDTO,
    GroupEntity> {

    private static final ObservationGroupQuerySpecifications ogQS = new ObservationGroupQuerySpecifications();
    private final GroupParameterRepository parameterRepository;

    public GroupService(GroupRepository repository,
                        GroupParameterRepository parameterRepository,
                        EntityManager em) {
        super(repository, em, GroupEntity.class);
        this.parameterRepository = parameterRepository;
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
        throws STAInvalidQueryException {
        Set<EntityGraphRepository.FetchGraph> fetchGraphs = new HashSet<>(5);
        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);
        if (expandOption != null) {
            fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case GroupEntityDefinition.RELATIONS:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_RELATIONS);
                        break;
                    default:
                        throw new STAInvalidQueryException(
                            String.format(CommonSTAServiceImpl.INVALID_EXPAND_OPTION_SUPPLIED,
                                          expandProperty,
                                          StaConstants.GROUP));
                }
            }
        }
        return fetchGraphs.toArray(new EntityGraphRepository.FetchGraph[0]);
    }

    @Override protected GroupEntity fetchExpandEntitiesWithFilter(GroupEntity entity,
                                                                             ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
                case GroupEntityDefinition.RELATIONS:
                    Page<RelationEntity> obsRelations = getObservationRelationService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                               STAEntityDefinition.GROUPS,
                                                               expandItem.getQueryOptions());
                    entity.setRelations(obsRelations.get().collect(Collectors.toSet()));
                    break;
                case GroupEntityDefinition.LICENSES:
                    entity.setLicense(getLicenseService().getEntityByIdRaw(entity.getLicense().getId(),
                                                                           expandItem.getQueryOptions()));
                    break;
                default:
                    throw new STAInvalidQueryException(
                        String.format(CommonSTAServiceImpl.INVALID_EXPAND_OPTION_SUPPLIED,
                                      expandProperty,
                                      StaConstants.GROUP));
            }
        }
        return entity;
    }

    @Override protected Specification<GroupEntity> byRelatedEntityFilter(String relatedId,
                                                                                    String relatedType,
                                                                                    String ownId) {
        Specification<GroupEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.RELATIONS:
                filter = ObservationGroupQuerySpecifications.withRelationStaIdentifier(relatedId);
                break;
            case STAEntityDefinition.LICENSES:
                filter = ObservationGroupQuerySpecifications.withLicenseStaIdentifier(relatedId);
                break;
            case STAEntityDefinition.OBSERVATIONS:
                filter = ObservationGroupQuerySpecifications.withObservationStaIdentifier(relatedId);
                break;
            default:
                throw new IllegalStateException(
                    String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE,
                                  relatedType));
        }

        if (ownId != null) {
            filter = filter.and(ogQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override public GroupEntity createOrfetch(GroupEntity entity) throws STACRUDException {
        GroupEntity obsGroup = entity;
        if (!obsGroup.isProcessed()) {
            if (obsGroup.getStaIdentifier() != null && !obsGroup.isSetName()) {
                Optional<GroupEntity> optionalEntity =
                    getRepository().findByStaIdentifier(obsGroup.getStaIdentifier());
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                             StaConstants.GROUP,
                                                             obsGroup.getStaIdentifier()));
                }
            } else if (obsGroup.getStaIdentifier() == null) {
                // Autogenerate Identifier
                String uuid = UUID.randomUUID().toString();
                obsGroup.setStaIdentifier(uuid);
            }
            synchronized (getLock(obsGroup.getStaIdentifier())) {
                if (getRepository().existsByStaIdentifier(obsGroup.getStaIdentifier())) {
                    throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS,
                                               HTTPStatus.CONFLICT);
                } else {
                    obsGroup.setProcessed(true);
                    /*
                    if (obsGroup.getObservations() != null) {
                        Set<StaPlusDataEntity<?>> persisted = new HashSet<>();
                        for (StaPlusDataEntity<?> obs : obsGroup.getObservations()) {
                            persisted.add(getObservationService().createOrfetch(obs));
                        }
                        obsGroup.setObservations(persisted);
                    }
                    */

                    if (obsGroup.getRelations() != null) {
                        Set<RelationEntity> relations = new HashSet<>();
                        for (RelationEntity relation : obsGroup.getRelations()) {
                            relations.add(getObservationRelationService().createOrfetch(relation));
                        }
                        obsGroup.setRelations(relations);
                    }

                    if (obsGroup.getLicense() != null) {
                        obsGroup.setLicense(getLicenseService().createOrfetch(obsGroup.getLicense()));
                    }

                    obsGroup = getRepository().save(obsGroup);

                    if (obsGroup.getParameters() != null) {
                        GroupEntity finalObsGroup = obsGroup;
                        parameterRepository.saveAll(obsGroup.getParameters()
                                                        .stream()
                                                        .filter(o -> o instanceof ObservationGroupParameterEntity)
                                                        .map(t -> {
                                                            ((ObservationGroupParameterEntity) t).setObsGroup(
                                                                finalObsGroup);
                                                            return (ObservationGroupParameterEntity) t;
                                                        })
                                                        .collect(Collectors.toSet()));
                        obsGroup.setParameters(obsGroup.getParameters());
                    }
                }
            }
        }
        return obsGroup;
    }

    @Override protected GroupEntity updateEntity(String id, GroupEntity entity, HttpMethod method)
        throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<GroupEntity> existing = getRepository().findByStaIdentifier(id);
                if (existing.isPresent()) {
                    GroupEntity merged = merge(existing.get(), entity);
                    return getRepository().save(merged);
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND,
                                           HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED,
                                       HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY,
                                   HTTPStatus.BAD_REQUEST);
    }

    @Override public GroupEntity createOrUpdate(GroupEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override protected GroupEntity merge(GroupEntity existing, GroupEntity toMerge)
        throws STACRUDException {
        if (toMerge.getStaIdentifier() != null) {
            existing.setStaIdentifier(toMerge.getStaIdentifier());
        }
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        mergeObservationRelations(existing, toMerge);

        // properties
        if (toMerge.getParameters() != null) {
            synchronized (getLock(String.valueOf(existing.getParameters().hashCode()))) {
                parameterRepository.saveAll(toMerge.getParameters()
                                                .stream()
                                                .filter(o -> o instanceof ObservationGroupParameterEntity)
                                                .map(t -> {
                                                    ((ObservationGroupParameterEntity) t).setObsGroup(existing);
                                                    return (ObservationGroupParameterEntity) t;
                                                })
                                                .collect(Collectors.toSet()));
                existing.getParameters().stream()
                    .filter(o -> o instanceof ObservationGroupParameterEntity)
                    .map(o -> (ObservationGroupParameterEntity) o)
                    .forEach(parameterRepository::delete);
                existing.setParameters(toMerge.getParameters());
            }
        }

        return existing;
    }

    @Override public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                // delete related relations
                // observations are not deleted!
                //getObservationRelationService().de.deleteAllByGroupStaIdentifier(id);
                //TODO: fix!
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND,
                                           HTTPStatus.NOT_FOUND);
            }
        }
    }

    private void mergeObservationRelations(GroupEntity existing,
                                           GroupEntity toMerge) {

        /*
        if (existing.getEntities() == null) {
            existing.setEntities(toMerge.getEntities());
        } else {
            if (toMerge.getEntities() != null) {
                existing.getEntities().addAll(toMerge.getEntities());
            }
        }
         */
    }
}
