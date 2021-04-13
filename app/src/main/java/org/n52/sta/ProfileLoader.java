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
import java.util.HashSet;
import java.util.Set;

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
    private static final String DATA_PREFIX = STA_PREFIX + "data";
    private static final String VANILLA = "vanilla";
    private static final String CITSCI = "citsci";
    private static final String UFZAGGREGATA = "ufz-aggregata";
    private Environment env;
    private Set<String> blacklist;

    private String[] VANILLA_PACKAGES = new String[] {
        HTTP_PREFIX + VANILLA,
        MQTT_PREFIX + VANILLA,
        DATA_PREFIX + VANILLA
    };

    private String[] CITSCI_PACKAGES = new String[] {
        HTTP_PREFIX + CITSCI,
        MQTT_PREFIX + CITSCI,
        DATA_PREFIX + CITSCI
    };

    private String[] UFZAGGREGATA_PACKAGES = new String[] {
        DATA_PREFIX + UFZAGGREGATA,
    };

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
        throws IOException {
        boolean match = true;

        // Check if class should be loaded
        for (String activeProfile : env.getActiveProfiles()) {
            if (activeProfile.equals(VANILLA)) {
                match = isClassInPackage(metadataReader.getClassMetadata(), VANILLA_PACKAGES);
            }
            if (activeProfile.equals(CITSCI)) {
                match = isClassInPackage(metadataReader.getClassMetadata(), CITSCI_PACKAGES);
            }
            if (activeProfile.equals(UFZAGGREGATA)) {
                match = isClassInPackage(metadataReader.getClassMetadata(), UFZAGGREGATA_PACKAGES);
            }
            System.out.println(metadataReader.getClassMetadata().getClassName() + "  " + match);
        }

        // Check against blacklist (some profiles may exclude certain classes from loading)
        match = match && !blacklist.contains(metadataReader.getClassMetadata().getClassName());
        return false;
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
        createBlacklist(env.getActiveProfiles());
    }

    private Set<String> createBlacklist(String[] activeProfiles) {
        this.blacklist = new HashSet<>();
        for (String activeProfile : activeProfiles) {
            if (activeProfile.equals(UFZAGGREGATA)) {
                // Block regular ObservationService
                blacklist.add(DATA_PREFIX + "." + VANILLA + ".service.ObservationService");
            }
        }
        return blacklist;
    }

}
