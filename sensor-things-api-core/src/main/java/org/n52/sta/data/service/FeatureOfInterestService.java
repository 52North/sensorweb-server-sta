/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.shetland.util.IdGenerator;
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
import org.n52.sta.exception.STACRUDException;
import org.n52.sta.serdes.model.ElementWithQueryOptions;
import org.n52.sta.serdes.model.ElementWithQueryOptions.FeatureOfInterestWithQueryOptions;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.utils.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class FeatureOfInterestService
        extends AbstractSensorThingsEntityService<FeatureOfInterestRepository, AbstractFeatureEntity<?>> {

    private static final Logger logger = LoggerFactory.getLogger(FeatureOfInterestService.class);

    private static final ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    private static final FeatureOfInterestQuerySpecifications foiQS = new FeatureOfInterestQuerySpecifications();
    private static final DatasetQuerySpecifications dQS = new DatasetQuerySpecifications();
    private static final DatastreamQuerySpecifications dsQS = new DatastreamQuerySpecifications();

    private final FormatRepository formatRepository;
    private final DataRepository dataRepository;
    private final DatasetRepository datasetRepository;
    private final DatastreamRepository datastreamRepository;

    @Autowired
    public FeatureOfInterestService(FeatureOfInterestRepository repository,
                                    FormatRepository formatRepository,
                                    DataRepository dataRepository,
                                    DatasetRepository datasetRepository,
                                    DatastreamRepository datastreamRepository) {
        super(repository, AbstractFeatureEntity.class);
        this.formatRepository = formatRepository;
        this.dataRepository = dataRepository;
        this.datasetRepository = datasetRepository;
        this.datastreamRepository = datastreamRepository;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.FeatureOfInterest, EntityTypes.FeaturesOfInterest};
    }

    @Override
    protected ElementWithQueryOptions createWrapper(Object entity, QueryOptions queryOptions) {
        return new FeatureOfInterestWithQueryOptions((AbstractFeatureEntity<?>) entity, queryOptions);
    }

    @Override
    protected Specification<AbstractFeatureEntity<?>> byRelatedEntityFilter(String relatedId,
                                                                            String relatedType,
                                                                            String ownId) {
        Specification<AbstractFeatureEntity<?>> filter;
        switch (relatedType) {
            case STAEntityDefinition.OBSERVATIONS: {
                filter = foiQS.withObservationIdentifier(relatedId);
                break;
            }
            default:
                return null;
        }

        if (ownId != null) {
            filter = filter.and(foiQS.withIdentifier(ownId));
        }
        return filter;
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
    public AbstractFeatureEntity<?> createEntity(AbstractFeatureEntity<?> feature) throws STACRUDException {
        if (feature.getIdentifier() != null && !feature.isSetName()) {
            return getRepository().findByIdentifier(feature.getIdentifier()).get();
        }
        if (feature.getIdentifier() == null) {
            if (getRepository().existsByName(feature.getName())) {
                Iterable<AbstractFeatureEntity<?>> features =
                        getRepository().findAll(foiQS.withName(feature.getName()));
                AbstractFeatureEntity<?> f = alreadyExistsFeature(features, feature);
                if (f != null) {
                    return f;
                } else {
                    // Autogenerate Identifier
                    feature.setIdentifier(UUID.randomUUID().toString());
                }
            } else {
                // Autogenerate Identifier
                feature.setIdentifier(UUID.randomUUID().toString());
            }
        } else if (getRepository().existsByIdentifier(feature.getIdentifier())) {
            // Return feature from database instead of creating it anew if it is based on a location.
            if (feature.getXml().equalsIgnoreCase("autogenerated")) {
                // This should never fail as we checked exist earlier
                return getRepository().findByIdentifier(feature.getIdentifier()).orElse(null);
            } else {
                throw new STACRUDException("Identifier already exists!", HttpStatus.BAD_REQUEST);
            }
        }
        feature.setXml(null);
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
    public AbstractFeatureEntity<?> updateEntity(String id, AbstractFeatureEntity<?> entity, HttpMethod method)
            throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            Optional<AbstractFeatureEntity<?>> existing = getRepository().findByIdentifier(id);
            if (existing.isPresent()) {
                AbstractFeatureEntity<?> merged = merge(existing.get(), entity);
                return getRepository().save(merged);
            }
            throw new STACRUDException("Unable to update. Entity not found.", HttpStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HttpStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected AbstractFeatureEntity<?> updateEntity(AbstractFeatureEntity<?> entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(String id) throws STACRUDException {
        if (getRepository().existsByIdentifier(id)) {
            // check observations
            deleteRelatedObservationsAndUpdateDatasets(id);
            getRepository().deleteByIdentifier(id);
        } else {
            throw new STACRUDException("Unable to delete. Entity not found.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    protected void delete(AbstractFeatureEntity<?> entity) throws STACRUDException {
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected AbstractFeatureEntity<?> createOrUpdate(AbstractFeatureEntity<?> entity)
            throws STACRUDException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return updateEntity(entity.getIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    private void deleteRelatedObservationsAndUpdateDatasets(String featureId) {

        // set dataset first/last to null
        Iterable<DatasetEntity> datasets = datasetRepository.findAll(dQS.matchFeatures(featureId));
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
            dataRepository.deleteAll(dataRepository.findAll(oQS.withDataset(d.getIdentifier())));
            getRepository().flush();
            datastreamRepository.findAll(dsQS.withDatasetIdentifier(d.getIdentifier())).forEach(ds -> {
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
        feature.setIdentifier(IdGenerator.generate(feature.getIdentifier()));
    }

    private AbstractSensorThingsEntityService<?, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(
                EntityTypes.Datastream);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    @Override
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();

        //AbstractFeatureEntity<?> entity = (AbstractFeatureEntity<?>) rawObject;
        //Iterable<DataEntity<?>> observations = dataRepository.findAll(d.withId(entity.getId()));
        //Set<Long> observationIds = new HashSet<>();
        //observations.forEach((o) -> {
        //    observationIds.add(o.getId());
        //});
        //collections.put(ET_FEATURE_OF_INTEREST_NAME, observationIds);

        return collections;
    }

    @Override
    public AbstractFeatureEntity<?> merge(AbstractFeatureEntity<?> existing, AbstractFeatureEntity<?> toMerge) {
        mergeIdentifierNameDescription(existing, toMerge);
        if (toMerge.isSetGeometry()) {
            existing.setGeometryEntity(toMerge.getGeometryEntity());
        }
        mergeFeatureType(existing);
        return existing;
    }

    private void mergeFeatureType(AbstractFeatureEntity<?> existing) {
        FormatEntity featureType = ServiceUtils.createFeatureType(existing.getGeometry());
        if (!featureType.getFormat().equals(existing.getFeatureType().getFormat())) {
            existing.setFeatureType(featureType);
        }
    }


}
