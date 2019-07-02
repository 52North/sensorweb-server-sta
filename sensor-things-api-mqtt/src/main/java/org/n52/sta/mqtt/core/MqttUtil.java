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
package org.n52.sta.mqtt.core;

import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.n52.sta.mapping.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class MqttUtil {

    public static final String OBSERVATION_ENTITY = "org.n52.series.db.beans.QuantityDataEntity";
    public static final String DATASTREAM_ENTITY = "org.n52.series.db.beans.sta.DatastreamEntity";
    public static final String FEATURE_ENTITY = "org.n52.series.db.beans.AbstractFeatureEntity";
    public static final String HISTORICAL_LOCATION_ENTITY = "org.n52.series.db.beans.sta.HistoricalLocationEntity";
    public static final String LOCATION_ENTITY = "org.n52.series.db.beans.sta.LocationEntity";
    public static final String OBSERVED_PROPERTY_ENTITY = "org.n52.series.db.beans.PhenomenonEntity";
    public static final String SENSOR_ENTITY = "org.n52.series.db.beans.ProcedureEntity";
    public static final String THING_ENTITY = "org.n52.series.db.beans.PlatformEntity";

    public static final Map<String, String> typeMap;

    /**
     * Maps olingo Types to Database types vice-versa..
     * @return Translation map from olingo Entities to raw Data Entities and vice-versa
     */
    static {
        HashMap<String, String> map = new HashMap<>();
        map.put("Observation", OBSERVATION_ENTITY);
        map.put("Datastream", DATASTREAM_ENTITY);
        map.put("FeatureOfInterest", FEATURE_ENTITY);
        map.put("HistoricalLocation", HISTORICAL_LOCATION_ENTITY);
        map.put("Location", LOCATION_ENTITY);
        map.put("ObservedProperty", OBSERVED_PROPERTY_ENTITY);
        map.put("Sensor", SENSOR_ENTITY);
        map.put("Thing", THING_ENTITY);

        map.put(OBSERVATION_ENTITY, "Observation");
        map.put(DATASTREAM_ENTITY, "Datastream");
        map.put(FEATURE_ENTITY, "FeatureOfInterest");
        map.put(HISTORICAL_LOCATION_ENTITY, "HistoricalLocation");
        map.put(LOCATION_ENTITY, "Location");
        map.put(OBSERVED_PROPERTY_ENTITY, "ObservedProperty");
        map.put(SENSOR_ENTITY, "Sensor");
        map.put(THING_ENTITY, "Thing");
        typeMap = Collections.unmodifiableMap(map);
    }

    @Autowired
    private ObservationMapper obsMapper;
    @Autowired
    private DatastreamMapper dsMapper;
    @Autowired
    private FeatureOfInterestMapper foiMapper;
    @Autowired
    private HistoricalLocationMapper hlocMapper;
    @Autowired
    private LocationMapper locMapper;
    @Autowired
    private ObservedPropertyMapper obspropMapper;
    @Autowired
    private SensorMapper sensorMapper;
    @Autowired
    private ThingMapper thingMapper;

    @Bean
    public Parser uriParser(CsdlAbstractEdmProvider provider) {
        OData odata = OData.newInstance();
        ServiceMetadata meta = odata.createServiceMetadata(provider, new ArrayList<EdmxReference>());
        return new Parser(meta.getEdm(), odata);
    }

    /**
     * Multiplexes to the different Mappers for transforming Beans into olingo Entities
     *
     * @param className Name of the base class
     * @return Mapper appropiate for this class
     */
    public AbstractMapper getMapper(String className) {
        switch (className) {
            case OBSERVATION_ENTITY:
                return obsMapper;
            case DATASTREAM_ENTITY:
                return dsMapper;
            case FEATURE_ENTITY:
                return foiMapper;
            case HISTORICAL_LOCATION_ENTITY:
                return hlocMapper;
            case LOCATION_ENTITY:
                return locMapper;
            case OBSERVED_PROPERTY_ENTITY:
                return obspropMapper;
            case SENSOR_ENTITY:
                return sensorMapper;
            case THING_ENTITY:
                return thingMapper;
            default:
                return null;
        }
    }

}
