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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("VisibilityModifier")
public class SensorEntityDefinition extends STAEntityDefinition {

    public static final String ENTITY_NAME = SENSOR;

    public static final String ENTITY_SET_NAME = SENSORS;

    private static final String[] NAV_PROPS_MANDATORY = new String[] {};

    private static final String[] NAV_PROPS_OPTIONAL = new String[] {
            DATASTREAMS
    };

    private static final String[] ENTITY_PROPS_MANDATORY = new String[] {
            PROP_NAME,
            PROP_DESCRIPTION,
            PROP_ENCODINGTYPE,
            PROP_METADATA
    };

    private static final String[] ENTITY_PROPS_OPTIONAL = new String[] {};

    public static final Set<String> NAVIGATION_PROPERTIES_OPTIONAL =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(NAV_PROPS_OPTIONAL)));

    public static final Set<String> NAVIGATION_PROPERTIES_MANDATORY =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(NAV_PROPS_MANDATORY)));

    public static final Set<String> NAVIGATION_PROPERTIES =
            Collections.unmodifiableSet(STAEntityDefinition.combineSets(NAVIGATION_PROPERTIES_OPTIONAL,
                                                                        NAVIGATION_PROPERTIES_MANDATORY));

    public static final Set<String> ENTITY_PROPERTIES_OPTIONAL =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ENTITY_PROPS_OPTIONAL)));

    public static final Set<String> ENTITY_PROPERTIES_MANDATORY =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ENTITY_PROPS_MANDATORY)));

    public static final Set<String> ENTITY_PROPERTIES =
            Collections.unmodifiableSet(STAEntityDefinition.combineSets(ENTITY_PROPERTIES_OPTIONAL,
                                                                        ENTITY_PROPERTIES_MANDATORY));

    public SensorEntityDefinition() {
        super(NAVIGATION_PROPERTIES_OPTIONAL,
              NAVIGATION_PROPERTIES_MANDATORY,
              ENTITY_PROPERTIES_OPTIONAL,
              ENTITY_PROPERTIES_MANDATORY);
    }
}
