/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.CategoryRepository;
import org.n52.series.db.DataRepository;
import org.n52.series.db.DatasetRepository;
import org.n52.series.db.OfferingRepository;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.BooleanDataEntity;
import org.n52.series.db.beans.BooleanDatasetEntity;
import org.n52.series.db.beans.CategoryDataEntity;
import org.n52.series.db.beans.CategoryDatasetEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.CountDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.NotInitializedDatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.QuantityDatasetEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.TextDatasetEntity;
import org.n52.series.db.beans.dataset.Dataset;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.series.db.query.DatasetQuerySpecifications;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.sta.data.query.ObservationQuerySpecifications;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.FeatureOfInterestMapper;
import org.n52.sta.mapping.ObservationMapper;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class ObservationService extends AbstractSensorThingsEntityService<DataRepository<DataEntity<?>>, DataEntity<?>> {

    private ObservationMapper mapper;
    
    @Autowired
    private FeatureOfInterestMapper featureMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferingRepository offeringRepository;
    
    @Autowired
    private DatasetRepository<DatasetEntity> datasetRepository;

    private ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    
    private DatasetQuerySpecifications dQS = DatasetQuerySpecifications.of(null);

    public ObservationService(DataRepository<DataEntity<?>> repository, ObservationMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }

    @Override
    public EntityTypes getType() {
        return EntityTypes.Observation;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) {
        EntityCollection retEntitySet = new EntityCollection();
        getRepository().findAll(createPageableRequest(queryOptions))
                .forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(Long id) {
        // TODO: check if this cast is possible
        Optional<DataEntity<?>> entity = (Optional<DataEntity<?>>) getRepository().findOne(byId(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType,
            QueryOptions queryOptions) {
        BooleanExpression filter = getFilter(sourceId, sourceEntityType);
        // TODO: check cast
        Iterable<DataEntity<?>> observations =
                (Iterable<DataEntity<?>>) getRepository().findAll(filter, createPageableRequest(queryOptions));
        EntityCollection retEntitySet = new EntityCollection();
        observations.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public long getRelatedEntityCollectionCount(Long sourceId, EdmEntityType sourceEntityType) {
        BooleanExpression filter = getFilter(sourceId, sourceEntityType);
        return getRepository().count(filter);
    }

    private BooleanExpression getFilter(Long sourceId, EdmEntityType sourceEntityType) {
        BooleanExpression filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.Datastream": {
            filter = oQS.withDatastream(sourceId);
            break;
        }
        case "iot.FeatureOfInterest": {
            filter = oQS.withFeatureOfInterest(sourceId);
            break;
        }
        default:
            return null;
        }
        return filter;
    }

    @Override
    public boolean existsEntity(Long id) {
        return getRepository().exists(byId(id));
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.existsRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        BooleanExpression filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.Datastream": {
            filter = oQS.withDatastream(sourceId);
            break;
        }
        case "iot.FeatureOfInterest": {
            filter = oQS.withFeatureOfInterest(sourceId);
            break;
        }
        default:
            return false;
        }
        if (targetId != null) {
            filter = filter.and(oQS.withId(targetId));
        }
        return getRepository().exists(filter);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<DataEntity<?>> observation = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (observation.isPresent()) {
            return OptionalLong.of(observation.get().getId());
        } else {
            return OptionalLong.empty();
        }
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<DataEntity<?>> observation = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (observation.isPresent()) {
            return mapper.createEntity(observation.get());
        } else {
            return null;
        }
    }

    @Override
    protected String checkPropertyForSorting(String property) {
        switch (property) {
        case "phenomenonTime":
            return DataEntity.PROPERTY_SAMPLING_TIME_START;
        case "validTime":
            return DataEntity.PROPERTY_VALID_TIME_START;
        // case "result":
        // return DataEntity.PROPERTY_VALUE;
        default:
            return super.checkPropertyForSorting(property);
        }
    }

    /**
     * Retrieves Observation Entity with Relation to sourceEntity from Database.
     * Returns empty if Observation is not found or Entities are not related.
     * 
     * @param sourceId
     *            Id of the Source Entity
     * @param sourceEntityType
     *            Type of the Source Entity
     * @param targetId
     *            Id of the Thing to be retrieved
     * @return Optional<DataEntity<?>> Requested Entity
     */
    private Optional<DataEntity<?>> getRelatedEntityRaw(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        BooleanExpression filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.Datastream": {
            filter = oQS.withDatastream(sourceId);
            break;
        }
        case "iot.FeatureOfInterest": {
            filter = oQS.withFeatureOfInterest(sourceId);
            break;
        }
        default:
            return Optional.empty();
        }

        if (targetId != null) {
            filter = filter.and(oQS.withId(targetId));
        }
        return (Optional<DataEntity<?>>) getRepository().findOne(filter);
    }

    /**
     * Constructs SQL Expression to request Entity by ID.
     * 
     * @param id
     *            id of the requested entity
     * @return BooleanExpression evaluating to true if Entity is found and valid
     */
    private BooleanExpression byId(Long id) {
        return oQS.isValidEntity().and(oQS.withId(id));
    }

    @Override
    public DataEntity<?> create(DataEntity<?> entity) throws ODataApplicationException {
        if (entity instanceof StaDataEntity) {
            StaDataEntity observation = (StaDataEntity) entity;
            if (!observation.isProcesssed()) {
                observation.setProcesssed(true);
                check(observation);
                DatastreamEntity datastream = checkDatastream(observation);
                // feature
                AbstractFeatureEntity<?> feature = checkFeature(observation, datastream);
                // category (obdProp)
                CategoryEntity category = checkCategory(datastream);
                // offering (sensor)
                OfferingEntity offering = checkOffering(datastream);
                // dataset
                Dataset dataset = checkDataset(datastream, feature, category, offering);
                // observation
                DataEntity<?> data = checkData(observation, dataset);
                if (data != null) {
                    updateDataset(dataset, data);
                }
                return data;
            }
            return observation;
        }
        return entity;
    }

    private void check(StaDataEntity observation) throws ODataApplicationException {
        if (observation.getFeatureOfInterest() == null || observation.getDatastream() == null) {
            throw new ODataApplicationException("The datastream to create is invalid",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
    }

    @Override
    public DataEntity<?> update(DataEntity<?> entity, HttpMethod method) throws ODataApplicationException {
        if (HttpMethod.PATCH.equals(method)) {
            Optional<DataEntity<?>> existing = getRepository().findOne(oQS.withId(entity.getId()));
            if (existing.isPresent()) {
                DataEntity<?> merged = mapper.merge(existing.get(), entity);
                return getRepository().save(merged);
            }
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        throw new ODataApplicationException("Invalid http method for updating entity!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }

    @Override
    public DataEntity<?> update(DataEntity<?> entity) throws ODataApplicationException {
        return getRepository().save(entity);
    }
    
    @Override
    public void delete(Long id) throws ODataApplicationException {
        if (getRepository().existsById(id)) {
            DataEntity<?> observation = getRepository().getOne(id);
            checkDataset(observation);
            getRepository().deleteById(id);
        } else {
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }
    }

    @Override
    public void delete(DataEntity<?> entity) throws ODataApplicationException {
        getRepository().deleteById(entity.getId());
    }

    private void checkDataset(DataEntity<?> observation) {
        // TODO get the next first/last observation and set it
        DatasetEntity dataset = observation.getDataset();
        if (dataset.getFirstObservation().getId().equals(observation.getId())) {
            dataset.setFirstObservation(null);
            dataset.setFirstQuantityValue(null);
            dataset.setFirstValueAt(null);
        }
        if (dataset.getLastObservation().getId().equals(observation.getId())) {
            dataset.setLastObservation(null);
            dataset.setLastQuantityValue(null);
            dataset.setLastValueAt(null);
        }
        observation.setDataset(datasetRepository.saveAndFlush(dataset));
    }

    private DatastreamEntity checkDatastream(StaDataEntity observation) throws ODataApplicationException {
        DatastreamEntity datastream = getDatastreamService().create(observation.getDatastream());
        observation.setDatastream(datastream);
        return datastream;
    }

    private AbstractFeatureEntity<?> checkFeature(StaDataEntity observation, DatastreamEntity datastream) throws ODataApplicationException {
        if (!observation.hasFeatureOfInterest()) {
            AbstractFeatureEntity<?> feature = null;
            for (LocationEntity location : datastream.getThing().getLocationEntities()) {
                if (feature == null) {
                    feature = featureMapper.createFeatureOfInterest(location);
                }
                if (location.isSetGeometry()) {
                    feature = featureMapper.createFeatureOfInterest(location);
                    break;
                }
            }
            observation.setFeatureOfInterest(feature);
        }
        AbstractFeatureEntity<?> feature = getFeatureOfInterestService().create(observation.getFeatureOfInterest());
        observation.setFeatureOfInterest(feature);
        return feature;
    }

    private OfferingEntity checkOffering(DatastreamEntity datastream) {
        OfferingEntity offering = new OfferingEntity();
        ProcedureEntity procedure = datastream.getProcedure();
        offering.setIdentifier(procedure.getIdentifier());
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
        offering.setObservationTypes(Sets.newHashSet(datastream.getObservationType()));
        
        if (!offeringRepository.existsByIdentifier(offering.getIdentifier())) {
            return offeringRepository.save(offering);
        } else {
            // TODO expand time and geometry if necessary
            return offeringRepository.findByIdentifier(offering.getIdentifier()).get();
        }
    }

    private CategoryEntity checkCategory(DatastreamEntity datastream) {
        CategoryEntity category = new CategoryEntity();
        PhenomenonEntity obsProp = datastream.getObservableProperty();
        category.setIdentifier(obsProp.getIdentifier());
        category.setName(obsProp.getName());
        category.setDescription(obsProp.getDescription());
        if (!categoryRepository.existsByIdentifier(category.getIdentifier())) {
            return categoryRepository.save(category);
        } else {
            return categoryRepository.findByIdentifier(category.getIdentifier()).get();
        }
    }

    private Dataset checkDataset(DatastreamEntity datastream, AbstractFeatureEntity<?> feature, CategoryEntity category,
            OfferingEntity offering) {
       DatasetEntity dataset = getDatasetEntity(datastream.getObservationType().getFormat());
       dataset.setProcedure(datastream.getProcedure());
       dataset.setPhenomenon(datastream.getObservableProperty());
       dataset.setCategory(category);
       dataset.setFeature(feature);
       dataset.setOffering(offering);
       dataset.setUnit(datastream.getUnit());
       dataset.setObservationType(datastream.getObservationType());
       BooleanExpression query = dQS.matchProcedures(Long.toString(datastream.getProcedure().getId()))
                .and(dQS.matchPhenomena(Long.toString(datastream.getObservableProperty().getId()))
                        .and(dQS.matchFeatures(Long.toString(feature.getId())))
                        .and(dQS.matchOfferings(Long.toString(offering.getId()))));
       if (!datasetRepository.exists(query)) {
           return datasetRepository.save(dataset);
       } else {
           return datasetRepository.findOne(query).get();
       }
    }

    private DataEntity<?> checkData(StaDataEntity observation, Dataset dataset) {
        DataEntity<?> data = getDataEntity(observation, dataset);
        if (data != null) {
            return getRepository().save(data);
        }
        return null;
    }

    private Dataset updateDataset(Dataset dataset, DataEntity<?> data) {
        if (!dataset.isSetFirstValueAt() || (dataset.isSetFirstValueAt() && data.getSamplingTimeStart().before(dataset.getFirstValueAt()))) {
            dataset.setFirstValueAt(data.getSamplingTimeStart());
            dataset.setFirstObservation(data);
            if (data instanceof QuantityDataEntity) {
                dataset.setFirstQuantityValue(((QuantityDataEntity) data).getValue());
            }
        }
        if (!dataset.isSetLastValueAt() || (dataset.isSetLastValueAt() && data.getSamplingTimeEnd().after(dataset.getLastValueAt()))) {
            dataset.setLastValueAt(data.getSamplingTimeEnd());
            dataset.setLastObservation(data);
            if (data instanceof QuantityDataEntity) {
                dataset.setLastQuantityValue(((QuantityDataEntity) data).getValue());
            }
        }
        return datasetRepository.save((DatasetEntity) dataset);
    }

    private AbstractSensorThingsEntityService<?, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(EntityTypes.Datastream);
    }

    private AbstractSensorThingsEntityService<?, AbstractFeatureEntity<?>> getFeatureOfInterestService() {
        return (AbstractSensorThingsEntityService<?, AbstractFeatureEntity<?>>) getEntityService(
                EntityTypes.FeatureOfInterest);
    }
    
    private DatasetEntity getDatasetEntity(String observationType) {
        switch (observationType) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                return new QuantityDatasetEntity();
            case OmConstants.OBS_TYPE_CATEGORY_OBSERVATION:
                return new CategoryDatasetEntity();
            case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
                return new CountDatasetEntity();
            case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
                return new TextDatasetEntity();
            case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
                return new BooleanDatasetEntity();
            default:
                return new NotInitializedDatasetEntity();
        }
    }
    
    private DataEntity<?> getDataEntity(StaDataEntity observation, Dataset dataset) {
        DataEntity<?> data = null;
        switch (dataset.getObservationType().getFormat()) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                QuantityDataEntity quantityDataEntity = new QuantityDataEntity();
                if (observation.hasValue()) {
                    quantityDataEntity.setValue(BigDecimal.valueOf(Double.parseDouble(observation.getValue())));
                }
                data = quantityDataEntity;
                break;
            case OmConstants.OBS_TYPE_CATEGORY_OBSERVATION:
                CategoryDataEntity categoryDataEntity = new CategoryDataEntity();
                if (observation.hasValue()) {
                    categoryDataEntity.setValue(observation.getValue());
                }
                data = categoryDataEntity;
                break;
            case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
                CountDataEntity countDataEntity = new CountDataEntity();
                if (observation.hasValue()) {
                    countDataEntity.setValue(Integer.parseInt(observation.getValue()));
                }
                data = countDataEntity;
                break;
            case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
                TextDataEntity textDataEntity = new TextDataEntity();
                if (observation.hasValue()) {
                    textDataEntity.setValue(observation.getValue());
                }
                data = textDataEntity;
                break;
            case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
                BooleanDataEntity booleanDataEntity = new BooleanDataEntity();
                if (observation.hasValue()) {
                    booleanDataEntity.setValue(Boolean.parseBoolean(observation.getValue()));
                }
                data = booleanDataEntity;
                break;
            default:
                break;
        }
        if (data != null) {
            data.setDataset((DatasetEntity) dataset);
            data.setSamplingTimeStart(observation.getSamplingTimeStart());
            data.setSamplingTimeEnd(observation.getSamplingTimeEnd());
            if (observation.getResultTime() != null) {
                data.setResultTime(observation.getResultTime());
            } else {
                data.setResultTime(observation.getSamplingTimeEnd());
            }
            data.setValidTimeStart(observation.getValidTimeStart());
            data.setValidTimeEnd(observation.getValidTimeEnd());
        }
        return data;
    }
}
