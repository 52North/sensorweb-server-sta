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

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.data.old.common.service.CommonEntityServiceRepository;
import org.n52.sta.data.old.service.ObservationService;
import org.n52.sta.plus.persistence.service.CitSciEntityServiceRepository;
import org.n52.sta.plus.persistence.service.CitSciObservationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Loads an alternate implementations for {@link ObservationService} and
 * {@link CommonEntityServiceRepository}.
 *
 * @see CitSciObservationService
 * @see CitSciEntityServiceRepository
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Configuration
@Profile(StaConstants.STAPLUS)
public class StaPlusConfiguration {

    @Bean
    @Primary
    public ObservationService getObservationService() {
        return new CitSciObservationService();
    }

    @Bean
    @Primary
    public CitSciEntityServiceRepository getEntityServiceRepository() {
        return new CitSciEntityServiceRepository();
    }
}
