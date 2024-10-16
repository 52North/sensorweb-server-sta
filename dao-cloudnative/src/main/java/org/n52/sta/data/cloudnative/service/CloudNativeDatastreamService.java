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

package org.n52.sta.data.cloudnative.service;

import org.jooq.Condition;
import org.jooq.Field;
import org.locationtech.jts.io.WKBWriter;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.impl.Datastream;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.DatastreamDao;
import org.n52.sta.data.cloudnative.dao.StaEntityDao;
import org.n52.sta.data.cloudnative.dao.impl.DatastreamDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.ObservationDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.UnitDaoImpl;
import org.n52.sta.data.cloudnative.dao.util.FilterExprVisitor;
import org.n52.sta.data.cloudnative.schema.tables.pojos.*;
import org.n52.svalbard.odata.core.expr.Expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.n52.sta.data.cloudnative.condition.StaEntity.DATASET_AGGREGATION_MARKER;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class CloudNativeDatastreamService extends CloudNativeAbstractSensorThingsEntityServiceImpl<
        DatastreamDao,
        DatastreamDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudNativeDatastreamService.class);
    private final DatastreamQueryConditions dQC;

    private static final String UNKNOWN = "unknown";
    private final ObservationDaoImpl observationDao;

    private final DatastreamDaoImpl datastreamDao;
    private final UnitDaoImpl unitDao;
    private final CloudNativeFormatService formatService;

    private final AtomicLong TS = new AtomicLong();

    public CloudNativeDatastreamService(DatastreamDaoImpl datastreamDao,
                                        CloudNativeFormatService formatService,
                                        ObservationDaoImpl observationDao,
                                        UnitDaoImpl unitDao,
                                        MutexFactory lock, DatastreamQueryConditions dQC) {
        super(datastreamDao, DatastreamDTO.class, lock);
        this.observationDao = observationDao;
        this.datastreamDao = datastreamDao;
        this.formatService = formatService;
        this.unitDao = unitDao;
        this.dQC = dQC;
    }

    @Override
    protected DatastreamDTO fetchExpandEntitiesWithFilter(DatastreamDTO entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            // Nested $expand and $filter is only supported for Observation entity
            // We have already handled $expand without filter and expand
            // Except for $expand on Observations
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())
                    && !expandProperty.equals(STAEntityDefinition.OBSERVATIONS)) {
                continue;
            }
            switch (expandProperty) {
                case STAEntityDefinition.SENSOR:
                    entity.setSensor(getSensorService().getEntityByRelatedEntity(
                            entity.getId(),
                            STAEntityDefinition.DATASTREAMS,
                            null,
                            expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.THING:
                    entity.setThing(getThingService().getEntityByRelatedEntity(
                            entity.getId(),
                            STAEntityDefinition.DATASTREAMS,
                            null,
                            expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.OBSERVED_PROPERTY:
                    entity.setObservedProperty(getObservedPropertyService().getEntityByRelatedEntity(
                            entity.getId(),
                            STAEntityDefinition.DATASTREAMS,
                            null,
                            expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.OBSERVATIONS:
                    CollectionWrapper observations = getObservationService()
                            .getEntityCollectionByRelatedEntity(entity.getId(),
                                    STAEntityDefinition.DATASTREAMS,
                                    expandItem.getQueryOptions());
                    entity.setObservations((Set<ObservationDTO>) observations
                            .getEntities()
                            .stream()
                            .collect(Collectors.toSet()));
                    break;
                default:
                    throw new STAInvalidQueryException(String.format(StaEntityDao.INVALID_EXPAND_OPTION_SUPPLIED,
                            expandProperty,
                            StaConstants.DATASTREAM));
            }
        }
        return entity;
    }

    @Override
    protected Condition byRelatedEntityFilter(String relatedId, String relatedType, String ownId) {
        Condition filter;
        switch (relatedType) {
            case STAEntityDefinition.THINGS: {
                filter = dQC.withThingStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.SENSORS: {
                filter = dQC.withSensorStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.OBSERVED_PROPERTIES: {
                filter = dQC.withObservedPropertyStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.OBSERVATIONS: {
                filter = dQC.withObservationStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(dQC.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    protected DatastreamDTO createOrfetch(DatastreamDTO datastream)
            throws STACRUDException, STAInvalidQueryException {
        if (datastream.getId() != null && datastream.getName() == null) {
            Optional<DatastreamDTO> optionalEntity;
            optionalEntity =
                    datastreamDao.findOne(dQC.withStaIdentifier(datastream.getId()),
                            null, entityClass);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(
                        "No Datastream with id '" + datastream.getId() + "' " + "found");
            }
        }

        check(datastream);

        if(datastream.getId() == null) {
            datastream.setId(NULL_ID_MASK);
        }

        synchronized (getLock(datastream.getId())) {

            if (!Objects.equals(datastream.getId(), NULL_ID_MASK) &&
                    datastreamDao.existsByStaIdentifier(datastream.getId(), entityClass)) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            }

            // override @iot.id provided by user
            // we do not allow users to provide their own id
            datastream.setId(getUniqueTimestamp().toString());
            // save entity
            datastreamDao.save(POJOWrapper(datastream));
            // save parameters
            if (datastream.getProperties() != null) {
                datastreamDao.saveDatastreamParameters(datastream.getId(), datastream.getProperties());
            }
            // save observations
            processObservation(datastream);
        }
        return datastream;
    }

    protected Dataset POJOWrapper(DatastreamDTO datastream)
            throws STACRUDException, STAInvalidQueryException {

        Dataset dataset = new Dataset();

        dataset.setDatasetId(Long.valueOf(datastream.getId()));
        dataset.setStaIdentifier(datastream.getId());
        dataset.setName(datastream.getName());
        dataset.setDescription(datastream.getDescription());
        if (datastream.getObservedArea() != null) {
            dataset.setObservedArea(new WKBWriter().write(datastream.getObservedArea()));
        }


        Time phenomenonTime = datastream.getPhenomenonTime();
        if (phenomenonTime instanceof TimeInstant) {
            LocalDateTime startTime = ((TimeInstant) phenomenonTime).getValue()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            dataset.setFirstTime(startTime);
            dataset.setLastTime(startTime);
        } else if (phenomenonTime instanceof TimePeriod) {
            LocalDateTime startTime = ((TimePeriod) phenomenonTime).getStart()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            LocalDateTime endTime = ((TimePeriod) phenomenonTime).getEnd()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            dataset.setFirstTime(startTime);
            dataset.setLastTime(endTime);
        }

        Time resultTime = datastream.getPhenomenonTime();
        if (resultTime instanceof TimeInstant) {
            LocalDateTime startTime = ((TimeInstant) resultTime).getValue().toDate()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            dataset.setResultTimeStart(startTime);
            dataset.setResultTimeEnd(startTime);
        } else if (resultTime instanceof TimePeriod) {
            LocalDateTime startTime = ((TimePeriod) resultTime).getStart()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            LocalDateTime endTime = ((TimePeriod) resultTime).getEnd()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            dataset.setResultTimeStart(startTime);
            dataset.setResultTimeEnd(endTime);
        }

        // Feature link is invisible to the STA client
        // hence at creation time it must be null
        // must set this link when observations are created
        dataset.setFkFeatureId(null);

        if (datastream.getObservedProperty() != null) {
            dataset.setFkPhenomenonId(Long.valueOf(getObservedPropertyService()
                    .createOrfetch(datastream.getObservedProperty())
                    .getId()));
        }

        if (datastream.getSensor() != null) {
            // avoid circular dependency in creation
            datastream.getSensor().setDatastreams(null);
            dataset.setFkProcedureId(Long.valueOf(getSensorService()
                    .createOrfetch(datastream.getSensor())
                    .getId()));
        }

        Unit uomPOJO = this.createOrfetchUnit(datastream);
        if (uomPOJO != null) {
            dataset.setFkUnitId(uomPOJO.getUnitId());
        }

        if (datastream.getThing() != null) {
            if (datastream.getThing().getDescription() == null ||
                    !Objects.equals(datastream.getThing().getDescription(), AUTOGENERATED_KEY)) {
                dataset.setFkPlatformId(Long.valueOf(getThingService()
                        .createOrfetch(datastream.getThing())
                        .getId()));
            }
        }

        if(datastream.getObservationType() != null) {
            Format format = formatService.createOrFetchFormat(datastream.getObservationType());
            dataset.setFkFormatId(format.getFormatId());
        }
        // we do not support other profiles as of now
        dataset.setObservationType("simple");

        return dataset;
    }

    private DatastreamDTO createAndSaveDatasetAggregation(Dataset parent, Long feature_id)
            throws STACRUDException {

        Dataset dataset = new Dataset();
        // we can't use staIdentifier and String.valueOf(DatasetId) interchangeably here as it is an aggregation
        dataset.setStaIdentifier(null);
        dataset.setDatasetId(getUniqueTimestamp());
        dataset.setFkFeatureId(feature_id);
        dataset.setFkAggregationId(parent.getDatasetId());

        dataset.setName(parent.getName());
        dataset.setDescription(parent.getDescription());
        dataset.setObservedArea(parent.getObservedArea());
        dataset.setObservationType(parent.getObservationType());
        dataset.setFkPhenomenonId(parent.getFkPhenomenonId());
        dataset.setFkProcedureId(parent.getFkProcedureId());
        dataset.setFkUnitId(parent.getFkUnitId());
        dataset.setFkPlatformId(parent.getFkPlatformId());
        dataset.setFkFormatId(parent.getFkFormatId());
        dataset.setFirstTime(parent.getFirstTime());
        dataset.setLastTime(parent.getLastTime());
        dataset.setResultTimeStart(parent.getResultTimeStart());
        dataset.setResultTimeEnd(parent.getResultTimeEnd());
        datastreamDao.save(dataset);

        DatastreamDTO datastreamDTO = new Datastream();
        String Id = dataset.getDatasetId().toString();
        // calling function will only need the Id, hence we do not set other fields
        datastreamDTO.setId(Id);
        return datastreamDTO;

    }

    @Override
    protected void deleteEntity(String staIdentifier) throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(staIdentifier)) {
            // caution to always pass parent entities
            // sub datasets have sta_identifier set to null
            if (datastreamDao.existsByStaIdentifier(staIdentifier, DatastreamDTO.class)) {

                Dataset dataset = datastreamDao.findByStaIdentifierPOJO(staIdentifier);

                // Delete sub-datasets if we are an aggregation
                if (dataset.getFkAggregationId() != null &&
                        Objects.equals(dataset.getFkAggregationId(), DATASET_AGGREGATION_MARKER)) {
                    // all sub-datasets have their fk_aggregation_id = parent_dataset_id
                    Long aggregationId = dataset.getDatasetId();
                    Set<Long> datasetIds = datastreamDao
                            .findAllByAggregationIdPOJO(aggregationId)
                            .stream()
                            .map(Dataset::getDatasetId)
                            .collect(Collectors.toSet());
                    // TODO: datastreamDao.deleteByAggregationId(aggregationId);

                    // delete observations from subdatastreams
                    observationDao.deleteAllByDatasetIdIn(datasetIds);
                    // delete subdatastreams
                    for (Long Id: datasetIds) {
                        datastreamDao.deleteById(Id);
                    }
                    // delete observations from parent datastream
                    observationDao.deleteByDatasetId(Long.parseLong(staIdentifier));
                } else {
                    // delete observations from singular datastream
                    observationDao.deleteAllByDatasetIdIn(Collections.singleton(dataset.getDatasetId()));
                }
                // delete properties
                datastreamDao.deleteDatastreamParameters(Long.valueOf(staIdentifier));
                //delete parent datastream
                datastreamDao.deleteByStaIdentifier(staIdentifier);
            } else {
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }

    }

    /**
     * Creates a DatasetAggregation or expands the existing Aggregation with a new dataset.
     *
     * @param dataset Existing Aggregation or Dataset
     * @param feature_id    Feature to be used for the new Dataset
     * @return specific Dataset that was created (not the aggregation)
     * @throws STACRUDException if an error occurred
     */
    DatastreamDTO createOrExpandAggregation(Dataset dataset, Long feature_id)
            throws STACRUDException {
        if (dataset.getFkAggregationId() == null) {
            LOGGER.debug("Creating new DatasetAggregation");

            // Delete existing dataset
            datastreamDao.deleteById(dataset.getDatasetId());

            // Create a parent dataset
            Dataset parent = new Dataset(dataset);
            parent.setFkAggregationId(DATASET_AGGREGATION_MARKER);
            parent.setFkFeatureId(null);
            // persist the parent dataset
            // linked entities of the original dataset are now linked to the parent
            datastreamDao.save(parent);

            // We need to create a new aggregation and link the existing dataset with it
            Dataset subdataset = new Dataset(dataset);
            subdataset.setStaIdentifier(null);
            subdataset.setDatasetId(getUniqueTimestamp());
            subdataset.setFkFeatureId(dataset.getFkFeatureId());
            subdataset.setFkAggregationId(dataset.getDatasetId());
            // Persist subdataset
            datastreamDao.save(subdataset);


            // finally create a new aggregation linked to the new feature_id
            return createAndSaveDatasetAggregation(parent, feature_id);
        } else {
            return createAndSaveDatasetAggregation(dataset, feature_id);
        }
    }

    @Override
    protected DatastreamDTO createOrUpdate(DatastreamDTO entity)
            throws STACRUDException, STAInvalidQueryException {
        if (entity.getId() != null && datastreamDao.existsByStaIdentifier(entity.getId(), DatastreamDTO.class)) {
            return updateEntity(entity.getId(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    /**
     * Constructs FilterPredicate based on given queryOptions. Additionally filters out Datasets that are aggregated
     * into DatasetAggregations.
     *
     * @param entityClass  Class of the requested Entity
     * @param queryOptions QueryOptions Object
     * @return Predicate based on FilterOption from queryOptions
     */
    @Override
    protected Condition getFilterPredicate(Class<DatastreamDTO> entityClass, QueryOptions queryOptions) {
        // filter out subdatasets and fetch only parent datasets
        Condition isNotAggregated = StaEntity.DATASTREAM.FK_AGGREGATION_ID.isNull()
                .or(StaEntity.DATASTREAM.FK_AGGREGATION_ID.eq(DATASET_AGGREGATION_MARKER));

        if (!queryOptions.hasFilterFilter()) {
            return isNotAggregated;
        } else {
            FilterFilter filterOption = queryOptions.getFilterFilter();
            Expr filter = (Expr) filterOption.getFilter();
            try {
                return isNotAggregated.and((Field<Boolean>) filter.accept(
                        new FilterExprVisitor(StaConstants.DATASTREAM)));
            } catch (STAInvalidQueryException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected DatastreamDTO updateEntity(String id, DatastreamDTO entity, HttpMethod method)
            throws STACRUDException, STAInvalidQueryException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<DatastreamDTO> existing =
                        datastreamDao.findOne(dQC.withStaIdentifier(id), null, DatastreamDTO.class);
                if (existing.isPresent()) {
                    DatastreamDTO merged = merge(existing.get(), entity);
                    datastreamDao.update(POJOWrapper(merged));
                    return merged;
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    private void checkUpdate(DatastreamDTO entity) throws STACRUDException {
        String ERROR_MSG = "Inlined entities are not allowed for updates!";

        if (entity.getObservedProperty() != null &&
                (entity.getObservedProperty().getId() == null ||
                        entity.getObservedProperty().getName() != null ||
                        entity.getObservedProperty().getDescription() != null)) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }

        if (entity.getSensor() != null
                && (entity.getSensor().getId() == null
                || entity.getSensor().getName() != null
                || entity.getSensor().getDescription() != null)) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }

        if (entity.getThing() != null &&
                (entity.getThing().getId() == null ||
                        entity.getThing().getName() != null ||
                        entity.getThing().getDescription() != null)) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }
        if (entity.getObservations() != null) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }
    }

    @Override
    protected String checkPropertyName(String property) {
        Field<?> field = datastreamDao.checkAliasedPropertyName(property);
        return field == StaEntity.DATASTREAM.OBSERVED_AREA ? "datastreamObservedArea" : field.getName();
    }

    @Override
    protected DatastreamDTO merge(DatastreamDTO existing, DatastreamDTO toMerge)
            throws STACRUDException {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        checkObservationType(existing, toMerge);
        // observedArea
        if (toMerge.getObservedArea() != null) {
            existing.setObservedArea(toMerge.getObservedArea());
        }
        // unit
        if (toMerge.getUnitOfMeasurement() != null &&
                existing.getUnitOfMeasurement().getSymbol().equals(toMerge.getUnitOfMeasurement().getSymbol())) {
            existing.setUnitOfMeasurement(toMerge.getUnitOfMeasurement());
        }

        // resultTime
        if (toMerge.getResultTime() != null) {
            existing.setResultTime(toMerge.getResultTime());
        }

        // observationType
        if (toMerge.getObservationType() != null
                && !existing.getObservationType().equals(toMerge.getObservationType())
                && !toMerge.getObservationType().equalsIgnoreCase(UNKNOWN)) {
            existing.setObservationType(toMerge.getObservationType());
        }
        return existing;
    }

    private void checkObservationType(DatastreamDTO existing, DatastreamDTO toMerge)
            throws STACRUDException {
        if (toMerge.getObservationType() != null &&
                !toMerge.getObservationType().equalsIgnoreCase(UNKNOWN) &&
                !existing.getObservationType().equals(toMerge.getObservationType())) {
            throw new STACRUDException(
                    String.format(
                            "The updated observationType (%s) does not comply with the existing observationType (%s)",
                            toMerge.getObservationType(),
                            existing.getObservationType()),
                    HTTPStatus.CONFLICT);
        }
    }

    private void processObservation(DatastreamDTO datastream)
            throws STACRUDException, STAInvalidQueryException {
        Set<ObservationDTO> observations = datastream.getObservations();
        if (observations != null && !observations.isEmpty()) {
            for (ObservationDTO observation : observations) {
                getObservationService().createOrfetchHelper(observation, datastream);
            }
        }
    }

    private Unit createOrfetchUnit(DatastreamDTO datastream) throws STACRUDException {
        Unit unitPOJO = null;
        if (datastream.getUnitOfMeasurement() != null &&
                datastream.getUnitOfMeasurement().getSymbol() != null) {
            synchronized (getLock(datastream.getUnitOfMeasurement().getSymbol() + "unit")) {
                if (!unitDao.existsBySymbol(datastream.getUnitOfMeasurement().getSymbol())) {
                    unitPOJO = new Unit();
                    unitPOJO.setSymbol(datastream.getUnitOfMeasurement().getSymbol());
                    unitPOJO.setName(datastream.getUnitOfMeasurement().getName());
                    unitPOJO.setLink(datastream.getUnitOfMeasurement().getDefinition());
                    unitPOJO.setUnitId(getUniqueTimestamp());
                    unitDao.save(unitPOJO);
                } else {
                    unitPOJO = unitDao.findBySymbol(datastream.getUnitOfMeasurement().getSymbol()).get();
                }
            }
        }
        return unitPOJO;
    }

    private void check(DatastreamDTO datastream) throws STACRUDException {
        if (datastream.getThing() == null ||
                datastream.getObservedProperty() == null ||
                datastream.getSensor() == null) {
            throw new STACRUDException("The datastream to create is invalid", HTTPStatus.BAD_REQUEST);
        }
    }

    @Override
    protected Field<String> getStaEntityId() {
        return StaEntity.DATASTREAM.STA_IDENTIFIER;
    }

    @Override
    protected AtomicLong getStaEntityTS() {
        return TS;
    }
}
