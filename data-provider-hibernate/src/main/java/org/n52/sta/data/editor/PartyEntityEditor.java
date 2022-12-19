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
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.series.db.beans.sta.RolePartyCode;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.GroupData;
import org.n52.sta.data.entity.PartyData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.PartyRepository;
import org.n52.sta.data.support.PartyGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class PartyEntityEditor extends DatabaseEntityAdapter<PartyEntity>
        implements
        EntityEditorDelegate<Party, PartyData> {

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ValueHelper valueHelper;

    private EntityEditorDelegate<Datastream, DatastreamData> datastreamEditor;
    private EntityEditorDelegate<Group, GroupData> groupEditor;
    private EntityEditorDelegate<Thing, ThingData> thingEditor;


    public PartyEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        this.datastreamEditor = (EntityEditorDelegate<Datastream, DatastreamData>)
                getService(Datastream.class).unwrapEditor();
        this.groupEditor = (EntityEditorDelegate<Group, GroupData>)
                getService(Group.class).unwrapEditor();
        this.thingEditor = (EntityEditorDelegate<Thing, ThingData>)
                getService(Thing.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public PartyData getOrSave(Party entity) throws EditorException {
        if (entity != null) {
            Optional<PartyEntity> stored = getEntity(entity.getId());
            return stored.map(e -> new PartyData(e, Optional.empty())).orElseGet(() -> save(entity));
        }
        throw new EditorException("The Party to get or save is NULL!");
    }

    @Override
    public PartyData save(Party entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        String id = checkExistsOrGetId(entity, Party.class);
        PartyEntity party = new PartyEntity();
        party.setStaIdentifier(id);
        party.setName(entity.getName());
        party.setDescription(entity.getDescription());

        // authId is in the db model the identifier
        party.setAuthId(entity.getAuthId());
        if (entity.getRole() != null) {
            party.setRole(RolePartyCode.valueOf(entity.getRole().toString()));
        }
        party.setDisplayName(entity.getDisplayName());

        // save entity
        PartyEntity saved = partyRepository.save(party);

        saved.setDatasets(Streams.stream(entity.getDatastreams())
                .map(datastreamEditor::getOrSave)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        saved.setGroups(Streams.stream(entity.getGroups())
                .map(groupEditor::getOrSave)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        saved.setPlatforms(Streams.stream(entity.getThings())
                .map(thingEditor::getOrSave)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        partyRepository.flush();

        return new PartyData(saved, Optional.empty());
    }

    @Override
    public PartyData update(Party oldEntity, Party updateEntity) throws EditorException {
        Objects.requireNonNull(oldEntity, "no entity to patch found");
        Objects.requireNonNull(updateEntity, "no patches found");

        PartyEntity data = ((PartyData) oldEntity).getData();

        setIfNotNull(updateEntity::getName, data::setName);
        setIfNotNull(updateEntity::getDescription, data::setDescription);

        return new PartyData(partyRepository.save(data), Optional.empty());
    }

    @Override
    public void delete(String id) throws EditorException {
        PartyEntity party = getEntity(id)
                                               .orElseThrow(() -> new EditorException("could not find entity with id: "
                                                       + id));
        partyRepository.delete(party);
    }

    @Override
    protected Optional<PartyEntity> getEntity(String id) {
        PartyGraphBuilder graphBuilder = PartyGraphBuilder.createEmpty();
        return partyRepository.findByStaIdentifier(id, graphBuilder);
    }
}
