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

import javax.xml.crypto.Data;

import org.geolatte.geom.V;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityEditorDelegate;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.data.editor.DatastreamEntityEditor;
import org.n52.sta.data.editor.ObservationEntityEditor;
import org.n52.sta.data.editor.ObservedPropertyEntityEditor;
import org.n52.sta.data.editor.SensorEntityEditor;
import org.n52.sta.data.editor.ThingEntityEditor;
import org.n52.sta.data.editor.ValueHelper;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.entity.ObservedPropertyData;
import org.n52.sta.data.entity.SensorData;
import org.n52.sta.data.entity.ThingData;
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
    public EntityEditorDelegate<Datastream, DatastreamData> datastreamEntityEditor() {
        return new DatastreamEntityEditor(serviceLookup);
    }

    @Bean
    public EntityEditorDelegate<Thing, ThingData> thingEntityEditor() {
        return new ThingEntityEditor(serviceLookup);
    }

    @Bean
    public EntityEditorDelegate<Observation, ObservationData> observationEntityEditor() {
        return new ObservationEntityEditor(serviceLookup);
    }

    @Bean
    public EntityEditorDelegate<ObservedProperty, ObservedPropertyData> observedPropertyEntityEditor() {
        return new ObservedPropertyEntityEditor(serviceLookup);
    }

    @Bean
    public EntityEditorDelegate<Sensor, SensorData> sensorEntityEditor() {
        return new SensorEntityEditor(serviceLookup);
    }

    @Bean
    public ValueHelper valueHelper() {
        return new ValueHelper();
    }

    // TODO add missing editors

}
