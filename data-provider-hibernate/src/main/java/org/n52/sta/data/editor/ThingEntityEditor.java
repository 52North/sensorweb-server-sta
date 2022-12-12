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
import org.n52.series.db.beans.PlatformEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.HistoricalLocationData;
import org.n52.sta.data.entity.LocationData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.PlatformRepository;
import org.n52.sta.data.support.ThingGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class ThingEntityEditor extends DatabaseEntityAdapter<PlatformEntity>
        implements
        EntityEditorDelegate<Thing, ThingData> {

    @Autowired
    private PlatformRepository platformRepository;

    private EntityEditorDelegate<Location, LocationData> locationEditor;
    private EntityEditorDelegate<Datastream, DatastreamData> datastreamEditor;
    private EntityEditorDelegate<HistoricalLocation, HistoricalLocationData> historicalLocationEditor;

    public ThingEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.locationEditor = (EntityEditorDelegate<Location, LocationData>)
                getService(Location.class).unwrapEditor();
        this.datastreamEditor = (EntityEditorDelegate<Datastream, DatastreamData>)
                getService(Datastream.class).unwrapEditor();
        this.historicalLocationEditor = (EntityEditorDelegate<HistoricalLocation, HistoricalLocationData>)
                getService(HistoricalLocation.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public ThingData getOrSave(Thing entity) throws EditorException {
        if (entity != null) {
            Optional<PlatformEntity> stored = getEntity(entity.getId());
            return stored.map(e -> new ThingData(e, Optional.empty())).orElseGet(() -> save(entity));
        }
        throw new EditorException("The Thing to get or save is NULL!");
    }

    @Override
    public ThingData save(Thing entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        if (entity.hasId()) {
            String staIdentifier = entity.getId();
            EntityService<Thing> thingService = getService(Thing.class);
            if (thingService.exists(staIdentifier)) {
                throw new EditorException("Thing already exists with Id '" + staIdentifier + "'");
            }
        }

        String id = entity.hasId()
                ? entity.getId()
                : generateId();
        PlatformEntity platformEntity = new PlatformEntity();
        platformEntity.setIdentifier(id);
        platformEntity.setStaIdentifier(id);
        platformEntity.setName(entity.getName());
        platformEntity.setDescription(entity.getDescription());

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(entry -> convertParameter(platformEntity, entry))
               .forEach(platformEntity::addParameter);

        // save entity
        PlatformEntity saved = platformRepository.save(platformEntity);

        // save related entities
        platformEntity.setLocations(Streams.stream(entity.getLocations())
                                           .map(locationEditor::getOrSave)
                                           .map(StaData::getData)
                                           .collect(Collectors.toSet()));

        platformEntity.setDatasets(Streams.stream(entity.getDatastreams())
                                          .map(datastreamEditor::getOrSave)
                                          .map(StaData::getData)
                                          .collect(Collectors.toSet()));

        platformEntity.setHistoricalLocations(Streams.stream(entity.getHistoricalLocations())
                                                     .map(historicalLocationEditor::getOrSave)
                                                     .map(StaData::getData)
                                                     .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        platformRepository.flush();

        return new ThingData(saved, Optional.empty());
    }

    @Override
    public ThingData update(Thing oldEntity, Thing updateEntity) throws EditorException {
        Objects.requireNonNull(oldEntity, "no entity to patch found");
        Objects.requireNonNull(updateEntity, "no patches found");

        PlatformEntity data = ((ThingData) oldEntity).getData();

        setIfNotNull(updateEntity::getName, data::setName);
        setIfNotNull(updateEntity::getDescription, data::setDescription);

        errorIfNotNull(updateEntity::getProperties, "properties");
        errorIfNotNull(updateEntity::getLocations, "locations");
        errorIfNotNull(updateEntity::getHistoricalLocations, "historicalLocations");
        errorIfNotNull(updateEntity::getDatastreams, "datastreams");

        return new ThingData(platformRepository.save(data), Optional.empty());
    }

    @Override
    public void delete(String id) throws EditorException {
        PlatformEntity platform = getEntity(id)
                                               .orElseThrow(() -> new EditorException("could not find entity with id: "
                                                       + id));

        platform.getDatasets()
                .forEach(ds -> {
                    datastreamEditor.delete(ds.getStaIdentifier());
                });

        platform.getHistoricalLocations()
                .forEach(hl -> {
                    platform.setHistoricalLocations(null);
                    platform.getLocations()
                            .forEach(loc -> loc.setHistoricalLocations(null));
                    historicalLocationEditor.delete(hl.getStaIdentifier());
                });

        platformRepository.delete(platform);
    }

    @Override
    protected Optional<PlatformEntity> getEntity(String id) {
        ThingGraphBuilder graphBuilder = ThingGraphBuilder.createEmpty();
        return platformRepository.findByStaIdentifier(id, graphBuilder);
    }
}
