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
import org.n52.sta.data.old.SerDesConfig;
import org.n52.sta.data.old.service.ObservationService;
import org.n52.sta.data.old.service.SensorService;
import org.n52.sta.data.ufzaggregata.UfzAggregataObservationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Loads an alternate ObservationService Implementation provided by
 * {@link UfzAggregataObservationService}.
 *
 * @see UfzAggregataObservationService
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Configuration
@Profile(StaConstants.UFZAGGREGATA)
// @ComponentScan(excludeFilters = {
//                 @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*service.ObservationService.*")
// })
public class UfzConfiguration {

        @Bean
        public ObservationService getObservationService(SerDesConfig serdesConfig, SensorService sensorService,
                        @Value("${server.security.aggregataToken}") String aggregataToken) {
                return new UfzAggregataObservationService(serdesConfig, sensorService, aggregataToken);
        }
}
