/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ENCODINGTYPE;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_METADATA;
import static org.n52.sta.edm.provider.entities.SensorEntityProvider.ES_SENSORS_NAME;
import static org.n52.sta.edm.provider.entities.SensorEntityProvider.ET_SENSOR_FQN;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.joda.time.DateTime;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ET_DATASTREAM_NAME;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class SensorMapper extends AbstractMapper<ProcedureEntity> {

    private static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";

    private static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";

    @Autowired
    EntityCreationHelper entityCreationHelper;

    public Entity createEntity(ProcedureEntity sensor) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, PROP_ID, ValueType.PRIMITIVE, sensor.getIdentifier()));
        addNameDescriptionProperties(entity, sensor);
        entity.addProperty(new Property(null, PROP_ENCODINGTYPE, ValueType.PRIMITIVE, checkQueryEncodingType(sensor.getFormat().getFormat())));
        String metadata = "metadata";
        if (sensor.getDescriptionFile() != null && !sensor.getDescriptionFile().isEmpty()) {
            metadata = sensor.getDescriptionFile();
        } else if (sensor.hasProcedureHistory()) {
            Optional<ProcedureHistoryEntity> history =
                    sensor.getProcedureHistory().stream().filter(h -> h.getEndTime() == null).findFirst();
            if (history.isPresent()) {
                metadata =  history.get().getXml();
            }
        }
        entity.addProperty(new Property(null, PROP_METADATA, ValueType.PRIMITIVE, metadata));
        entity.setType(ET_SENSOR_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_SENSORS_NAME, PROP_ID));

        return entity;
    }

    public Entity createEntity(SensorEntity sensor) {
        return createEntity(sensor);
    }

    public SensorEntity createAndCheckEntity(Entity entity) throws ODataApplicationException {
        checkEntity(entity);
        return createEntity(entity);
    }

    public SensorEntity createEntity(Entity entity) {
        SensorEntity sensor = new SensorEntity();
        setId(sensor, entity);
        setIdentifier(sensor, entity);
        setName(sensor, entity);
        setDescription(sensor, entity);
        sensor.setFormat(createFormat(entity));
        if (sensor.getFormat().getFormat().equalsIgnoreCase(SENSORML_2)) {
            sensor.setProcedureHistory(createProcedureHistory(sensor, entity));
        } else {
            sensor.setDescriptionFile(getPropertyValue(entity, PROP_METADATA).toString());
        }
        setDatastreams(sensor, entity);
        return sensor;
    }

    @Override
    public ProcedureEntity merge(ProcedureEntity existing, ProcedureEntity toMerge) {
        mergeIdentifierNameDescription(existing, toMerge);
        if (toMerge.isSetDescriptionFile()) {
            existing.setDescriptionFile(toMerge.getDescriptionFile());
        }
        return existing;
    }

    public SensorEntity mergeSensorEntity(SensorEntity existing, SensorEntity toMerge) {
       if (toMerge.hasDatastreams()) {
           toMerge.getDatastreams().forEach(d -> {
               existing.addDatastream(d);
           });
       }
       return (SensorEntity) merge(existing, toMerge);
    }

    private FormatEntity createFormat(Entity entity) {
        if (checkProperty(entity, PROP_ENCODINGTYPE)) {
            return new FormatEntity().setFormat(checkInsertEncodingType(getPropertyValue(entity, PROP_ENCODINGTYPE).toString()));
        }
        return new FormatEntity().setFormat("unknown");
    }

    private Set<ProcedureHistoryEntity> createProcedureHistory(ProcedureEntity sensor, Entity entity) {
        ProcedureHistoryEntity procedureHistoryEntity = new ProcedureHistoryEntity();
        procedureHistoryEntity.setProcedure(sensor);
        procedureHistoryEntity.setFormat(sensor.getFormat());
        procedureHistoryEntity.setStartTime(DateTime.now().toDate());
        procedureHistoryEntity.setXml(getPropertyValue(entity, PROP_METADATA).toString());
        Set<ProcedureHistoryEntity> set = new LinkedHashSet<>();
        set.add(procedureHistoryEntity);
        return set;
    }

    private String checkQueryEncodingType(String format) {
        if (format.equalsIgnoreCase(SENSORML_2)) {
            return STA_SENSORML_2;
        }
        return format;
    }

    private String checkInsertEncodingType(String format) {
        if (format.equalsIgnoreCase(STA_SENSORML_2)) {
            return SENSORML_2;
        }
        return format;
    }

    @Override
    public Entity  checkEntity(Entity entity) throws ODataApplicationException {
        checkNameAndDescription(entity);
        checkPropertyValidity(PROP_ENCODINGTYPE, entity);
        checkPropertyValidity(PROP_METADATA, entity);
        return entity;
    }

}
