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

import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.*;
import org.n52.sta.data.editor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public ValueHelper valueHelper() {
        return new ValueHelper();
    }

}