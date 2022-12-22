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

package org.n52.sta.data.editor;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Project;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.ProjectData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.repositories.entity.ProjectRepository;
import org.n52.sta.data.support.ProjectGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class ProjectEntityEditor extends DatabaseEntityAdapter<ProjectEntity>
        implements
        EntityEditorDelegate<Project, ProjectData> {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ValueHelper valueHelper;

    private EntityEditorDelegate<Datastream, DatastreamData> datastreamEditor;

    public ProjectEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        this.datastreamEditor = (EntityEditorDelegate<Datastream, DatastreamData>)
                getService(Datastream.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public ProjectData getOrSave(Project entity) throws EditorException {
        if (entity != null) {
            Optional<ProjectEntity> stored = getEntity(entity.getId());
            return stored.map(e -> new ProjectData(e, Optional.empty())).orElseGet(() -> save(entity));
        }
        throw new EditorException("The Project to get or save is NULL!");
    }

    @Override
    public ProjectData save(Project entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        String id = checkExistsOrGetId(entity, Project.class);
        ProjectEntity project = new ProjectEntity();
        project.setIdentifier(id);
        project.setStaIdentifier(id);
        project.setName(entity.getName());
        project.setDescription(entity.getDescription());

        project.setClassification(entity.getClassification());
        project.setTermsOfUse(entity.getTermsOfUse());
        project.setPrivacyPolicy(entity.getPrivacyPolicy());
        project.setUrl(entity.getUrl());

        if (entity.getRunTime() != null) {
            Time runTime = entity.getRunTime();
            valueHelper.setStartTime(project::setRunTimeStart, runTime);
            valueHelper.setEndTime(project::setRunTimeEnd, runTime);
        }

        Time creationTime = entity.getCreationTime();
        valueHelper.setTime(project::setCreationTime, (TimeInstant) creationTime);

        // save entity
        ProjectEntity saved = projectRepository.save(project);

        saved.setDatasets(Streams.stream(entity.getDatastreams())
                .map(datastreamEditor::getOrSave)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

//        saved.setGroups(Streams.stream(entity.getGroups())
//                .map(groupEditor::getOrSave)
//                .map(StaData::getData)
//                .collect(Collectors.toSet()));
//
//        saved.setPlatforms(Streams.stream(entity.getThings())
//                .map(thingEditor::getOrSave)
//                .map(StaData::getData)
//                .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        projectRepository.flush();

        return new ProjectData(saved, Optional.empty());
    }

    @Override
    public ProjectData update(Project oldEntity, Project updateEntity) throws EditorException {
        Objects.requireNonNull(oldEntity, "no entity to patch found");
        Objects.requireNonNull(updateEntity, "no patches found");

        ProjectEntity data = ((ProjectData) oldEntity).getData();

        setIfNotNull(updateEntity::getName, data::setName);
        setIfNotNull(updateEntity::getDescription, data::setDescription);
        if (updateEntity.getCreationTime() != null) {
            valueHelper.setTime(data::setCreationTime, (TimeInstant) updateEntity.getCreationTime());
        }
        if (updateEntity.getRunTime() != null) {
            Time runTime = updateEntity.getRunTime();
            valueHelper.setStartTime(data::setRunTimeStart, runTime);
            valueHelper.setEndTime(data::setRunTimeEnd, runTime);
        }
        setIfNotNull(updateEntity::getClassification, data::setClassification);
        setIfNotNull(updateEntity::getTermsOfUse, data::setTermsOfUse);
        setIfNotNull(updateEntity::getPrivacyPolicy, data::setPrivacyPolicy);
        setIfNotNull(updateEntity::getUrl, data::setUrl);
        errorIfNotEmptyMap(updateEntity::getProperties, "properties");

        return new ProjectData(projectRepository.save(data), Optional.empty());
    }

    @Override
    public void delete(String id) throws EditorException {
        ProjectEntity project = getEntity(id)
                                               .orElseThrow(() -> new EditorException("could not find entity with id: "
                                                       + id));
        projectRepository.delete(project);
    }

    @Override
    protected Optional<ProjectEntity> getEntity(String id) {
        ProjectGraphBuilder graphBuilder = ProjectGraphBuilder.createEmpty();
        return projectRepository.findByStaIdentifier(id, graphBuilder);
    }
}
