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
package org.n52.sta.http.old;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.n52.sta.api.old.CoreRequestUtils;
import org.n52.sta.api.old.EntityServiceFactory;
import org.n52.sta.api.old.dto.common.StaDTO;
import org.n52.sta.http.old.common.PropertyRequestHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Handles all requests to Entity Properties
 * e.g. /Things(52)/name
 * e.g. /Things(52)/name/$value
 * e.g. /Datastreams(52)/Thing/name
 * e.g. /Datastreams(52)/Thing/name/$value
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("checkstyle:linelength")
public class CorePropertyRequestHandler extends PropertyRequestHandler implements CoreRequestUtils {

        public CorePropertyRequestHandler(
                        @Value("${server.config.service-root-url}") String rootUrl,
                        @Value("${server.feature.escapeId:true}") boolean escapeId,
                        EntityServiceFactory serviceRepository,
                        ObjectMapper mapper) {
                super(rootUrl, escapeId, serviceRepository, mapper);
        }

        @GetMapping(value = MAPPING_PREFIX + CoreRequestUtils.ENTITY_IDENTIFIED_DIRECTLY + SLASH
                        + PATH_PROPERTY, produces = "application/json")
        public StaDTO readEntityPropertyDirect(@PathVariable String entity,
                        @PathVariable String id,
                        @PathVariable String property,
                        HttpServletRequest request) throws Exception {
                return super.readEntityPropertyDirect(entity, id, property, request);
        }

        @GetMapping(value = {
                        MAPPING_PREFIX + CoreRequestUtils.ENTITY_PROPERTY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE,
                        MAPPING_PREFIX + CoreRequestUtils.ENTITY_PROPERTY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE,
                        MAPPING_PREFIX + CoreRequestUtils.ENTITY_PROPERTY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE
        }, produces = "application/json")
        public StaDTO readRelatedEntityProperty(@PathVariable String entity,
                        @PathVariable String target,
                        @PathVariable String property,
                        HttpServletRequest request)
                        throws Exception {
                return super.readRelatedEntityProperty(entity, target, property, request);
        }

        @GetMapping(value = MAPPING_PREFIX
                        + CoreRequestUtils.ENTITY_IDENTIFIED_DIRECTLY + SLASH
                        + PATH_PROPERTY
                        + SLASHVALUE, produces = "text/plain")
        public String readEntityPropertyValueDirect(@PathVariable String entity,
                        @PathVariable String id,
                        @PathVariable String property,
                        HttpServletRequest request) throws Exception {
                return super.readEntityPropertyValueDirect(entity, id, property, request);
        }

        @GetMapping(value = {
                        MAPPING_PREFIX
                                        + CoreRequestUtils.ENTITY_PROPERTY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE
                                        + SLASHVALUE,
                        MAPPING_PREFIX
                                        + CoreRequestUtils.ENTITY_PROPERTY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE
                                        + SLASHVALUE,
                        MAPPING_PREFIX
                                        + CoreRequestUtils.ENTITY_PROPERTY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE
                                        + SLASHVALUE
        }, produces = "text/plain")
        public String readRelatedEntityPropertyValue(@PathVariable String entity,
                        @PathVariable String target,
                        @PathVariable String property,
                        HttpServletRequest request) throws Exception {
                return super.readRelatedEntityPropertyValue(entity, target, property, request);
        }
}
