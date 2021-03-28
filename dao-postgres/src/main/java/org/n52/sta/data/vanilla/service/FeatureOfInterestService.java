/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.vanilla.service;

import org.hibernate.Hibernate;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.parameter.feature.FeatureParameterEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.data.vanilla.query.DatastreamQuerySpecifications;
import org.n52.sta.data.vanilla.query.FeatureOfInterestQuerySpecifications;
import org.n52.sta.data.vanilla.query.ObservationQuerySpecifications;
import org.n52.sta.data.vanilla.repositories.DatastreamRepository;
import org.n52.sta.data.vanilla.repositories.EntityGraphRepository;
import org.n52.sta.data.vanilla.repositories.FeatureOfInterestParameterRepository;
import org.n52.sta.data.vanilla.repositories.FeatureOfInterestRepository;
import org.n52.sta.data.vanilla.repositories.FormatRepository;
import org.n52.sta.data.vanilla.repositories.ObservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class FeatureOfInterestService
    extends AbstractSensorThingsEntityServiceImpl<
    FeatureOfInterestRepository,
    FeatureOfInterestDTO,
    AbstractFeatureEntity<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureOfInterestService.class);

    private static final ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    private static final FeatureOfInterestQuerySpecifications foiQS = new FeatureOfInterestQuerySpecifications();
    private static final DatastreamQuerySpecifications dsQS = new DatastreamQuerySpecifications();

    private final FormatRepository formatRepository;
    private final ObservationRepository observationRepository;
    private final DatastreamRepository datastreamRepository;
    private final FeatureOfInterestParameterRepository parameterRepository;

    @Autowired
    public FeatureOfInterestService(FeatureOfInterestRepository repository,
                                    FormatRepository formatRepository,
                                    ObservationRepository observationRepository,
                                    DatastreamRepository datastreamRepository,
                                    FeatureOfInterestParameterRepository parameterRepository,
                                    EntityManager em) {
        super(repository,
              em,
              AbstractFeatureEntity.class);
        this.formatRepository = formatRepository;
        this.observationRepository = observationRepository;
        this.datastreamRepository = datastreamRepository;
        this.parameterRepository = parameterRepository;
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption) {
        return new EntityGraphRepository.FetchGraph[] {
            EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURETYPE,
            EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS
        };
    }

    @Override
    protected AbstractFeatureEntity<?> fetchExpandEntitiesWithFilter(AbstractFeatureEntity<?> entity,
                                                                     ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        StaFeatureEntity<?> foi = new StaFeatureEntity<>(entity);
        Set<DataEntity<?>> observations = new HashSet<>();
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (STAEntityDefinition.OBSERVATIONS.equals(expandProperty)) {
                Page<DataEntity<?>> observation = getObservationService()
                    .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                           STAEntityDefinition.FEATURES_OF_INTEREST,
                                                           expandItem.getQueryOptions());
                observations.addAll(observation.toSet());
            } else {
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                 expandProperty,
                                                                 StaConstants.FEATURE_OF_INTEREST));
            }
        }
        foi.setObservations(observations);
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
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(foiQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public AbstractFeatureEntity<?> createOrfetch(AbstractFeatureEntity<?> feature) throws STACRUDException {
        // Get by reference
        if (feature.getStaIdentifier() != null && !feature.isSetName()) {
            Optional<AbstractFeatureEntity<?>> optionalEntity =
                getRepository().findByStaIdentifier(feature.getStaIdentifier(),
                                                    EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURETYPE);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                         StaConstants.FEATURE_OF_INTEREST,
                                                         feature.getStaIdentifier()));
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
                    throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
                }
            } else {
                feature.setXml(null);
                checkFeatureType(feature);
                AbstractFeatureEntity<?> intermediateSave = getRepository().intermediateSave(feature);
                if (feature.getParameters() != null) {
                    parameterRepository.saveAll(feature.getParameters()
                                                    .stream()
                                                    .filter(t -> t instanceof FeatureParameterEntity)
                                                    .map(t -> {
                                                        ((FeatureParameterEntity) t).setFeature(intermediateSave);
                                                        return (FeatureParameterEntity) t;
                                                    })
                                                    .collect(Collectors.toSet()));
                }
                return getRepository().save(feature);
            }
        }
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
                    AbstractFeatureEntity<?> result = getRepository().save(merged);
                    Hibernate.initialize(result.getParameters());
                    return result;
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override
    public AbstractFeatureEntity<?> createOrUpdate(AbstractFeatureEntity<?> entity)
        throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override
    public String checkPropertyName(String property) {
        return foiQS.checkPropertyName(property);
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

    @Override
    public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                // check observations
                deleteRelatedObservationsAndUpdateDatasets(id);
                AbstractFeatureEntity<?> foi = getRepository().findByStaIdentifier(id).get();

                if (foi.hasParameters()) {
                    foi.getParameters()
                        .forEach(entity -> parameterRepository.delete((FeatureParameterEntity) entity));
                }
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    public AbstractFeatureEntity<?> getEntityByDatasetIdRaw(Long id, QueryOptions queryOptions)
        throws STACRUDException {
        try {
            Long foiId = datastreamRepository.findById(id).get().getFeature().getId();
            AbstractFeatureEntity<?> entity =
                getRepository().findById(foiId, createFetchGraph(queryOptions.getExpandFilter())).get();
            entity = (AbstractFeatureEntity<?>) Hibernate.unproxy(entity);
            if (queryOptions.hasExpandFilter()) {
                return fetchExpandEntitiesWithFilter(entity, queryOptions.getExpandFilter());
            } else {
                return entity;
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
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

    private void deleteRelatedObservationsAndUpdateDatasets(String featureId) throws STACRUDException {
        // set dataset first/last to null
        synchronized (getLock(featureId)) {
            Iterable<AbstractDatasetEntity> datasets =
                datastreamRepository.findAll(dsQS.withFeatureStaIdentifier(featureId));
            // update datasets
            datasets.forEach(d -> {
                d.setFirstObservation(null);
                d.setFirstQuantityValue(null);
                d.setFirstValueAt(null);
                d.setLastObservation(null);
                d.setLastQuantityValue(null);
                d.setLastValueAt(null);
                d.setFeature(null);
                datastreamRepository.saveAndFlush(d);
                // delete observations
                observationRepository.deleteAllByDatasetIdIn(Collections.singleton(d.getId()));
                getRepository().flush();
            });
            // delete
            datasets.forEach(d -> {
                // only delete if we are part of an aggregation
                // if we are not part of an aggregation we must not delete as this would also delete the whole
                // datastream
                if (d.isSetAggregation()) {
                    d.setFirstObservation(null);
                    d.setFirstQuantityValue(null);
                    d.setFirstValueAt(null);
                    d.setLastObservation(null);
                    d.setLastQuantityValue(null);
                    d.setLastValueAt(null);
                    datastreamRepository.delete(d);
                }
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

    private void mergeFeatureType(AbstractFeatureEntity<?> existing) {
        FormatEntity featureType = ServiceUtils.createFeatureType(existing.getGeometry());
        if (!featureType.getFormat().equals(existing.getFeatureType().getFormat())) {
            existing.setFeatureType(featureType);
        }
    }

}
