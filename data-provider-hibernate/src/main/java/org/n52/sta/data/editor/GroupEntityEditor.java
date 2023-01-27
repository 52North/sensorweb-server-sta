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
import org.n52.series.db.beans.sta.GroupEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.License;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Relation;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.data.entity.GroupData;
import org.n52.sta.data.entity.LicenseData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.entity.PartyData;
import org.n52.sta.data.entity.RelationData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.repositories.entity.GroupRepository;
import org.n52.sta.data.support.GroupGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class GroupEntityEditor extends DatabaseEntityAdapter<GroupEntity>
        implements
        EntityEditorDelegate<Group, GroupData> {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ValueHelper valueHelper;

    private EntityEditorDelegate<Observation, ObservationData> observationEditor;
    private EntityEditorDelegate<License, LicenseData> licenseEditor;
    private EntityEditorDelegate<Party, PartyData> partyEditor;
    private EntityEditorDelegate<Relation, RelationData> relationEditor;


    public GroupEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        this.observationEditor = (EntityEditorDelegate<Observation, ObservationData>)
                getService(Observation.class).unwrapEditor();
        this.licenseEditor = (EntityEditorDelegate<License, LicenseData>)
                getService(License.class).unwrapEditor();
        this.partyEditor = (EntityEditorDelegate<Party, PartyData>)
                getService(Party.class).unwrapEditor();
        this.relationEditor = (EntityEditorDelegate<Relation, RelationData>)
                getService(Relation.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public GroupData get(Group entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must be present!");
        Optional<GroupEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new GroupData(e, Optional.empty()))
            .orElseThrow(() -> new EditorException(String.format("entity with id %s not found", entity.getId())));
    }

    @Override
    public GroupData save(Group entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        String id = checkExistsOrGetId(entity, Group.class);
        GroupEntity group = new GroupEntity();
        group.setIdentifier(id);
        group.setStaIdentifier(id);
        group.setName(entity.getName());
        group.setDescription(entity.getDescription());

        group.setPurpose(entity.getPurpose());
        if (entity.getRunTime() != null) {
            Time runTime = entity.getRunTime();
            valueHelper.setStartTime(group::setRunTimeStart, runTime);
            valueHelper.setEndTime(group::setRunTimeEnd, runTime);
        }

        Time creationTime = entity.getCreationTime();
        valueHelper.setTime(group::setCreationTime, (TimeInstant) creationTime);

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(entry -> convertParameter(group, entry))
               .forEach(group::addParameter);

        // save entity
        GroupEntity saved = groupRepository.save(group);

        saved.setObservations(Streams.stream(entity.getObservations())
                .map(observationEditor::get)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        if (entity.getLicense() != null) {
            LicenseData licence = licenseEditor.get(entity.getLicense());
            saved.setLicense(licence.getData());
        }

        if (entity.getParty() != null) {
            PartyData party = partyEditor.get(entity.getParty());
            saved.setParty(party.getData());
        }

        saved.setRelations(Streams.stream(entity.getRelations())
                .map(relationEditor::get)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        groupRepository.flush();

        return new GroupData(saved, Optional.empty());
    }

    @Override
    public GroupData update(Group oldEntity, Group updateEntity) throws EditorException {
        Objects.requireNonNull(oldEntity, "no entity to patch found");
        Objects.requireNonNull(updateEntity, "no patches found");

        GroupEntity data = ((GroupData) oldEntity).getData();

        setIfNotNull(updateEntity::getName, data::setName);
        setIfNotNull(updateEntity::getDescription, data::setDescription);
        setIfNotNull(updateEntity::getPurpose, data::setPurpose);

        if (updateEntity.getCreationTime() != null) {
            valueHelper.setTime(data::setCreationTime, (TimeInstant) updateEntity.getCreationTime());
        }
        if (updateEntity.getRunTime() != null) {
            Time runTime = updateEntity.getRunTime();
            valueHelper.setStartTime(data::setRunTimeStart, runTime);
            valueHelper.setEndTime(data::setRunTimeEnd, runTime);
        }

        errorIfNotEmptyMap(updateEntity::getProperties, "properties");

        return new GroupData(groupRepository.save(data), Optional.empty());
    }

    @Override
    public void delete(String id) throws EditorException {
        GroupEntity group = getEntity(id)
                                               .orElseThrow(() -> new EditorException("could not find entity with id: "
                                                       + id));
        groupRepository.delete(group);
    }

    @Override
    protected Optional<GroupEntity> getEntity(String id) {
        GroupGraphBuilder graphBuilder = GroupGraphBuilder.createEmpty();
        return groupRepository.findByStaIdentifier(id, graphBuilder);
    }
}
