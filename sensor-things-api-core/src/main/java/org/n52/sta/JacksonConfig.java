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
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.SensorEntity;
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
        serializers.addSerializer(new ThingSerDes.ThingSerializer(rootUrl));
        serializers.addSerializer(new LocationSerDes.LocationSerializer(rootUrl));
        serializers.addSerializer(new SensorSerDes.SensorSerializer(rootUrl));
        serializers.addSerializer(new ObservationSerDes.ObservationSerializer(rootUrl));
        serializers.addSerializer(new ObservedPropertySerDes.ObservedPropertySerializer(rootUrl));
        serializers.addSerializer(new FeatureOfInterestSerDes.FeatureOfInterestSerializer(rootUrl));
        serializers.addSerializer(new HistoricalLocationSerDes.HistoricalLocationSerializer(rootUrl));
        serializers.addSerializer(new DatastreamSerDes.DatastreamSerializer(rootUrl));

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(PlatformEntity.class,
                new ThingSerDes.ThingDeserializer());
        deserializers.addDeserializer(LocationEntity.class,
                new LocationSerDes.LocationDeserializer());
        deserializers.addDeserializer(SensorEntity.class,
                new SensorSerDes.SensorDeserializer());
        deserializers.addDeserializer(DataEntity.class,
                new ObservationSerDes.ObservationDeserializer());
        deserializers.addDeserializer(PhenomenonEntity.class,
                new ObservedPropertySerDes.ObservedPropertyDeserializer());
        deserializers.addDeserializer(AbstractFeatureEntity.class,
                new FeatureOfInterestSerDes.FeatureOfInterestDeserializer());
        deserializers.addDeserializer(HistoricalLocationEntity.class,
                new HistoricalLocationSerDes.HistoricalLocationDeserializer());
        deserializers.addDeserializer(DatastreamEntity.class,
                new DatastreamSerDes.DatastreamDeserializer());

        deserializers.addDeserializer(ThingSerDes.PlatformEntityPatch.class,
                new ThingSerDes.ThingPatchDeserializer());
        deserializers.addDeserializer(LocationSerDes.LocationEntityPatch.class,
                new LocationSerDes.LocationPatchDeserializer());
        deserializers.addDeserializer(SensorSerDes.SensorEntityPatch.class,
                new SensorSerDes.SensorPatchDeserializer());
        deserializers.addDeserializer(ObservationSerDes.StaDataEntityPatch.class,
                new ObservationSerDes.ObservationPatchDeserializer());
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
