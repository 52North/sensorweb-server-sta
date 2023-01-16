/*
 * Copyright (C) 2018-2023 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.mqtt.core;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class MqttUtil {

    public static final String OBSERVATION_ENTITY = "org.n52.series.db.beans.ObservationEntity";
    public static final String DATASTREAM_ENTITY = "org.n52.series.db.beans.sta.AbstractDatasetEntity";
    public static final String FEATURE_ENTITY = "org.n52.series.db.beans.AbstractFeatureEntity";
    public static final String HISTORICAL_LOCATION_ENTITY = "org.n52.series.db.beans.sta.HistoricalLocationEntity";
    public static final String LOCATION_ENTITY = "org.n52.series.db.beans.sta.LocationEntity";
    public static final String OBSERVED_PROPERTY_ENTITY = "org.n52.series.db.beans.PhenomenonEntity";
    public static final String SENSOR_ENTITY = "org.n52.series.db.beans.ProcedureEntity";
    public static final String THING_ENTITY = "org.n52.series.db.beans.PlatformEntity";

    public static final String OBSERVATION = "Observation";
    public static final String DATASTREAM = "Datastream";
    public static final String FEATURE = "FeatureOfInterest";
    public static final String HISTORICAL_LOCATION = "HistoricalLocation";
    public static final String LOCATION = "Location";
    public static final String OBSERVED_PROPERTY = "ObservedProperty";
    public static final String SENSOR = "Sensor";
    public static final String THING = "Thing";

    public static final Map<String, String> TYPEMAP;

    /**
     * Maps olingo Types to Database types vice-versa..
     * @return Translation map from olingo Entities to raw Data Entities and vice-versa
     */
    static {
        HashMap<String, String> map = new HashMap<>();
        map.put(OBSERVATION, OBSERVATION_ENTITY);
        map.put(DATASTREAM, DATASTREAM_ENTITY);
        map.put(FEATURE, FEATURE_ENTITY);
        map.put(HISTORICAL_LOCATION, HISTORICAL_LOCATION_ENTITY);
        map.put(LOCATION, LOCATION_ENTITY);
        map.put(OBSERVED_PROPERTY, OBSERVED_PROPERTY_ENTITY);
        map.put(SENSOR, SENSOR_ENTITY);
        map.put(THING, THING_ENTITY);

        map.put(OBSERVATION_ENTITY, OBSERVATION);
        map.put(DATASTREAM_ENTITY, DATASTREAM);
        map.put(FEATURE_ENTITY, FEATURE);
        map.put(HISTORICAL_LOCATION_ENTITY, HISTORICAL_LOCATION);
        map.put(LOCATION_ENTITY, LOCATION);
        map.put(OBSERVED_PROPERTY_ENTITY, OBSERVED_PROPERTY);
        map.put(SENSOR_ENTITY, SENSOR);
        map.put(THING_ENTITY, THING);
        TYPEMAP = Collections.unmodifiableMap(map);
    }

}
