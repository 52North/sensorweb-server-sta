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

package org.n52.sta.service.extension;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.sta.service.EntityRequestHandler;
import org.n52.sta.utils.CitSciExtensionRequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles all requests to Entities and to Entity association links
 * e.g. /Things(52)
 * e.g. /Datastreams(52)/Thing
 * e.g. /Things(52)/$ref
 * e.g. /Datastreams(52)/Thing/$ref
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@RestController
@Profile(StaConstants.CITSCIEXTENSION)
public class CitSciEntityRequestHandler extends EntityRequestHandler implements CitSciExtensionRequestUtils {

    public CitSciEntityRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                      @Value("${server.feature.escapeId:true}") boolean shouldEscapeId,
                                      EntityServiceRepository serviceRepository) {
        super(rootUrl, shouldEscapeId, serviceRepository);
    }

    @GetMapping(
            value = MAPPING_PREFIX + CitSciExtensionRequestUtils.ENTITY_IDENTIFIED_DIRECTLY,
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readEntityDirect(@PathVariable String entity,
                                                       @PathVariable String id,
                                                       HttpServletRequest request) throws Exception {
        return super.readEntityDirect(entity, id, request);
    }

    @GetMapping(
            value = MAPPING_PREFIX + CitSciExtensionRequestUtils.ENTITY_IDENTIFIED_DIRECTLY + SLASHREF,
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readEntityRefDirect(@PathVariable String entity,
                                                          @PathVariable String id,
                                                          HttpServletRequest request) throws Exception {
        return super.readEntityRefDirect(entity, id, request);
    }

    @GetMapping(
            value = {
                    MAPPING_PREFIX + CitSciExtensionRequestUtils.ENTITY_IDENTIFIED_BY_OBSERVATIONRELATION_PATH_VARIABLE,
                    MAPPING_PREFIX + CitSciExtensionRequestUtils.ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE
            },
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readRelatedEntity(@PathVariable String entity,
                                                        @PathVariable String target,
                                                        HttpServletRequest request)
            throws Exception {
        return super.readRelatedEntity(entity, target, request);
    }

    @GetMapping(
            value = {
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.ENTITY_IDENTIFIED_BY_OBSERVATIONRELATION_PATH_VARIABLE
                            + SLASHREF,
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE
                            + SLASHREF
            },
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readRelatedEntityRef(@PathVariable String entity,
                                                           @PathVariable String target,
                                                           HttpServletRequest request)
            throws Exception {
        return super.readRelatedEntityRef(entity, target, request);
    }
}
