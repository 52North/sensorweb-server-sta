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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.FeatureOfInterestData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.repositories.entity.FeatureOfInterestRepository;
import org.n52.sta.data.support.FeatureOfInterestGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class FeatureOfInterestEntityEditor extends DatabaseEntityAdapter<AbstractFeatureEntity>
        implements
        EntityEditorDelegate<FeatureOfInterest, FeatureOfInterestData> {

    @Autowired
    private FeatureOfInterestRepository featureOfInterestRepository;

    @Autowired
    private ValueHelper valueHelper;

    private ObservationEditorDelegate<Observation, ObservationData> observationEditor;
    private DatastreamEditorDelegate<Datastream, DatastreamData> datastreamEditor;

    public FeatureOfInterestEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.observationEditor = (ObservationEditorDelegate<Observation, ObservationData>)
                getService(Observation.class).unwrapEditor();
        this.datastreamEditor = (DatastreamEditorDelegate<Datastream, DatastreamData>)
                getService(Datastream.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public FeatureOfInterestData get(FeatureOfInterest entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must be present!");
        Optional<AbstractFeatureEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new FeatureOfInterestData(e, Optional.empty()))
                .orElseThrow(() -> new EditorException(String.format("entity with id %s not found", entity.getId())));
    }

    @Override
    public FeatureOfInterestData save(FeatureOfInterest entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must be set!");

        String staIdentifier = entity.getId();
        EntityService<FeatureOfInterest> service = getService(FeatureOfInterest.class);

        if (service.exists(staIdentifier)) {
            throw new EditorException("FeatureOfInterest already exists with Id '" + staIdentifier + "'");
        }

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setIdentifier(id);
        featureEntity.setStaIdentifier(id);
        featureEntity.setName(entity.getName());
        featureEntity.setDescription(entity.getDescription());
        featureEntity.setGeometry(entity.getFeature());

        valueHelper.setFormat(featureEntity::setFeatureType, entity.getEncodingType());

        // TODO: Autogenerate Feature based on Location
        // TODO: Implement updating of Geometry via 'updateFOI' Feature
        // TODO: evaluate if functionality of FeatureOfInterestService#alreadyExistsFeature is needed here
        // TODO: Implement persisting nested observations

        FeatureEntity saved = featureOfInterestRepository.save(featureEntity);

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(entry -> convertParameter(featureEntity, entry))
               .forEach(featureEntity::addParameter);

        // we need to flush else updates to relations are not persisted
        featureOfInterestRepository.flush();

        return new FeatureOfInterestData(saved, Optional.empty());
    }

    @Override
    public FeatureOfInterestData update(FeatureOfInterest oldEntity, FeatureOfInterest updateEntity)
            throws EditorException {
        Objects.requireNonNull(oldEntity, "no entity to patch found");
        Objects.requireNonNull(updateEntity, "no patches found");

        AbstractFeatureEntity<?> data = ((FeatureOfInterestData) oldEntity).getData();

        setIfNotNull(updateEntity::getName, data::setName);
        setIfNotNull(updateEntity::getDescription, data::setDescription);
        setIfNotNull(updateEntity::getFeature, data::setGeometry);

        errorIfNotEmptyMap(updateEntity::getProperties, "properties");

        return new FeatureOfInterestData(featureOfInterestRepository.save(data), Optional.empty());
    }

    @Override
    public void delete(String id) throws EditorException {
        AbstractFeatureEntity<?> foi = getEntity(id)
                .orElseThrow(() -> new EditorException("could not find entity with id: " + id));

        foi.getDatasets()
           .forEach(ds -> {
               // delete foreign keys in dataset table
               datastreamEditor.clearFirstObservationLastObservationFeature(ds);
               // delete observations without updating first/last observation
               observationEditor.deleteObservationsByDatasetId(Collections.singleton(ds.getId()));
           });

        featureOfInterestRepository.deleteByStaIdentifier(id);

        // TODO: Implmement Deletion of related observations + update of related Datasets
        // TODO: See FeatureOfInterestService#deleteRelatedObservationsAndUpdateDatasets
    }

    @Override
    protected Optional<AbstractFeatureEntity> getEntity(String id) {
        FeatureOfInterestGraphBuilder graphBuilder = FeatureOfInterestGraphBuilder.createEmpty();
        return featureOfInterestRepository.findByStaIdentifier(id, graphBuilder);
    }
}
