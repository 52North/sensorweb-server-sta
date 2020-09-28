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
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.ProjectEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.ProjectQuerySpecifications;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.ProjectRepository;
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
public class ProjectService
    extends AbstractSensorThingsEntityServiceImpl<ProjectRepository, ProjectEntity, ProjectEntity> {

    private static final ProjectQuerySpecifications pQS = new ProjectQuerySpecifications();

    public ProjectService(ProjectRepository repository) {
        super(repository, ProjectEntity.class);
    }

    @Override public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Project, EntityTypes.Projects};
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
        throws STAInvalidQueryException {
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                String expandProperty = expandItem.getPath();
                if (ProjectEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                    return new EntityGraphRepository.FetchGraph[] {
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS,
                    };
                } else {
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                     expandProperty,
                                                                     StaConstants.PROJECT));
                }
            }
        }
        return new EntityGraphRepository.FetchGraph[0];
    }

    @Override protected ProjectEntity fetchExpandEntitiesWithFilter(ProjectEntity entity, ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (ProjectEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                Page<AbstractDatasetEntity> datastreams = getDatastreamService()
                    .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                           STAEntityDefinition.PROJECTS,
                                                           expandItem.getQueryOptions());
                entity.setDatasets(datastreams.get().collect(Collectors.toSet()));
                return entity;
            } else {
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                 expandProperty,
                                                                 StaConstants.PROJECT));
            }
        }
        return entity;
    }

    @Override protected Specification<ProjectEntity> byRelatedEntityFilter(String relatedId,
                                                                           String relatedType,
                                                                           String ownId) {
        Specification<ProjectEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS:
                filter = pQS.withDatastreamStaIdentifier(relatedId);
                break;
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(pQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override public ProjectEntity createOrfetch(ProjectEntity entity) throws STACRUDException {
        ProjectEntity project = entity;
        //if (!project.isProcessed()) {
        if (project.getStaIdentifier() != null && !project.isSetName()) {
            Optional<ProjectEntity> optionalEntity =
                getRepository().findByStaIdentifier(project.getStaIdentifier());
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                         StaConstants.PROJECT,
                                                         project.getStaIdentifier()));
            }
        } else if (project.getStaIdentifier() == null) {
            // Autogenerate Identifier
            String uuid = UUID.randomUUID().toString();
            project.setStaIdentifier(uuid);
        }
        synchronized (getLock(project.getStaIdentifier())) {
            if (getRepository().existsByStaIdentifier(project.getStaIdentifier())) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            } else {
                if (project.hasDatastreams()) {
                    for (AbstractDatasetEntity datastream : project.getDatasets()) {
                        getDatastreamService().create(datastream);
                    }
                }
                getRepository().save(project);
            }
        }
        //}
        return project;
    }

    @Override protected ProjectEntity updateEntity(String id, ProjectEntity entity, HttpMethod method)
        throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<ProjectEntity> existing = getRepository().findByStaIdentifier(id);
                if (existing.isPresent()) {
                    ProjectEntity merged = merge(existing.get(), entity);
                    return getRepository().save(merged);
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override public ProjectEntity createOrUpdate(ProjectEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override protected ProjectEntity merge(ProjectEntity existing, ProjectEntity toMerge)
        throws STACRUDException {

        if (toMerge.getStaIdentifier() != null) {
            existing.setStaIdentifier(toMerge.getStaIdentifier());
        }
        if (toMerge.getName() != null) {
            existing.setName(toMerge.getName());
        }
        if (toMerge.getRuntimeStart() != null) {
            existing.setRuntimeStart(toMerge.getRuntimeStart());
        }
        if (toMerge.getRuntimeEnd() != null) {
            existing.setRuntimeEnd(toMerge.getRuntimeEnd());
        }
        if (toMerge.getDescription() != null) {
            existing.setDescription(toMerge.getDescription());
        }
        if (toMerge.getUrl() != null) {
            existing.setUrl(toMerge.getUrl());
        }

        mergeDatastreams(existing, toMerge);
        return existing;
    }

    @Override public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                ProjectEntity project = getRepository().findByStaIdentifier(id).get();
                // Delete related Datastreams
                for (AbstractDatasetEntity ds : project.getDatasets()) {
                    getDatastreamService().delete(ds.getStaIdentifier());
                }
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }
}
