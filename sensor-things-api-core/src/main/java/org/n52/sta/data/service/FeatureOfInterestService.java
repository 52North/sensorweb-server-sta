/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.data.service;

import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.shetland.util.JavaHelper;
import org.n52.sta.data.query.DatasetQuerySpecifications;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.FeatureOfInterestQuerySpecifications;
import org.n52.sta.data.query.ObservationQuerySpecifications;
import org.n52.sta.data.repositories.DataRepository;
import org.n52.sta.data.repositories.DatasetRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.FeatureOfInterestRepository;
import org.n52.sta.data.repositories.FormatRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.FeatureOfInterestMapper;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.n52.sta.edm.provider.entities.FeatureOfInterestEntityProvider.ET_FEATURE_OF_INTEREST_NAME;

/**
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class FeatureOfInterestService
        extends AbstractSensorThingsEntityService<FeatureOfInterestRepository, AbstractFeatureEntity<?>> {

    private FeatureOfInterestMapper mapper;

    @Autowired
    private FormatRepository formatRepository;

    @Autowired
    private DataRepository<DataEntity<?>> dataRepository;

    @Autowired
    private DatasetRepository<DatasetEntity> datasetRepository;

    @Autowired
    private DatastreamRepository datastreamRepository;

    private final static FeatureOfInterestQuerySpecifications foiQS = new FeatureOfInterestQuerySpecifications();

    private ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();

    private DatasetQuerySpecifications dQS = new DatasetQuerySpecifications();

    private DatastreamQuerySpecifications dsQS = new DatastreamQuerySpecifications();

    public FeatureOfInterestService(FeatureOfInterestRepository repository, FeatureOfInterestMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }

    @Override
    public EntityTypes getType() {
        return EntityTypes.FeatureOfInterest;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) throws ODataApplicationException {
        EntityCollection retEntitySet = new EntityCollection();
        Predicate filter = getFilterPredicate(AbstractFeatureEntity.class, queryOptions);
        getRepository().findAll(filter, createPageableRequest(queryOptions))
                .forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(Long id) {
        Optional<AbstractFeatureEntity<?>> entity = getRepository().findOne(byId(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType,
            QueryOptions queryOptions) {
        return null;
    }

    @Override
    public boolean existsEntity(Long id) {
        return getRepository().exists(byId(id));
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.existsRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Observation": {
                BooleanExpression filter = foiQS.withObservation(sourceId);
                if (targetId != null) {
                    filter = filter.and(foiQS.withId(targetId));
                }
                return getRepository().exists(filter);
            }
            default:
                return false;
        }
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<AbstractFeatureEntity<?>> foi = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (foi.isPresent()) {
            return OptionalLong.of(foi.get().getId());
        } else {
            return OptionalLong.empty();
        }
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<AbstractFeatureEntity<?>> feature = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (feature.isPresent()) {
            return mapper.createEntity(feature.get());
        } else {
            return null;
        }
    }

    /**
     * Retrieves FeatureOfInterest Entity (aka Feature Entity) with Relation to
     * sourceEntity from Database. Returns empty if Feature is not found or
     * Entities are not related.
     *
     * @param sourceId Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId Id of the Entity to be retrieved
     * @return Optional<FeatureEntity> Requested Entity
     */
    private Optional<AbstractFeatureEntity<?>> getRelatedEntityRaw(Long sourceId, EdmEntityType sourceEntityType,
            Long targetId) {
        BooleanExpression filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Observation": {
                filter = foiQS.withObservation(sourceId);
                break;
            }
            default:
                return Optional.empty();
        }

        if (targetId != null) {
            filter = filter.and(foiQS.withId(targetId));
        }
        return getRepository().findOne(filter);
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            case "encodingType":
                return AbstractFeatureEntity.PROPERTY_FEATURE_TYPE;
            default:
                return property;
        }
    }

    @Override
    public long getCount(QueryOptions queryOptions) throws ODataApplicationException {
        return getRepository().count(getFilterPredicate(AbstractFeatureEntity.class, queryOptions));
    }

    /**
     * Constructs SQL Expression to request Entity by ID.
     *
     * @param id id of the requested entity
     * @return BooleanExpression evaluating to true if Entity is found and valid
     */
    private BooleanExpression byId(Long id) {
        return foiQS.withId(id);
    }

    @Override
    public AbstractFeatureEntity<?> create(AbstractFeatureEntity<?> feature) {
        if (feature.getId() != null && !feature.isSetName()) {
            return getRepository().findOne(foiQS.withId(feature.getId())).get();
        }
        if (getRepository().exists(foiQS.withName(feature.getName()))) {
            Iterable<AbstractFeatureEntity<?>> features = getRepository().findAll(foiQS.withName(feature.getName()));
            AbstractFeatureEntity<?> f = alreadyExistsFeature(features, feature);
            if (f != null) {
                return f;
            }
        }
        checkFeatureType(feature);
        return getRepository().save(feature);
    }

    private AbstractFeatureEntity<?> alreadyExistsFeature(Iterable<AbstractFeatureEntity<?>> features,
            AbstractFeatureEntity<?> feature) {
        for (AbstractFeatureEntity<?> f : features) {
            if (f.isSetGeometry() && feature.isSetGeometry() && f.getGeometry().equals(feature.getGeometry())
                    && f.getDescription().equals(feature.getDescription())) {
                return f;
            }
        }
        return null;
    }

    @Override
    public AbstractFeatureEntity<?> update(AbstractFeatureEntity<?> entity, HttpMethod method)
            throws ODataApplicationException {
        if (HttpMethod.PATCH.equals(method)) {
            Optional<AbstractFeatureEntity<?>> existing = getRepository().findOne(foiQS.withId(entity.getId()));
            if (existing.isPresent()) {
                AbstractFeatureEntity<?> merged = mapper.merge(existing.get(), entity);
                return getRepository().save(merged);
            }
            throw new ODataApplicationException("Entity not found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        throw new ODataApplicationException("Invalid http method for updating entity!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }

    @Override
    protected AbstractFeatureEntity<?> update(AbstractFeatureEntity<?> entity) throws ODataApplicationException {
        return getRepository().save(entity);
    }

    @Override
    public void delete(Long id) throws ODataApplicationException {
        if (getRepository().existsById(id)) {
            // check observations
            deleteRelatedObservationsAndUpdateDatasets(id);
            getRepository().deleteById(id);
        } else {
            throw new ODataApplicationException("Entity not found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        }
    }

    @Override
    protected void delete(AbstractFeatureEntity<?> entity) throws ODataApplicationException {
        getRepository().deleteById(entity.getId());
    }

    private void deleteRelatedObservationsAndUpdateDatasets(Long featureId) {

        // set dataset first/last to null
        Iterable<DatasetEntity> datasets = datasetRepository.findAll(dQS.matchFeatures(Long.toString(featureId)));
        // update datasets
        datasets.forEach(d -> {
            d.setFirstObservation(null);
            d.setFirstQuantityValue(null);
            d.setFirstValueAt(null);
            d.setLastObservation(null);
            d.setLastQuantityValue(null);
            d.setLastValueAt(null);
            datasetRepository.saveAndFlush(d);
            // delete observations
            dataRepository.deleteAll(dataRepository.findAll(oQS.withDataset(d.getId())));
            getRepository().flush();
            datastreamRepository.findAll(dsQS.withDataset(d.getId())).forEach(ds -> {
                ds.getDatasets().remove(d);
                datastreamRepository.saveAndFlush(ds);

            });
        });
        // delete datasets
        datasets.forEach(d -> {
            d.setFirstObservation(null);
            d.setFirstQuantityValue(null);
            d.setFirstValueAt(null);
            d.setLastObservation(null);
            d.setLastQuantityValue(null);
            d.setLastValueAt(null);
            datasetRepository.delete(d);
        });
        getRepository().flush();
    }

    private void checkFeatureType(AbstractFeatureEntity<?> feature) {
        FormatEntity format;
        if (!formatRepository.existsByFormat(feature.getFeatureType().getFormat())) {
            format = formatRepository.save(feature.getFeatureType());
        } else {
            format = formatRepository.findByFormat(feature.getFeatureType().getFormat());
        }
        feature.setFeatureType(format);
    }

    private void generateIdentifier(AbstractFeatureEntity<?> feature) {
        feature.setIdentifier(JavaHelper.generateID(feature.getIdentifier()));
    }

    private AbstractSensorThingsEntityService<?, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(
                EntityTypes.Datastream);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    @Override
    public Map<String, Set<Long>> getRelatedCollections(Object rawObject) {
        Map<String, Set<Long>> collections = new HashMap<String, Set<Long>>();

        AbstractFeatureEntity<?> entity = (AbstractFeatureEntity<?>) rawObject;

        Iterable<DataEntity<?>> observations = dataRepository.findAll(foiQS.withId(entity.getId()));
        Set<Long> observationIds = new HashSet<Long>();
        observations.forEach((o) -> {
            observationIds.add(o.getId());
        });
        collections.put(ET_FEATURE_OF_INTEREST_NAME, observationIds);

        return collections;
    }
}
