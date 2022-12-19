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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.HistoricalLocationData;
import org.n52.sta.data.entity.LocationData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.LocationRepository;
import org.n52.sta.data.support.LocationGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class LocationEntityEditor extends DatabaseEntityAdapter<LocationEntity>
        implements
        EntityEditorDelegate<Location, LocationData> {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ValueHelper valueHelper;

    private EntityEditorDelegate<Thing, ThingData> thingEditor;
    private EntityEditorDelegate<HistoricalLocation, HistoricalLocationData> historicalLocationEditor;

    public LocationEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.thingEditor = (EntityEditorDelegate<Thing, ThingData>)
                getService(Thing.class).unwrapEditor();
        this.historicalLocationEditor = (EntityEditorDelegate<HistoricalLocation, HistoricalLocationData>)
                getService(HistoricalLocation.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public LocationData getOrSave(Location entity) throws EditorException {
        Optional<LocationEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new LocationData(e, Optional.empty()))
                     .orElseGet(() -> save(entity));
    }

    @Override
    public LocationData save(Location entity) throws EditorException {

        String staIdentifier = entity.getId();
        EntityService<Location> locationService = getService(Location.class);
        if (locationService.exists(staIdentifier)) {
            throw new EditorException("Location already exists with Id '" + staIdentifier + "'");
        }

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();

        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setIdentifier(id);
        locationEntity.setStaIdentifier(id);
        locationEntity.setName(entity.getName());
        locationEntity.setDescription(entity.getDescription());
        locationEntity.setGeometry(entity.getGeometry());
        // TODO: check if this String representation is legacy or actually used.
        locationEntity.setLocation(entity.getGeometry()
                                         .toString());

        valueHelper.setFormat(locationEntity::setLocationEncoding, entity.getEncodingType());

        LocationEntity saved = locationRepository.save(locationEntity);

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(entry -> convertParameter(locationEntity, entry))
               .forEach(locationEntity::addParameter);

        locationEntity.setPlatforms(Streams.stream(entity.getThings())
                                           .map(thingEditor::getOrSave)
                                           .map(StaData::getData)
                                           .collect(Collectors.toSet()));

        locationEntity.setHistoricalLocations(Streams.stream(entity.getHistoricalLocations())
                                                     .map(historicalLocationEditor::getOrSave)
                                                     .map(StaData::getData)
                                                     .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        locationRepository.flush();

        return new LocationData(saved, Optional.empty());
    }

    @Override
    public LocationData update(Location oldEntity, Location updateEntity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        LocationEntity location = getEntity(id)
                                               .orElseThrow(() -> new EditorException("could not find entity with id: "
                                                       + id));

        Set<HistoricalLocationEntity> historicalLocations = location.getHistoricalLocations();

        location.setHistoricalLocations(null);
        historicalLocations.forEach(hl -> {
            historicalLocationEditor.delete(hl.getStaIdentifier());
        });

        location.getPlatforms()
                .forEach(thing -> {
                    thing.getLocations()
                         .remove(location);
                });
        location.setPlatforms(null);

        locationRepository.delete(location);
    }

    @Override
    protected Optional<LocationEntity> getEntity(String id) {
        LocationGraphBuilder graphBuilder = LocationGraphBuilder.createEmpty();
        return locationRepository.findByStaIdentifier(id, graphBuilder);
    }

}