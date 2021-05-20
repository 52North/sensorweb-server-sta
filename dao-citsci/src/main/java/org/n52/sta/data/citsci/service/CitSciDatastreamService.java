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

package org.n52.sta.data.citsci.service;

import org.n52.sta.data.vanilla.repositories.CategoryRepository;
import org.n52.sta.data.vanilla.repositories.DatastreamParameterRepository;
import org.n52.sta.data.vanilla.repositories.DatastreamRepository;
import org.n52.sta.data.vanilla.repositories.ObservationRepository;
import org.n52.sta.data.vanilla.repositories.UnitRepository;
import org.n52.sta.data.vanilla.service.DatastreamService;
import org.n52.sta.data.vanilla.service.FormatService;
import org.n52.sta.data.vanilla.service.OfferingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext", "datastreamRepository"})
@Transactional
public class CitSciDatastreamService extends DatastreamService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamService.class);

    public CitSciDatastreamService(
        DatastreamRepository repository,
        @Value("${server.feature.isMobile:false}") boolean isMobileFeatureEnabled,
        @Value("${server.feature.includeDatastreamCategory:false}")
            boolean includeDatastreamCategory,
        UnitRepository unitRepository,
        CategoryRepository categoryRepository,
        ObservationRepository observationRepository,
        DatastreamParameterRepository parameterRepository,
        OfferingService offeringService,
        FormatService formatService,
        EntityManager em) {
        super(repository,
              isMobileFeatureEnabled,
              includeDatastreamCategory,
              unitRepository,
              categoryRepository,
              observationRepository,
              parameterRepository,
              offeringService,
              formatService,
              em);
    }

    //TODO: Overwrite some methods
}
