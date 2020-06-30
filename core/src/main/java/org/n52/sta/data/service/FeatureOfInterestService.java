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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.sta.mapped.ObservationEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.FeatureOfInterestEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.DatasetQuerySpecifications;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.FeatureOfInterestQuerySpecifications;
import org.n52.sta.data.query.ObservationQuerySpecifications;
import org.n52.sta.data.repositories.DatasetRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.FeatureOfInterestRepository;
import org.n52.sta.data.repositories.FormatRepository;
import org.n52.sta.data.repositories.ObservationRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class FeatureOfInterestService
        extends AbstractSensorThingsEntityServiceImpl<FeatureOfInterestRepository, AbstractFeatureEntity<?>,
        StaFeatureEntity<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureOfInterestService.class);

    private static final ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    private static final FeatureOfInterestQuerySpecifications foiQS = new FeatureOfInterestQuerySpecifications();
    private static final DatasetQuerySpecifications dQS = new DatasetQuerySpecifications();
    private static final DatastreamQuerySpecifications dsQS = new DatastreamQuerySpecifications();

    private final FormatRepository formatRepository;
    private final ObservationRepository observationRepository;
    private final DatasetRepository datasetRepository;
    private final DatastreamRepository datastreamRepository;

    @Autowired
    public FeatureOfInterestService(FeatureOfInterestRepository repository,
                                    FormatRepository formatRepository,
                                    ObservationRepository observationRepository,
                                    DatasetRepository datasetRepository,
                                    DatastreamRepository datastreamRepository) {
        super(repository,
              AbstractFeatureEntity.class,
              EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURETYPE);
        this.formatRepository = formatRepository;
        this.observationRepository = observationRepository;
        this.datasetRepository = datasetRepository;
        this.datastreamRepository = datastreamRepository;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.FeatureOfInterest, EntityTypes.FeaturesOfInterest};
    }

    public AbstractFeatureEntity<?> getEntityByDatasetIdRaw(Long id, QueryOptions queryOptions)
            throws STACRUDException {
        try {
            Long foiId = datasetRepository.findById(id).get().getFeature().getId();
            AbstractFeatureEntity<?> entity = getRepository().findById(foiId).get();
            if (queryOptions.hasExpandFilter()) {
                return fetchExpandEntities(entity, queryOptions.getExpandFilter());
            } else {
                return entity;
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override
    protected StaFeatureEntity<?> fetchExpandEntities(AbstractFeatureEntity<?> entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        StaFeatureEntity<?> foi = new StaFeatureEntity<>(entity);
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (FeatureOfInterestEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                Page<ObservationEntity<?>> observation = getObservationService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                               STAEntityDefinition.FEATURES_OF_INTEREST,
                                                               expandItem.getQueryOptions());
                return foi.setObservations(observation.toSet());
            } else {
                throw new STAInvalidQueryException("Invalid expandOption supplied. Cannot find " + expandProperty +
                                                           " on Entity of type 'FeatureOfInterest'");
            }
        }
        return foi;
    }

    @Override
    protected Specification<AbstractFeatureEntity<?>> byRelatedEntityFilter(String relatedId,
                                                                            String relatedType,
                                                                            String ownId) {
        Specification<AbstractFeatureEntity<?>> filter;
        switch (relatedType) {
        case STAEntityDefinition.OBSERVATIONS: {
            filter = foiQS.withObservationStaIdentifier(relatedId);
            break;
        }
        default:
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(foiQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public String checkPropertyName(String property) {
        return foiQS.checkPropertyName(property);
    }

    @Override
    public AbstractFeatureEntity<?> createEntity(AbstractFeatureEntity<?> feature) throws STACRUDException {
        // Get by reference
        if (feature.getStaIdentifier() != null && !feature.isSetName()) {
            Optional<AbstractFeatureEntity<?>> optionalEntity =
                    getRepository().findByStaIdentifier(feature.getStaIdentifier(),
                                                        EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURETYPE);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException("No FeatureOfInterest with id '" + feature.getStaIdentifier() + "' found! ");
            }
        }
        if (feature.getStaIdentifier() == null) {
            if (getRepository().existsByName(feature.getName())) {
                Iterable<AbstractFeatureEntity<?>> features =
                        getRepository().findAll(foiQS.withName(feature.getName()));
                AbstractFeatureEntity<?> f = alreadyExistsFeature(features, feature);
                if (f != null) {
                    return f;
                } else {
                    // Autogenerate Identifier
                    String uuid = UUID.randomUUID().toString();
                    feature.setIdentifier(uuid);
                    feature.setStaIdentifier(uuid);
                }
            } else {
                // Autogenerate Identifier
                String uuid = UUID.randomUUID().toString();
                feature.setIdentifier(uuid);
                feature.setStaIdentifier(uuid);
            }
        }
        synchronized (getLock(feature.getStaIdentifier())) {
            // Check whether feature exists by sta and sos identifier
            if (getRepository().existsByStaIdentifier(feature.getStaIdentifier())) {
                // Return feature from database instead of creating it anew if it is based on a location.
                if (feature.getXml() != null && feature.getXml().equalsIgnoreCase(ServiceUtils.AUTOGENERATED_KEY)) {
                    // This should never fail as we checked exist earlier
                    return getRepository().findByStaIdentifier(feature.getStaIdentifier(),
                                                               EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURETYPE)
                                          .orElse(null);
                } else {
                    throw new STACRUDException("StaIdentifier already exists!", HTTPStatus.CONFLICT);
                }
            } else if (getRepository().existsByIdentifier(feature.getIdentifier())) {
                // Return feature from database instead of creating it anew if it is based on a location.
                if (feature.getXml() != null && feature.getXml().equalsIgnoreCase(ServiceUtils.AUTOGENERATED_KEY)) {
                    // This should never fail as we checked exist earlier
                    return getRepository().findByIdentifier(feature.getIdentifier(),
                                                            EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURETYPE)
                                          .orElse(null);
                } else {
                    throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
                }
            } else {
                feature.setXml(null);
                checkFeatureType(feature);
                return getRepository().save(feature);
            }
        }
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
            synchronized (getLock(id)) {
                Optional<AbstractFeatureEntity<?>> existing =
                        getRepository().findByStaIdentifier(id,
                                                            EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURETYPE);
                if (existing.isPresent()) {
                    AbstractFeatureEntity<?> merged = merge(existing.get(), entity);
                    return getRepository().save(merged);
                }
                throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    @Override
    protected AbstractFeatureEntity<?> updateEntity(AbstractFeatureEntity<?> entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                // check observations
                deleteRelatedObservationsAndUpdateDatasets(id);
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException("Unable to delete. Entity not found.", HTTPStatus.NOT_FOUND);
            }
        }
    }

    @Override
    protected void delete(AbstractFeatureEntity<?> entity) throws STACRUDException {
        delete(entity.getStaIdentifier());
    }

    @Override
    public AbstractFeatureEntity<?> createOrUpdate(AbstractFeatureEntity<?> entity)
            throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    private void deleteRelatedObservationsAndUpdateDatasets(String featureId) throws STACRUDException {
        // set dataset first/last to null
        synchronized (getLock(featureId)) {
            Iterable<DatasetEntity> datasets = datasetRepository.findAll(dQS.matchFeatureStaIdentifier(featureId));
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
                observationRepository.deleteAll(observationRepository.findAll(oQS.withDatasetId(d.getId())));
                getRepository().flush();
                datastreamRepository.findAll(dsQS.withDatasetId(d.getId()),
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS).forEach(ds -> {
                    ds.getDatasets().removeIf(e -> e.getId().equals(d.getId()));
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
    }

    private void checkFeatureType(AbstractFeatureEntity<?> feature) throws STACRUDException {
        FormatEntity format;
        synchronized (getLock(feature.getFeatureType().getFormat())) {
            if (!formatRepository.existsByFormat(feature.getFeatureType().getFormat())) {
                format = formatRepository.save(feature.getFeatureType());
            } else {
                format = formatRepository.findByFormat(feature.getFeatureType().getFormat());
            }
            feature.setFeatureType(format);
        }
    }

    /**
     * Extends the geometry of the FOI with given id by geom. Used for automatically expanding FOIs e.g. for
     * Observations along a track.
     * Used for non-standard feature 'updateFOI'.
     *
     * @param id   id of the FOI
     * @param geom geom to expand the existing Geometry
     * @throws STACRUDException if an error occurred
     */
    public void updateFeatureOfInterestGeometry(String id, Geometry geom) throws STACRUDException {
        synchronized (getLock(id)) {
            Optional<AbstractFeatureEntity<?>> existing =
                    getRepository().findByStaIdentifier(id, EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURETYPE);
            if (existing.isPresent()) {
                AbstractFeatureEntity<?> featureOfInterest = existing.get();
                if (featureOfInterest.isSetGeometry()) {
                    if (geom instanceof Point) {
                        List<Coordinate> coords = new ArrayList<>();
                        Geometry convert = featureOfInterest.getGeometry();
                        if (convert instanceof Point) {
                            coords.add(convert.getCoordinate());
                        } else if (convert instanceof LineString || convert instanceof GeometryCollection) {
                            coords.addAll(Arrays.asList(convert.getCoordinates()));
                        } else {
                            LOGGER.error("Could not update FOI geometry. Unknown GeometryType." +
                                                 convert.getClass().getSimpleName());
                            throw new STACRUDException(
                                    "Could not update FeatureOfInterest. Unknown GeometryType:" +
                                            convert.getClass().getSimpleName());
                        }
                        Geometry newGeometry;
                        if (!coords.isEmpty()) {
                            coords.add(geom.getCoordinate());
                            newGeometry = new GeometryFactory()
                                    .createLineString(coords.toArray(new Coordinate[coords.size()]));
                        } else {
                            newGeometry =
                                    new GeometryFactory().createPoint(geom.getCoordinate());
                        }
                        newGeometry.setSRID(featureOfInterest.getGeometry().getSRID());
                        featureOfInterest.setGeometry(newGeometry);
                    }
                } else {
                    featureOfInterest.setGeometry(geom);
                }
                getRepository().save(featureOfInterest);
            } else {
                throw new STACRUDException("Could not update FeatureOfInterest. No FeatureOfInterest with id \"" + id +
                                                   "\" found!");
            }
        }
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
