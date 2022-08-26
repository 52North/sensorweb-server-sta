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

import org.n52.sta.data.provider.DatastreamEntityProvider;
import org.n52.sta.data.provider.FeatureOfInterestEntityProvider;
import org.n52.sta.data.provider.HistoricalLocationEntityProvider;
import org.n52.sta.data.provider.LocationEntityProvider;
import org.n52.sta.data.provider.ObservationEntityProvider;
import org.n52.sta.data.provider.ObservedPropertyEntityProvider;
import org.n52.sta.data.provider.SensorEntityProvider;
import org.n52.sta.data.provider.ThingEntityProvider;
import org.n52.sta.data.repositories.BaseRepositoryImpl;
import org.n52.sta.data.repositories.entity.DatastreamRepository;
import org.n52.sta.data.repositories.entity.FeatureOfInterestRepository;
import org.n52.sta.data.repositories.entity.HistoricalLocationRepository;
import org.n52.sta.data.repositories.entity.LocationRepository;
import org.n52.sta.data.repositories.entity.ObservationRepository;
import org.n52.sta.data.repositories.entity.PhenomenonRepository;
import org.n52.sta.data.repositories.entity.PlatformRepository;
import org.n52.sta.data.repositories.entity.ProcedureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DataProviderConfiguration {

    @Autowired
    private EntityPropertyMapping propertyMapping;

    @Bean
    public ThingEntityProvider thingEntityProvider(PlatformRepository repository) {
        return new ThingEntityProvider(repository, propertyMapping);
    }

    @Bean
    public DatastreamEntityProvider datastreamEntityProvider(DatastreamRepository repository) {
        return new DatastreamEntityProvider(repository, propertyMapping);
    }

    @Bean
    public SensorEntityProvider sensorEntityProvider(ProcedureRepository repository) {
        return new SensorEntityProvider(repository, propertyMapping);
    }

    @Bean
    public LocationEntityProvider locationEntityProvider(LocationRepository repository) {
        return new LocationEntityProvider(repository, propertyMapping);
    }

    @Bean
    public HistoricalLocationEntityProvider historicalLocationEntityProvider(HistoricalLocationRepository repository) {
        return new HistoricalLocationEntityProvider(repository, propertyMapping);
    }

    @Bean
    public ObservedPropertyEntityProvider observedPropertyEntityProvider(PhenomenonRepository repository) {
        return new ObservedPropertyEntityProvider(repository, propertyMapping);
    }

    @Bean
    public ObservationEntityProvider observationEntityProvider(ObservationRepository repository) {
        return new ObservationEntityProvider(repository, propertyMapping);
    }

    @Bean
    public FeatureOfInterestEntityProvider featureOfInterestEntityProvider(FeatureOfInterestRepository repository) {
        return new FeatureOfInterestEntityProvider(repository, propertyMapping);
    }

    @Configuration
    @EnableJpaRepositories(
        repositoryBaseClass = BaseRepositoryImpl.class,
        basePackages = "org.n52.sta.data.repositories")
    public static class RepositoryConfig {
        // inject via annotations
    }
}
