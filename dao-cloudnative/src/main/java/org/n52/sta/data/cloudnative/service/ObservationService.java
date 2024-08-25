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
import org.n52.janmayen.http.HTTPStatus;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.filter.FilterFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.n52.sta.data.cloudnative.condition.ObservationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.ObservationDao;
import org.n52.sta.data.cloudnative.dao.StaEntityDao;
import org.n52.sta.data.cloudnative.dao.impl.DatastreamDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.LocationDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.ObservationDaoImpl;
import org.n52.sta.data.cloudnative.dao.util.FilterExprVisitor;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Dataset;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Observation;
import org.n52.svalbard.odata.core.expr.Expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import java.util.concurrent.atomic.AtomicLong;

import static org.n52.sta.api.RequestUtils.QUERY_OPTIONS_FACTORY;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class ObservationService
        extends AbstractSensorThingsEntityServiceImpl<ObservationDao, ObservationDTO> {

    private static final ObservationQueryConditions oQC = new ObservationQueryConditions();
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationService.class);
    protected final DatastreamDaoImpl datastreamDao;
    protected final ObservationDaoImpl observationDao;
    private final String OBS_TYPE_SENSORML_OBSERVATION =
            "http://www.52north.org/def/observationType/OGC-OM/2.0/OM_SensorML20Observation";
    private final Class<ObservationDTO> entityClass;
    private final AtomicLong TS = new AtomicLong();

    public ObservationService(ObservationDaoImpl observationDao,
                              DatastreamDaoImpl datastreamDao) {
        super(observationDao, ObservationDTO.class);
        this.entityClass = ObservationDTO.class;
        this.datastreamDao = datastreamDao;
        this.observationDao = observationDao;
    }

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions)
            throws STACRUDException {
        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Condition predicate = getFilterPredicate(entityClass, queryOptions);
            List<String> identifierList = observationDao
                    .getColumnList(predicate,
                            pageableRequest,
                            StaEntity.OBSERVATION.STA_IDENTIFIER.getName(),
                            entityClass);
            if (identifierList.isEmpty()) {
                return new CollectionWrapper(-1, Collections.emptyList(), false);
            } else {
                return getEntityCollectionWrapperByIdentifierList(identifierList, pageableRequest, queryOptions, predicate);
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    private CollectionWrapper getEntityCollectionWrapperByIdentifierList(List<String> identifierList,
                                                                         OffsetLimitBasedPageRequest pageableRequest,
                                                                         QueryOptions queryOptions,
                                                                         Condition predicate)
            throws STAInvalidQueryException {

        Page<ObservationDTO> pages = observationDao.findAll(
                        oQC.withStaIdentifier(identifierList),
                        new OffsetLimitBasedPageRequest(0,
                                pageableRequest.getPageSize(),
                                pageableRequest.getSort()),
                        queryOptions,
                        entityClass);

        CollectionWrapper wrapper = createCollectionWrapperAndExpand(queryOptions, pages);
        // Create Page manually as we used Database Pagination and
        // are not sure how many Entities there are in the Database
        if (pages.isEmpty()) {
            return wrapper;
        } else {
            long count = -1;
            boolean hasNextPage = false;
            if (queryOptions.hasCountFilter() && queryOptions.getCountFilter().getValue()) {
                count = observationDao.count(predicate, entityClass);
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

    public Page<ObservationDTO> getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                      String relatedType,
                                                      QueryOptions queryOptions)
            throws STACRUDException {
        try {
            OffsetLimitBasedPageRequest pageableRequest = createPageableRequest(queryOptions);
            Condition predicate =
                    byRelatedEntityFilter(relatedId, relatedType, null)
                            .and(getFilterPredicate(entityClass, queryOptions));

            List<String> identifierList = observationDao.getColumnList(predicate,
                    createPageableRequest(queryOptions),
                    StaEntity.OBSERVATION.STA_IDENTIFIER.getName(),
                    entityClass);

            if (identifierList.isEmpty()) {
                return Page.empty();
            } else {
                Page<ObservationDTO> pages = observationDao.findAll(
                        oQC.withStaIdentifier(identifierList),
                        new OffsetLimitBasedPageRequest(0,
                                pageableRequest.getPageSize(),
                                pageableRequest.getSort()),
                        queryOptions,
                        entityClass);

                if (queryOptions.hasExpandFilter()) {
                    return pages
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
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override
    protected ObservationDTO fetchExpandEntitiesWithFilter(ObservationDTO entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
                case STAEntityDefinition.DATASTREAM:
                    DatastreamDTO datastream = getDatastreamService()
                            .getEntityByIdRaw(Long.valueOf(entity.getDatastream().getId()),
                                    expandItem.getQueryOptions());
                    entity.setDatastream(datastream);
                    break;
                case STAEntityDefinition.FEATURE_OF_INTEREST:
                    FeatureOfInterestDTO foi = getFeatureOfInterestService()
                            .getEntityByDatasetIdRaw(Long.valueOf(entity.getDatastream().getId()),
                                    expandItem.getQueryOptions());
                    entity.setFeatureOfInterest(foi);
                    break;
                default:
                    throw new STAInvalidQueryException(String.format(StaEntityDao.INVALID_EXPAND_OPTION_SUPPLIED,
                            expandProperty,
                            StaConstants.OBSERVATIONS));
            }
        }
        return entity;
    }

    @Override
    protected Condition byRelatedEntityFilter(String relatedId, String relatedType, String ownId) {
        Condition filter;
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS: {
                filter = oQC.withDatastreamStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.FEATURES_OF_INTEREST: {
                filter = oQC.withFeatureOfInterestStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }
        if (ownId != null) {
            filter = filter.and(oQC.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    protected ObservationDTO createOrfetch(ObservationDTO observation)
            throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(observation.getId())) {

            check(observation);

            // Fetch dataset and check if FOI matches to reuse existing dataset
            Dataset dataset = datastreamDao.findPOJOByDatasetId(Long.valueOf(observation.getDatastream().getId()));
            FeatureOfInterestDTO feature = this.createOrfetchFeature(observation, dataset.getFkPlatformId());

            // Check all subdatasets for a matching  dataset
            Set<Dataset> datasets;
            if (dataset.getFkAggregationId() == null) {
                // We are not an aggregate so there is only one dataset to check for fit
                datasets = Collections.singleton(dataset);
            } else {
                datasets = datastreamDao.findAllPOJOByAggregationId(dataset.getDatasetId());
            }

            // Check all datasets for a matching FOI
            boolean found = false;
            for (Dataset ds : datasets) {
                if (ds.getFkFeatureId() == null) {
                    // We have a dataset without a feature
                    LOGGER.debug("Reusing existing dataset without FOI.");
                    ds.setFkFeatureId(Long.valueOf(feature.getId()));
                    datastreamDao.update(ds.getDatasetId(), ds);
                    found = true;
                    break;
                } else if (feature.getId().equals(ds.getFkFeatureId().toString())) {
                    // We have a dataset with a matching feature
                    LOGGER.debug("Reusing existing dataset with matching FOI.");
                    found = true;
                    break;
                }
            }

            if (!found) {
                // We have not found a matching dataset, so we need to create a new one
                LOGGER.debug("Creating new dataset as none with matching FOI exists");
                observation.setDatastream(getDatastreamService()
                        .createOrExpandAggregation(dataset, Long.valueOf(feature.getId())));
            }

            // Save Observation
            observation.setId(getUniqueTimestamp().toString());
            Observation observationPOJO = POJOWrapper(observation);
            observationDao.save(observationPOJO);

            // Save Observation Parameters
            if (observation.getParameters() != null) {
                observationDao.saveObservationParameters(observation.getId(), observation.getParameters());
            }

            return observation;
        }
    }

    private Observation POJOWrapper(ObservationDTO entity) {
        Observation observationPOJO = new Observation();

        observationPOJO.setObservationId(Long.valueOf(entity.getId()));
        observationPOJO.setStaIdentifier(entity.getId());
        observationPOJO.setIdentifier(entity.getId());

        observationPOJO.setResultTime(((TimeInstant) entity.getResultTime()).getValue().toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());

        Time phenomenonTime = entity.getPhenomenonTime();
        if (phenomenonTime instanceof TimeInstant) {
            LocalDateTime startTime = ((TimeInstant) phenomenonTime).getValue().toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            observationPOJO.setSamplingTimeStart(startTime);
            observationPOJO.setSamplingTimeEnd(startTime);
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
            observationPOJO.setSamplingTimeStart(startTime);
            observationPOJO.setSamplingTimeEnd(endTime);
        }

        Time validTime = entity.getValidTime();
        if (validTime instanceof TimeInstant) {
            observationPOJO.setValidTimeStart(((TimeInstant) validTime).getValue().toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            observationPOJO.setValidTimeEnd(((TimeInstant) validTime).getValue().toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        } else if (validTime instanceof TimePeriod) {
            observationPOJO.setValidTimeStart(((TimePeriod) validTime).getStart().toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            observationPOJO.setValidTimeEnd(((TimePeriod) validTime).getEnd().toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        }


        setObservationPOJOValueType(observationPOJO, entity.getDatastream().getObservationType(), entity.getResult());
        observationPOJO.setFkDatasetId(Long.valueOf(entity.getDatastream().getId()));

        return observationPOJO;
    }

    private void setObservationPOJOValueType(Observation entity, String observationType, Object value) {
        switch (observationType) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                entity.setValueQuantity(BigDecimal.valueOf(Double.parseDouble((String) value)));
                entity.setValueType("quantity");
                break;
            case OmConstants.OBS_TYPE_CATEGORY_OBSERVATION:
                entity.setValueCategory((String) value);
                entity.setValueType("category");
                break;
            case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
                entity.setValueCount(Integer.parseInt((String) value));
                entity.setValueType("count");
                break;
            case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
                entity.setValueText((String) value);
                entity.setValueType("text");
                break;
            case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
                entity.setValueBoolean((short) (((String) value).equals("true") ? 1 : 0));
                entity.setValueType("bool");
                break;
        }
    }

    private FeatureOfInterestDTO createOrfetchFeature(ObservationDTO observation, Long thingId)
            throws STAInvalidQueryException, STACRUDException {
        FeatureOfInterestDTO feature = null;
        // Create feature based on Thing.location if there is no feature given
        if (observation.getFeatureOfInterest() == null) {
            LocationDaoImpl locationDao = new LocationDaoImpl();
            List<LocationDTO> locations = locationDao.findAllByThingId(thingId, LocationDTO.class);
            for (LocationDTO location : locations) {
                if (feature == null) {
                    feature = getFeatureOfInterestService().createFeatureOfInterest(location);
                }
                if (location.getGeometry() != null) {
                    feature = getFeatureOfInterestService().createFeatureOfInterest(location);
                    break;
                }
            }
            if (feature == null) {
                throw new STACRUDException("The observation to create is invalid." +
                        " Missing feature or thing.location!", HTTPStatus.BAD_REQUEST);
            }
            observation.setFeatureOfInterest(feature);
        } else {
            // save feature to db
            feature = getFeatureOfInterestService().createOrfetch(observation.getFeatureOfInterest());
            observation.setFeatureOfInterest(feature);
        }
        return feature;
    }

    private void check(ObservationDTO observation) throws STACRUDException {
        if (observation.getDatastream() == null) {
            throw new STACRUDException("The observation to create is invalid. Missing datastream!",
                    HTTPStatus.BAD_REQUEST);
        }
    }

    @Override
    protected ObservationDTO createOrUpdate(ObservationDTO entity) throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(entity.getId())) {
            if (entity.getId() != null && observationDao.existsByStaIdentifier(entity.getId(), ObservationDTO.class)) {
                return updateEntity(entity.getId(), entity, HttpMethod.PATCH);
            }
            return createOrfetch(entity);
        }
    }

    @Override
    protected ObservationDTO updateEntity(String id, ObservationDTO entity, HttpMethod method)
            throws STACRUDException, STAInvalidQueryException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                QueryOptions options = QUERY_OPTIONS_FACTORY
                        .createQueryOptions("$expand=Datastream($select=observationType)");
                Optional<ObservationDTO> existing =
                        observationDao.findByStaIdentifier(id, options, ObservationDTO.class);
                if (existing.isPresent()) {
                    ObservationDTO merged = merge(existing.get(), entity);
                    Observation updatedObservation = POJOWrapper(merged);
                    observationDao.update(updatedObservation);
                    Dataset dataset = datastreamDao.findPOJOByDatasetId(updatedObservation.getFkDatasetId());
                    updateDatastreamPhenomenonTimeOnObservationUpdate(dataset, updatedObservation);
                    return merged;
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override
    public Condition getFilterPredicate(Class entityClass, QueryOptions queryOptions) {
        Condition defaultFilter = StaEntity.OBSERVATION.FK_PARENT_OBSERVATION_ID.isNull();
        if (!queryOptions.hasFilterFilter()) {
            // Filter out non-root observations
            // e.g. Profile-/TrajectoryObservations
            return defaultFilter;
        } else {
            FilterFilter filterOption = queryOptions.getFilterFilter();
            Expr filter = (Expr) filterOption.getFilter();
            try {
                return defaultFilter.and((Field<Boolean>) filter.accept(
                                new FilterExprVisitor(STAEntityDefinition.OBSERVATION)));
            } catch (STAInvalidQueryException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateDatastreamPhenomenonTimeOnObservationUpdate(Dataset datastreamEntity,
                                                                   Observation observation)
            throws STAInvalidQueryException, STACRUDException {
        if (datastreamEntity.getFirstTime() == null ||
                datastreamEntity.getLastTime() == null ||
                observation.getSamplingTimeStart().compareTo(datastreamEntity.getFirstTime()) != 1 ||
                observation.getSamplingTimeEnd().compareTo(datastreamEntity.getLastTime()) != -1
        ) {
            // Setting new phenomenonTimeStart
            Observation firstObservation = observationDao.findFirstByDatasetIdOrderBySamplingTimeStartAsc(
                    datastreamEntity.getDatasetId(),
                    ObservationDTO.class);

            LocalDateTime newPhenomenonStart = (firstObservation == null) ? null : firstObservation.getSamplingTimeStart();

            // Set Start and End to null if there is no observation.
            if (newPhenomenonStart == null) {
                datastreamEntity.setFirstTime(null);
                datastreamEntity.setLastTime(null);
            } else {
                datastreamEntity.setFirstTime(newPhenomenonStart);

                // Setting new phenomenonTimeEnd
                Observation lastObservation = observationDao.findFirstByDatasetIdOrderBySamplingTimeEndDesc(
                        datastreamEntity.getDatasetId(),
                        ObservationDTO.class);
                LocalDateTime newPhenomenonEnd = (lastObservation == null) ? null : lastObservation.getSamplingTimeEnd();
                if (newPhenomenonEnd != null) {
                    datastreamEntity.setFirstTime(newPhenomenonEnd);
                } else {
                    datastreamEntity.setFirstTime(null);
                    datastreamEntity.setLastTime(null);
                }
            }
            datastreamDao.update(datastreamEntity.getDatasetId(), datastreamEntity);
            // update parent if its part of the aggregation
            if (datastreamEntity.getFkAggregationId() != null && datastreamEntity.getFkAggregationId() != 1L) {
                updateDatastreamPhenomenonTimeOnObservationUpdate(
                        datastreamDao.findPOJOByDatasetId(datastreamEntity.getFkAggregationId()),
                        observation);
            }
        }
    }

    @Override
    protected void deleteEntity(String identifier) throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(identifier)) {
            if (observationDao.existsByStaIdentifier(identifier, ObservationDTO.class)) {
                QueryOptions options = QUERY_OPTIONS_FACTORY
                        .createQueryOptions("$expand=Datastream($select=id)");
                ObservationDTO observation = observationDao
                        .findByStaIdentifier(identifier, options, ObservationDTO.class).get();

                if (observation.getParameters() != null) {
                    observationDao.deleteObservationParameters(Long.valueOf(observation.getId()),
                            observation.getParameters());
                }
                observationDao.deleteByStaIdentifier(observation.getId(), ObservationDTO.class);
                Dataset dataset = datastreamDao.findPOJOByDatasetId(Long.valueOf(observation.getDatastream().getId()));
                updateDatastreamPhenomenonTimeOnObservationUpdate(dataset, POJOWrapper(observation));
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }


    @Override
    protected String checkPropertyName(String property) {
        return observationDao.checkPropertyName(property).getName();
    }

    @Override
    protected ObservationDTO merge(ObservationDTO existing, ObservationDTO toMerge)
            throws STACRUDException {

        // phenomenonTime
        mergeSamplingTimeAndCheckResultTime(existing, toMerge);
        // resultTime
        if (toMerge.getResultTime() != null) {
            existing.setResultTime(toMerge.getResultTime());
        }
        // validTime
        if (toMerge.getValidTime() != null) {
            existing.setValidTime(toMerge.getValidTime());
        }
        // parameter
        if (toMerge.getParameters() != null) {
            synchronized (getLock(String.valueOf(
                    toMerge.getParameters().hashCode() + existing.getParameters().hashCode()))) {

                observationDao.saveObservationParameters(existing.getId(), toMerge.getParameters());
                existing.setParameters(toMerge.getParameters());
            }
        }
        // value
        if (toMerge.getResult() != null) {
            checkValue(existing, toMerge);
        }
        return existing;
    }

    private void checkValue(ObservationDTO existing, ObservationDTO toMerge) throws STACRUDException {
        switch (existing.getDatastream().getObservationType()) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                existing.setResult(BigDecimal.valueOf(Double.parseDouble(toMerge.getResult().toString())));
                break;
            case OmConstants.OBS_TYPE_CATEGORY_OBSERVATION:
                existing.setResult(toMerge.getResult().toString());
                break;
            case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
                existing.setResult(Integer.parseInt(toMerge.getResult().toString()));
                break;
            case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
                existing.setResult(toMerge.getResult().toString());
                break;
            case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
                existing.setResult((short) ((toMerge.getResult().toString()).equals("true") ? 1 : 0));
                break;
            default:
                throw new STACRUDException(
                    String.format("The observation value for @iot.id %s can not be updated!",
                            existing.getId()),
                    HTTPStatus.CONFLICT);
        }
    }

    private void mergeSamplingTimeAndCheckResultTime(ObservationDTO existing, ObservationDTO toMerge) {
        // TODO:
        Time toMergeSamplingTimeEnd = toMerge.getPhenomenonTime();
        Time existingSamplingTimeEnd = existing.getPhenomenonTime();
        if (toMergeSamplingTimeEnd != null && existingSamplingTimeEnd.equals(existing.getResultTime())) {
            existing.setResultTime(toMergeSamplingTimeEnd);
        }
        mergePhenomenonTime(existing, toMerge);
    }


    @Override
    Field<String> getStaEntityId() {
        return StaEntity.OBSERVATION.STA_IDENTIFIER;
    }

    @Override
    AtomicLong getStaEntityTS() {
        return TS;
    }
}
