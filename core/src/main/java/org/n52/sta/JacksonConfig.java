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
package org.n52.sta;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.serdes.CollectionSer;
import org.n52.sta.serdes.DatastreamSerDes;
import org.n52.sta.serdes.FeatureOfInterestSerDes;
import org.n52.sta.serdes.HistoricalLocationSerDes;
import org.n52.sta.serdes.LocationSerDes;
import org.n52.sta.serdes.ObservationSerDes;
import org.n52.sta.serdes.ObservedPropertySerDes;
import org.n52.sta.serdes.SensorSerDes;
import org.n52.sta.serdes.ThingSerDes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper customMapper(
        @Value("${server.rootUrl}") String rootUrl,
        @Value("${server.feature.variableEncodingType:false}") boolean variableSensorEncodingTypeEnabled,
        @Value("${server.feature.observation.samplingGeometry}") String samplingGeometryMapping,
        @Value("${server.feature.observation.verticalFrom}") String verticalFromMapping,
        @Value("${server.feature.observation.verticalTo}") String verticalToMapping,
        @Value("${server.feature.observation.verticalFromTo}") String verticalFromToMapping,
        @Value("${server.feature.implicitExpand:false}") boolean implicitExpand,
        @Value("${server.feature.includeDatastreamCategory:false}") boolean includeDatastreamCategory,
        Environment environment
    ) {
        Map<String, String> parameterMapping = new HashMap<>();
        parameterMapping.put("samplingGeometry", samplingGeometryMapping);
        parameterMapping.put("verticalFrom", verticalFromMapping);
        parameterMapping.put("verticalTo", verticalToMapping);
        parameterMapping.put("verticalFromTo", verticalFromToMapping);

        String[] activeProfiles = environment.getActiveProfiles();
        ArrayList<Module> modules = new ArrayList<>();

        SimpleModule module = new SimpleModule();

        // Register Serializers/Deserializers for STA Core Entities
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(new CollectionSer(CollectionWrapper.class));
        serializers.addSerializer(
            new ThingSerDes.ThingSerializer(rootUrl,
                                            implicitExpand,
                                            activeProfiles));
        serializers.addSerializer(
            new LocationSerDes.LocationSerializer(rootUrl,
                                                  implicitExpand,
                                                  activeProfiles));
        serializers.addSerializer(
            new SensorSerDes.SensorSerializer(rootUrl,
                                              implicitExpand,
                                              activeProfiles));
        serializers.addSerializer(
            new ObservationSerDes.ObservationSerializer(rootUrl,
                                                        implicitExpand,
                                                        activeProfiles));
        serializers.addSerializer(
            new ObservedPropertySerDes.ObservedPropertySerializer(rootUrl,
                                                                  implicitExpand,
                                                                  activeProfiles));
        serializers.addSerializer(
            new FeatureOfInterestSerDes.FeatureOfInterestSerializer(rootUrl,
                                                                    implicitExpand,
                                                                    activeProfiles));
        serializers.addSerializer(
            new HistoricalLocationSerDes.HistoricalLocationSerializer(rootUrl,
                                                                      implicitExpand,
                                                                      activeProfiles));
        serializers.addSerializer(
            new DatastreamSerDes.DatastreamSerializer(rootUrl,
                                                      implicitExpand,
                                                      includeDatastreamCategory,
                                                      activeProfiles));

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(PlatformEntity.class,
                                      new ThingSerDes.ThingDeserializer());
        deserializers.addDeserializer(LocationEntity.class,
                                      new LocationSerDes.LocationDeserializer());
        deserializers.addDeserializer(ProcedureEntity.class,
                                      new SensorSerDes.SensorDeserializer(variableSensorEncodingTypeEnabled));
        deserializers.addDeserializer(DataEntity.class,
                                      new ObservationSerDes.ObservationDeserializer(parameterMapping));
        deserializers.addDeserializer(PhenomenonEntity.class,
                                      new ObservedPropertySerDes.ObservedPropertyDeserializer());
        deserializers.addDeserializer(AbstractFeatureEntity.class,
                                      new FeatureOfInterestSerDes.FeatureOfInterestDeserializer());
        deserializers.addDeserializer(HistoricalLocationEntity.class,
                                      new HistoricalLocationSerDes.HistoricalLocationDeserializer());
        deserializers.addDeserializer(AbstractDatasetEntity.class,
                                      new DatastreamSerDes.DatastreamDeserializer());

        deserializers.addDeserializer(ThingSerDes.PlatformEntityPatch.class,
                                      new ThingSerDes.ThingPatchDeserializer());
        deserializers.addDeserializer(LocationSerDes.LocationEntityPatch.class,
                                      new LocationSerDes.LocationPatchDeserializer());
        deserializers.addDeserializer(SensorSerDes.ProcedureEntityPatch.class,
                                      new SensorSerDes.SensorPatchDeserializer(variableSensorEncodingTypeEnabled));
        deserializers.addDeserializer(ObservationSerDes.ObservationEntityPatch.class,
                                      new ObservationSerDes.ObservationPatchDeserializer(parameterMapping));
        deserializers.addDeserializer(ObservedPropertySerDes.PhenomenonEntityPatch.class,
                                      new ObservedPropertySerDes.ObservedPropertyPatchDeserializer());
        deserializers.addDeserializer(FeatureOfInterestSerDes.AbstractFeatureEntityPatch.class,
                                      new FeatureOfInterestSerDes.FeatureOfInterestPatchDeserializer());
        deserializers.addDeserializer(HistoricalLocationSerDes.HistoricalLocationEntityPatch.class,
                                      new HistoricalLocationSerDes.HistoricalLocationPatchDeserializer());
        deserializers.addDeserializer(DatastreamSerDes.DatastreamEntityPatch.class,
                                      new DatastreamSerDes.DatastreamPatchDeserializer());

        module.setSerializers(serializers);
        module.setDeserializers(deserializers);
        modules.add(module);
        modules.add(new AfterburnerModule());
        return Jackson2ObjectMapperBuilder.json()
            .modules(modules)
            .build();
    }
}
