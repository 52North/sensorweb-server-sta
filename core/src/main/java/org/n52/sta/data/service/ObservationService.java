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
import org.n52.series.db.beans.BooleanDataEntity;
import org.n52.series.db.beans.CategoryDataEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.ObservationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.DatasetQuerySpecifications;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.ObservationQuerySpecifications;
import org.n52.sta.data.repositories.CategoryRepository;
import org.n52.sta.data.repositories.DataRepository;
import org.n52.sta.data.repositories.DatasetRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.OfferingRepository;
import org.n52.sta.data.repositories.ParameterRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class ObservationService extends
        AbstractSensorThingsEntityService<DataRepository<DataEntity<?>>, DataEntity<?>, StaDataEntity<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationService.class);

    private static final ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    private static final DatasetQuerySpecifications dQS = new DatasetQuerySpecifications();
    private static final DatastreamQuerySpecifications dsQS = new DatastreamQuerySpecifications();

    private static final String STA = "STA";

    private final boolean isMobileFeatureEnabled;
    private final CategoryRepository categoryRepository;
    private final OfferingRepository offeringRepository;
    private final DatastreamRepository datastreamRepository;
    private final DatasetRepository datasetRepository;
    private final ParameterRepository parameterRepository;
    private final Pattern isMobilePattern = Pattern.compile(".*\"isMobile\":true.*");

    @Autowired
    public ObservationService(DataRepository<DataEntity<?>> repository,
                              CategoryRepository categoryRepository,
                              OfferingRepository offeringRepository,
                              DatastreamRepository datastreamRepository,
                              DatasetRepository datasetRepository,
                              ParameterRepository parameterRepository,
                              @Value("${server.feature.isMobile:false}") boolean isMobileFeatureEnabled) {
        super(repository,
              DataEntity.class,
              EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);
        this.categoryRepository = categoryRepository;
        this.offeringRepository = offeringRepository;
        this.datastreamRepository = datastreamRepository;
        this.datasetRepository = datasetRepository;
        this.parameterRepository = parameterRepository;
        this.isMobileFeatureEnabled = isMobileFeatureEnabled;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Observation, EntityTypes.Observations};
    }

    @Override
    protected StaDataEntity<?> fetchExpandEntities(DataEntity<?> entity,
                                                   ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        StaDataEntity<?> returned = new StaDataEntity(entity);
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (ObservationEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                ElementWithQueryOptions<?> expandedEntity;
                switch (expandProperty) {
                case STAEntityDefinition.DATASTREAM:
                    expandedEntity = getDatastreamService()
                            .getEntityByRelatedEntity(entity.getIdentifier(),
                                                      DataEntity.class.getSimpleName(),
                                                      null,
                                                      expandItem.getQueryOptions());
                    returned.setDatastream((DatastreamEntity) expandedEntity.getEntity());
                    break;
                case STAEntityDefinition.FEATURE_OF_INTEREST:
                    expandedEntity = getFeatureOfInterestService()
                            .getEntityByRelatedEntity(entity.getIdentifier(),
                                                      DataEntity.class.getSimpleName(),
                                                      null,
                                                      expandItem.getQueryOptions());
                    returned.setFeatureOfInterest((AbstractFeatureEntity<?>) expandedEntity.getEntity());
                    break;
                default:
                    throw new RuntimeException("This can never happen!");
                }
            } else {
                throw new STAInvalidQueryException("Invalid expandOption supplied. Cannot find " + expandProperty +
                                                           " on Entity of type 'Observations'");
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
            filter = oQS.withDatastreamIdentifier(relatedId);
            break;
        }
        case STAEntityDefinition.FEATURES_OF_INTEREST: {
            filter = oQS.withFeatureOfInterestIdentifier(relatedId);
            break;
        }
        default:
            return null;
        }
        if (ownId != null) {
            filter = filter.and(oQS.withIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
        case "phenomenonTime":
            // TODO: proper ISO8601 comparison
            return DataEntity.PROPERTY_SAMPLING_TIME_END;
        case "result":
            return DataEntity.PROPERTY_VALUE;
        default:
            return super.checkPropertyName(property);
        }
    }

    @Override
    public DataEntity<?> createEntity(DataEntity<?> entity) throws STACRUDException {
        if (entity instanceof StaDataEntity) {
            StaDataEntity observation = (StaDataEntity) entity;
            if (!observation.isProcessed()) {
                observation.setProcessed(true);
                check(observation);

                DatastreamEntity datastream = getDatastreamService().createEntity(observation.getDatastream());
                observation.setDatastream(datastream);

                // Fetch with all needed associations
                datastream = datastreamRepository
                        .findByIdentifier(datastream.getIdentifier(),
                                          EntityGraphRepository.FetchGraph.FETCHGRAPH_THINGLOCATION,
                                          EntityGraphRepository.FetchGraph.FETCHGRAPH_PROCEDURE,
                                          EntityGraphRepository.FetchGraph.FETCHGRAPH_UOM,
                                          EntityGraphRepository.FetchGraph.FETCHGRAPH_OBS_TYPE,
                                          EntityGraphRepository.FetchGraph.FETCHGRAPH_OBSERVABLE_PROP,
                                          EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS
                        ).get();

                AbstractFeatureEntity<?> feature = checkFeature(observation, datastream);
                // category (obdProp)
                CategoryEntity category = checkCategory(datastream);
                // offering (sensor)
                OfferingEntity offering = checkOffering(datastream);
                // dataset
                DatasetEntity dataset = checkDataset(datastream, feature, category, offering);
                // observation
                DataEntity<?> data = checkData(observation, dataset);
                if (data != null) {
                    updateDataset(dataset, data);
                    updateDatastream(datastream, dataset, data);
                }
                return data;
            }
            return observation;
        }
        return entity;
    }

    private void check(StaDataEntity observation) throws STACRUDException {
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
            List<DatastreamEntity> datastreams, DataEntity<?> observation) {
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
                DataEntity<?> firstObservation = getRepository()
                        .findFirstByDataset_idInOrderBySamplingTimeStartAsc(datasetIds);
                Date newPhenomenonStart = (firstObservation == null) ? null : firstObservation.getPhenomenonTimeStart();

                // Set Start and End to null if there is no observation.
                if (newPhenomenonStart == null) {
                    datastreamEntity.setPhenomenonTimeStart(null);
                    datastreamEntity.setPhenomenonTimeEnd(null);
                } else {
                    datastreamEntity.setPhenomenonTimeStart(newPhenomenonStart);

                    // Setting new phenomenonTimeEnd
                    DataEntity<?> lastObservation = getRepository()
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
    public DataEntity<?> updateEntity(String id, DataEntity<?> entity, HttpMethod method) throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            Optional<DataEntity<?>> existing =
                    getRepository().findByIdentifier(id,
                                                     EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);
            if (existing.isPresent()) {
                DataEntity<?> merged = merge(existing.get(), entity);
                DataEntity<?> saved = getRepository().save(merged);

                List<DatastreamEntity> datastreamEntity =
                        datastreamRepository.findAll(dsQS.withObservationIdentifier(saved.getIdentifier()),
                                                     EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS);
                if (!datastreamEntity.isEmpty()) {
                    updateDatastreamPhenomenonTimeOnObservationUpdate(datastreamEntity, saved);
                }
                return saved;
            }
            throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    @Override
    public DataEntity<?> updateEntity(DataEntity<?> entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(String identifier) throws STACRUDException {
        if (getRepository().existsByIdentifier(identifier)) {
            DataEntity<?> observation =
                    getRepository().findByIdentifier(identifier,
                                                     EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASET)
                                   .get();
            checkDataset(observation);
            delete(observation);
        } else {
            throw new STACRUDException("Unable to delete. Entity not found.", HTTPStatus.NOT_FOUND);
        }
    }

    @Override
    public void delete(DataEntity<?> entity) {
        List<DatastreamEntity> datastreamEntity =
                datastreamRepository.findAll(dsQS.withObservationIdentifier(entity.getIdentifier()),
                                             EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS);
        // Important! Delete first and then update else we find ourselves again in search for new latest/earliest obs.
        getRepository().deleteByIdentifier(entity.getIdentifier());
        if (!datastreamEntity.isEmpty()) {
            updateDatastreamPhenomenonTimeOnObservationUpdate(datastreamEntity, entity);
        }
    }

    @Override
    protected DataEntity<?> createOrUpdate(DataEntity<?> entity) throws STACRUDException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return updateEntity(entity.getIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    private void checkDataset(DataEntity<?> observation) {
        // TODO get the next first/last observation and set it
        DatasetEntity dataset = observation.getDataset();
        if (dataset.getFirstObservation() != null
                && dataset.getFirstObservation().getIdentifier().equals(observation.getIdentifier())) {
            dataset.setFirstObservation(null);
            dataset.setFirstQuantityValue(null);
            dataset.setFirstValueAt(null);
        }
        if (dataset.getLastObservation() != null && dataset.getLastObservation()
                                                           .getIdentifier()
                                                           .equals(observation.getIdentifier())) {
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
                                       OfferingEntity offering) {
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
        Specification<DatasetEntity> query = dQS.matchProcedures(datastream.getProcedure().getIdentifier())
                                                .and(dQS.matchPhenomena(datastream.getObservableProperty()
                                                                                  .getIdentifier())
                                                        .and(dQS.matchFeatures(feature.getIdentifier()))
                                                        .and(dQS.matchOfferings(offering.getIdentifier())));
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

    DatastreamEntity checkDatastream(StaDataEntity observation) throws STACRUDException {
        DatastreamEntity datastream = getDatastreamService().createEntity(observation.getDatastream());
        observation.setDatastream(datastream);
        return datastream;
    }

    private AbstractFeatureEntity<?> checkFeature(StaDataEntity observation, DatastreamEntity datastream)
            throws STACRUDException {
        if (!observation.hasFeatureOfInterest()) {
            AbstractFeatureEntity<?> feature = null;
            for (LocationEntity location : datastream.getThing().getLocations()) {
                //TODO: check why this looks weird.
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
            observation.setFeatureOfInterest(feature);
        }
        AbstractFeatureEntity<?> feature = getFeatureOfInterestService()
                .createEntity(observation.getFeatureOfInterest());
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
        HashSet<FormatEntity> set = new HashSet<>();
        set.add(datastream.getObservationType());
        offering.setObservationTypes(set);

        if (!offeringRepository.existsByIdentifier(offering.getIdentifier())) {
            return offeringRepository.save(offering);
        } else {
            // TODO expand time and geometry if necessary
            return offeringRepository.findByIdentifier(offering.getIdentifier()).get();
        }
    }

    private CategoryEntity checkCategory(DatastreamEntity datastream) {
        CategoryEntity category = new CategoryEntity();
        // PhenomenonEntity obsProp = datastream.getObservableProperty();
        category.setIdentifier(STA);
        category.setName(STA);
        category.setDescription("Default SOS category");
        if (!categoryRepository.existsByIdentifier(category.getIdentifier())) {
            return categoryRepository.save(category);
        } else {
            return categoryRepository.findByIdentifier(category.getIdentifier()).get();
        }
    }

    private DataEntity<?> checkData(StaDataEntity observation, DatasetEntity dataset) throws STACRUDException {
        DataEntity<?> data = getDataEntity(observation, dataset);
        if (data != null) {
            return getRepository().save(data);
        }
        return null;
    }

    private DatasetEntity updateDataset(DatasetEntity dataset, DataEntity<?> data) {
        if (!dataset.isSetFirstValueAt()
                || (dataset.isSetFirstValueAt() && data.getSamplingTimeStart().before(dataset.getFirstValueAt()))) {
            dataset.setFirstValueAt(data.getSamplingTimeStart());
            dataset.setFirstObservation(data);
            if (data instanceof QuantityDataEntity) {
                dataset.setFirstQuantityValue(((QuantityDataEntity) data).getValue());
            }
        }
        if (!dataset.isSetLastValueAt()
                || (dataset.isSetLastValueAt() && data.getSamplingTimeEnd().after(dataset.getLastValueAt()))) {
            dataset.setLastValueAt(data.getSamplingTimeEnd());
            dataset.setLastObservation(data);
            if (data instanceof QuantityDataEntity) {
                dataset.setLastQuantityValue(((QuantityDataEntity) data).getValue());
            }
        }
        return datasetRepository.save(dataset);
    }

    private void updateDatastream(DatastreamEntity datastream, DatasetEntity dataset, DataEntity<?> data)
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

    private DataEntity<?> getDataEntity(StaDataEntity observation, DatasetEntity dataset)
            throws STACRUDException {
        DataEntity<?> data = null;
        String value = (String) observation.getValue();
        switch (dataset.getOmObservationType().getFormat()) {
        case OmConstants.OBS_TYPE_MEASUREMENT:
            QuantityDataEntity quantityDataEntity = new QuantityDataEntity();
            if (observation.hasValue()) {
                if (value.equals("NaN") || value.equals("Inf") || value.equals("-Inf")) {
                    quantityDataEntity.setValue(null);
                } else {
                    quantityDataEntity.setValue(BigDecimal.valueOf(Double.parseDouble(value)));
                }
            }
            data = quantityDataEntity;
            break;
        case OmConstants.OBS_TYPE_CATEGORY_OBSERVATION:
            CategoryDataEntity categoryDataEntity = new CategoryDataEntity();
            if (observation.hasValue()) {
                categoryDataEntity.setValue(value);
            }
            data = categoryDataEntity;
            break;
        case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
            CountDataEntity countDataEntity = new CountDataEntity();
            if (observation.hasValue()) {
                countDataEntity.setValue(Integer.parseInt(value));
            }
            data = countDataEntity;
            break;
        case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
            TextDataEntity textDataEntity = new TextDataEntity();
            if (observation.hasValue()) {
                textDataEntity.setValue(value);
            }
            data = textDataEntity;
            break;
        case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
            BooleanDataEntity booleanDataEntity = new BooleanDataEntity();
            if (observation.hasValue()) {
                booleanDataEntity.setValue(Boolean.parseBoolean(value));
            }
            data = booleanDataEntity;
            break;
        default:
            break;
        }
        if (data != null) {
            data.setDataset(dataset);
            if (observation.getIdentifier() != null) {
                if (getRepository().existsByIdentifier(observation.getIdentifier())) {
                    throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
                } else {
                    data.setIdentifier(observation.getIdentifier());
                }
            } else {
                data.setIdentifier(UUID.randomUUID().toString());
            }
            data.setSamplingTimeStart(observation.getSamplingTimeStart());
            data.setSamplingTimeEnd(observation.getSamplingTimeEnd());
            if (observation.getResultTime() != null) {
                data.setResultTime(observation.getResultTime());
            } else {
                data.setResultTime(observation.getSamplingTimeEnd());
            }
            data.setValidTimeStart(observation.getValidTimeStart());
            data.setValidTimeEnd(observation.getValidTimeEnd());

            data.setGeometryEntity(observation.getGeometryEntity());

            if (observation.getParameters() != null) {
                parameterRepository.saveAll(observation.getParameters());
                data.setParameters(observation.getParameters());
            }
        }
        return data;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    @Override
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        DataEntity<?> entity = (DataEntity<?>) rawObject;

        if (entity.getDataset() != null && entity.getDataset().getFeature() != null) {
            collections.put(STAEntityDefinition.FEATURES_OF_INTEREST,
                            Collections.singleton(entity.getDataset().getFeature().getIdentifier()));
        }

        Optional<DatastreamEntity> datastreamEntity =
                datastreamRepository.findOne(dsQS.withObservationIdentifier(entity.getIdentifier()));
        if (datastreamEntity.isPresent()) {
            collections.put(STAEntityDefinition.DATASTREAMS,
                            Collections.singleton(datastreamEntity.get().getIdentifier()));
        } else {
            LOGGER.debug("No Datastream associated with this Entity {}", entity.getIdentifier());
        }
        return collections;
    }

    @Override
    public DataEntity<?> merge(DataEntity<?> existing, DataEntity<?> toMerge) throws STACRUDException {
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
            parameterRepository.saveAll(toMerge.getParameters());
            existing.getParameters().forEach(parameterRepository::delete);
            existing.setParameters(toMerge.getParameters());
        }
        // value
        if (toMerge.getValue() != null) {
            checkValue(existing, toMerge);
        }
        return existing;
    }

    protected void mergeSamplingTimeAndCheckResultTime(DataEntity<?> existing, DataEntity<?> toMerge) {
        if (toMerge.getSamplingTimeEnd() != null && existing.getSamplingTimeEnd().equals(existing.getResultTime())) {
            existing.setResultTime(toMerge.getSamplingTimeEnd());
        }
        super.mergeSamplingTime(existing, toMerge);
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
                    String.format("The observation value for @iot.id %s can not be updated!", existing.getIdentifier()),
                    HTTPStatus.CONFLICT);
        }
    }
}
