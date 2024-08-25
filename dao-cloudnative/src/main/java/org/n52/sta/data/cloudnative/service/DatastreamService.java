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
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.impl.Datastream;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class DatastreamService extends AbstractSensorThingsEntityServiceImpl<
        DatastreamDao,
        DatastreamDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamService.class);
    private static final DatastreamQueryConditions dQC = new DatastreamQueryConditions();
    private static final String UNKNOWN = "unknown";
    private final ObservationDaoImpl observationDao;
    private final DatastreamDaoImpl datastreamDao;
    private final FormatService formatService;
    private final UnitDaoImpl unitDao;
    private final AtomicLong TS = new AtomicLong();

    public DatastreamService(DatastreamDaoImpl datastreamDao,
                             FormatService formatService,
                             ObservationDaoImpl observationDao,
                             UnitDaoImpl unitDao,
                             Class<DatastreamDTO> entityClass) {
        super(datastreamDao, entityClass);
        this.observationDao = observationDao;
        this.datastreamDao = datastreamDao;
        this.formatService = formatService;
        this.unitDao = unitDao;
    }

    @Override
    protected DatastreamDTO fetchExpandEntitiesWithFilter(DatastreamDTO entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
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
                    entity.setSensor(getSensorService().getEntityByIdRaw(Long.valueOf(entity.getSensor().getId()),
                            expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.THING:
                    entity.setThing(getThingService().getEntityByIdRaw(Long.valueOf(entity.getThing().getId()),
                            expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.OBSERVED_PROPERTY:
                    entity.setObservedProperty(getObservedPropertyService().getEntityByIdRaw(
                            Long.valueOf(entity.getObservedProperty().getId()), expandItem.getQueryOptions()));
                    break;
                case STAEntityDefinition.OBSERVATIONS:
//                    // Optimize Request when only First/Last Observation is requested as we have already fetched that.
//                    if (checkForFirstLastObservation(expandItem)) {
//                        if (checkForFirstObservation(expandItem) && entity.getFirstObservation() != null) {
//                            ObservationDTO firstObservation = entity.getFirstObservation();
//                            entity.setObservations(Collections.singleton(firstObservation));
//                            break;
//                        } else if (checkForLastObservation(expandItem) && entity.getLastObservation() != null) {
//                            ObservationDTO lastObservation = entity.getLastObservation();
//                            entity.setObservations(Sets.newHashSet(Collections.singleton(lastObservation)));
//                            break;
//                        }
//                    }
                    Page<ObservationDTO> observations = getObservationService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getId(),
                                    STAEntityDefinition.DATASTREAMS,
                                    expandItem.getQueryOptions());
                    entity.setObservations(observations.get().collect(Collectors.toSet()));
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
            try {
                optionalEntity =
                        datastreamDao.findOne(dQC.withStaIdentifier(datastream.getId()),
                                null, entityClass);
            } catch (STAInvalidQueryException e) {
                // this should never occur
                throw new STACRUDException("Invalid query", HTTPStatus.BAD_REQUEST);
            }
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(
                        "No Datastream with id '" + datastream.getId() + "' " + "found");
            }
        }
        check(datastream);

        synchronized (getLock(datastream.getId())) {
            try {
                if (datastreamDao.existsByStaIdentifier(datastream.getId(), entityClass)) {
                    throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
                }
            } catch (STAInvalidQueryException e) {
                // this should never occur
                throw new STACRUDException("Invalid Query!", HTTPStatus.BAD_REQUEST);
            }
            // override @iot.id provided by user
            // we do not allow users to provide their own id
            datastream.setId(getUniqueTimestamp().toString());
            // save
            datastreamDao.save(POJOWrapper(datastream));

            if (datastream.getProperties() != null) {
                datastreamDao.saveDatastreamParameters(datastream.getId(), datastream.getProperties());
            }

            processObservation(datastream);
        }
//        return datastreamDao.findByStaIdentifier(datastream.getId(), null, DatastreamDTO.class)
//                .orElseThrow(() -> new STACRUDException("Datastream requested but still " +
//                        "processing!"));
        return datastream;
    }

    private Dataset POJOWrapper(DatastreamDTO datastream)
            throws STACRUDException, STAInvalidQueryException {

        Dataset dataset = new Dataset();

        dataset.setDatasetId(Long.valueOf(datastream.getId()));
        dataset.setStaIdentifier(datastream.getId());
        dataset.setName(datastream.getName());
        dataset.setDescription(datastream.getDescription());
        dataset.setObservedArea(new WKBWriter().write(datastream.getObservedArea()));


        Time phenomenonTime = datastream.getPhenomenonTime();
        if (phenomenonTime instanceof TimeInstant) {
            LocalDateTime startTime = ((TimeInstant) phenomenonTime).getValue().toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            dataset.setFirstTime(startTime);
            dataset.setLastTime(startTime);
        } else if (phenomenonTime instanceof TimePeriod) {
            LocalDateTime startTime = ((TimePeriod) phenomenonTime).getStart()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            LocalDateTime endTime = ((TimePeriod) phenomenonTime).getEnd()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            dataset.setFirstTime(startTime);
            dataset.setLastTime(endTime);
        }

        Time resultTime = datastream.getPhenomenonTime();
        if (resultTime instanceof TimeInstant) {
            LocalDateTime startTime = ((TimeInstant) resultTime).getValue().toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            dataset.setResultTimeStart(startTime);
            dataset.setResultTimeEnd(startTime);
        } else if (resultTime instanceof TimePeriod) {
            LocalDateTime startTime = ((TimePeriod) resultTime).getStart()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            LocalDateTime endTime = ((TimePeriod) resultTime).getEnd()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            dataset.setResultTimeStart(startTime);
            dataset.setResultTimeEnd(endTime);
        }

        // Feature link is invisible to the STA client
        // hence at creation time it must be null
        // must set this link when observations are created
        dataset.setFkFeatureId(null);

        dataset.setFkPhenomenonId(Long.valueOf(getObservedPropertyService()
                .createOrfetch(datastream.getObservedProperty())
                .getId()));

        dataset.setFkProcedureId(Long.valueOf(getSensorService()
                .createOrfetch(datastream.getSensor())
                .getId()));

        dataset.setFkUnitId(this
                .createOrfetchUnit(datastream)
                .getUnitId());

        dataset.setFkPlatformId(Long.valueOf(getThingService()
                .createOrfetch(datastream.getThing())
                .getId()));

        Format format = formatService.createOrFetchFormat(datastream.getObservationType());
        dataset.setObservationType(format.getDefinition());
        dataset.setFkFormatId(format.getFormatId());

        return dataset;
    }

    private DatastreamDTO createAndSaveDatasetAggregation(Dataset parent, Long feature, String staIdentifier) {

        Dataset dataset = new Dataset();

        // we can't use staIdentifier and String.valueOf(DatasetId) interchangeably here as it is an aggregation
        dataset.setStaIdentifier(staIdentifier);
        dataset.setDatasetId(getUniqueTimestamp());
        dataset.setIdentifier(dataset.getDatasetId().toString());
        dataset.setName(parent.getName());
        dataset.setDescription(parent.getDescription());
        dataset.setObservedArea(parent.getObservedArea());
        dataset.setObservationType(parent.getObservationType());
        dataset.setFkFeatureId(feature);
        dataset.setFkPhenomenonId(parent.getFkPhenomenonId());
        dataset.setFkProcedureId(parent.getFkProcedureId());
        dataset.setFkUnitId(parent.getFkUnitId());
        dataset.setFkPlatformId(parent.getFkPlatformId());
        dataset.setFkFormatId(parent.getFkFormatId());
        dataset.setFirstTime(parent.getFirstTime());
        dataset.setLastTime(parent.getLastTime());
        dataset.setResultTimeStart(parent.getResultTimeStart());
        dataset.setResultTimeEnd(parent.getResultTimeEnd());
        if (staIdentifier == null) {
            dataset.setFkAggregationId(parent.getDatasetId());
        }

        datastreamDao.save(dataset);

        DatastreamDTO datastream = new Datastream();
        // datastream.setId() sets the staIdentifier, dataset.getDatasetId() returns DatasetId
        // but we maintain the rule staIdentifier == String.valueOf(DatasetId) hence we can use it interchangeably
        // because the STA client does not know about aggregations and treats the entire aggregation as a single DS
        // calling function will only need the Id, hence we do not set other fields
        datastream.setId(dataset.getDatasetId().toString());
        return datastream;

    }

    @Override
    protected void deleteEntity(String staIdentifier) throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(staIdentifier)) {
            if (datastreamDao.existsByStaIdentifier(staIdentifier, DatastreamDTO.class)) {

                Condition predicate = getStaEntityId().eq(staIdentifier);
                Dataset dataset = datastreamDao
                        .selectQueryBuilder(predicate, entityClass, null, null)
                        .fetchInto(Dataset.class)
                        .get(0);

                // Delete sub-datasets if we are an aggregation
                if (dataset.getFkAggregationId() != null) {
                    Set<Long> datasetIds = datastreamDao
                            .findAllPOJOByAggregationId(dataset.getDatasetId())
                            .stream()
                            .map(Dataset::getDatasetId)
                            .collect(Collectors.toSet());

                    // delete observations
                    observationDao.deleteAllByDatasetIdIn(datasetIds);
                    // delete subdatastreams
                    datasetIds.forEach(datastreamDao::deleteById);
                } else {
                    // delete observations
                    observationDao.deleteAllByDatasetIdIn(Collections.singleton(dataset.getDatasetId()));
                }
                // delete properties
                datastreamDao.deleteDatastreamParametersByStaIdentifier(staIdentifier);

                //delete main datastream
                datastreamDao.deleteByStaIdentifier(staIdentifier, DatastreamDTO.class);
            } else {
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }

    }

    /**
     * Creates a DatasetAggregation or expands the existing Aggregation with a new dataset.
     *
     * @param dataset Existing Aggregation or Dataset
     * @param feature    Feature to be used for the new Dataset
     * @return specific Dataset that was created (not the aggregation)
     * @throws STACRUDException if an error occurred
     */
    DatastreamDTO createOrExpandAggregation(Dataset dataset, Long feature)
            throws STACRUDException {
        if (dataset.getFkAggregationId() == null) {
            LOGGER.debug("Creating new DatasetAggregation");

            // We need to create a new aggregation and link the existing dataset with it
            Dataset parent = new Dataset(dataset);
            parent.setFkFeatureId(null);
            // 1L is a marker that means this dataset is a parent of an aggregation
            parent.setFkAggregationId(1L);

            // Update existing dataset
            dataset.setDatasetId(getUniqueTimestamp());
            dataset.setStaIdentifier(null);
            dataset.setFkAggregationId(parent.getDatasetId());
            datastreamDao.deleteById(parent.getDatasetId());
            datastreamDao.save(dataset);

            // Persist parent
            datastreamDao.save(parent);

            return createAndSaveDatasetAggregation(parent, feature, null);
        } else {
            return createAndSaveDatasetAggregation(dataset, feature, null);
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
    public Condition getFilterPredicate(Class<DatastreamDTO> entityClass, QueryOptions queryOptions) {
        Condition isNotAggregated = StaEntity.DATASTREAM.FK_AGGREGATION_ID.isNull();
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
                    if (entity.getUnitOfMeasurement() != null) {
                        // TODO: WTF is happening here?
                        merged.setUnitOfMeasurement(entity.getUnitOfMeasurement());
                    }
                    datastreamDao.update(Long.valueOf(existing.get().getId()), POJOWrapper(merged));
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
        return datastreamDao.checkPropertyName(property).getName();
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
                getObservationService().createOrfetch(observation);
            }
        }
    }

    private Unit createOrfetchUnit(DatastreamDTO datastream) throws STACRUDException {
        Unit unitPOJO = null;
        if (datastream.getUnitOfMeasurement() != null) {
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
        if (datastream.getThing() == null || datastream.getObservedProperty() == null
                || datastream.getSensor() == null) {
            throw new STACRUDException("The datastream to create is invalid", HTTPStatus.BAD_REQUEST);
        }
    }

    @Override
    Field<String> getStaEntityId() {
        return StaEntity.DATASTREAM.STA_IDENTIFIER;
    }

    @Override
    AtomicLong getStaEntityTS() {
        return TS;
    }
}
