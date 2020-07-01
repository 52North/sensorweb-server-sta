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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;
import org.n52.series.db.beans.sta.AbstractObservationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.mapped.BooleanObservationEntity;
import org.n52.series.db.beans.sta.mapped.CategoryObservationEntity;
import org.n52.series.db.beans.sta.mapped.CountObservationEntity;
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.series.db.beans.sta.mapped.ObservationEntity;
import org.n52.series.db.beans.sta.mapped.QuantityObservationEntity;
import org.n52.series.db.beans.sta.mapped.TextObservationEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.n52.sta.data.query.DatasetQuerySpecifications;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.EntityQuerySpecifications;
import org.n52.sta.data.repositories.CategoryRepository;
import org.n52.sta.data.repositories.DataRepository;
import org.n52.sta.data.repositories.DatasetRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.GetFirstLastObservation;
import org.n52.sta.data.repositories.OfferingRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class AbstractObservationService<T extends StaIdentifierRepository<I> & GetFirstLastObservation<I>,
        I extends AbstractObservationEntity, O extends I>
        extends AbstractSensorThingsEntityServiceImpl<T, I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationService.class);
    private static final DatasetQuerySpecifications dQS = new DatasetQuerySpecifications();
    private static final DatastreamQuerySpecifications dsQS = new DatastreamQuerySpecifications();
    private static final String STA = "STA";

    protected final boolean isMobileFeatureEnabled;
    protected final DataRepository<DataEntity<?>> dataRepository;
    protected final CategoryRepository categoryRepository;
    protected final OfferingRepository offeringRepository;
    protected final DatastreamRepository datastreamRepository;
    protected final DatasetRepository datasetRepository;
    protected final ParameterRepository parameterRepository;

    private final EntityQuerySpecifications<I> oQS;
    private final Pattern isMobilePattern = Pattern.compile(".*\"isMobile\":true.*");

    public AbstractObservationService(
            T repository,
            Class entityClass,
            EntityQuerySpecifications<I> oQS,
            boolean isMobileFeatureEnabled,
            DataRepository<DataEntity<?>> dataRepository,
            CategoryRepository categoryRepository,
            OfferingRepository offeringRepository,
            DatastreamRepository datastreamRepository,
            DatasetRepository datasetRepository,
            ParameterRepository parameterRepository,
            EntityGraphRepository.FetchGraph... defaultFetchGraphs) {
        super(repository, entityClass, defaultFetchGraphs);
        this.oQS = oQS;
        this.isMobileFeatureEnabled = isMobileFeatureEnabled;
        this.dataRepository = dataRepository;
        this.categoryRepository = categoryRepository;
        this.offeringRepository = offeringRepository;
        this.datastreamRepository = datastreamRepository;
        this.datasetRepository = datasetRepository;
        this.parameterRepository = parameterRepository;
    }

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Specification<I> spec = getFilterPredicate(ObservationEntity.class, queryOptions);
            List<String> identifierList = getRepository()
                    .identifierList(spec,
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

            List<String> identifierList = getRepository().identifierList(spec,
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

    protected Page getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                         String relatedType,
                                                         QueryOptions queryOptions)
            throws STACRUDException {
        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Specification<I> spec =
                    byRelatedEntityFilter(relatedId, relatedType, null)
                            .and(getFilterPredicate(ObservationEntity.class, queryOptions));

            List<String> identifierList = getRepository().identifierList(spec,
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
    public I createEntity(I entity) throws STACRUDException {
        synchronized (getLock(entity.getStaIdentifier())) {
            I observation = entity;
            if (!observation.isProcessed()) {
                observation.setProcessed(true);
                check(observation);

                DatastreamEntity datastream = getDatastreamService().createEntity(observation.getDatastream());
                observation.setDatastream(datastream);

                // Fetch with all needed associations
                datastream = datastreamRepository
                        .findByStaIdentifier(datastream.getStaIdentifier(),
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_THINGLOCATION,
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_PROCEDURE,
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_UOM,
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_OBS_TYPE,
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_OBSERVABLE_PROP,
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS
                        ).orElseThrow(() -> new STACRUDException("Unable to find Datastream!"));

                AbstractFeatureEntity<?> feature = checkFeature(observation, datastream);
                // category (obdProp)
                CategoryEntity category = checkCategory();
                // offering (sensor)
                OfferingEntity offering = checkOffering(datastream);
                // dataset
                DatasetEntity dataset = checkDataset(datastream, feature, category, offering);
                // observation
                I data = checkData(observation, dataset);
                if (data != null) {
                    updateDataset(dataset, data);
                    updateDatastream(datastream, dataset, data);
                }
                return data;
            }
            return observation;
        }
    }

    private void check(AbstractObservationEntity observation) throws STACRUDException {
        if (observation.getDatastream() == null) {
            throw new STACRUDException("The observation to create is invalid. Missing datastream!",
                                       HTTPStatus.BAD_REQUEST);
        }
    }

    /**
     * Handles updating the phenomenonTime field of the associated Datastream when Observation phenomenonTime is
     * updated or deleted
     */
    private void updateDatastreamPhenomenonTimeOnObservationUpdate(
            List<DatastreamEntity> datastreams, I observation) {
        for (DatastreamEntity datastreamEntity : datastreams) {
            if (datastreamEntity.getPhenomenonTimeStart() == null ||
                    datastreamEntity.getPhenomenonTimeEnd() == null ||
                    observation.getPhenomenonTimeStart().compareTo(datastreamEntity.getPhenomenonTimeStart()) != 1 ||
                    observation.getPhenomenonTimeEnd().compareTo(datastreamEntity.getPhenomenonTimeEnd()) != -1
            ) {
                List<Long> datasetIds = datastreamEntity
                        .getDatasets()
                        .stream()
                        .map(datasetEntity -> datasetEntity.getId())
                        .collect(Collectors.toList());
                // Setting new phenomenonTimeStart
                I firstObservation = getRepository()
                        .findFirstByDataset_idInOrderBySamplingTimeStartAsc(datasetIds);
                Date newPhenomenonStart = (firstObservation == null) ? null : firstObservation.getPhenomenonTimeStart();

                // Set Start and End to null if there is no observation.
                if (newPhenomenonStart == null) {
                    datastreamEntity.setPhenomenonTimeStart(null);
                    datastreamEntity.setPhenomenonTimeEnd(null);
                } else {
                    datastreamEntity.setPhenomenonTimeStart(newPhenomenonStart);

                    // Setting new phenomenonTimeEnd
                    I lastObservation = getRepository()
                            .findFirstByDataset_idInOrderBySamplingTimeEndDesc(datasetIds);
                    Date newPhenomenonEnd = (lastObservation == null) ? null : lastObservation.getPhenomenonTimeEnd();
                    if (newPhenomenonEnd != null) {
                        datastreamEntity.setPhenomenonTimeEnd(newPhenomenonEnd);
                    } else {
                        datastreamEntity.setPhenomenonTimeStart(null);
                        datastreamEntity.setPhenomenonTimeEnd(null);
                    }
                }
                datastreamRepository.save(datastreamEntity);
            }
        }
    }

    @Override
    public I updateEntity(String id, I entity, HttpMethod method)
            throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<I> existing =
                        getRepository().findByStaIdentifier(id,
                                                            EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);
                if (existing.isPresent()) {
                    I merged = merge(existing.get(), entity);
                    I saved = getRepository().save(merged);

                    List<DatastreamEntity> datastreamEntity =
                            datastreamRepository.findAll(dsQS.withObservationStaIdentifier(saved.getStaIdentifier()),
                                                         EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS);
                    if (!datastreamEntity.isEmpty()) {
                        updateDatastreamPhenomenonTimeOnObservationUpdate(datastreamEntity, saved);
                    }
                    return saved;
                }
                throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    @Override
    protected I updateEntity(I entity) {
        return getRepository().save(entity);
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
                checkDataset(observation);
                List<DatastreamEntity> datastreamEntity =
                        datastreamRepository.findAll(dsQS.withObservationStaIdentifier(observation.getStaIdentifier()),
                                                     EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS);
                // Important! Delete first and then update else we find
                // ourselves again in search for new latest/earliest obs.
                getRepository().deleteByStaIdentifier(observation.getStaIdentifier());
                if (!datastreamEntity.isEmpty()) {
                    updateDatastreamPhenomenonTimeOnObservationUpdate(datastreamEntity, observation);
                }
            } else {
                throw new STACRUDException("Unable to delete. Entity not found.", HTTPStatus.NOT_FOUND);
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
            return createEntity(entity);
        }
    }

    private void checkDataset(I observation) {
        // TODO get the next first/last observation and set it
        DatasetEntity dataset = observation.getDataset();
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
        observation.setDataset(datasetRepository.saveAndFlush(dataset));
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private DatasetEntity checkDataset(DatastreamEntity datastream,
                                       AbstractFeatureEntity<?> feature,
                                       CategoryEntity category,
                                       OfferingEntity offering) throws STACRUDException {
        DatasetEntity dataset = getDatasetEntity(datastream.getObservationType().getFormat(),
                                                 (isMobileFeatureEnabled
                                                         && datastream.getThing().hasProperties())
                                                         && isMobilePattern.matcher(datastream.getThing()
                                                                                              .getProperties())
                                                                           .matches());
        dataset.setProcedure(datastream.getProcedure());
        dataset.setPhenomenon(datastream.getObservableProperty());
        dataset.setCategory(category);
        dataset.setFeature(feature);
        dataset.setOffering(offering);
        dataset.setPlatform(datastream.getThing());
        dataset.setUnit(datastream.getUnit());
        dataset.setOmObservationType(datastream.getObservationType());
        Specification<DatasetEntity> query =
                dQS.matchProcedureIdentifier(datastream.getProcedure().getIdentifier())
                   .and(dQS.matchPhenomenaIdentifier(datastream.getObservableProperty().getIdentifier())
                           .and(dQS.matchFeatureIdentifier(feature.getIdentifier()))
                           .and(dQS.matchOfferingsIdentifier(offering.getIdentifier()))
                           .and(dQS.matchOmObservationTypeId(datastream.getObservationType().getId())));
        synchronized (getLock(datastream.getStaIdentifier())) {
            Optional<DatasetEntity> queried =
                    datasetRepository.findOne(query,
                                              EntityGraphRepository.FetchGraph.FETCHGRAPH_OM_OBS_TYPE,
                                              EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURE);
            if (queried.isPresent()) {
                LOGGER.debug("Checking Dataset: Found existing dataset");
                return queried.get();
            } else {
                LOGGER.debug("Checking Dataset: Creating new dataset");
                return datasetRepository.save(dataset);
            }
        }
    }

    DatastreamEntity checkDatastream(ObservationEntity observation) throws STACRUDException {
        DatastreamEntity datastream = getDatastreamService().createEntity(observation.getDatastream());
        observation.setDatastream(datastream);
        return datastream;
    }

    private AbstractFeatureEntity<?> checkFeature(AbstractObservationEntity observation, DatastreamEntity datastream)
            throws STACRUDException {
        if (!observation.hasFeature()) {
            AbstractFeatureEntity<?> feature = null;
            for (LocationEntity location : datastream.getThing().getLocations()) {
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
        AbstractFeatureEntity<?> feature = getFeatureOfInterestService()
                .createEntity(observation.getFeature());
        observation.setFeature(feature);
        return feature;
    }

    private OfferingEntity checkOffering(DatastreamEntity datastream) throws STACRUDException {
        ProcedureEntity procedure = datastream.getProcedure();
        synchronized (getLock(procedure.getIdentifier())) {
            if (!offeringRepository.existsByIdentifier(procedure.getIdentifier())) {
                OfferingEntity offering = new OfferingEntity();
                offering.setIdentifier(procedure.getIdentifier());
                offering.setStaIdentifier(procedure.getStaIdentifier());
                offering.setName(procedure.getName());
                offering.setDescription(procedure.getDescription());
                if (datastream.hasSamplingTimeStart()) {
                    offering.setSamplingTimeStart(datastream.getSamplingTimeStart());
                }
                if (datastream.hasSamplingTimeEnd()) {
                    offering.setSamplingTimeEnd(datastream.getSamplingTimeEnd());
                }
                if (datastream.getResultTimeStart() != null) {
                    offering.setResultTimeStart(datastream.getResultTimeStart());
                }
                if (datastream.getResultTimeEnd() != null) {
                    offering.setResultTimeEnd(datastream.getResultTimeEnd());
                }
                if (datastream.isSetGeometry()) {
                    offering.setGeometryEntity(datastream.getGeometryEntity());
                }
                HashSet<FormatEntity> set = new HashSet<>();
                set.add(datastream.getObservationType());
                offering.setObservationTypes(set);
                return offeringRepository.save(offering);
            } else {
                // TODO expand time and geometry if necessary
                return offeringRepository.findByIdentifier(procedure.getIdentifier()).get();
            }
        }
    }

    private CategoryEntity checkCategory() throws STACRUDException {
        synchronized (getLock(STA)) {
            if (!categoryRepository.existsByIdentifier(STA)) {
                CategoryEntity category = new CategoryEntity();
                category.setIdentifier(STA);
                category.setName(STA);
                category.setDescription("Default SOS category");
                return categoryRepository.save(category);
            } else {
                return categoryRepository.findByIdentifier(STA).get();
            }
        }
    }

    private I checkData(AbstractObservationEntity observation, DatasetEntity dataset)
            throws STACRUDException {
        I data = castToConcreteObservationType(observation, dataset);
        if (data != null) {
            return getRepository().save(data);
        }
        return null;
    }

    private DatasetEntity updateDataset(DatasetEntity dataset, I data) throws STACRUDException {
        Optional<DataEntity<?>> rawObservation = dataRepository.findById(data.getId());
        if (rawObservation.isPresent()) {
            synchronized (getLock(dataset.getId().toString() + "Dataset")) {
                if (!dataset.isSetFirstValueAt()
                        || (dataset.isSetFirstValueAt() &&
                        data.getSamplingTimeStart().before(dataset.getFirstValueAt()))) {
                    dataset.setFirstValueAt(data.getSamplingTimeStart());
                    dataset.setFirstObservation(rawObservation.get());
                    if (data instanceof QuantityObservationEntity) {
                        dataset.setFirstQuantityValue(((QuantityObservationEntity) data).getValue());
                    }
                }
                if (!dataset.isSetLastValueAt()
                        || (dataset.isSetLastValueAt() && data.getSamplingTimeEnd().after(dataset.getLastValueAt()))) {
                    dataset.setLastValueAt(data.getSamplingTimeEnd());
                    dataset.setLastObservation(rawObservation.get());
                    if (data instanceof QuantityObservationEntity) {
                        dataset.setLastQuantityValue(((QuantityObservationEntity) data).getValue());
                    }
                }
                return datasetRepository.save(dataset);
            }
        } else {
            throw new STACRUDException("Could not update Dataset->firstObservation or Dataset->firstObservation. " +
                                               "Unable to find Observation with Id:" + data.getId());
        }
    }

    private void updateDatastream(DatastreamEntity datastream, DatasetEntity dataset, I data)
            throws STACRUDException {
        if (datastream.getDatasets() != null) {
            if (!datastream.getDatasets().contains(dataset)) {
                datastream.addDataset(dataset);
                getDatastreamService().updateEntity(datastream);
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

    private DatasetEntity getDatasetEntity(String observationType, boolean isMobile) {
        DatasetEntity dataset = new DatasetEntity().setObservationType(ObservationType.simple);
        if (isMobile) {
            LOGGER.debug("Setting DatasetType to 'trajectory'");
            dataset = dataset.setDatasetType(DatasetType.trajectory);
            dataset.setMobile(true);
        } else {
            dataset = dataset.setDatasetType(DatasetType.timeseries);
        }
        switch (observationType) {
        case OmConstants.OBS_TYPE_MEASUREMENT:
            return dataset.setValueType(ValueType.quantity);
        case OmConstants.OBS_TYPE_CATEGORY_OBSERVATION:
            return dataset.setValueType(ValueType.category);
        case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
            return dataset.setValueType(ValueType.count);
        case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
            return dataset.setValueType(ValueType.text);
        case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
            return dataset.setValueType(ValueType.bool);
        default:
            return dataset;
        }
    }

    protected abstract I castToConcreteObservationType(AbstractObservationEntity observation,
                                                       DatasetEntity dataset)
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
                                            DatasetEntity dataset) throws STACRUDException {
        data.setDataset(dataset);
        if (observation.getStaIdentifier() != null) {
            if (getRepository().existsByStaIdentifier(observation.getStaIdentifier())) {
                throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
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
