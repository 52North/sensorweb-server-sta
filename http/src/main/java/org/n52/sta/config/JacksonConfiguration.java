/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import org.n52.sta.api.old.CollectionWrapper;
import org.n52.sta.api.old.entity.DatastreamDTO;
import org.n52.sta.api.old.entity.FeatureOfInterestDTO;
import org.n52.sta.api.old.entity.HistoricalLocationDTO;
import org.n52.sta.api.old.entity.LocationDTO;
import org.n52.sta.api.old.entity.ObservationDTO;
import org.n52.sta.api.old.entity.ObservedPropertyDTO;
import org.n52.sta.api.old.entity.SensorDTO;
import org.n52.sta.api.old.entity.ThingDTO;
import org.n52.sta.api.old.serialize.DatastreamSerDes;
import org.n52.sta.api.old.serialize.FeatureOfInterestSerDes;
import org.n52.sta.api.old.serialize.HistoricalLocationSerDes;
import org.n52.sta.api.old.serialize.LocationSerDes;
import org.n52.sta.api.old.serialize.ObservationSerDes;
import org.n52.sta.api.old.serialize.ObservedPropertySerDes;
import org.n52.sta.api.old.serialize.SensorSerDes;
import org.n52.sta.api.old.serialize.ThingSerDes;
import org.n52.sta.api.old.serialize.common.CollectionSer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper customMapper(
                                     @Value("${server.config.service-root-url}") String rootUrl,
                                     @Value("${server.feature.variableEncodingType:false}") boolean variableSensorEncodingTypeEnabled,
                                     @Value("${server.feature.observation.samplingGeometry}") String samplingGeometryMapping,
                                     @Value("${server.feature.observation.verticalFrom}") String verticalFromMapping,
                                     @Value("${server.feature.observation.verticalTo}") String verticalToMapping,
                                     @Value("${server.feature.observation.verticalFromTo}") String verticalFromToMapping,
                                     @Value("${server.feature.implicitExpand:false}") boolean implicitExpand,
                                     @Value("${server.feature.includeDatastreamCategory:false}") boolean includeDatastreamCategory,
                                     Environment environment) {
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
                                                                  activeProfiles));
        serializers.addSerializer(
                                  new LocationSerDes.LocationSerializer(rootUrl,
                                                                        activeProfiles));
        serializers.addSerializer(
                                  new SensorSerDes.SensorSerializer(rootUrl,
                                                                    activeProfiles));
        serializers.addSerializer(
                                  new ObservationSerDes.ObservationSerializer(rootUrl,
                                                                              activeProfiles));
        serializers.addSerializer(
                                  new ObservedPropertySerDes.ObservedPropertySerializer(rootUrl,
                                                                                        activeProfiles));
        serializers.addSerializer(
                                  new FeatureOfInterestSerDes.FeatureOfInterestSerializer(rootUrl,
                                                                                          activeProfiles));
        serializers.addSerializer(
                                  new HistoricalLocationSerDes.HistoricalLocationSerializer(rootUrl, activeProfiles));
        serializers.addSerializer(
                                  new DatastreamSerDes.DatastreamSerializer(rootUrl,
                                                                            activeProfiles));

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(ThingDTO.class,
                                      new ThingSerDes.ThingDeserializer());
        deserializers.addDeserializer(LocationDTO.class,
                                      new LocationSerDes.LocationDeserializer());
        deserializers.addDeserializer(SensorDTO.class,
                                      new SensorSerDes.SensorDeserializer(variableSensorEncodingTypeEnabled));
        deserializers.addDeserializer(ObservationDTO.class,
                                      new ObservationSerDes.ObservationDeserializer(parameterMapping));
        deserializers.addDeserializer(ObservedPropertyDTO.class,
                                      new ObservedPropertySerDes.ObservedPropertyDeserializer());
        deserializers.addDeserializer(FeatureOfInterestDTO.class,
                                      new FeatureOfInterestSerDes.FeatureOfInterestDeserializer());
        deserializers.addDeserializer(HistoricalLocationDTO.class,
                                      new HistoricalLocationSerDes.HistoricalLocationDeserializer());
        deserializers.addDeserializer(DatastreamDTO.class,
                                      new DatastreamSerDes.DatastreamDeserializer());

        deserializers.addDeserializer(ThingSerDes.ThingDTOPatch.class,
                                      new ThingSerDes.ThingPatchDeserializer());
        deserializers.addDeserializer(LocationSerDes.LocationDTOPatch.class,
                                      new LocationSerDes.LocationPatchDeserializer());
        deserializers.addDeserializer(SensorSerDes.SensorDTOPatch.class,
                                      new SensorSerDes.SensorPatchDeserializer(variableSensorEncodingTypeEnabled));
        deserializers.addDeserializer(ObservationSerDes.ObservationDTOPatch.class,
                                      new ObservationSerDes.ObservationPatchDeserializer(parameterMapping));
        deserializers.addDeserializer(ObservedPropertySerDes.ObservedPropertyDTOPatch.class,
                                      new ObservedPropertySerDes.ObservedPropertyPatchDeserializer());
        deserializers.addDeserializer(FeatureOfInterestSerDes.FeatureOfInterestDTOPatch.class,
                                      new FeatureOfInterestSerDes.FeatureOfInterestPatchDeserializer());
        deserializers.addDeserializer(HistoricalLocationSerDes.HistoricalLocationDTOPatch.class,
                                      new HistoricalLocationSerDes.HistoricalLocationPatchDeserializer());
        deserializers.addDeserializer(DatastreamSerDes.DatastreamDTOPatch.class,
                                      new DatastreamSerDes.DatastreamPatchDeserializer());

        // for (String activeProfile : environment.getActiveProfiles()) {
        // if (activeProfile.equals(StaConstants.STAPLUS)) {
        // serializers.addSerializer(
        // new ObservationGroupSerDes.ObservationGroupSerializer(rootUrl,
        // activeProfiles));
        // serializers.addSerializer(
        // new ObservationRelationSerDes.ObservationRelationSerializer(rootUrl,
        // activeProfiles));
        // serializers.addSerializer(
        // new LicenseSerDes.LicenseSerializer(rootUrl,
        // activeProfiles));
        // serializers.addSerializer(
        // new PartySerDes.PartySerializer(rootUrl,
        // activeProfiles));
        // serializers.addSerializer(
        // new ProjectSerDes.ProjectSerializer(rootUrl,
        // activeProfiles));
        //
        // deserializers.addDeserializer(GroupDTO.class,
        // new ObservationGroupSerDes.ObservationGroupDeserializer());
        // deserializers.addDeserializer(RelationDTO.class,
        // new ObservationRelationSerDes.ObservationRelationDeserializer());
        // deserializers.addDeserializer(LicenseDTO.class,
        // new LicenseSerDes.LicenseDeserializer());
        // deserializers.addDeserializer(PartyDTO.class,
        // new PartySerDes.PartyDeserializer());
        // deserializers.addDeserializer(ProjectDTO.class,
        // new ProjectSerDes.ProjectDeserializer());
        //
        // deserializers.addDeserializer(ObservationGroupSerDes.ObservationGroupDTOPatch.class,
        // new ObservationGroupSerDes.ObservationGroupPatchDeserializer());
        // deserializers.addDeserializer(ObservationRelationSerDes.ObservationRelationDTOPatch.class,
        // new ObservationRelationSerDes.ObservationRelationPatchDeserializer());
        // deserializers.addDeserializer(LicenseSerDes.LicenseDTOPatch.class,
        // new LicenseSerDes.LicensePatchDeserializer());
        // deserializers.addDeserializer(PartySerDes.PartyDTOPatch.class,
        // new PartySerDes.PartyPatchDeserializer());
        // deserializers.addDeserializer(ProjectSerDes.ProjectDTOPatch.class,
        // new ProjectSerDes.ProjectPatchDeserializer());
        // }
        // }

        module.setSerializers(serializers);
        module.setDeserializers(deserializers);
        modules.add(module);
        modules.add(new AfterburnerModule());
        return Jackson2ObjectMapperBuilder.json()
                                          .modules(modules)
                                          .build();
    }
}
