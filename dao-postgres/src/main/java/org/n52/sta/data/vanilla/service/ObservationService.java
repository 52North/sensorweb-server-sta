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
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.BooleanDataEntity;
import org.n52.series.db.beans.CategoryDataEntity;
import org.n52.series.db.beans.CompositeDataEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetAggregationEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.parameter.observation.ObservationParameterEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.data.vanilla.OffsetLimitBasedPageRequest;
import org.n52.sta.data.vanilla.query.ObservationQuerySpecifications;
import org.n52.sta.data.vanilla.repositories.DatastreamRepository;
import org.n52.sta.data.vanilla.repositories.EntityGraphRepository;
import org.n52.sta.data.vanilla.repositories.LocationRepository;
import org.n52.sta.data.vanilla.repositories.ObservationParameterRepository;
import org.n52.sta.data.vanilla.repositories.ObservationRepository;
import org.n52.sta.data.vanilla.service.util.FilterExprVisitor;
import org.n52.sta.data.vanilla.service.util.HibernateSpatialCriteriaBuilderImpl;
import org.n52.svalbard.odata.core.expr.Expr;
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
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
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
public class ObservationService
    extends AbstractSensorThingsEntityServiceImpl<ObservationRepository<DataEntity<?>>, ObservationDTO, DataEntity<?>> {

    private static final ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationService.class);
    protected final DatastreamRepository datastreamRepository;
    protected final ObservationParameterRepository parameterRepository;
    private final String OBS_TYPE_SENSORML_OBSERVATION =
        "http://www.52north.org/def/observationType/OGC-OM/2.0/OM_SensorML20Observation";
    private final Class entityClass;

    public ObservationService() {
        super();
        this.entityClass = null;
        this.datastreamRepository = null;
        this.parameterRepository = null;
    }

    @Autowired
    public ObservationService(ObservationRepository<DataEntity<?>> repository,
                              EntityManager em,
                              DatastreamRepository datastreamRepository,
                              ObservationParameterRepository parameterRepository) {
        super(repository, em, DataEntity.class);
        this.entityClass = DataEntity.class;
        this.datastreamRepository = datastreamRepository;
        this.parameterRepository = parameterRepository;
    }

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Specification<DataEntity<?>> spec = getFilterPredicate(DataEntity.class, queryOptions);
            List<String> identifierList = getRepository()
                .getColumnList(spec,
                               pageableRequest,
                               STAIDENTIFIER);
            if (identifierList.isEmpty()) {
                return new CollectionWrapper(-1, Collections.emptyList(), false);
            } else {
                return getEntityCollectionWrapperByIdentifierList(identifierList, pageableRequest, queryOptions, spec);
            }
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }
    /*
    @Override public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                          String relatedType,
                                                                          QueryOptions queryOptions)
        throws STACRUDException {

        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Specification<DataEntity<?>> spec =
                byRelatedEntityFilter(relatedId, relatedType, null)
                    .and(getFilterPredicate(entityClass, queryOptions));

            List<String> identifierList = getRepository().getColumnList(spec,
                                                                        createPageableRequest(queryOptions),
                                                                        STAIDENTIFIER);
            if (identifierList.isEmpty()) {
                return new CollectionWrapper(-1, Collections.emptyList(), false);
            } else {
                return getEntityCollectionWrapperByIdentifierList(identifierList, pageableRequest, queryOptions, spec);
            }
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }
     */

    @Override
    public DataEntity<?> getEntityByIdRaw(Long id, QueryOptions queryOptions) throws STACRUDException {
        DataEntity<?> entity = super.getEntityByIdRaw(id, queryOptions);
        fetchValueIfCompositeDataEntity(entity);
        return entity;
    }

    @Override
    public DataEntity<?> getEntityByRelatedEntityRaw(String relatedId,
                                                     String relatedType,
                                                     String ownId,
                                                     QueryOptions queryOptions) throws STACRUDException {
        DataEntity<?> entity =
            super.getEntityByRelatedEntityRaw(relatedId, relatedType, ownId, queryOptions);
        fetchValueIfCompositeDataEntity(entity);
        return entity;
    }

    public Page getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                      String relatedType,
                                                      QueryOptions queryOptions)
        throws STACRUDException {
        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Specification<DataEntity<?>> spec =
                byRelatedEntityFilter(relatedId, relatedType, null)
                    .and(getFilterPredicate(DataEntity.class, queryOptions));

            List<String> identifierList = getRepository().getColumnList(spec,
                                                                        createPageableRequest(queryOptions),
                                                                        STAIDENTIFIER);
            if (identifierList.isEmpty()) {
                return Page.empty();
            } else {
                Page<DataEntity<?>> pages = getRepository().findAll(
                    oQS.withStaIdentifier(identifierList),
                    new OffsetLimitBasedPageRequest(0,
                                                    pageableRequest.getPageSize(),
                                                    pageableRequest.getSort()),
                    EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);
                pages.forEach(Hibernate::initialize);
                pages.forEach(this::fetchValueIfCompositeDataEntity);
                if (queryOptions.hasExpandFilter()) {
                    return pages
                        .map(e -> (DataEntity) Hibernate.unproxy(e))
                        .map(e -> {
                            try {
                                return fetchExpandEntitiesWithFilter(e, queryOptions.getExpandFilter());
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

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption) {
        return new EntityGraphRepository.FetchGraph[] {
            EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS,
        };
    }

    @Override
    protected DataEntity<?> fetchExpandEntitiesWithFilter(DataEntity<?> returned,
                                                          ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
                case STAEntityDefinition.DATASTREAM:
                    DatasetEntity datastream = (DatasetEntity) getDatastreamService()
                        .getEntityByIdRaw(returned.getDataset().getId(), expandItem.getQueryOptions());
                    returned.setDataset(datastream);
                    break;
                case STAEntityDefinition.FEATURE_OF_INTEREST:
                    AbstractFeatureEntity<?> foi = getFeatureOfInterestService()
                        .getEntityByDatasetIdRaw(returned.getDataset().getId(), expandItem.getQueryOptions());
                    returned.setFeature(foi);
                    break;
                default:
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                     expandProperty,
                                                                     StaConstants.OBSERVATIONS));
            }
        }
        return returned;
    }

    @Override
    public Specification<DataEntity<?>> byRelatedEntityFilter(String relatedId,
                                                              String relatedType,
                                                              String ownId) {
        Specification<DataEntity<?>> filter;
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS: {
                filter = ObservationQuerySpecifications.withDatastreamStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.FEATURES_OF_INTEREST: {
                filter = ObservationQuerySpecifications.withFeatureOfInterestStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }
        if (ownId != null) {
            filter = filter.and(oQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public DataEntity<?> createOrfetch(DataEntity<?> entity) throws STACRUDException {
        synchronized (getLock(entity.getStaIdentifier())) {
            DataEntity<?> observation = entity;
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
                Set<DatasetEntity> datasets;
                if (datastream.getAggregation() == null && !(datastream instanceof DatasetAggregationEntity)) {
                    // We are not an aggregate so there is only one dataset to check for fit
                    datasets = Collections.singleton((DatasetEntity) datastream);
                } else {
                    datasets = datastreamRepository.findAllByAggregationId(datastream.getId())
                        .stream()
                        .map(d -> (DatasetEntity) d)
                        .collect(Collectors.toSet());
                }

                // Check all datasets for a matching FOI
                boolean found = false;
                for (DatasetEntity dataset : datasets) {
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
                DataEntity<?> data = saveObservation(observation, observation.getDataset());

                // Save parameters
                if (observation.getParameters() != null) {
                    parameterRepository.saveAll(
                        observation
                            .getParameters()
                            .stream()
                            .filter(o -> o instanceof ObservationParameterEntity)
                            .map(o -> {
                                ((ObservationParameterEntity<?>) o).setObservation(data);
                                return (ObservationParameterEntity) o;
                            })
                            .collect(Collectors.toSet())
                    );
                    data.setParameters(observation.getParameters());
                }

                // Update FirstValue/LastValue + FirstObservation/LastObservation of Dataset + Aggregation
                updateDataset(observation.getDataset(), data);
                return data;
            }
            return observation;
        }
    }

    @Override
    public DataEntity<?> updateEntity(String id, DataEntity<?> entity, HttpMethod method)
        throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<DataEntity<?>> existing =
                    getRepository()
                        .findByStaIdentifier(id,
                                             EntityGraphRepository
                                                 .FetchGraph
                                                 .FETCHGRAPH_PARAMETERS);
                if (existing.isPresent()) {
                    DataEntity<?> merged = merge(existing.get(), entity);
                    DataEntity<?> saved = getRepository().save(merged);

                    AbstractDatasetEntity datastreamEntity =
                        datastreamRepository.findById(saved.getDataset().getId()).get();

                    updateDatastreamPhenomenonTimeOnObservationUpdate(datastreamEntity, saved);
                    Hibernate.initialize(saved.getParameters());
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
    public DataEntity<?> createOrUpdate(DataEntity<?> entity) throws STACRUDException {
        synchronized (getLock(entity.getStaIdentifier())) {
            if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
                return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
            }
            return createOrfetch(entity);
        }
    }

    @Override
    public Specification<DataEntity<?>> getFilterPredicate(Class entityClass, QueryOptions queryOptions) {
        return (root, query, builder) -> {
            Predicate defaultFilter = builder.isNull(root.get(DataEntity.PROPERTY_PARENT));
            if (!queryOptions.hasFilterFilter()) {
                // Filter out non-root observations
                // e.g. Profile-/TrajectoryObservations
                return defaultFilter;
            } else {
                FilterFilter filterOption = queryOptions.getFilterFilter();
                Expr filter = (Expr) filterOption.getFilter();
                try {
                    HibernateSpatialCriteriaBuilderImpl staBuilder =
                        new HibernateSpatialCriteriaBuilderImpl((CriteriaBuilderImpl) builder);
                    return builder.and(defaultFilter,
                                       (Predicate) filter.accept(
                                           new FilterExprVisitor<DataEntity<?>>(root, query, staBuilder)));
                } catch (STAInvalidQueryException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public String checkPropertyName(String property) {
        return oQS.checkPropertyName(property);
    }

    @Override
    public DataEntity<?> merge(DataEntity<?> existing, DataEntity<?> toMerge)
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
            synchronized (getLock(String.valueOf(
                toMerge.getParameters().hashCode() + existing.getParameters().hashCode()))) {
                parameterRepository.saveAll(toMerge.getParameters()
                                                .stream()
                                                .filter(o -> o instanceof ObservationParameterEntity)
                                                .map(o -> {
                                                    ((ObservationParameterEntity<?>) o).setObservation(existing);
                                                    return (ObservationParameterEntity) o;
                                                })
                                                .collect(Collectors.toSet()));
                existing.getParameters()
                    .stream()
                    .filter(o -> o instanceof ObservationParameterEntity)
                    .map(o -> (ObservationParameterEntity) o)
                    .forEach(parameterRepository::delete);
                existing.setParameters(toMerge.getParameters());
            }
        }
        // value
        if (toMerge.getValueText() != null) {
            checkValue(existing, toMerge);
        }
        return existing;
    }

    @Override
    public void delete(String identifier) throws STACRUDException {
        synchronized (getLock(identifier)) {
            if (getRepository().existsByStaIdentifier(identifier)) {
                DataEntity<?> observation =
                    getRepository().findByStaIdentifier(
                        identifier,
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASET_FIRSTLAST_OBSERVATION)
                        .get();
                deleteReferenceFromDatasetFirstLast(observation);

                if (observation.hasParameters()) {
                    observation.getParameters()
                        .forEach(entity -> parameterRepository.delete((ObservationParameterEntity) entity));
                }
                // Important! Delete first and then update else we find
                // ourselves again in search for new latest/earliest obs.
                getRepository().deleteByStaIdentifier(observation.getStaIdentifier());
                updateDatastreamPhenomenonTimeOnObservationUpdate(observation.getDataset(), observation);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    /**
     * We touch DataEntity->value here to make sure it is fetched from the database and not lazy-loaded.
     * We cannot fetch it ourselves as any assignment to entity->value will update the database (issue delete+insert
     * statemenets).
     *
     * @param entity to be loaded
     * @return entity with not-lazy-loaded value and value->parameters
     */
    private DataEntity<?> fetchValueIfCompositeDataEntity(DataEntity<?> entity) {
        // We need to unproxy first to be able to check the type & access properties
        // As we unproxy later anyway this is no overhead.
        DataEntity<?> unproxy = (DataEntity<?>) Hibernate.unproxy(entity);
        if (unproxy instanceof CompositeDataEntity) {
            // touch each entity + it's parameters to make sure they are loaded
            Hibernate.initialize(unproxy.getValue());
            Hibernate.initialize(unproxy.getParameters());
        }
        return unproxy;
    }

    protected DataEntity castToConcreteObservationType(DataEntity<?> observation,
                                                       DatasetEntity dataset)
        throws STACRUDException {
        DataEntity data = null;
        Object value = observation.getValue();
        switch (dataset.getOMObservationType().getFormat()) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                QuantityDataEntity quantityObservationEntity = new QuantityDataEntity();
                if (value.equals("NaN") || value.equals("Inf") || value.equals("-Inf")) {
                    quantityObservationEntity.setValue(null);
                } else {
                    quantityObservationEntity.setValue(BigDecimal.valueOf(Double.parseDouble((String) value)));
                }
                data = quantityObservationEntity;
                break;
            case OmConstants.OBS_TYPE_CATEGORY_OBSERVATION:
                CategoryDataEntity categoryObservationEntity = new CategoryDataEntity();
                categoryObservationEntity.setValue((String) value);
                data = categoryObservationEntity;
                break;
            case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
                CountDataEntity countObservationEntity = new CountDataEntity();
                countObservationEntity.setValue(Integer.parseInt((String) value));
                data = countObservationEntity;
                break;
            case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
                TextDataEntity textObservationEntity = new TextDataEntity();
                textObservationEntity.setValue((String) value);
                data = textObservationEntity;
                break;
            case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
                BooleanDataEntity booleanObservationEntity = new BooleanDataEntity();
                booleanObservationEntity.setValue(Boolean.parseBoolean((String) value));
                data = booleanObservationEntity;
                break;
            //            case OBS_TYPE_SENSORML_OBSERVATION:
            //                SensorML20DataEntity sensorML20DataEntity = new SensorML20DataEntity();
            //                sensorML20DataEntity.setValue((String) value);
            //                data = sensorML20DataEntity;
            //                break;
            default:
                throw new STACRUDException(
                    "Unable to handle OMObservation with type: " + dataset.getOMObservationType().getFormat());
        }
        return fillConcreteObservationType(data, observation, dataset);
    }

    private CollectionWrapper getEntityCollectionWrapperByIdentifierList(List<String> identifierList,
                                                                         OffsetLimitBasedPageRequest pageableRequest,
                                                                         QueryOptions queryOptions,
                                                                         Specification<DataEntity<?>> spec) {
        Page<DataEntity<?>> pages = getRepository()
            .findAll(
                oQS.withStaIdentifier(identifierList),
                new OffsetLimitBasedPageRequest(0,
                                                pageableRequest.getPageSize(),
                                                pageableRequest.getSort()),
                EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS)
            .map(this::fetchValueIfCompositeDataEntity);

        CollectionWrapper wrapper = createCollectionWrapperAndExpand(queryOptions, pages);
        // Create Page manually as we used Database Pagination and are not sure how many Entities there are in
        // the Database
        if (pages.isEmpty()) {
            return wrapper;
        } else {
            long count = -1;
            boolean hasNextPage = false;
            if (queryOptions.hasCountFilter() && queryOptions.getCountFilter().getValue()) {
                count = getRepository().count(spec);
                // we can calculate whether there is an additional page
                hasNextPage = identifierList.size() + pageableRequest.getOffset() < count;
            } else {
                // we presume there is a next page if this page is filled completely.
                // In the case that the entity count is divided by the page size directly this nextpage is empty
                hasNextPage = identifierList.size() == pageableRequest.getPageSize();
            }
            return new CollectionWrapper(count,
                                         wrapper.getEntities(),
                                         hasNextPage);
        }
    }

    private void check(DataEntity<?> observation) throws STACRUDException {
        if (observation.getDataset() == null) {
            throw new STACRUDException("The observation to create is invalid. Missing datastream!",
                                       HTTPStatus.BAD_REQUEST);
        }
    }

    /**
     * Handles updating the phenomenonTime field of the associated Datastream when Observation phenomenonTime is
     * updated or deleted
     *
     * @param datastreamEntity DatstreamEntity
     * @param observation      ObservationEntity
     */
    protected void updateDatastreamPhenomenonTimeOnObservationUpdate(AbstractDatasetEntity datastreamEntity,
                                                                     DataEntity<?> observation) {
        if (datastreamEntity.getPhenomenonTimeStart() == null ||
            datastreamEntity.getPhenomenonTimeEnd() == null ||
            observation.getPhenomenonTimeStart().compareTo(datastreamEntity.getPhenomenonTimeStart()) != 1 ||
            observation.getPhenomenonTimeEnd().compareTo(datastreamEntity.getPhenomenonTimeEnd()) != -1
        ) {
            // Setting new phenomenonTimeStart
            DataEntity<?> firstObservation = getRepository()
                .findFirstByDataset_idOrderBySamplingTimeStartAsc(datastreamEntity.getId());
            Date newPhenomenonStart = (firstObservation == null) ? null : firstObservation.getPhenomenonTimeStart();

            // Set Start and End to null if there is no observation.
            if (newPhenomenonStart == null) {
                datastreamEntity.setPhenomenonTimeStart(null);
                datastreamEntity.setPhenomenonTimeEnd(null);
            } else {
                datastreamEntity.setPhenomenonTimeStart(newPhenomenonStart);

                // Setting new phenomenonTimeEnd
                DataEntity<?> lastObservation = getRepository()
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

    private void deleteReferenceFromDatasetFirstLast(DataEntity<?> observation) {
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
        observation.setDataset(datastreamRepository.saveAndFlush(dataset));
    }

    private AbstractFeatureEntity<?> createOrfetchFeature(DataEntity observation,
                                                          Long thingId)
        throws STACRUDException {
        // Create feature based on Thing.location if there is no feature given
        if (!observation.hasFeature()) {
            AbstractFeatureEntity<?> feature = null;
            LocationRepository locationRepository = getLocationService().getRepository();
            Set<LocationEntity> locations = locationRepository.findAllByPlatformsIdEquals(thingId);
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

    private DataEntity<?> saveObservation(DataEntity<?> observation, DatasetEntity dataset)
        throws STACRUDException {
        DataEntity<?> data = castToConcreteObservationType(observation, dataset);
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
    private AbstractDatasetEntity updateDataset(AbstractDatasetEntity dataset, DataEntity<?> data)
        throws STACRUDException {
        Optional<DataEntity<?>> rawObservation = getRepository().findById(data.getId());
        if (rawObservation.isPresent()) {
            synchronized (getLock(dataset.getId().toString() + "Dataset")) {
                LOGGER.debug("Updating First/Last/Geometry of of Dataset: {}", dataset.getId());
                if (!dataset.isSetFirstValueAt()
                    || (dataset.isSetFirstValueAt()
                    && data.getSamplingTimeStart().before(dataset.getFirstValueAt()))) {
                    dataset.setFirstValueAt(data.getSamplingTimeStart());
                    dataset.setFirstObservation(rawObservation.get());
                    if (data instanceof QuantityDataEntity) {
                        dataset.setFirstQuantityValue(((QuantityDataEntity) data).getValue());
                    }
                }
                if (!dataset.isSetLastValueAt()
                    || (dataset.isSetLastValueAt()
                    && data.getSamplingTimeEnd().after(dataset.getLastValueAt()))) {
                    dataset.setLastValueAt(data.getSamplingTimeEnd());
                    dataset.setLastObservation(rawObservation.get());
                    if (data instanceof QuantityDataEntity) {
                        dataset.setLastQuantityValue(((QuantityDataEntity) data).getValue());
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

    protected void mergeSamplingTimeAndCheckResultTime(DataEntity<?> existing, DataEntity<?> toMerge) {
        if (toMerge.getSamplingTimeEnd() != null && existing.getSamplingTimeEnd().equals(existing.getResultTime())) {
            existing.setResultTime(toMerge.getSamplingTimeEnd());
        }
        mergeSamplingTime(existing, toMerge);
    }

    private void checkValue(DataEntity<?> existing, DataEntity<?> toMerge) throws STACRUDException {
        if (existing instanceof QuantityDataEntity) {
            ((QuantityDataEntity) existing)
                .setValue(BigDecimal.valueOf(Double.parseDouble(toMerge.getValue().toString())));
        } else if (existing instanceof CountDataEntity) {
            ((CountDataEntity) existing).setValue(Integer.parseInt(toMerge.getValue().toString()));
        } else if (existing instanceof BooleanDataEntity) {
            ((BooleanDataEntity) existing).setValue(Boolean.parseBoolean(toMerge.getValue().toString()));
        } else if (existing instanceof TextDataEntity) {
            ((TextDataEntity) existing).setValue(toMerge.getValue().toString());
        } else if (existing instanceof CategoryDataEntity) {
            ((CategoryDataEntity) existing).setValue(toMerge.getValue().toString());
        } else {
            throw new STACRUDException(
                String.format("The observation value for @iot.id %s can not be updated!",
                              existing.getStaIdentifier()),
                HTTPStatus.CONFLICT);
        }
    }

    protected DataEntity<?> fillConcreteObservationType(DataEntity<?> data,
                                                        DataEntity<?> observation,
                                                        DatasetEntity dataset) throws STACRUDException {
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
        data.setGeometryEntity(observation.getGeometryEntity());
        data.setVerticalFrom(observation.getVerticalFrom());
        data.setVerticalTo(observation.getVerticalTo());
        return data;
    }
}
