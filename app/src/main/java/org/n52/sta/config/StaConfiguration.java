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

import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.domain.service.DomainService;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.service.DatastreamService;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.api.service.FeatureOfInterestService;
import org.n52.sta.api.service.HistoricalLocationService;
import org.n52.sta.api.service.LocationService;
import org.n52.sta.api.service.ObservationService;
import org.n52.sta.api.service.ObservedPropertyService;
import org.n52.sta.api.service.SensorService;
import org.n52.sta.api.service.ThingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class StaConfiguration {

    @Bean
    public EntityServiceLookup getEntityProviderLookup() {
        return new EntityServiceLookup();
    }

    @Bean
    public EntityService<Thing> getThingService(
            EntityProvider<Thing> entityProvider,
            Optional<DomainService<Thing>> thingDomainService,
            EntityServiceLookup lookup) {

        ThingService service = thingDomainService.isPresent()
                ? new ThingService(entityProvider, thingDomainService.get())
                : new ThingService(entityProvider);
        lookup.addEntityService(Thing.class, service);
        return service;
    }

    @Bean
    public EntityService<Datastream> getDatastreamService(
            EntityProvider<Datastream> entityProvider,
            Optional<DomainService<Datastream>> datastreamDomainService,
            EntityServiceLookup lookup) {

        DatastreamService service = datastreamDomainService.isPresent()
                ? new DatastreamService(entityProvider, datastreamDomainService.get())
                : new DatastreamService(entityProvider);
        lookup.addEntityService(Datastream.class, service);
        return service;
    }

    @Bean
    public EntityService<Sensor> getSensorService(
        EntityProvider<Sensor> entityProvider,
        Optional<DomainService<Sensor>> sensorDomainService,
        EntityServiceLookup lookup) {

        SensorService service = sensorDomainService.isPresent()
            ? new SensorService(entityProvider, sensorDomainService.get())
            : new SensorService(entityProvider);
        lookup.addEntityService(Sensor.class, service);
        return service;
    }

    @Bean
    public EntityService<Location> getLocationService(
        EntityProvider<Location> entityProvider,
        Optional<DomainService<Location>> locationDomainService,
        EntityServiceLookup lookup) {

        LocationService service = locationDomainService.isPresent()
            ? new LocationService(entityProvider, locationDomainService.get())
            : new LocationService(entityProvider);
        lookup.addEntityService(Location.class, service);
        return service;
    }

    @Bean
    public EntityService<ObservedProperty> getObservedPropertyService(
        EntityProvider<ObservedProperty> entityProvider,
        Optional<DomainService<ObservedProperty>> observedPropertyDomainService,
        EntityServiceLookup lookup) {

        ObservedPropertyService service = observedPropertyDomainService.isPresent()
            ? new ObservedPropertyService(entityProvider, observedPropertyDomainService.get())
            : new ObservedPropertyService(entityProvider);
        lookup.addEntityService(ObservedProperty.class, service);
        return service;
    }

    @Bean
    public EntityService<Observation> getObservationService(
        EntityProvider<Observation> entityProvider,
        Optional<DomainService<Observation>> observationDomainService,
        EntityServiceLookup lookup) {

        ObservationService service = observationDomainService.isPresent()
            ? new ObservationService(entityProvider, observationDomainService.get())
            : new ObservationService(entityProvider);
        lookup.addEntityService(Observation.class, service);
        return service;
    }

    @Bean
    public EntityService<FeatureOfInterest> getFeatureOfInterestService(
        EntityProvider<FeatureOfInterest> entityProvider,
        Optional<DomainService<FeatureOfInterest>> featureOfInterestDomainService,
        EntityServiceLookup lookup) {

        FeatureOfInterestService service = featureOfInterestDomainService.isPresent()
            ? new FeatureOfInterestService(entityProvider, featureOfInterestDomainService.get())
            : new FeatureOfInterestService(entityProvider);
        lookup.addEntityService(FeatureOfInterest.class, service);
        return service;
    }

    @Bean
    public EntityService<HistoricalLocation> getHistoricalLocationService(
        EntityProvider<HistoricalLocation> entityProvider,
        Optional<DomainService<HistoricalLocation>> historicalLocationDomainService,
        EntityServiceLookup lookup) {

        HistoricalLocationService service = historicalLocationDomainService.isPresent()
            ? new HistoricalLocationService(entityProvider, historicalLocationDomainService.get())
            : new HistoricalLocationService(entityProvider);
        lookup.addEntityService(HistoricalLocation.class, service);
        return service;
    }
}
