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

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;
import static org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider.ES_OBSERVED_PROPERTIES_NAME;
import static org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider.ET_OBSERVED_PROPERTY_FQN;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class ObservationMapper {

    public Entity createObservationEntity(DataEntity observation) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, ID_ANNOTATION, ValueType.PRIMITIVE, observation.getId()));
        //TODO: check observation type, cast and get value
        entity.addProperty(new Property(null, AbstractSensorThingsEntityProvider.PROP_RESULT, ValueType.PRIMITIVE, observation.getValue().toString()));
        //TODO: use DateTimeUtils from Arctic Sea to format date to string
        entity.addProperty(new Property(null, AbstractSensorThingsEntityProvider.PROP_RESULT_TIME, ValueType.PRIMITIVE, observation.getResultTime().toString()));
        //TODO: if start time equals end time set only start time, otherwise construct time period
        entity.addProperty(new Property(null, AbstractSensorThingsEntityProvider.PROP_PHENOMENON_TIME, ValueType.PRIMITIVE, observation.getSamplingTimeStart()));

        return entity;
    }

}
