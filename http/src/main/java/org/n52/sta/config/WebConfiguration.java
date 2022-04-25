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

import javax.servlet.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.n52.sta.api.old.EntityServiceFactory;
import org.n52.sta.http.old.CoreCollectionRequestHandler;
import org.n52.sta.http.old.CoreCudRequestHandler;
import org.n52.sta.http.old.CoreEntityRequestHandler;
import org.n52.sta.http.old.CorePropertyRequestHandler;
import org.n52.sta.http.old.common.CollectionRequestHandler;
import org.n52.sta.http.old.common.CustomUrlPathHelper;
import org.n52.sta.http.old.common.EntityRequestHandler;
import org.n52.sta.http.old.common.PropertyRequestHandler;
import org.n52.sta.http.old.common.RootRequestHandler;
import org.n52.sta.http.old.filter.CorsFilter;
import org.n52.sta.old.utils.DTOMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUrlPathHelper(new CustomUrlPathHelper());
    }

    @Bean
    public RootRequestHandler getRootRequestHandler(
            ObjectMapper mapper,
            Environment environment,
            ServerProperties serverProperties) {
        return new RootRequestHandler(mapper, environment, serverProperties);
    }

    @Bean
    public EntityRequestHandler getEntityRequestHandler(
            @Value("${server.rootUrl}") String rootUrl,
            @Value("${server.feature.escapeId:true}") boolean escapeId,
            EntityServiceFactory serviceRepository) {
        return new CoreEntityRequestHandler(rootUrl, escapeId, serviceRepository);
    }

    @Bean
    public PropertyRequestHandler getPropertyRequestHandler(
            @Value("${server.rootUrl}") String rootUrl,
            @Value("${server.feature.escapeId:true}") boolean escapeId,
            EntityServiceFactory serviceRepository,
            ObjectMapper mapper) {
        return new CorePropertyRequestHandler(rootUrl, escapeId, serviceRepository, mapper);
    }

    @Bean
    public CollectionRequestHandler getCollectionRequestHandler(
            @Value("${server.rootUrl}") String rootUrl,
            @Value("${server.feature.escapeId:true}") boolean escapeId,
            EntityServiceFactory serviceRepository) {
        return new CoreCollectionRequestHandler(rootUrl, escapeId, serviceRepository);
    }

    @Bean
    @ConditionalOnProperty(value = "server.feature.httpReadOnly", havingValue = "false", matchIfMissing = true)
    public CoreCudRequestHandler getWritableRequestHandler(
            @Value("${server.rootUrl}") String rootUrl,
            @Value("${server.feature.escapeId:true}") boolean escapeId,
            EntityServiceFactory serviceRepository,
            ObjectMapper mapper,
            DTOMapper dtoMapper) {
        return new CoreCudRequestHandler(rootUrl, escapeId, serviceRepository, mapper, dtoMapper);
    }

    @Bean
    public Filter getCorsFilter(
            @Value("${http.cors.allowOrigin:*}") String origin,
            @Value("${http.cors.allowMethods:POST, GET, OPTIONS, DELETE, PATCH}") String methods,
            @Value("${http.cors.maxAge:3600}") String maxAge,
            @Value("${http.cors.allowHeaders:Access-Control-Allow-Headers," +
                    "Content-Type, Access-Control-Allow-Headers," +
                    "Authorization," +
                    "X-Requested-With}") String headers) {
        return new CorsFilter(origin, methods, maxAge, headers);
    }

}
