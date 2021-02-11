/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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

package org.n52.sta;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;

/**
 * This implements a filter to only load packages that are used by the current Profile.
 * With this no @Profile Annotation is needed on each individual Bean inside the respective packages
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ProfileLoader implements TypeFilter, EnvironmentAware {

    private static final String STA_PREFIX = "org.n52.sta.";
    private static final String HTTP_PREFIX = STA_PREFIX + "http";
    private static final String MQTT_PREFIX = STA_PREFIX + "mqtt";
    private static final String VANILLA = "vanilla";
    private static final String CITSCI = "citsci";
    private Environment env;
    private String[] VANILLA_PACKAGES = new String[] {
        HTTP_PREFIX + VANILLA,
        MQTT_PREFIX + VANILLA
    };

    private String[] CITSCI_PACKAGES = new String[] {
        HTTP_PREFIX + CITSCI,
        MQTT_PREFIX + CITSCI
    };

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
        throws IOException {

        boolean match = false;
        for (String activeProfile : env.getActiveProfiles()) {
            if (activeProfile.equals(VANILLA)) {
                match = isClassInPackage(metadataReader.getClassMetadata(), VANILLA_PACKAGES);
            } else if (activeProfile.equals(CITSCI)) {
                match = isClassInPackage(metadataReader.getClassMetadata(), CITSCI_PACKAGES);
            }
        }
        return match;
    }

    private boolean isClassInPackage(ClassMetadata metadata, String[] name) {
        for (String s : name) {
            if (metadata.getClassName().startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

}
