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

import java.util.Optional;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.domain.service.DatastreamDomainService;
import org.n52.sta.api.domain.service.FeatureOfInterestDomainService;
import org.n52.sta.api.domain.service.GroupDomainService;
import org.n52.sta.api.domain.service.HistoricalLocationDomainService;
import org.n52.sta.api.domain.service.LicenseDomainService;
import org.n52.sta.api.domain.service.LocationDomainService;
import org.n52.sta.api.domain.service.ObservationDomainService;
import org.n52.sta.api.domain.service.ObservedPropertyDomainService;
import org.n52.sta.api.domain.service.PartyDomainService;
import org.n52.sta.api.domain.service.ProjectDomainService;
import org.n52.sta.api.domain.service.RelationDomainService;
import org.n52.sta.api.domain.service.SensorDomainService;
import org.n52.sta.api.domain.service.ThingDomainService;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.License;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Project;
import org.n52.sta.api.entity.Relation;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.service.AbstractEntityService;
import org.n52.sta.api.service.DatastreamService;
import org.n52.sta.api.service.FeatureOfInterestService;
import org.n52.sta.api.service.GroupService;
import org.n52.sta.api.service.HistoricalLocationService;
import org.n52.sta.api.service.LicenseService;
import org.n52.sta.api.service.LocationService;
import org.n52.sta.api.service.ObservationService;
import org.n52.sta.api.service.ObservedPropertyService;
import org.n52.sta.api.service.PartyService;
import org.n52.sta.api.service.ProjectService;
import org.n52.sta.api.service.RelationService;
import org.n52.sta.api.service.SensorService;
import org.n52.sta.api.service.ThingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class EntityServiceConfiguration {

    @Autowired
    private EntityServiceLookup serviceLookup;

    @Bean
    public EntityServiceLookup entityServiceLookup() {
        return new EntityServiceLookup();
    }

    @Bean
    public AbstractEntityService<Thing> thingService(EntityProvider<Thing> entityProvider,
            Optional<EntityEditor<Thing>> entityEditor) {
        ThingService service = new ThingService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(Thing.class, new ThingDomainService(service));
        return service;
    }

    @Bean
    public AbstractEntityService<Datastream> datastreamService(EntityProvider<Datastream> entityProvider,
            Optional<EntityEditor<Datastream>> entityEditor) {
        DatastreamService service = new DatastreamService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(Datastream.class, new DatastreamDomainService(service));
        return service;
    }

    @Bean
    public AbstractEntityService<Sensor> sensorService(EntityProvider<Sensor> entityProvider,
            Optional<EntityEditor<Sensor>> entityEditor) {
        SensorService service = new SensorService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(Sensor.class, new SensorDomainService(service));
        return service;
    }

    @Bean
    public AbstractEntityService<Location> locationService(EntityProvider<Location> entityProvider,
            Optional<EntityEditor<Location>> entityEditor) {
        LocationService service = new LocationService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(Location.class, new LocationDomainService(service));
        return service;
    }

    @Bean
    public AbstractEntityService<ObservedProperty> observedPropertyService(
            EntityProvider<ObservedProperty> entityProvider,
            Optional<EntityEditor<ObservedProperty>> entityEditor,
            EntityServiceLookup serviceLookup) {
        ObservedPropertyService service = new ObservedPropertyService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(ObservedProperty.class, new ObservedPropertyDomainService(service));
        return service;
    }

    @Bean
    public AbstractEntityService<Observation> observationService(EntityProvider<Observation> entityProvider,
            Optional<EntityEditor<Observation>> entityEditor,
            EntityServiceLookup serviceLookup) {
        ObservationService service = new ObservationService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(Observation.class, new ObservationDomainService(service));
        return service;
    }

    @Bean
    public AbstractEntityService<FeatureOfInterest> featureOfInterestService(
            EntityProvider<FeatureOfInterest> entityProvider,
            Optional<EntityEditor<FeatureOfInterest>> entityEditor,
            EntityServiceLookup serviceLookup) {
        FeatureOfInterestService service = new FeatureOfInterestService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(FeatureOfInterest.class, new FeatureOfInterestDomainService(service));
        return service;
    }

    @Bean
    public AbstractEntityService<HistoricalLocation> historicalLocationService(
            EntityProvider<HistoricalLocation> entityProvider,
            Optional<EntityEditor<HistoricalLocation>> entityEditor,
            EntityServiceLookup serviceLookup) {
        HistoricalLocationService service = new HistoricalLocationService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(HistoricalLocation.class, new HistoricalLocationDomainService(service));
        return service;
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    public AbstractEntityService<Group> groupService(EntityProvider<Group> entityProvider,
            Optional<EntityEditor<Group>> entityEditor) {
        GroupService service = new GroupService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(Group.class, new GroupDomainService(service));
        return service;
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    public AbstractEntityService<License> licenseService(EntityProvider<License> entityProvider,
            Optional<EntityEditor<License>> entityEditor) {
        LicenseService service = new LicenseService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(License.class, new LicenseDomainService(service));
        return service;
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    public AbstractEntityService<Party> partyService(EntityProvider<Party> entityProvider,
            Optional<EntityEditor<Party>> entityEditor) {
        PartyService service = new PartyService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(Party.class, new PartyDomainService(service));
        return service;
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    public AbstractEntityService<Project> projectService(EntityProvider<Project> entityProvider,
            Optional<EntityEditor<Project>> entityEditor) {
        ProjectService service = new ProjectService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(Project.class, new ProjectDomainService(service));
        return service;
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    public AbstractEntityService<Relation> relationService(EntityProvider<Relation> entityProvider,
            Optional<EntityEditor<Relation>> entityEditor) {
        RelationService service = new RelationService(entityProvider);
        entityEditor.ifPresent(service::setEditor);
        serviceLookup.addEntityService(Relation.class, new RelationDomainService(service));
        return service;
    }
}
