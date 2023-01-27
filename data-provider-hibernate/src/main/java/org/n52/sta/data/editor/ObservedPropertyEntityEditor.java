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
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.ObservedPropertyData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.repositories.entity.PhenomenonRepository;
import org.n52.sta.data.support.ObservedPropertyGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class ObservedPropertyEntityEditor extends DatabaseEntityAdapter<PhenomenonEntity> implements
        EntityEditorDelegate<ObservedProperty, ObservedPropertyData> {

    @Autowired
    private PhenomenonRepository phenomenonRepository;

    private EntityEditorDelegate<Datastream, DatastreamData> datastreamEditor;

    public ObservedPropertyEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.datastreamEditor = (EntityEditorDelegate<Datastream, DatastreamData>)
                getService(Datastream.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public ObservedPropertyData get(ObservedProperty entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must be present!");
        Optional<PhenomenonEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new ObservedPropertyData(e, Optional.empty()))
                .orElseThrow(() -> new EditorException(String.format("entity with id %s not found", entity.getId())));
    }

    @Override
    public ObservedPropertyData save(ObservedProperty entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        assertNew(entity);

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();
        PhenomenonEntity phenomenonEntity = new PhenomenonEntity();
        phenomenonEntity.setIdentifier(entity.getDefinition());
        phenomenonEntity.setStaIdentifier(id);
        phenomenonEntity.setName(entity.getName());
        phenomenonEntity.setDescription(entity.getDescription());

        PhenomenonEntity saved = phenomenonRepository.save(phenomenonEntity);

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(entry -> convertParameter(phenomenonEntity, entry))
               .forEach(phenomenonEntity::addParameter);

        // save related entities
        phenomenonEntity.setDatasets(Streams.stream(entity.getDatastreams())
                                            .map(datastreamEditor::get)
                                            .map(StaData::getData)
                                            .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        phenomenonRepository.flush();

        return new ObservedPropertyData(saved, Optional.empty());
    }

    @Override
    public ObservedPropertyData update(ObservedProperty oldEntity, ObservedProperty updateEntity)
            throws EditorException {
        Objects.requireNonNull(oldEntity, "no entity to patch found");
        Objects.requireNonNull(updateEntity, "no patches found");

        PhenomenonEntity data = ((ObservedPropertyData) oldEntity).getData();

        setIfNotNull(updateEntity::getName, data::setName);
        setIfNotNull(updateEntity::getDescription, data::setDescription);
        setIfNotNull(updateEntity::getDefinition, data::setIdentifier);

        errorIfNotEmptyMap(updateEntity::getProperties, "properties");

        return new ObservedPropertyData(phenomenonRepository.save(data), Optional.empty());
    }

    @Override
    public void delete(String id) throws EditorException {
        PhenomenonEntity phenomenon = getEntity(id)
                .orElseThrow(() -> new EditorException("could not find entity with id: " + id));

        phenomenon.getDatasets()
                  .forEach(ds -> {
                      datastreamEditor.delete(ds.getStaIdentifier());
                  });

        phenomenonRepository.delete(phenomenon);
    }

    @Override
    protected Optional<PhenomenonEntity> getEntity(String id) {
        ObservedPropertyGraphBuilder graphBuilder = ObservedPropertyGraphBuilder.createEmpty();
        return phenomenonRepository.findByStaIdentifier(id, graphBuilder);
    }

    private void assertNew(ObservedProperty observedProperty) throws EditorException {
        EntityService<ObservedProperty> service = getService(ObservedProperty.class);
        String staIdentifier = observedProperty.getId();
        if (service.exists(staIdentifier)) {
            throw new EditorException("ObservedProperty already exists with ID '" + staIdentifier + "'");
        }

        // definition (mapped in the data model as identifier) must be unique
        if (phenomenonRepository.existsByIdentifier(observedProperty.getDefinition())) {
            throw new EditorException("ObservedProperty with given definition already exists!");
        }
    }
}
