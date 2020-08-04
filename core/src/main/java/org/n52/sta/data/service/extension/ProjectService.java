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
import org.n52.series.db.beans.sta.AbstractDatastreamEntity;
import org.n52.series.db.beans.sta.mapped.extension.CSDatastream;
import org.n52.series.db.beans.sta.mapped.extension.Project;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.ProjectQuerySpecifications;
import org.n52.sta.data.repositories.ProjectRepository;
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
public class ProjectService
        extends AbstractSensorThingsEntityServiceImpl<ProjectRepository, Project, Project> {

    private static final ProjectQuerySpecifications pQS = new ProjectQuerySpecifications();
    private static final String NOT_IMPLEMENTED = "not implemented yet!";

    public ProjectService(ProjectRepository repository) {
        super(repository, Project.class);
    }

    @Override public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Project, EntityTypes.Projects};
    }

    @Override protected Project fetchExpandEntities(Project entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        return null;
    }

    @Override protected Specification<Project> byRelatedEntityFilter(String relatedId,
                                                                     String relatedType,
                                                                     String ownId) {
        Specification<Project> filter;
        switch (relatedType) {
        case STAEntityDefinition.CSDATASTREAMS:
            filter = pQS.withDatastreamStaIdentifier(relatedId);
            break;
        default:
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(pQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override public Project createEntity(Project entity) throws STACRUDException {
        Project project = entity;
        //if (!project.isProcessed()) {
        if (project.getStaIdentifier() != null && !project.isSetName()) {
            Optional<Project> optionalEntity =
                    getRepository().findByStaIdentifier(project.getStaIdentifier());
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException("No Project with id '"
                                                   + project.getStaIdentifier() + "' "
                                                   + "found");
            }
        } else if (project.getStaIdentifier() == null) {
            // Autogenerate Identifier
            String uuid = UUID.randomUUID().toString();
            project.setStaIdentifier(uuid);
        }
        synchronized (getLock(project.getStaIdentifier())) {
            if (getRepository().existsByStaIdentifier(project.getStaIdentifier())) {
                throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
            } else {
                for (AbstractDatastreamEntity datastream : project.getDatastreams()) {
                    getCSDatastreamService().create((CSDatastream) datastream);
                }
                getRepository().save(project);
            }
        }
        //}
        return project;
    }

    @Override protected Project updateEntity(String id, Project entity, HttpMethod method)
            throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<Project> existing = getRepository().findByStaIdentifier(id);
                if (existing.isPresent()) {
                    Project merged = merge(existing.get(), entity);
                    return getRepository().save(merged);
                }
                throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    @Override protected Project updateEntity(Project entity) throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override public Project createOrUpdate(Project entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override protected Project merge(Project existing, Project toMerge)
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

    @Override protected void delete(Project entity) throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override public void delete(String id) throws STACRUDException {
        getRepository().deleteByStaIdentifier(id);
    }
}
