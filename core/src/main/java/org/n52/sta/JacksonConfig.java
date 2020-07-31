/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.series.db.beans.sta.mapped.ObservationEntity;
import org.n52.series.db.beans.sta.mapped.extension.CSDatastream;
import org.n52.series.db.beans.sta.mapped.extension.CSObservation;
import org.n52.series.db.beans.sta.mapped.extension.License;
import org.n52.series.db.beans.sta.mapped.extension.ObservationGroup;
import org.n52.series.db.beans.sta.mapped.extension.ObservationRelation;
import org.n52.series.db.beans.sta.mapped.extension.Party;
import org.n52.series.db.beans.sta.mapped.extension.Project;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.serdes.CSDatastreamSerDes;
import org.n52.sta.serdes.CSObservationSerDes;
import org.n52.sta.serdes.CollectionSer;
import org.n52.sta.serdes.DatastreamSerDes;
import org.n52.sta.serdes.FeatureOfInterestSerDes;
import org.n52.sta.serdes.HistoricalLocationSerDes;
import org.n52.sta.serdes.LicenseSerDes;
import org.n52.sta.serdes.LocationSerDes;
import org.n52.sta.serdes.ObservationGroupSerDes;
import org.n52.sta.serdes.ObservationRelationSerDes;
import org.n52.sta.serdes.ObservationSerDes;
import org.n52.sta.serdes.ObservedPropertySerDes;
import org.n52.sta.serdes.PartySerDes;
import org.n52.sta.serdes.ProjectSerDes;
import org.n52.sta.serdes.SensorSerDes;
import org.n52.sta.serdes.ThingSerDes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.ArrayList;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper customMapper(@Value("${server.rootUrl}") String rootUrl,
                                     @Value("${server.feature.variableEncodingType:false}")
                                             boolean variableSensorEncodingTypeEnabled,
                                     Environment environment) {
        ArrayList<Module> modules = new ArrayList<>();
        SimpleModule module = new SimpleModule();

        // Register Serializers/Deserializers for all custom types
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(new CollectionSer(CollectionWrapper.class));
        serializers.addSerializer(new ThingSerDes.ThingSerializer(rootUrl, environment.getActiveProfiles()));
        serializers.addSerializer(new LocationSerDes.LocationSerializer(rootUrl));
        serializers.addSerializer(new SensorSerDes.SensorSerializer(rootUrl, environment.getActiveProfiles()));
        serializers.addSerializer(new ObservationSerDes.ObservationSerializer(rootUrl));
        serializers.addSerializer(
                new ObservedPropertySerDes.ObservedPropertySerializer(rootUrl,
                                                                      environment.getActiveProfiles()));
        serializers.addSerializer(new FeatureOfInterestSerDes.FeatureOfInterestSerializer(rootUrl));
        serializers.addSerializer(new HistoricalLocationSerDes.HistoricalLocationSerializer(rootUrl));
        serializers.addSerializer(new DatastreamSerDes.DatastreamSerializer(rootUrl));

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(PlatformEntity.class,
                                      new ThingSerDes.ThingDeserializer());
        deserializers.addDeserializer(LocationEntity.class,
                                      new LocationSerDes.LocationDeserializer());
        deserializers.addDeserializer(SensorEntity.class,
                                      new SensorSerDes.SensorDeserializer(variableSensorEncodingTypeEnabled));
        deserializers.addDeserializer(ObservationEntity.class,
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
                                      new SensorSerDes.SensorPatchDeserializer(variableSensorEncodingTypeEnabled));
        deserializers.addDeserializer(ObservationSerDes.ObservationEntityPatch.class,
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

    @Bean
    @Primary
    @Profile("citSciExtension")
    public ObjectMapper citSciMapper(@Qualifier("customMapper") ObjectMapper mapper,
                                     @Value("${server.rootUrl}") String rootUrl) {
        SimpleModule module = new SimpleModule();

        SimpleSerializers serializers = new SimpleSerializers();
        SimpleDeserializers deserializers = new SimpleDeserializers();

        serializers.addSerializer(new ObservationGroupSerDes.ObservationGroupSerializer(rootUrl));
        serializers.addSerializer(new ObservationRelationSerDes.ObservationRelationSerializer(rootUrl));
        serializers.addSerializer(new CSObservationSerDes.CSObservationSerializer(rootUrl));
        serializers.addSerializer(new LicenseSerDes.LicenseSerializer(rootUrl));
        serializers.addSerializer(new PartySerDes.PartySerializer(rootUrl));
        serializers.addSerializer(new ProjectSerDes.ProjectSerializer(rootUrl));
        serializers.addSerializer(new CSDatastreamSerDes.CSDatastreamSerializer(rootUrl));

        deserializers.addDeserializer(ObservationGroup.class,
                                      new ObservationGroupSerDes.ObservationGroupDeserializer());
        deserializers.addDeserializer(ObservationRelation.class,
                                      new ObservationRelationSerDes.ObservationRelationDeserializer());
        deserializers.addDeserializer(License.class,
                                      new LicenseSerDes.LicenseDeserializer());
        deserializers.addDeserializer(Party.class,
                                      new PartySerDes.PartyDeserializer());
        deserializers.addDeserializer(Project.class,
                                      new ProjectSerDes.ProjectDeserializer());
        deserializers.addDeserializer(CSDatastream.class,
                                      new CSDatastreamSerDes.CSDatastreamDeserializer());

        deserializers.addDeserializer(CSObservation.class,
                                      new CSObservationSerDes.CSObservationDeserializer());

        //TODO: Add patch deserializers here

        module.setSerializers(serializers);
        module.setDeserializers(deserializers);
        mapper.registerModule(module);
        return mapper;
    }

}
