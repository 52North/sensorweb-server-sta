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
public abstract class STAEntityDefinition implements StaConstants {


    public static String[] ALLCOLLECTIONS = new String[] {
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
    public static String PROP_ID = "id";
    public static String PROP_SELF_LINK = "selfLink";
    public static String PROP_DEFINITION = "definition";
    public static String PROP_DESCRIPTION = "description";
    public static String PROP_ENCODINGTYPE = "encodingType";
    public static String PROP_FEATURE = "feature";
    public static String PROP_LOCATION = "location";
    public static String PROP_NAME = "name";
    public static String PROP_OBSERVATION_TYPE = "observationType";
    public static String PROP_OBSERVED_AREA = "observedArea";
    public static String PROP_PARAMETERS = "parameters";
    public static String PROP_PHENOMENON_TIME = "phenomenonTime";
    public static String PROP_PROPERTIES = "properties";
    public static String PROP_RESULT = "result";
    public static String PROP_RESULT_QUALITY = "resultQuality";
    public static String PROP_RESULT_TIME = "resultTime";
    public static String PROP_TIME = "time";
    public static String PROP_UOM = "unitOfMeasurement";
    public static String PROP_VALID_TIME = "validTime";
    public static String PROP_METADATA = "metadata";
    public static String PROP_SYMBOL = "symbol";
    protected STAEntityDefinition(Set<String> navPropOptional,
                                  Set<String> navPropMandatory,
                                  Set<String> entityPropOptional,
                                  Set<String> entityPropMandatory) {
        this.navPropsOptional = navPropOptional;
        this.navPropsMandatory = navPropMandatory;
        this.entityPropsOptional = entityPropOptional;
        this.entityPropsMandatory = entityPropMandatory;
    }

    private final Set<String> navPropsOptional;
    private final Set<String> navPropsMandatory;
    private final Set<String> entityPropsOptional;
    private final Set<String> entityPropsMandatory;

    static Set<String> combineSets(Set<String>... sets) {
        HashSet<String> result = new HashSet<>();
        for (Set<String> set : sets) {
            result.addAll(set);
        }
        return result;
    }

    public Set<String> getNavPropsOptional() {
        return navPropsOptional;
    }

    public Set<String> getNavPropsMandatory() {
        return navPropsMandatory;
    }

    public Set<String> getEntityPropsOptional() {
        return entityPropsOptional;
    }

    public Set<String> getEntityPropsMandatory() {
        return entityPropsMandatory;
    }
}
