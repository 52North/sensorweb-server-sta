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

package org.n52.sta.http.citsci;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.EntityServiceFactory;
import org.n52.sta.api.RequestUtils;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.http.PropertyRequestHandler;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.sta.service.PropertyRequestHandler;
import org.n52.sta.utils.CitSciExtensionRequestUtils;
import org.n52.sta.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles all requests to Entity Properties
 * e.g. /Things(52)/name
 * e.g. /Things(52)/name/$value
 * e.g. /Datastreams(52)/Thing/name
 * e.g. /Datastreams(52)/Thing/name/$value
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@RestController
@Profile(StaConstants.CITSCIEXTENSION)
public class CitSciPropertyRequestHandler extends PropertyRequestHandler implements CitSciExtensionRequestUtils {

    public CitSciPropertyRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                        @Value("${server.feature.escapeId:true}") boolean shouldEscapeId,
                                        EntityServiceFactory serviceRepository,
                                        ObjectMapper mapper) {
        super(rootUrl, shouldEscapeId, serviceRepository, mapper);
    }

    @GetMapping(
        value = RequestUtils.MAPPING_PREFIX
            + CitSciExtensionRequestUtils.ENTITY_IDENTIFIED_DIRECTLY
            + RequestUtils.SLASH
            + RequestUtils.PATH_PROPERTY,
        produces = "application/json"
    )
    public StaDTO readEntityPropertyDirect(@PathVariable String entity,
                                              @PathVariable String id,
                                              @PathVariable String property,
                                              HttpServletRequest request) throws Exception {
        return super.readEntityPropertyDirect(entity, id, property, request);
    }

    /*
    @GetMapping(
            value = {

            },
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readRelatedEntityProperty(@PathVariable String entity,
                                                                @PathVariable String target,
                                                                @PathVariable String property,
                                                                HttpServletRequest request)
            throws Exception {
        return super.readRelatedEntityProperty(entity, target, property, request);
    }
    */

    @GetMapping(
        value = RequestUtils.MAPPING_PREFIX
            + CitSciExtensionRequestUtils.ENTITY_IDENTIFIED_DIRECTLY
            + RequestUtils.SLASH
            + RequestUtils.PATH_PROPERTY
            + RequestUtils.SLASHVALUE,
        produces = "text/plain"
    )
    public String readEntityPropertyValueDirect(@PathVariable String entity,
                                                @PathVariable String id,
                                                @PathVariable String property,
                                                HttpServletRequest request) throws Exception {
        return super.readEntityPropertyValueDirect(entity, id, property, request);
    }

    /*
    @GetMapping(
            value = {

            },
            produces = "text/plain"
    )
    public String readRelatedEntityPropertyValue(@PathVariable String entity,
                                                 @PathVariable String target,
                                                 @PathVariable String property,
                                                 HttpServletRequest request) throws Exception {
        return super.readRelatedEntityPropertyValue(entity, target, property, request);
    }
    */
}
