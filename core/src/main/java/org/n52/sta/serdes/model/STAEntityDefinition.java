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

package org.n52.sta.serdes.model;

import org.n52.shetland.ogc.sta.StaConstants;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("VisibilityModifier")
public interface STAEntityDefinition extends StaConstants {

     String[] ALLCOLLECTIONS = new String[] {
            DATASTREAMS,
            OBSERVATIONS,
            THINGS,
            LOCATIONS,
            HISTORICAL_LOCATIONS,
            SENSORS,
            OBSERVED_PROPERTIES,
            FEATURES_OF_INTEREST
    };

    // Entity Property Names
     String PROP_ID = "id";
     String PROP_SELF_LINK = "selfLink";
     String PROP_DEFINITION = "definition";
     String PROP_DESCRIPTION = "description";
     String PROP_ENCODINGTYPE = "encodingType";
     String PROP_FEATURE = "feature";
     String PROP_LOCATION = "location";
     String PROP_NAME = "name";
     String PROP_OBSERVATION_TYPE = "observationType";
     String PROP_OBSERVED_AREA = "observedArea";
     String PROP_PARAMETERS = "parameters";
     String PROP_PHENOMENON_TIME = "phenomenonTime";
     String PROP_PROPERTIES = "properties";
     String PROP_RESULT = "result";
     String PROP_RESULT_QUALITY = "resultQuality";
     String PROP_RESULT_TIME = "resultTime";
     String PROP_TIME = "time";
     String PROP_UOM = "unitOfMeasurement";
     String PROP_VALID_TIME = "validTime";
     String PROP_METADATA = "metadata";
     String PROP_SYMBOL = "symbol";

    static Set<String> combineSets(Set<String>... sets) {
        HashSet<String> result = new HashSet<>();
        for (Set<String> set : sets) {
            result.addAll(set);
        }
        return result;
    }
}
