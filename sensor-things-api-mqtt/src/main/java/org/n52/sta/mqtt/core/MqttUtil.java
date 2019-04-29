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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.n52.sta.mapping.AbstractMapper;
import org.n52.sta.mapping.DatastreamMapper;
import org.n52.sta.mapping.FeatureOfInterestMapper;
import org.n52.sta.mapping.HistoricalLocationMapper;
import org.n52.sta.mapping.LocationMapper;
import org.n52.sta.mapping.ObservationMapper;
import org.n52.sta.mapping.ObservedPropertyMapper;
import org.n52.sta.mapping.SensorMapper;
import org.n52.sta.mapping.ThingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class MqttUtil {

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
     * @param className Name of the base class
     * @return Mapper appropiate for this class
     */
    public AbstractMapper getMapper(String className) {
        switch(className) {
        case "org.n52.series.db.beans.QuantityDataEntity":
            return obsMapper;
        case "org.n52.series.db.beans.sta.DatastreamEntity":
            return dsMapper;
        case "org.n52.series.db.beans.AbstractFeatureEntity":
            return foiMapper;
        case "org.n52.series.db.beans.sta.HistoricalLocationEntity":
            return hlocMapper;
        case "org.n52.series.db.beans.sta.LocationEntity":
            return locMapper;
        case "org.n52.series.db.beans.PhenomenonEntity":
            return obspropMapper;
        case "org.n52.series.db.beans.ProcedureEntity":
            return sensorMapper;
        case "org.n52.series.db.beans.sta.ThingEntity":
            return thingMapper;
        default: return null;
        }
    }

    /**
     * Maps olingo Types to Base types. Needed for fail-fast.
     * @return Translation map from olingo Entities to raw Data Entities
     */
    public static Map<String, String> getBeanTypes() {
        HashMap<String,String> map = new HashMap<>();
        map.put("Observation", "org.n52.series.db.beans.QuantityDataEntity");
        map.put("Datastream", "org.n52.series.db.beans.sta.DatastreamEntity");
        map.put("FeatureOfInterest", "org.n52.series.db.beans.AbstractFeatureEntity");
        map.put("HistoricalLocation", "org.n52.series.db.beans.sta.HistoricalLocationEntity");
        map.put("Location", "org.n52.series.db.beans.sta.LocationEntity");
        map.put("ObservedProperty", "org.n52.series.db.beans.PhenomenonEntity");
        map.put("Sensor", "org.n52.series.db.beans.ProcedureEntity");
        map.put("Thing", "org.n52.series.db.beans.sta.ThingEntity");
        return map;
    }

     /**
     * Maps olingo Base types to Olingo types. Needed for retrieving
     * the corresponding EntityServices.
     * @return Translation map from olingo Entities to raw Data Entities
     */
    public static Map<String, String> getEntityTypes() {
        HashMap<String,String> map = new HashMap<>();
        map.put("org.n52.series.db.beans.QuantityDataEntity", "Observation");
        map.put("org.n52.series.db.beans.sta.DatastreamEntity", "Datastream");
        map.put("org.n52.series.db.beans.AbstractFeatureEntity", "FeatureOfInterest");
        map.put("org.n52.series.db.beans.sta.HistoricalLocationEntity", "HistoricalLocation");
        map.put("org.n52.series.db.beans.sta.LocationEntity", "Location");
        map.put("org.n52.series.db.beans.PhenomenonEntity", "ObservedProperty");
        map.put("org.n52.series.db.beans.ProcedureEntity", "Sensor");
        map.put("org.n52.series.db.beans.sta.ThingEntity", "Thing");
        return map;
    }

    /**
     * Translates Olingo Property into Database Property to check property changes against Event emitted by Database
     * @param staProperty STA property of STA entity
     * @return Set of all Database field storing property information
     */
    public static Set<String> translateSTAtoToDbProperty(String staProperty) {
        Set<String> returnSet = new HashSet<>();
        switch(staProperty) {
        case "iot.Thing.name":
        case "iot.Location.name":
        case "iot.ObservedProperty.name":
        case "iot.FeatureOfInterest.name":
        case "iot.Datastream.name":
            returnSet.add("name");
            break;
        case "iot.Thing.description":
        case "iot.Location.description":
        case "iot.Sensor.description":
        case "iot.ObservedProperty.description":
        case "iot.FeatureOfInterest.description":
        case "iot.Datastream.description":
            returnSet.add("description");
            break;
        case "iot.Thing.properties":
            returnSet.add("properties");
            break;
        case "iot.Location.encodingType":
            returnSet.add("locationEncoding");
            break;
        case "iot.Location.location":
            returnSet.add("location");
            break;
        case "iot.HistoricalLocation.time":
            returnSet.add("time");
            break;
        case "iot.Sensor.name":
        case "iot.ObservedProperty.definition":
            returnSet.add("identifier");
            break;
        case "iot.Sensor.encodingType":
            returnSet.add("format");
            break;
        case "iot.Sensor.metadata":
            returnSet.add("descriptionFile");
            break;
        case "iot.FeatureOfInterest.encodingType":
            returnSet.add("featureType");
            break;
        case "iot.FeatureOfInterest.feature":
            returnSet.add("geometryEntity");
            break;
        case "iot.Observation.phenomenonTime":
        case "iot.Datastream.phenomenonTime":
            returnSet.add("samplingTimeStart");
            returnSet.add("samplingTimeEnd");
            break;
        case "iot.Observation.resultTime":
            returnSet.add("resultTime");
            break;
        case "iot.Observation.result":
            returnSet.add("value");
            break;
        case "iot.Observation.validTime":
            returnSet.add("validTimeStart");
            returnSet.add("validTimeEnd");
            break;
        case "iot.Observation.parameters":
            returnSet.add("parameters");
            break;
        case "iot.Datastream.observationType":
            returnSet.add("observationType");
            break;
        case "iot.Datastream.unitOfMeasurement":
            returnSet.add("unitOfMeasurement");
            break;
        case "iot.Datastream.observedArea":
            returnSet.add("geometryEntity");
            break;
        case "iot.Datastream.resultTime":
            returnSet.add("resultTimeStart");
            returnSet.add("resultTimeEnd");
            break;
        }
        return returnSet;
    }

}
