/*
 * Copyright (C) 2018-2023 52°North Initiative for Geospatial Open Source
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;

import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.Hibernate;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetAggregationEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;
import org.n52.series.db.beans.parameter.BooleanParameterEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.filter.OrderProperty;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterConstants.SortOrder;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.repositories.CategoryRepository;
import org.n52.sta.data.repositories.DatastreamParameterRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.ObservationRepository;
import org.n52.sta.data.repositories.UnitRepository;
import org.n52.sta.data.service.util.FilterExprVisitor;
import org.n52.sta.data.service.util.HibernateSpatialCriteriaBuilderImpl;
import org.n52.svalbard.odata.core.expr.Expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({ "springApplicationContext", "datastreamRepository" })
@Transactional
@SuppressFBWarnings({ "EI_EXPOSE_REP2" })
public class DatastreamService
        extends AbstractSensorThingsEntityServiceImpl<DatastreamRepository, AbstractDatasetEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamService.class);
    private static final DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    private static final String UNKNOWN = "unknown";

    private final boolean isMobileFeatureEnabled;
    private final boolean includeDatastreamCategory;

    private final UnitRepository unitRepository;
    private final CategoryRepository categoryRepository;
    private final ObservationRepository observationRepository;
    private final DatastreamParameterRepository parameterRepository;

    private final OfferingService offeringService;
    private final FormatService formatService;

    @Autowired
    public DatastreamService(DatastreamRepository repository,
            @Value("${server.feature.isMobile:false}") boolean isMobileFeatureEnabled,
            @Value("${server.feature.includeDatastreamCategory:false}") boolean includeDatastreamCategory,
            UnitRepository unitRepository, CategoryRepository categoryRepository,
            ObservationRepository observationRepository, DatastreamParameterRepository parameterRepository,
            OfferingService offeringService, FormatService formatService, EntityManager em) {
        super(repository, em, AbstractDatasetEntity.class);
        this.isMobileFeatureEnabled = isMobileFeatureEnabled;
        this.includeDatastreamCategory = includeDatastreamCategory;

        this.unitRepository = unitRepository;
        this.observationRepository = observationRepository;
        this.parameterRepository = parameterRepository;
        this.formatService = formatService;
        this.offeringService = offeringService;
        this.categoryRepository = categoryRepository;
    }

    @Override
    protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
            throws STAInvalidQueryException {
        Set<EntityGraphRepository.FetchGraph> fetchGraphs = new HashSet<>();
        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_OM_OBS_TYPE);
        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_UOM);
        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);
        if (includeDatastreamCategory) {
            fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_CATEGORY);
        }
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.SENSOR:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_PROCEDURE);
                        break;
                    case STAEntityDefinition.THING:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_PLATFORM);
                        break;
                    case STAEntityDefinition.OBSERVED_PROPERTY:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_PHENOMENON);
                        break;
                    case STAEntityDefinition.OBSERVATIONS:
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                expandProperty, StaConstants.DATASTREAM));
                }
            }
        }
        return fetchGraphs.toArray(new EntityGraphRepository.FetchGraph[0]);
    }

    @Override
    protected AbstractDatasetEntity fetchExpandEntitiesWithFilter(AbstractDatasetEntity entity,
            ExpandFilter expandOption) throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            // We have already handled $expand without filter and expand
            // Except for $expand on Observations
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())
                    && !expandProperty.equals(STAEntityDefinition.OBSERVATIONS)) {
                continue;
            }
            switch (expandProperty) {
                case STAEntityDefinition.SENSOR:
                    entity.setProcedure(getSensorService().getEntityByIdRaw(entity.getProcedure().getId(),
                            expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.THING:
                    entity.setPlatform(getThingService().getEntityByIdRaw(entity.getPlatform().getId(),
                            expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.OBSERVED_PROPERTY:
                    entity.setObservableProperty(getObservedPropertyService()
                            .getEntityByIdRaw(entity.getObservableProperty().getId(), expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.OBSERVATIONS:
                    // Optimize Request when only First/Last Observation is
                    // requested as we have already fetched that.
                    if (checkForFirstLastObservation(expandItem)) {
                        if (checkForFirstObservation(expandItem) && entity.getFirstObservation() != null) {
                            DataEntity<?> firstObservation = entity.getFirstObservation();
                            // make sure parameters are initialized
                            Hibernate.initialize(firstObservation.getParameters());
                            entity.setObservations(Collections.singleton(firstObservation));
                            break;
                        } else if (checkForLastObservation(expandItem) && entity.getLastObservation() != null) {
                            DataEntity<?> lastObservation = entity.getLastObservation();
                            // make sure parameters are initialized
                            Hibernate.initialize(lastObservation.getParameters());
                            entity.setObservations(Sets.newHashSet(Collections.singleton(lastObservation)));
                            break;
                        }
                    }
                    Page<DataEntity<?>> observations = getObservationService().getEntityCollectionByRelatedEntityRaw(
                            entity.getStaIdentifier(), STAEntityDefinition.DATASTREAMS, expandItem.getQueryOptions());
                    entity.setObservations(observations.get().collect(Collectors.toSet()));
                    break;
                default:
                    throw new STAInvalidQueryException(
                            String.format(INVALID_EXPAND_OPTION_SUPPLIED, expandProperty, StaConstants.DATASTREAM));
            }
        }
        return entity;
    }

    @Override
    protected Specification<AbstractDatasetEntity> byRelatedEntityFilter(String relatedId, String relatedType,
            String ownId) {
        Specification<AbstractDatasetEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.THINGS: {
                filter = dQS.withThingStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.SENSORS: {
                filter = dQS.withSensorStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.OBSERVED_PROPERTIES: {
                filter = dQS.withObservedPropertyStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.OBSERVATIONS: {
                filter = dQS.withObservationStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(dQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public AbstractDatasetEntity createOrfetch(AbstractDatasetEntity datastream) throws STACRUDException {
        AbstractDatasetEntity entity = datastream;
        if (!datastream.isProcessed()) {
            // Getting by reference
            if (datastream.getStaIdentifier() != null && !datastream.isSetName()) {
                Optional<AbstractDatasetEntity> optionalEntity =
                        getRepository().findOne(dQS.withStaIdentifier(datastream.getStaIdentifier()));
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException(
                            "No Datastream with id '" + datastream.getStaIdentifier() + "' " + "found");
                }
            }
            check(datastream);
            if (datastream.getStaIdentifier() == null) {
                String uuid = UUID.randomUUID().toString();
                datastream.setIdentifier(uuid);
                datastream.setStaIdentifier(uuid);
            }
            synchronized (getLock(datastream.getStaIdentifier())) {
                if (getRepository().existsByStaIdentifier(datastream.getStaIdentifier())) {
                    throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
                }
                datastream.setProcessed(true);
                datastream.setOMObservationType(formatService.createOrFetchFormat(datastream.getOMObservationType()));
                createOrfetchUnit(datastream);
                datastream.setObservableProperty(
                        getObservedPropertyService().createOrfetch(datastream.getObservableProperty()));
                datastream.setProcedure(getSensorService().createOrfetch(datastream.getProcedure()));
                datastream.setPlatform(getThingService().createOrfetch(datastream.getPlatform()));

                DatasetEntity dataset = createandSaveDataset(datastream, null, datastream.getStaIdentifier());
                processObservation(dataset, entity.getObservations());
            }
            return getRepository()
                    .findByStaIdentifier(entity.getStaIdentifier(), EntityGraphRepository.FetchGraph.FETCHGRAPH_UOM,
                            EntityGraphRepository.FetchGraph.FETCHGRAPH_OM_OBS_TYPE)
                    .orElseThrow(() -> new STACRUDException("Datastream requested but still " + "processing!"));
        } else {
            return entity;
        }
    }

    @Override
    public AbstractDatasetEntity updateEntity(String id, AbstractDatasetEntity entity, HttpMethod method)
            throws STACRUDException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<AbstractDatasetEntity> existing = getRepository().findOne(dQS.withStaIdentifier(id),
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_UOM,
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_OM_OBS_TYPE);
                if (existing.isPresent()) {
                    AbstractDatasetEntity merged = merge(existing.get(), entity);
                    createOrfetchUnit(merged, entity);
                    AbstractDatasetEntity result = getRepository().save(merged);
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
    protected AbstractDatasetEntity createOrUpdate(AbstractDatasetEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    /**
     * Constructs FilterPredicate based on given queryOptions. Additionally
     * filters out Datasets that are aggregated into DatasetAggregations.
     *
     * @param entityClass
     *            Class of the requested Entity
     * @param queryOptions
     *            QueryOptions Object
     * @return Predicate based on FilterOption from queryOptions
     */
    public Specification<AbstractDatasetEntity> getFilterPredicate(Class entityClass, QueryOptions queryOptions) {
        return (root, query, builder) -> {
            Predicate isNotAggregated = builder.isNull(root.get(AbstractDatasetEntity.PROPERTY_AGGREGATION));
            if (!queryOptions.hasFilterFilter()) {
                return isNotAggregated;
            } else {
                FilterFilter filterOption = queryOptions.getFilterFilter();
                Expr filter = (Expr) filterOption.getFilter();
                try {
                    HibernateSpatialCriteriaBuilderImpl staBuilder =
                            new HibernateSpatialCriteriaBuilderImpl((CriteriaBuilderImpl) builder);
                    return builder.and(isNotAggregated, (Predicate) filter
                            .accept(new FilterExprVisitor<AbstractDatasetEntity>(root, query, staBuilder)));
                } catch (STAInvalidQueryException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public String checkPropertyName(String property) {
        return dQS.checkPropertyName(property);
    }

    @Override
    public AbstractDatasetEntity merge(AbstractDatasetEntity existing, AbstractDatasetEntity toMerge)
            throws STACRUDException {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        checkObservationType(existing, toMerge);
        // observedArea
        if (toMerge.isSetGeometry()) {
            existing.setGeometryEntity(toMerge.getGeometryEntity());
        }
        // unit
        if (toMerge.isSetUnit() && existing.getUnit().getSymbol().equals(toMerge.getUnit().getSymbol())) {
            existing.setUnit(toMerge.getUnit());
        }

        // resultTime
        if (toMerge.hasResultTimeStart() && toMerge.hasResultTimeEnd()) {
            existing.setResultTimeStart(toMerge.getResultTimeStart());
            existing.setResultTimeEnd(toMerge.getResultTimeEnd());
        }

        // observationType
        if (toMerge.isSetOMObservationType()
                && !existing.getOMObservationType().getFormat().equals(toMerge.getOMObservationType().getFormat())
                && !toMerge.getOMObservationType().getFormat().equalsIgnoreCase(UNKNOWN)) {
            existing.setOMObservationType(toMerge.getOMObservationType());
        }
        if (toMerge.hasParameters()) {
            existing.getParameters().clear();
            toMerge.getParameters().forEach(p -> {
                p.setDescribeableEntity(existing);
                existing.addParameter(p);
            });
        }
        return existing;
    }

    /*
     * private Specification<AbstractDatasetEntity>
     * createQuery(AbstractDatasetEntity datastream) {
     * Specification<AbstractDatasetEntity> expression; if
     * (datastream.getThing().getStaIdentifier() != null &&
     * !datastream.getThing().isSetName()) { expression =
     * dQS.withThingStaIdentifier(datastream.getThing().getStaIdentifier()); }
     * else { expression = dQS.withThingName(datastream.getThing().getName()); }
     * if (datastream.getProcedure().getStaIdentifier() != null &&
     * !datastream.getProcedure().isSetName()) { expression =
     * expression.and(dQS.withSensorStaIdentifier(datastream.getProcedure().
     * getStaIdentifier())); } else { expression =
     * expression.and(dQS.withSensorName(datastream.getProcedure().getName()));
     * } if (datastream.getObservableProperty().getStaIdentifier() != null &&
     * !datastream.getObservableProperty() .isSetName()) { expression =
     * expression.and(dQS.withObservedPropertyStaIdentifier(datastream.
     * getObservableProperty() .getStaIdentifier())); } else { expression =
     * expression.and(dQS.withObservedPropertyName(datastream.
     * getObservableProperty().getName())); } return expression; }
     */

    /**
     * Creates a DatasetAggregation or expands the existing Aggregation with a
     * new dataset.
     *
     * @param datastream
     *            Existing Aggregation or Dataset
     * @param feature
     *            Feature to be used for the new Dataset
     * @return specific Dataset that was created (not the aggregation)
     * @throws STACRUDException
     *             if an error occurred
     */
    DatasetEntity createOrExpandAggregation(AbstractDatasetEntity datastream, AbstractFeatureEntity<?> feature)
            throws STACRUDException {
        if (datastream.getAggregation() == null && !(datastream instanceof DatasetAggregationEntity)) {
            LOGGER.debug("Creating new DatasetAggregation");
            // We need to create a new aggregation and link the existing
            // datastream with it

            DatasetAggregationEntity parent = new DatasetAggregationEntity();
            parent.copy(datastream);
            parent.setIdentifier(UUID.randomUUID().toString());
            parent.setFeature(null);

            // Free up staIdentifier
            datastream.setStaIdentifier(null);
            getRepository().saveAndFlush(datastream);

            // Persist parent
            AbstractDatasetEntity aggregation = getRepository().intermediateSave(parent);

            // update existing datastream with new parent
            datastream.setAggregation(aggregation);
            getRepository().intermediateSave(datastream);
            return createandSaveDataset(parent, feature, null);
        } else {
            return createandSaveDataset(datastream, feature, null);
        }

        // We need to create a new dataset
        // datastream.setIdentifier(UUID.randomUUID().toString());
        // datastream.setStaIdentifier(null);
    }

    private DatasetEntity createandSaveDataset(AbstractDatasetEntity datastream, AbstractFeatureEntity<?> feature,
            String staIdentifier) throws STACRUDException {
        CategoryEntity category = categoryRepository.findByIdentifier(CategoryService.DEFAULT_CATEGORY)
                .orElseThrow(() -> new STACRUDException("Could not find default SOS Category!"));
        OfferingEntity offering = offeringService.createOrFetchOffering(datastream.getProcedure());
        DatasetEntity dataset = createDatasetSkeleton(datastream.getOMObservationType().getFormat(),
                isMobileFeatureEnabled && datastream.getPlatform().hasParameters()
                        && datastream.getPlatform().getParameters().stream()
                                .filter(p -> p instanceof BooleanParameterEntity)
                                .filter(p -> p.getName().equals("isMobile"))
                                .anyMatch(p -> ((ParameterEntity<Boolean>) p).getValue()));
        dataset.setIdentifier(UUID.randomUUID().toString());
        dataset.setStaIdentifier(staIdentifier);
        dataset.setName(datastream.getName());
        dataset.setDescription(datastream.getDescription());
        dataset.setProcedure(datastream.getProcedure());
        dataset.setPhenomenon(datastream.getObservableProperty());
        dataset.setCategory(category);
        dataset.setFeature(feature);
        dataset.setProcedure(datastream.getProcedure());
        dataset.setOffering(offering);
        dataset.setPlatform(datastream.getPlatform());
        dataset.setUnit(datastream.getUnit());
        dataset.setOMObservationType(datastream.getOMObservationType());
        // dataset.setParameters(datastream.getParameters());
        if (datastream.getId() != null) {
            dataset.setAggregation(datastream);
        }
        return getRepository().save(dataset);
    }

    private DatasetEntity createDatasetSkeleton(String observationType, boolean isMobile) {
        DatasetEntity dataset = new DatasetEntity();
        dataset.setObservationType(ObservationType.simple);
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

    private void check(AbstractDatasetEntity datastream) throws STACRUDException {
        if (datastream.getPlatform() == null || datastream.getObservableProperty() == null
                || datastream.getProcedure() == null) {
            throw new STACRUDException("The datastream to create is invalid", HTTPStatus.BAD_REQUEST);
        }
    }

    private void checkUpdate(AbstractDatasetEntity entity) throws STACRUDException {
        String ERROR_MSG = "Inlined entities are not allowed for updates!";
        if (entity.getObservableProperty() != null && (entity.getObservableProperty().getIdentifier() == null
                || entity.getObservableProperty().isSetName() || entity.getObservableProperty().isSetDescription())) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }

        if (entity.getProcedure() != null && (entity.getProcedure().getStaIdentifier() == null
                || entity.getProcedure().isSetName() || entity.getProcedure().isSetDescription())) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }

        if (entity.getPlatform() != null && (entity.getPlatform().getStaIdentifier() == null
                || entity.getPlatform().isSetName() || entity.getPlatform().isSetDescription())) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }
        if (entity.getObservations() != null) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }
    }

    @Override
    public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                AbstractDatasetEntity datastream = getRepository().findByStaIdentifier(id).get();

                // Delete first/last to be able to delete observations
                datastream.setFirstObservation(null);
                datastream.setLastObservation(null);

                // Delete subdatasets if we are aggregation
                if (datastream instanceof DatasetAggregationEntity) {
                    Set<AbstractDatasetEntity> allByAggregationId =
                            getRepository().findAllByAggregationId(datastream.getId());
                    Set<Long> datasetIds =
                            allByAggregationId.stream().map(IdEntity::getId).collect(Collectors.toSet());
                    allByAggregationId.forEach(dataset -> {
                        dataset.setFirstObservation(null);
                        dataset.setLastObservation(null);
                    });

                    // Flush to disk
                    getRepository().saveAll(allByAggregationId);
                    getRepository().flush();
                    // delete observations
                    observationRepository.deleteAllByDatasetIdIn(datasetIds);
                    // delete subdatastreams
                    datasetIds.forEach(datasetId -> getRepository().deleteById(datasetId));
                } else {
                    // delete observations
                    observationRepository.deleteAllByDatasetIdIn(Collections.singleton(datastream.getId()));
                }

                // delete main datastream
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    private void createOrfetchUnit(AbstractDatasetEntity datastream) throws STACRUDException {
        UnitEntity unit;
        if (datastream.isSetUnit()) {
            synchronized (getLock(datastream.getUnit().getSymbol() + "unit")) {
                if (!unitRepository.existsBySymbol(datastream.getUnit().getSymbol())) {
                    unit = unitRepository.save(datastream.getUnit());
                } else {
                    unit = unitRepository.findBySymbol(datastream.getUnit().getSymbol());
                }
                datastream.setUnit(unit);
            }
        }
    }

    private void createOrfetchUnit(AbstractDatasetEntity merged, AbstractDatasetEntity toMerge)
            throws STACRUDException {
        if (toMerge.isSetUnit()) {
            createOrfetchUnit(toMerge);
            merged.setUnit(toMerge.getUnit());
        }
    }

    private void checkObservationType(AbstractDatasetEntity existing, AbstractDatasetEntity toMerge)
            throws STACRUDException {
        if (toMerge.isSetOMObservationType() && !toMerge.getOMObservationType().getFormat().equalsIgnoreCase(UNKNOWN)
                && !existing.getOMObservationType().getFormat().equals(toMerge.getOMObservationType().getFormat())) {
            throw new STACRUDException(
                    String.format(
                            "The updated observationType (%s) does not comply with the existing observationType (%s)",
                            toMerge.getOMObservationType().getFormat(), existing.getOMObservationType().getFormat()),
                    HTTPStatus.CONFLICT);
        }
    }

    private AbstractDatasetEntity processObservation(DatasetEntity datastream, Set<DataEntity<?>> observations)
            throws STACRUDException {
        if (observations != null && !observations.isEmpty()) {
            for (DataEntity<?> observation : observations) {
                getObservationService().createOrfetch(observation);
            }
        }
        return datastream;
    }

    private boolean checkForFirstLastObservation(ExpandItem expandItem) {
        return expandItem.getQueryOptions().hasTopFilter()
                && expandItem.getQueryOptions().getTopFilter().getValue() == 1
                && expandItem.getQueryOptions().hasOrderByFilter() && checkPhenomenonTime(expandItem);
    }

    private boolean checkPhenomenonTime(ExpandItem expandItem) {
        return expandItem.getQueryOptions().getOrderByFilter().getSortProperties().stream()
                .filter(p -> p.getValueReference().equals(StaConstants.PROP_PHENOMENON_TIME)).findAny().isPresent();
    }

    private SortOrder getSortOrder(ExpandItem expandItem) {
        for (OrderProperty orderProperty : expandItem.getQueryOptions().getOrderByFilter().getSortProperties()) {
            if (orderProperty.getValueReference().equals(StaConstants.PROP_PHENOMENON_TIME)) {
                return orderProperty.getSortOrder();
            }
        }
        return null;
    }

    private boolean checkForFirstObservation(ExpandItem expandItem) {
        return checkSortOrder(getSortOrder(expandItem), SortOrder.ASC);
    }

    private boolean checkForLastObservation(ExpandItem expandItem) {
        return checkSortOrder(getSortOrder(expandItem), SortOrder.DESC);
    }

    private boolean checkSortOrder(SortOrder sortOrder, SortOrder check) {
        return sortOrder != null && sortOrder.equals(check);
    }

}
