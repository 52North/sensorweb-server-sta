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

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityServiceLookup;
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
import org.n52.sta.data.editor.DatastreamEntityEditor;
import org.n52.sta.data.editor.FeatureOfInterestEntityEditor;
import org.n52.sta.data.editor.GroupEntityEditor;
import org.n52.sta.data.editor.HistoricalLocationEntityEditor;
import org.n52.sta.data.editor.LicenseEntityEditor;
import org.n52.sta.data.editor.LocationEntityEditor;
import org.n52.sta.data.editor.ObservationEntityEditor;
import org.n52.sta.data.editor.ObservedPropertyEntityEditor;
import org.n52.sta.data.editor.PartyEntityEditor;
import org.n52.sta.data.editor.ProjectEntityEditor;
import org.n52.sta.data.editor.RelationEntityEditor;
import org.n52.sta.data.editor.SensorEntityEditor;
import org.n52.sta.data.editor.ThingEntityEditor;
import org.n52.sta.data.editor.ValueHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ConditionalOnExpression(""
        + "${server.feature.http.writable:true} or "
        + "(${server.feature.mqtt.enabled:true} and ${server.feature.mqtt.writable:true})")
@EnableTransactionManagement
public class DataEditorConfiguration {

    @Autowired
    private EntityServiceLookup serviceLookup;

    @Bean
    public EntityEditor<Datastream> datastreamEntityEditor() {
        return new DatastreamEntityEditor(serviceLookup);
    }

    @Bean
    public EntityEditor<Thing> thingEntityEditor() {
        return new ThingEntityEditor(serviceLookup);
    }

    @Bean
    public EntityEditor<Observation> observationEntityEditor() {
        return new ObservationEntityEditor(serviceLookup);
    }

    @Bean
    public EntityEditor<ObservedProperty> observedPropertyEntityEditor() {
        return new ObservedPropertyEntityEditor(serviceLookup);
    }

    @Bean
    public EntityEditor<Sensor> sensorEntityEditor() {
        return new SensorEntityEditor(serviceLookup);
    }

    @Bean
    public EntityEditor<Location> locationEntityEditor() {
        return new LocationEntityEditor(serviceLookup);
    }

    @Bean
    EntityEditor<HistoricalLocation> historicalLocationEntityEditor() {
        return new HistoricalLocationEntityEditor(serviceLookup);
    }

    @Bean
    EntityEditor<FeatureOfInterest> featureOfInterestEntityEditor() {
        return new FeatureOfInterestEntityEditor(serviceLookup);
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    EntityEditor<Group> groupEntityEditor() {
        return new GroupEntityEditor(serviceLookup);
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    EntityEditor<License> licenseEntityEditor() {
        return new LicenseEntityEditor(serviceLookup);
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    EntityEditor<Party> partyEntityEditor() {
        return new PartyEntityEditor(serviceLookup);
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    EntityEditor<Project> projectEntityEditor() {
        return new ProjectEntityEditor(serviceLookup);
    }

    @Bean
    @Profile(StaConstants.STAPLUS)
    EntityEditor<Relation> relationEntityEditor() {
        return new RelationEntityEditor(serviceLookup);
    }

    @Bean
    public ValueHelper valueHelper() {
        return new ValueHelper();
    }

}
