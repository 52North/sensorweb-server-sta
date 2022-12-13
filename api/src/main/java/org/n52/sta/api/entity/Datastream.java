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

package org.n52.sta.api.entity;

import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.time.Time;

import java.util.Map;
import java.util.Set;

public interface Datastream extends Identifiable {

    String getName();

    String getDescription();

    Map<String, Object> getProperties();

    String getObservationType();

    UnitOfMeasurement getUnitOfMeasurement();

    Geometry getObservedArea();

    Time getPhenomenonTime();

    Time getResultTime();

    Thing getThing();

    Sensor getSensor();

    ObservedProperty getObservedProperty();

    Set<Observation> getObservations();

    Set<Project> getProjects();

    Party getParty();

    License getLicense();

    class UnitOfMeasurement {

        private final String name;
        private final String symbol;
        private final String definition;

        public UnitOfMeasurement(String symbol, String name, String definition) {
            this.name = name;
            this.symbol = symbol;
            this.definition = definition;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getName() {
            return name;
        }

        public String getDefinition() {
            return definition;
        }
    }
}
