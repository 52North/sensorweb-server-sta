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

import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetAggregationEntity;
import org.n52.series.db.beans.sta.AbstractObservationEntity;
import org.n52.series.db.beans.sta.BooleanObservationEntity;
import org.n52.series.db.beans.sta.CategoryObservationEntity;
import org.n52.series.db.beans.sta.CountObservationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservationEntity;
import org.n52.series.db.beans.sta.QuantityObservationEntity;
import org.n52.series.db.beans.sta.TextObservationEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.EntityQuerySpecifications;
import org.n52.sta.data.repositories.DataRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.n52.sta.data.repositories.ObservationRepository;
import org.n52.sta.data.repositories.ParameterRepository;
import org.n52.sta.data.repositories.StaIdentifierRepository;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class AbstractObservationService<
        T extends StaIdentifierRepository<I>,
        I extends AbstractObservationEntity, O extends I>
        extends AbstractSensorThingsEntityServiceImpl<T, I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationService.class);
    private static final DatastreamQuerySpecifications dsQS = new DatastreamQuerySpecifications();

    protected final DataRepository<DataEntity<?>> dataRepository;
    protected final DatastreamRepository datastreamRepository;
    protected final ParameterRepository parameterRepository;

    private final Class entityClass;
    private final EntityQuerySpecifications<I> oQS;

    public AbstractObservationService(
            T repository,
            Class entityClass,
            EntityQuerySpecifications<I> oQS,
            DataRepository<DataEntity<?>> dataRepository,
            DatastreamRepository datastreamRepository,
            ParameterRepository parameterRepository,
            EntityGraphRepository.FetchGraph... defaultFetchGraphs) {
        super(repository, entityClass, defaultFetchGraphs);
        this.entityClass = entityClass;
        this.oQS = oQS;
        this.dataRepository = dataRepository;
        this.datastreamRepository = datastreamRepository;
        this.parameterRepository = parameterRepository;
    }

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Specification<I> spec = getFilterPredicate(ObservationEntity.class, queryOptions);
            List<String> identifierList = getRepository()
                    .getColumnList(spec,
                                   pageableRequest,
                                   STAIDENTIFIER);
            if (identifierList.isEmpty()) {
                return new CollectionWrapper(0, Collections.emptyList(), false);
            } else {
                return getEntityCollectionWrapperByIdentifierList(identifierList, pageableRequest, queryOptions, spec);
            }
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                          String relatedType,
                                                                          QueryOptions queryOptions)
            throws STACRUDException {

        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Specification<I> spec =
                    byRelatedEntityFilter(relatedId, relatedType, null)
                            .and(getFilterPredicate(entityClass, queryOptions));

            List<String> identifierList = getRepository().getColumnList(spec,
                                                                        createPageableRequest(queryOptions),
                                                                        STAIDENTIFIER);
            if (identifierList.isEmpty()) {
                return new CollectionWrapper(0, Collections.emptyList(), false);
            } else {
                return getEntityCollectionWrapperByIdentifierList(identifierList, pageableRequest, queryOptions, spec);
            }
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    private CollectionWrapper getEntityCollectionWrapperByIdentifierList(List<String> identifierList,
                                                                         OffsetLimitBasedPageRequest pageableRequest,
                                                                         QueryOptions queryOptions,
                                                                         Specification<I> spec) {
        Page<I> pages = getRepository().findAll(
                oQS.withStaIdentifier(identifierList),
                new OffsetLimitBasedPageRequest(0,
                                                pageableRequest.getPageSize(),
                                                pageableRequest.getSort()),
                EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);

        CollectionWrapper wrapper = getCollectionWrapper(queryOptions, pages);
        // Create Page manually as we used Database Pagination and are not sure how many Entities there are in
        // the Database
        if (pages.isEmpty()) {
            return wrapper;
        } else {
            long count = getRepository().count(spec);
            return new CollectionWrapper(count, wrapper.getEntities(),
                                         identifierList.size() + pageableRequest.getOffset() < count);
        }
    }

    public Page getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                      String relatedType,
                                                      QueryOptions queryOptions)
            throws STACRUDException {
        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Specification<I> spec =
                    byRelatedEntityFilter(relatedId, relatedType, null)
                            .and(getFilterPredicate(ObservationEntity.class, queryOptions));

            List<String> identifierList = getRepository().getColumnList(spec,
                                                                        createPageableRequest(queryOptions),
                                                                        STAIDENTIFIER);
            if (identifierList.isEmpty()) {
                return Page.empty();
            } else {
                Page<I> pages = getRepository().findAll(
                        oQS.withStaIdentifier(identifierList),
                        new OffsetLimitBasedPageRequest(0,
                                                        pageableRequest.getPageSize(),
                                                        pageableRequest.getSort()),
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);
                if (queryOptions.hasExpandFilter()) {
                    return pages.map(e -> {
                        try {
                            return fetchExpandEntities(e, queryOptions.getExpandFilter());
                        } catch (STACRUDException | STAInvalidQueryException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                } else {
                    return pages;
                }
            }
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override
    public String checkPropertyName(String property) {
        return oQS.checkPropertyName(property);
    }

    @Override
    public I createOrfetch(I entity) throws STACRUDException {
        synchronized (getLock(entity.getStaIdentifier())) {
            I observation = entity;
            if (!observation.isProcessed()) {
                observation.setProcessed(true);
                check(observation);

                // Fetch dataset and check if FOI matches to reuse existing dataset
                AbstractDatasetEntity datastream = datastreamRepository
                        .findByStaIdentifier(entity.getDataset().getStaIdentifier(),
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURE)
                        .orElseThrow(() -> new STACRUDException("Unable to find Datastream!"));
                AbstractFeatureEntity<?> feature = createOrfetchFeature(observation, datastream.getPlatform().getId());

                // Check all subdatasets for a matching  dataset
                Set<AbstractDatasetEntity> datasets;
                if (datastream.getAggregation() == null && !(datastream instanceof DatasetAggregationEntity)) {
                    // We are not an aggregate so there is only one dataset to check for fit
                    datasets = Collections.singleton(datastream);
                } else {
                    datasets = datastreamRepository.findAllByAggregationId(datastream.getId());
                }

                // Check all datasets for a matching FOI
                boolean found = false;
                for (AbstractDatasetEntity dataset : datasets) {
                    if (!dataset.hasFeature()) {
                        // We have a dataset without a feature
                        LOGGER.debug("Reusing existing dataset without FOI.");
                        dataset.setFeature(feature);
                        observation.setDataset(datastreamRepository.save(dataset));
                        found = true;
                        break;
                    } else if (feature.getId().equals(dataset.getFeature().getId())) {
                        // We have a dataset with a matching feature
                        observation.setDataset(dataset);
                        LOGGER.debug("Reusing existing dataset with matching FOI.");
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // We have not found a matching dataset so we need to create a new one
                    LOGGER.debug("Creating new dataset as none with matching FOI exists");
                    observation.setDataset(getDatastreamService().createOrExpandAggregation(datastream, feature));
                }

                // Save Observation
                I data = saveObservation(observation, observation.getDataset());

                // Update FirstValue/LastValue + FirstObservation/LastObservation of Dataset + Aggregation
                updateDataset(observation.getDataset(), data);
                return data;
            }
            return observation;
        }
    }

    private void check(AbstractObservationEntity observation) throws STACRUDException {
        if (observation.getDataset() == null) {
            throw new STACRUDException("The observation to create is invalid. Missing datastream!",
                                       HTTPStatus.BAD_REQUEST);
        }
    }

    /**
     * Handles updating the phenomenonTime field of the associated Datastream when Observation phenomenonTime is
     * updated or deleted
     */
    protected void updateDatastreamPhenomenonTimeOnObservationUpdate(AbstractDatasetEntity datastreamEntity,
                                                                     I observation) {
        if (datastreamEntity.getPhenomenonTimeStart() == null ||
                datastreamEntity.getPhenomenonTimeEnd() == null ||
                observation.getPhenomenonTimeStart().compareTo(datastreamEntity.getPhenomenonTimeStart()) != 1 ||
                observation.getPhenomenonTimeEnd().compareTo(datastreamEntity.getPhenomenonTimeEnd()) != -1
        ) {
            // Setting new phenomenonTimeStart
            ObservationEntity<?> firstObservation = ((ObservationRepository) getRepository())
                    .findFirstByDataset_idOrderBySamplingTimeStartAsc(datastreamEntity.getId());
            Date newPhenomenonStart = (firstObservation == null) ? null : firstObservation.getPhenomenonTimeStart();

            // Set Start and End to null if there is no observation.
            if (newPhenomenonStart == null) {
                datastreamEntity.setPhenomenonTimeStart(null);
                datastreamEntity.setPhenomenonTimeEnd(null);
            } else {
                datastreamEntity.setPhenomenonTimeStart(newPhenomenonStart);

                // Setting new phenomenonTimeEnd
                ObservationEntity<?> lastObservation = ((ObservationRepository) getRepository())
                        .findFirstByDataset_idOrderBySamplingTimeEndDesc(datastreamEntity.getId());
                Date newPhenomenonEnd = (lastObservation == null) ? null : lastObservation.getPhenomenonTimeEnd();
                if (newPhenomenonEnd != null) {
                    datastreamEntity.setPhenomenonTimeEnd(newPhenomenonEnd);
                } else {
                    datastreamEntity.setPhenomenonTimeStart(null);
                    datastreamEntity.setPhenomenonTimeEnd(null);
                }
            }
            datastreamRepository.save(datastreamEntity);
            // update parent if its part of the aggregation
            if (datastreamEntity.isSetAggregation()) {
                updateDatastreamPhenomenonTimeOnObservationUpdate(
                        datastreamRepository.findById(datastreamEntity.getAggregation()
                                                                      .getId()).get(),
                        observation);
            }
        }
    }

    @Override
    public I updateEntity(String id, I entity, HttpMethod method)
            throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<I> existing =
                        getRepository()
                                .findByStaIdentifier(id,
                                                     EntityGraphRepository
                                                             .FetchGraph
                                                             .FETCHGRAPH_PARAMETERS);
                if (existing.isPresent()) {
                    I merged = merge(existing.get(), entity);
                    I saved = getRepository().save(merged);

                    AbstractDatasetEntity datastreamEntity =
                            datastreamRepository.findById(saved.getDataset().getId()).get();

                    updateDatastreamPhenomenonTimeOnObservationUpdate(datastreamEntity, saved);
                    return saved;
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override
    public void delete(String identifier) throws STACRUDException {
        synchronized (getLock(identifier)) {
            if (getRepository().existsByStaIdentifier(identifier)) {
                I observation =
                        getRepository().findByStaIdentifier(
                                identifier,
                                EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASET_FIRSTLAST_OBSERVATION)
                                       .get();
                updateDatasetFirstLast(observation);

                // Important! Delete first and then update else we find
                // ourselves again in search for new latest/earliest obs.
                getRepository().deleteByStaIdentifier(observation.getStaIdentifier());
                updateDatastreamPhenomenonTimeOnObservationUpdate(observation.getDataset(), observation);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    @Override
    public void delete(I entity) throws STACRUDException {
        delete(entity.getStaIdentifier());
    }

    @Override
    public I createOrUpdate(I entity) throws STACRUDException {
        synchronized (getLock(entity.getStaIdentifier())) {
            if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
                return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
            }
            return createOrfetch(entity);
        }
    }

    private void updateDatasetFirstLast(I observation) {
        // TODO get the next first/last observation and set it
        AbstractDatasetEntity dataset = observation.getDataset();
        if (dataset.getFirstObservation() != null
                && dataset.getFirstObservation().getStaIdentifier().equals(observation.getStaIdentifier())) {
            dataset.setFirstObservation(null);
            dataset.setFirstQuantityValue(null);
            dataset.setFirstValueAt(null);
        }
        if (dataset.getLastObservation() != null && dataset.getLastObservation()
                                                           .getStaIdentifier()
                                                           .equals(observation.getStaIdentifier())) {
            dataset.setLastObservation(null);
            dataset.setLastQuantityValue(null);
            dataset.setLastValueAt(null);
        }
        observation.setDataset(datastreamRepository.saveAndFlush(dataset));
    }

    private AbstractFeatureEntity<?> createOrfetchFeature(AbstractObservationEntity observation,
                                                          Long thingId)
            throws STACRUDException {
        // Create feature based on Thing.location if there is no feature given
        if (!observation.hasFeature()) {
            AbstractFeatureEntity<?> feature = null;
            LocationRepository locationRepository = (LocationRepository) getLocationService().getRepository();
            Set<LocationEntity> locations = locationRepository.findAllByThingsIdEquals(thingId);
            for (LocationEntity location : locations) {
                if (feature == null) {
                    feature = ServiceUtils.createFeatureOfInterest(location);
                }
                if (location.isSetGeometry()) {
                    feature = ServiceUtils.createFeatureOfInterest(location);
                    break;
                }
            }
            if (feature == null) {
                throw new STACRUDException("The observation to create is invalid." +
                                                   " Missing feature or thing.location!", HTTPStatus.BAD_REQUEST);
            }
            observation.setFeature(feature);
        }
        // save feature to db
        AbstractFeatureEntity<?> feature = getFeatureOfInterestService().createOrfetch(observation.getFeature());
        observation.setFeature(feature);
        return feature;
    }

    private I saveObservation(AbstractObservationEntity observation, AbstractDatasetEntity dataset)
            throws STACRUDException {
        I data = castToConcreteObservationType(observation, dataset);
        return getRepository().save(data);
    }

    /**
     * Updates FirstValue/LastValue, FirstObservation/LastObservation, Geometry of Dataset and DatasetAggregation
     *
     * @param dataset Dataset to be updated
     * @param data    New Observation
     * @return update DatasetEntity
     * @throws STACRUDException if an error occurred
     */
    private AbstractDatasetEntity updateDataset(AbstractDatasetEntity dataset, I data) throws STACRUDException {
        Optional<DataEntity<?>> rawObservation = dataRepository.findById(data.getId());
        if (rawObservation.isPresent()) {
            synchronized (getLock(dataset.getId().toString() + "Dataset")) {
                LOGGER.debug("Updating First/Last/Geometry of of Dataset: {}", dataset.getId());
                if (!dataset.isSetFirstValueAt()
                        || (dataset.isSetFirstValueAt()
                        && data.getSamplingTimeStart().before(dataset.getFirstValueAt()))) {
                    dataset.setFirstValueAt(data.getSamplingTimeStart());
                    dataset.setFirstObservation(rawObservation.get());
                    if (data instanceof QuantityObservationEntity) {
                        dataset.setFirstQuantityValue(((QuantityObservationEntity) data).getValue());
                    }
                }
                if (!dataset.isSetLastValueAt()
                        || (dataset.isSetLastValueAt()
                        && data.getSamplingTimeEnd().after(dataset.getLastValueAt()))) {
                    dataset.setLastValueAt(data.getSamplingTimeEnd());
                    dataset.setLastObservation(rawObservation.get());
                    if (data instanceof QuantityObservationEntity) {
                        dataset.setLastQuantityValue(((QuantityObservationEntity) data).getValue());
                    }
                }
                // Update phenomenonTime
                if (dataset.getPhenomenonTimeStart() == null) {
                    dataset.setPhenomenonTimeStart(data.getPhenomenonTimeStart());
                    dataset.setPhenomenonTimeEnd(data.getPhenomenonTimeEnd());
                } else {
                    if (dataset.getPhenomenonTimeStart().after(data.getPhenomenonTimeStart())) {
                        dataset.setPhenomenonTimeStart(data.getPhenomenonTimeStart());
                    }
                    if (dataset.getPhenomenonTimeEnd().before(data.getPhenomenonTimeEnd())) {
                        dataset.setPhenomenonTimeEnd(data.getPhenomenonTimeEnd());
                    }
                }
                // Update aggregation if present
                if (dataset.getAggregation() != null) {
                    //TODO: We might need to fetch the aggregation first!
                    LOGGER.debug("Updating First/Last/Geometry of parent Aggregation: {}",
                                 dataset.getAggregation().getId());
                    updateDataset(dataset.getAggregation(), data);
                }

                return datastreamRepository.save(dataset);
            }
        } else {
            throw new STACRUDException("Could not update Dataset->firstObservation or Dataset->firstObservation. " +
                                               "Unable to find Observation with Id:" + data.getId());
        }
    }

    /*
    private void updateDatastream(AbstractDatastreamEntity datastream, DatasetEntity dataset, I data)
            throws STACRUDException {
        if (datastream.getDatasets() != null) {
            if (!datastream.getDatasets().contains(dataset)) {
                datastream.getDatasets().add(dataset);
                getAbstractDatastreamService(datastream).save(datastream);
            }
        }
        if (datastream.getPhenomenonTimeStart() == null) {
            datastream.setPhenomenonTimeStart(data.getPhenomenonTimeStart());
            datastream.setPhenomenonTimeEnd(data.getPhenomenonTimeEnd());
        } else {
            if (datastream.getPhenomenonTimeStart().after(data.getPhenomenonTimeStart())) {
                datastream.setPhenomenonTimeStart(data.getPhenomenonTimeStart());
            }
            if (datastream.getPhenomenonTimeEnd().before(data.getPhenomenonTimeEnd())) {
                datastream.setPhenomenonTimeEnd(data.getPhenomenonTimeEnd());
            }
        }
    }
    */

    protected abstract I castToConcreteObservationType(AbstractObservationEntity observation,
                                                       AbstractDatasetEntity dataset)
            throws STACRUDException;

    @Override
    public I merge(I existing, I toMerge)
            throws STACRUDException {
        // phenomenonTime
        mergeSamplingTimeAndCheckResultTime(existing, toMerge);
        // resultTime
        if (toMerge.getResultTime() != null) {
            existing.setResultTime(toMerge.getResultTime());
        }
        // validTime
        if (toMerge.isSetValidTime()) {
            existing.setValidTimeStart(toMerge.getValidTimeStart());
            existing.setValidTimeEnd(toMerge.getValidTimeEnd());
        }
        // parameter
        if (toMerge.getParameters() != null) {
            synchronized (getLock(String.valueOf(existing.getParameters().hashCode()))) {
                parameterRepository.saveAll(toMerge.getParameters());
                existing.getParameters().forEach(parameterRepository::delete);
                existing.setParameters(toMerge.getParameters());
            }
        }
        // value
        if (toMerge.getValue() != null) {
            checkValue(existing, toMerge);
        }
        return existing;
    }

    protected void mergeSamplingTimeAndCheckResultTime(I existing, I toMerge) {
        if (toMerge.getSamplingTimeEnd() != null && existing.getSamplingTimeEnd().equals(existing.getResultTime())) {
            existing.setResultTime(toMerge.getSamplingTimeEnd());
        }
        mergeSamplingTime(existing, toMerge);
    }

    private void checkValue(I existing, I toMerge) throws STACRUDException {
        if (existing instanceof QuantityObservationEntity) {
            ((QuantityObservationEntity) existing)
                    .setValue(BigDecimal.valueOf(Double.parseDouble(toMerge.getValue().toString())));
        } else if (existing instanceof CountObservationEntity) {
            ((CountObservationEntity) existing).setValue(Integer.parseInt(toMerge.getValue().toString()));
        } else if (existing instanceof BooleanObservationEntity) {
            ((BooleanObservationEntity) existing).setValue(Boolean.parseBoolean(toMerge.getValue().toString()));
        } else if (existing instanceof TextObservationEntity) {
            ((TextObservationEntity) existing).setValue(toMerge.getValue().toString());
        } else if (existing instanceof CategoryObservationEntity) {
            ((CategoryObservationEntity) existing).setValue(toMerge.getValue().toString());
        } else {
            throw new STACRUDException(
                    String.format("The observation value for @iot.id %s can not be updated!",
                                  existing.getStaIdentifier()),
                    HTTPStatus.CONFLICT);
        }
    }

    protected I fillConcreteObservationType(I data,
                                            AbstractObservationEntity observation,
                                            AbstractDatasetEntity dataset) throws STACRUDException {
        data.setDataset(dataset);
        if (observation.getStaIdentifier() != null) {
            if (getRepository().existsByStaIdentifier(observation.getStaIdentifier())) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            } else {
                data.setIdentifier(observation.getIdentifier());
                data.setStaIdentifier(observation.getStaIdentifier());
            }
        } else {
            String uuid = UUID.randomUUID().toString();
            data.setIdentifier(uuid);
            data.setStaIdentifier(uuid);
        }
        data.setSamplingTimeStart(observation.getSamplingTimeStart());
        data.setSamplingTimeEnd(observation.getSamplingTimeEnd());
        data.setResultTime(observation.getResultTime());
        data.setValidTimeStart(observation.getValidTimeStart());
        data.setValidTimeEnd(observation.getValidTimeEnd());
        data.setSamplingGeometry(observation.getSamplingGeometry());

        if (observation.getParameters() != null) {
            parameterRepository.saveAll(observation.getParameters());
            data.setParameters(observation.getParameters());
        }
        return data;
    }
}
