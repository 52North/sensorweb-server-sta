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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.sta.RelationEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.Relation;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.data.entity.GroupData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.entity.RelationData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.repositories.entity.RelationRepository;
import org.n52.sta.data.support.RelationGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class RelationEntityEditor extends DatabaseEntityAdapter<RelationEntity>
        implements
        EntityEditorDelegate<Relation, RelationData> {

    @Autowired
    private RelationRepository relationRepository;

    @Autowired
    private ValueHelper valueHelper;

    @Autowired
    private EntityPropertyMapping propertyMapping;

    private EntityEditorDelegate<Observation, ObservationData> observationEditor;
    private EntityEditorDelegate<Group, GroupData> groupEditor;


    public RelationEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        this.observationEditor = (EntityEditorDelegate<Observation, ObservationData>)
                getService(Observation.class).unwrapEditor();
        this.groupEditor = (EntityEditorDelegate<Group, GroupData>)
                getService(Group.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public RelationData getOrSave(Relation entity) throws EditorException {
        if (entity != null) {
            Optional<RelationEntity> stored = getEntity(entity.getId());
            return stored.map(e -> new RelationData(e, Optional.of(propertyMapping))).orElseGet(() -> save(entity));
        }
        throw new EditorException("The Relation to get or save is NULL!");
    }

    @Override
    public RelationData save(Relation entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        String id = checkExistsOrGetId(entity, Relation.class);
        RelationEntity relation = new RelationEntity();
        relation.setIdentifier(id);
        relation.setStaIdentifier(id);
        relation.setDescription(entity.getDescription());

        relation.setRole(entity.getRole());


        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(entry -> convertParameter(relation, entry))
               .forEach(relation::addParameter);

        relation.setSubject(observationEditor.getOrSave(entity.getSubject()).getData());

        if (entity.getObject().isObjectPresent()) {
            relation.setObject(observationEditor.getOrSave(entity.getObject().getObject()).getData());
        } else {
            relation.setExternalObject(entity.getObject().getExternalObject());
        }

        // save entity
        RelationEntity saved = relationRepository.save(relation);

        saved.setGroups(Streams.stream(entity.getGroups())
                .map(groupEditor::getOrSave)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        relationRepository.flush();

        return new RelationData(saved, Optional.of(propertyMapping));
    }

    @Override
    public RelationData update(Relation oldEntity, Relation updateEntity) throws EditorException {
        Objects.requireNonNull(oldEntity, "no entity to patch found");
        Objects.requireNonNull(updateEntity, "no patches found");

        RelationEntity data = ((RelationData) oldEntity).getData();

        setIfNotNull(updateEntity::getDescription, data::setDescription);

        errorIfNotNull(updateEntity::getProperties, "properties");

        return new RelationData(relationRepository.save(data), Optional.of(propertyMapping));
    }

    @Override
    public void delete(String id) throws EditorException {
        RelationEntity relation = getEntity(id)
                                               .orElseThrow(() -> new EditorException("could not find entity with id: "
                                                       + id));
        relationRepository.delete(relation);
    }

    @Override
    protected Optional<RelationEntity> getEntity(String id) {
        RelationGraphBuilder graphBuilder = RelationGraphBuilder.createEmpty();
        return relationRepository.findByStaIdentifier(id, graphBuilder);
    }
}
