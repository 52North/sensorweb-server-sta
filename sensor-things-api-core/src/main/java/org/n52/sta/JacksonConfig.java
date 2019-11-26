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
package org.n52.sta;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.serdes.DatastreamSerdes.DatastreamDeserializer;
import org.n52.sta.serdes.DatastreamSerdes.DatastreamSerializer;
import org.n52.sta.serdes.FeatureOfInterestSerdes.FeatureOfInterestDeserializer;
import org.n52.sta.serdes.FeatureOfInterestSerdes.FeatureOfInterestSerializer;
import org.n52.sta.serdes.HistoricalLocationSerde.HistoricalLocationDeserializer;
import org.n52.sta.serdes.HistoricalLocationSerde.HistoricalLocationSerializer;
import org.n52.sta.serdes.LocationSerdes.LocationDeserializer;
import org.n52.sta.serdes.LocationSerdes.LocationSerializer;
import org.n52.sta.serdes.ObservationSerde.ObservationDeserializer;
import org.n52.sta.serdes.ObservationSerde.ObservationSerializer;
import org.n52.sta.serdes.ObservedPropertySerde.ObservedPropertyDeserializer;
import org.n52.sta.serdes.ObservedPropertySerde.ObservedPropertySerializer;
import org.n52.sta.serdes.SensorSerdes;
import org.n52.sta.serdes.SensorSerdes.SensorDeserializer;
import org.n52.sta.serdes.ThingSerdes.ThingDeserializer;
import org.n52.sta.serdes.ThingSerdes.ThingSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.ArrayList;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper customMapper(@Value("${server.rootUrl}") String rootUrl) {
        ArrayList<Module> modules = new ArrayList<>();

        //CollectionType Serialization
        SimpleModule module = new SimpleModule();

        // Register Serializers/Deserializers for all custom types
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(new ThingSerializer(rootUrl));
        serializers.addSerializer(new LocationSerializer(rootUrl));
        serializers.addSerializer(new SensorSerdes.SensorSerializer(rootUrl));
        serializers.addSerializer(new ObservationSerializer(rootUrl));
        serializers.addSerializer(new ObservedPropertySerializer(rootUrl));
        serializers.addSerializer(new FeatureOfInterestSerializer(rootUrl));
        serializers.addSerializer(new HistoricalLocationSerializer(rootUrl));
        serializers.addSerializer(new DatastreamSerializer(rootUrl));

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(PlatformEntity.class, new ThingDeserializer());
        deserializers.addDeserializer(LocationEntity.class, new LocationDeserializer());
        deserializers.addDeserializer(ProcedureEntity.class, new SensorDeserializer());
        deserializers.addDeserializer(DataEntity.class, new ObservationDeserializer());
        deserializers.addDeserializer(PhenomenonEntity.class, new ObservedPropertyDeserializer());
        deserializers.addDeserializer(AbstractFeatureEntity.class, new FeatureOfInterestDeserializer());
        deserializers.addDeserializer(HistoricalLocationEntity.class, new HistoricalLocationDeserializer());
        deserializers.addDeserializer(DatastreamEntity.class, new DatastreamDeserializer());

        module.setSerializers(serializers);
        module.setDeserializers(deserializers);
        modules.add(module);
        modules.add(new AfterburnerModule());
        return Jackson2ObjectMapperBuilder.json()
                .modules(modules)
                .build();
    }
}
