/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.repositories.entity.ObservationRepository;
import org.n52.sta.data.support.GraphBuilder;
import org.n52.sta.data.support.ObservationGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class ObservationEntityEditor extends DatabaseEntityAdapter<DataEntity>
        implements
        ObservationEditorDelegate<Observation, ObservationData> {

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private ValueHelper valueHelper;

    @Autowired
    private EntityPropertyMapping propertyMapping;

    private DatastreamEditorDelegate<Datastream, DatastreamData> datastreamEditor;

    public ObservationEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.datastreamEditor = (DatastreamEditorDelegate<Datastream, DatastreamData>)
                getService(Datastream.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public ObservationData getOrSave(Observation entity) throws EditorException {
        Optional<DataEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new ObservationData(e, propertyMapping))
                     .orElseGet(() -> save(entity));
    }

    @Override
    public ObservationData save(Observation entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");
        assertNew(entity);

        DatasetEntity datastream = (DatasetEntity) getDatastreamOf(entity);
        return Streams.stream(saveAll(Collections.singleton(entity), datastream))
                      .map(savedEntity -> new ObservationData(savedEntity, propertyMapping))
                      .findFirst()
                      .orElseThrow();
    }

    @Override
    public ObservationData update(Observation oldEntity, Observation updateEntity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        DataEntity< ? > data = getEntity(id)
                                            .orElseThrow(() -> new EditorException("could not find entity with id: "
                                                    + id));

        // Delete from first/last if present there
        boolean changedFirst = datastreamEditor.removeAsFirstObservation(data);
        boolean changedLast = datastreamEditor.removeAsLastObservation(data);

        // Delete observation
        observationRepository.deleteByStaIdentifier(id);

        // We have deleted First/Last Observation so we need to refresh it
        // Optimized to reduce Database queries over observations table
        DataEntity< ? > start = null;
        DataEntity< ? > end = null;
        if (changedFirst) {
            start = observationRepository.findFirstByDataset_idOrderBySamplingTimeStartAsc(
                                                                                           data.getDatasetId())
                                         .orElse(null);
        }
        if (changedLast) {
            end = observationRepository.findFirstByDataset_idOrderBySamplingTimeEndDesc(
                                                                                        data.getDatasetId())
                                       .orElse(null);
        }

        if (changedFirst || changedLast) {
            // we have other observations that now form first/last observation
            datastreamEditor.updateFirstLastObservation(data.getDataset(), start, end);
        }
    }

    @Override
    protected Optional<DataEntity> getEntity(String id) {
        GraphBuilder<DataEntity> graphBuilder = ObservationGraphBuilder.createEmpty();
        return observationRepository.findByStaIdentifier(id, graphBuilder);
    }

    // TODO: check why we need this method. Currently only called with Collection.singleton in #save
    private Set<DataEntity< ? >> saveAll(Set<Observation> observations, DatasetEntity datasetEntity)
            throws EditorException {
        Objects.requireNonNull(observations, "observations must not be null");
        Objects.requireNonNull(datasetEntity, "datasetEntity must not be null");
        Set<DataEntity< ? >> entities = Streams.stream(observations)
                                               .map(o -> createEntity(o, datasetEntity))
                                               .collect(Collectors.toSet());
        return Streams.stream(observationRepository.saveAll(entities))
                      .collect(Collectors.toSet());
    }

    private AbstractDatasetEntity getDatastreamOf(Observation entity) throws EditorException {
        return datastreamEditor.getOrSave(entity.getDatastream())
                               .getData();
        // return datastreamEditor.getEntity(datastream.getId())
        // .orElseThrow(() -> new IllegalStateException("Datastream not found for Observation!"));
    }

    private DataEntity< ? > createEntity(Observation observation, DatasetEntity datasetEntity) throws EditorException {
        FormatEntity formatEntity = datasetEntity.getOmObservationType();
        Object value = observation.getResult();
        String format = formatEntity.getFormat();
        DataEntity< ? > dataEntity;
        switch (format) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                QuantityDataEntity quantityObservationEntity = new QuantityDataEntity();
                if (value == null || value.equals("NaN") || value.equals("Inf") || value.equals("-Inf")) {
                    quantityObservationEntity.setValue(null);
                } else {
                    double doubleValue = value instanceof String
                            ? Double.parseDouble((String) value)
                            : (double) value;
                    quantityObservationEntity.setValue(BigDecimal.valueOf(doubleValue));
                }
                dataEntity = initDataEntity(quantityObservationEntity, observation, datasetEntity);
                // we need to set valueType manually as it is not yet autogenerated by the DB but needed for
                // response
                // serialization.
                dataEntity.setValueType("quantity");
                break;

            // TODO add further observation types
            default:
                throw new EditorException("Unknown OMObservation type: " + format);
        }

        return dataEntity;
    }

    private DataEntity< ? > initDataEntity(DataEntity< ? > data, Observation observation, DatasetEntity dataset) {

        // metadata
        String id = observation.getId() == null
                ? generateId()
                : observation.getId();
        data.setIdentifier(id);
        data.setStaIdentifier(id);

        // values
        Time phenomenonTime = observation.getPhenomenonTime();
        valueHelper.setStartTime(data::setSamplingTimeStart, phenomenonTime);
        valueHelper.setEndTime(data::setSamplingTimeEnd, phenomenonTime);

        Time validTime = observation.getValidTime();
        valueHelper.setStartTime(data::setValidTimeStart, validTime);
        valueHelper.setEndTime(data::setValidTimeEnd, validTime);

        // SHOULD assign null value
        // see 18-088 Section 10.2 Special case #2
        if (observation.getResultTime() != null) {
            Time resultTime = observation.getResultTime();
            valueHelper.setTime(data::setResultTime, (TimeInstant) resultTime);

        }
        Map<String, Object> parameters = observation.getParameters();
        Streams.stream(parameters.entrySet())
               .map(e -> convertParameter(data, e))
               .forEach(data::addParameter);

        // following parameters have to be set explicitly, too
        if (parameters.containsKey(propertyMapping.getSamplingGeometry())) {
            GeometryEntity geometryEntity = valueToGeometry(propertyMapping.getSamplingGeometry(), parameters);
            data.setGeometryEntity(geometryEntity);
        }

        if (parameters.containsKey(propertyMapping.getVerticalFrom())) {
            BigDecimal verticalFrom = valueToDouble(propertyMapping.getVerticalFrom(), parameters);
            data.setVerticalFrom(verticalFrom);
        }

        if (parameters.containsKey(propertyMapping.getVerticalTo())) {
            BigDecimal verticalTo = valueToDouble(propertyMapping.getVerticalTo(), parameters);
            data.setVerticalFrom(verticalTo);
        }

        if (parameters.containsKey(propertyMapping.getVerticalFromTo())) {
            BigDecimal verticalFromTo = valueToDouble(propertyMapping.getVerticalFromTo(), parameters);
            data.setVerticalFrom(verticalFromTo);
            data.setVerticalTo(verticalFromTo);
        }

        // references
        data.setDataset(dataset);
        return data;
    }

    private BigDecimal valueToDouble(String parameter, Map<String, Object> parameters) {
        Object value = parameters.get(parameter);
        Double doubleValue = value instanceof String
                ? Double.parseDouble((String) value)
                : (Double) value;
        return BigDecimal.valueOf(doubleValue);
    }

    private GeometryEntity valueToGeometry(String parameter, Map<String, Object> parameters) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    private void assertNew(Observation observation) throws EditorException {
        String staIdentifier = observation.getId();
        if (getEntity(staIdentifier).isPresent()) {
            throw new EditorException("Observation already exists with ID '" + staIdentifier + "'");
        }
    }

    @Override
    public void deleteObservationsByDatasetId(Set<Long> ids) {
        observationRepository.deleteAllByDatasetIdIn(ids);
    }

}
