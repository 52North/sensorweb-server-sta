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

package org.n52.sta.data.support;

enum GraphText {
    GRAPH_DEFAULT("identifier"),
    GRAPH_PLATFORMS("platforms(parameters)"),
    GRAPH_PLATFORM("platform(parameters)"),
    GRAPH_LOCATIONS("locations(parameters)"),
    GRAPH_HIST_LOCATIONS("historicalLocations"),

    GRAPH_UOM("unit"),
    GRAPH_PARAMETERS("parameters"),
    GRAPH_FORMAT("format"),
    GRAPH_FEATURETYPE("featureType"),
    GRAPH_PROCEDURE("procedure(format,procedureHistory,parameters)"),
    GRAPH_PHENOMENON("phenomenon(parameters)"),
    GRAPH_OM_OBS_TYPE("omObservationType"),
    GRAPH_DATASETS("datasets(category,unit,omObservationType,parameters)"),
    GRAPH_FEATURE("feature"),
    GRAPH_DATASET_FIRSTLAST_OBSERVATION("dataset(firstObservation,lastObservation)"),
    GRAPH_PROCEDUREHISTORY("procedureHistory"),
    GRAPH_CATEGORY("category"),

    GRAPH_PLATFORMSHISTLOCATION("platforms(historicalLocations)"),
    GRAPH_LOCATIONHISTLOCATION("locations(historicalLocations)");

    private final String value;

    GraphText(String graphText) {
        this.value = graphText;
    }

    String value() {
        return value;
    }

}
