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

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Starts a Postgis Container to be used in IT Tests
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class STAPostgresContainer extends PostgreSQLContainer<STAPostgresContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME =
            DockerImageName.parse("postgis/postgis").asCompatibleSubstituteFor("postgres");
    private static final String DEFAULT_TAG = "12-3.0";
    private static STAPostgresContainer container;

    private STAPostgresContainer() {
        super(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    public static STAPostgresContainer instance() {
        if (container == null) {
            container = new STAPostgresContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
        System.setProperty("ALLOW_IP_RANGE", "0.0.0.0/0");
        System.setProperty("POSTGRES_MULTIPLE_EXTENSIONS", "postgis");
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
