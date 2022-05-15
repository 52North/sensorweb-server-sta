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

import org.n52.grammar.STAPathGrammar;
import org.n52.grammar.STAPathLexer;
import org.n52.sta.http.old.filter.CorsFilter;
import org.n52.sta.http.util.CustomUrlPathHelper;
import org.n52.sta.http.util.StaUriValidator;
import org.n52.sta.http.util.path.PathFactory;
import org.n52.sta.http.util.path.StaPathVisitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUrlPathHelper(new CustomUrlPathHelper());
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
            .ignoreAcceptHeader(true);
    }

    @Bean
    public PathFactory pathFactory() {
        return new PathFactory(
            STAPathLexer::new,
            STAPathGrammar::new,
            StaPathVisitor::new,
            "path"
        );
    }

    @Bean
    StaUriValidator uriValidator() {
        return new StaUriValidator();
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
