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
package org.n52.sta.mapping;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_PARAMETERS;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_PHENOMENON_TIME;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_RESULT;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_RESULT_TIME;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_VALID_TIME;
import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ET_DATASTREAM_NAME;
import static org.n52.sta.edm.provider.entities.FeatureOfInterestEntityProvider.ET_FEATURE_OF_INTEREST_NAME;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ES_OBSERVATIONS_NAME;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ET_OBSERVATION_FQN;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.n52.janmayen.Json;
import org.n52.series.db.beans.BlobDataEntity;
import org.n52.series.db.beans.BooleanDataEntity;
import org.n52.series.db.beans.CategoryDataEntity;
import org.n52.series.db.beans.ComplexDataEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataArrayDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.GeometryDataEntity;
import org.n52.series.db.beans.ProfileDataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ReferencedDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.parameter.Parameter;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.util.DateTimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class ObservationMapper extends AbstractMapper<DataEntity<?>> {
    
    @Autowired
    private DatastreamMapper datastreamMapper;
    
    @Autowired
    private FeatureOfInterestMapper featureMapper;

    public Entity createEntity(DataEntity<?> observation) {
        Entity entity = new Entity();

        entity.addProperty(new Property(null, PROP_ID, ValueType.PRIMITIVE, observation.getId()));
        
        //TODO: urlencode whitespaces to allow for copy pasting into filter expression
        entity.addProperty(new Property(null, PROP_RESULT, ValueType.PRIMITIVE, this.getResult(observation).getBytes()));

        Date resultTime = observation.getResultTime();
        Date samplingTime = observation.getSamplingTimeEnd();
        entity.addProperty(new Property(null, PROP_RESULT_TIME, ValueType.PRIMITIVE, (resultTime.equals(samplingTime)) ? null : resultTime.getTime()));

        String phenomenonTime = DateTimeHelper.format(createPhenomenonTime(observation));
        entity.addProperty(new Property(null, PROP_PHENOMENON_TIME, ValueType.PRIMITIVE, phenomenonTime));
        
        
        entity.addProperty(new Property(null, PROP_VALID_TIME, ValueType.PRIMITIVE, (observation.isSetValidTime())
                                                                                    ? DateTimeHelper.format(createValidTime(observation))
                                                                                    : null));

        // TODO: check for quality property
        // entity.addProperty(new Property(null, PROP_RESULT_QUALITY,
        // ValueType.PRIMITIVE, null));
        // List<JsonNode> parameters = observation.getParameters().stream()
        // .map(p -> createParameterProperty(p))
        // .collect(Collectors.toList());
        List<ComplexValue> parameters = observation.getParameters().stream().map(p -> createParameterComplexValue(p))
                .collect(Collectors.toList());

        entity.addProperty(new Property(null, PROP_PARAMETERS, ValueType.COLLECTION_COMPLEX, parameters));

        entity.setType(ET_OBSERVATION_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_OBSERVATIONS_NAME, PROP_ID));

        return entity;
    }

    private String getResult(DataEntity o) {
        if (o instanceof QuantityDataEntity) {
            if ((((QuantityDataEntity) o).getValue().doubleValue() - ((QuantityDataEntity) o).getValue().intValue()) == 0.0) {
                return Integer.toString(((QuantityDataEntity) o).getValue().intValue());
            }
            return ((QuantityDataEntity) o).getValue().toString();
        } else if (o instanceof BlobDataEntity) {
            // TODO: check if Object.tostring is what we want here
            return ((BlobDataEntity) o).getValue().toString();
        } else if (o instanceof BooleanDataEntity) {
            return ((BooleanDataEntity) o).getValue().toString();
        } else if (o instanceof CategoryDataEntity) {
            return ((CategoryDataEntity) o).getValue();
        } else if (o instanceof ComplexDataEntity) {

            // TODO: implement
            // return ((ComplexDataEntity)o).getValue();
            return null;

        } else if (o instanceof CountDataEntity) {
            return ((CountDataEntity) o).getValue().toString();
        } else if (o instanceof GeometryDataEntity) {

            // TODO: check if we want WKT here
            return ((GeometryDataEntity) o).getValue().getGeometry().toText();

        } else if (o instanceof TextDataEntity) {
            return ((TextDataEntity) o).getValue();
        } else if (o instanceof DataArrayDataEntity) {

            // TODO: implement
            // return ((DataArrayDataEntity)o).getValue();
            return null;

        } else if (o instanceof ProfileDataEntity) {

            // TODO: implement
            // return ((ProfileDataEntity)o).getValue();
            return null;

        } else if (o instanceof ReferencedDataEntity) {
            return ((ReferencedDataEntity) o).getValue();
        }
        return "";
    }

    private JsonNode createParameterProperty(Parameter<?> p) {
        return Json.nodeFactory().objectNode().put(p.getName(), p.getValueAsString());
    }

    private ComplexValue createParameterComplexValue(Parameter<?> p) {
        ComplexValue cv = new ComplexValue();
        cv.getValue().add(new Property(null, null, ValueType.PRIMITIVE, createParameterProperty(p)));
        return cv;
    }
    
    private Time createPhenomenonTime(DataEntity<?> observation) {
        final DateTime start = createDateTime(observation.getSamplingTimeStart());
        DateTime end;
        if (observation.getSamplingTimeEnd() != null) {
            end = createDateTime(observation.getSamplingTimeEnd());
        } else {
            end = start;
        }
        return createTime(start, end);
    }
    
    private Time createValidTime(DataEntity<?> observation) {
        final DateTime start = createDateTime(observation.getValidTimeStart());
        DateTime end;
        if (observation.getValidTimeEnd() != null) {
            end = createDateTime(observation.getValidTimeEnd());
        } else {
            end = start;
        }
        return createTime(start, end);
    }

    public StaDataEntity createEntity(Entity entity) {
        StaDataEntity observation = new StaDataEntity();
        setId(observation, entity);
        setName(observation, entity);
        setDescription(observation, entity);
        addPhenomenonTime(observation, entity);
        addResultTime(observation, entity);
        addValidTime(observation, entity);
        addParameter(observation, entity);
        addFeatureOfInterest(observation, entity);
        addDatastream(observation, entity);
        addResult(observation, entity);
        return observation;
    }
    
    private void addResult(StaDataEntity observation, Entity entity) {
        if (checkProperty(entity, PROP_RESULT)) {
            observation.setValue(new String((byte[])getPropertyValue(entity, PROP_RESULT)));
        }
    }

    private void addResultTime(StaDataEntity observation, Entity entity) {
        if (checkProperty(entity, PROP_RESULT_TIME)) {
            Time time = parseTime(getPropertyValue(entity, PROP_RESULT_TIME));
            observation.setResultTime(((TimeInstant) time).getValue().toDate());
        }
    }
    
    private void addValidTime(StaDataEntity observation, Entity entity) {
        if (checkProperty(entity, PROP_RESULT_TIME)) {
            Time time = parseTime(getPropertyValue(entity, PROP_RESULT_TIME));
            if (time instanceof TimeInstant) {
                observation.setValidTimeStart(((TimeInstant) time).getValue().toDate());
                observation.setValidTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                observation.setValidTimeStart(((TimePeriod) time).getStart().toDate());
                observation.setValidTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }
    }

    private void addParameter(StaDataEntity observation, Entity entity) {
        // TODO Auto-generated method stub
        
    }

    private void addFeatureOfInterest(StaDataEntity observation, Entity entity) {
        if (checkNavigationLink(entity, ET_FEATURE_OF_INTEREST_NAME)) {
            observation.setFeatureOfInterest(featureMapper
                    .createEntity(entity.getNavigationLink(ET_FEATURE_OF_INTEREST_NAME).getInlineEntity()));
        }
    }

    private void addDatastream(StaDataEntity observation, Entity entity) {
        if (checkNavigationLink(entity, ET_DATASTREAM_NAME)) {
            observation.setDatastream(datastreamMapper
                    .createEntity(entity.getNavigationLink(ET_DATASTREAM_NAME).getInlineEntity()));
        }
    }

    @Override
    public DataEntity<?> merge(DataEntity<?> existing, DataEntity<?> toMerge) throws ODataApplicationException {
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
        // value
        if (toMerge.getValue() != null) {
            checkValue(existing, toMerge);
        }
        return existing;
    }

    protected void mergeSamplingTimeAndCheckResultTime(DataEntity<?> existing, DataEntity<?> toMerge) {
        if (toMerge.hasSamplingTimeEnd() && existing.getSamplingTimeEnd().equals(existing.getResultTime())) {
            existing.setResultTime(toMerge.getSamplingTimeEnd());
        }
        super.mergeSamplingTime(existing, toMerge);
    }
    
    private void checkValue(DataEntity<?> existing, DataEntity<?> toMerge) throws ODataApplicationException {
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
            throw new ODataApplicationException(
                    String.format("The observation value for @iot.id %i can not be updated!", existing.getId()),
                    HttpStatusCode.CONFLICT.getStatusCode(), Locale.getDefault());
        }
    }

    @Override
    public Entity checkEntity(Entity entity) throws ODataApplicationException {
        checkPropertyValidity(PROP_PHENOMENON_TIME, entity);
        checkPropertyValidity(PROP_RESULT, entity);
//        checkPropertyValidity(PROP_RESULT_TIME, entity);
        if (checkNavigationLink(entity, ET_FEATURE_OF_INTEREST_NAME)) {
            featureMapper
                    .checkNavigationLink(entity.getNavigationLink(ET_FEATURE_OF_INTEREST_NAME).getInlineEntity());
        }
        if (checkNavigationLink(entity, ET_DATASTREAM_NAME)) {
            datastreamMapper
                    .checkNavigationLink(entity.getNavigationLink(ET_DATASTREAM_NAME).getInlineEntity());
        }
        return entity;
    }

}
