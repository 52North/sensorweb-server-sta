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

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_PARAMETERS;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_PHENOMENON_TIME;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_RESULT;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_RESULT_TIME;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_VALID_TIME;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ES_OBSERVATIONS_NAME;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ET_OBSERVATION_FQN;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.joda.time.DateTime;
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
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.util.DateTimeHelper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class ObservationMapper extends AbstractMapper<DataEntity<?>> {

    public Entity createEntity(DataEntity<?> observation) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, ID_ANNOTATION, ValueType.PRIMITIVE, observation.getId()));
        entity.addProperty(new Property(null, PROP_RESULT, ValueType.PRIMITIVE, this.getResult(observation)));
        
        entity.addProperty(new Property(null, PROP_RESULT_TIME, ValueType.PRIMITIVE,
                DateTimeHelper.format(createResultTime(observation))));

        String phenomenonTime = DateTimeHelper.format(createPhenomenonTime(observation));
        entity.addProperty(new Property(null, PROP_PHENOMENON_TIME, ValueType.PRIMITIVE, phenomenonTime));

        if (observation.isSetValidTime()) {
            entity.addProperty(new Property(null, PROP_VALID_TIME, ValueType.PRIMITIVE,
                    DateTimeHelper.format(createValidTime(observation))));
        }

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
        entity.setId(entityCreationHelper.createId(entity, ES_OBSERVATIONS_NAME, ID_ANNOTATION));

        return entity;
    }

    private String getResult(DataEntity o) {
        if (o instanceof QuantityDataEntity) {
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
    
    private Time createResultTime(DataEntity<?> observation) {
        return createTime(createDateTime(observation.getResultTime()));
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

}
