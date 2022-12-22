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
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.License;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.GroupData;
import org.n52.sta.data.entity.LicenseData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.repositories.entity.LicenseRepository;
import org.n52.sta.data.support.LicenseGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class LicenseEntityEditor extends DatabaseEntityAdapter<LicenseEntity>
        implements
        EntityEditorDelegate<License, LicenseData> {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private ValueHelper valueHelper;

    private EntityEditorDelegate<Datastream, DatastreamData> datastreamEditor;
    private EntityEditorDelegate<Group, GroupData> groupEditor;

    public LicenseEntityEditor(EntityServiceLookup serviceLookup) {
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
        //@formatter:on
    }

    @Override
    public LicenseData getOrSave(License entity) throws EditorException {
        if (entity != null) {
            Optional<LicenseEntity> stored = getEntity(entity.getId());
            return stored.map(e -> new LicenseData(e, Optional.empty())).orElseGet(() -> save(entity));
        }
        throw new EditorException("The License to get or save is NULL!");
    }

    @Override
    public LicenseData save(License entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        String id = checkExistsOrGetId(entity, License.class);
        LicenseEntity license = new LicenseEntity();
        license.setStaIdentifier(id);
        license.setName(entity.getName());
        license.setDescription(entity.getDescription());

        // definition is in the db model the identifier
        license.setDefinition(entity.getDefinition());
        license.setLogo(entity.getLogo());

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(entry -> convertParameter(license, entry))
               .forEach(license::addParameter);

        // save entity
        LicenseEntity saved = licenseRepository.save(license);

        saved.setDatasets(Streams.stream(entity.getDatastreams())
                .map(datastreamEditor::getOrSave)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        saved.setGroups(Streams.stream(entity.getGroups())
                .map(groupEditor::getOrSave)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        licenseRepository.flush();

        return new LicenseData(saved, Optional.empty());
    }

    @Override
    public LicenseData update(License oldEntity, License updateEntity) throws EditorException {
        Objects.requireNonNull(oldEntity, "no entity to patch found");
        Objects.requireNonNull(updateEntity, "no patches found");

        LicenseEntity data = ((LicenseData) oldEntity).getData();

        setIfNotNull(updateEntity::getName, data::setName);
        setIfNotNull(updateEntity::getDescription, data::setDescription);
        setIfNotNull(updateEntity::getDefinition, data::setDefinition);
        setIfNotNull(updateEntity::getLogo, data::setLogo);

        errorIfNotEmptyMap(updateEntity::getProperties, "properties");

        return new LicenseData(licenseRepository.save(data), Optional.empty());
    }

    @Override
    public void delete(String id) throws EditorException {
        LicenseEntity license = getEntity(id)
                                               .orElseThrow(() -> new EditorException("could not find entity with id: "
                                                       + id));
        licenseRepository.delete(license);
    }

    @Override
    protected Optional<LicenseEntity> getEntity(String id) {
        LicenseGraphBuilder graphBuilder = LicenseGraphBuilder.createEmpty();
        return licenseRepository.findByStaIdentifier(id, graphBuilder);
    }
}
