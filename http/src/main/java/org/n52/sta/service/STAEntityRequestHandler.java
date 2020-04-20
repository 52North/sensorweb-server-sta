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

package org.n52.sta.service;

import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.sta.utils.STARequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.HashSet;

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
public class STAEntityRequestHandler implements STARequestUtils {

    private final EntityServiceRepository serviceRepository;

    public STAEntityRequestHandler(EntityServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /**
     * Matches all requests on Entities referenced directly via id
     * e.g. /Datastreams(52)
     *
     * @param entity  name of entity. Automatically set by Spring via @PathVariable
     * @param id      id of entity. Automatically set by Spring via @PathVariable
     * @param request full request
     */
    @GetMapping(
            value = MAPPING_PREFIX + ENTITY_IDENTIFIED_DIRECTLY,
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readEntityDirect(@PathVariable String entity,
                                                       @PathVariable String id,
                                                       HttpServletRequest request) throws Exception {
        validateResource(request.getRequestURI().substring(request.getContextPath().length()), serviceRepository);

        String entityId = id.substring(1, id.length() - 1);
        String queryString = request.getQueryString();
        QueryOptions options;
        if (queryString != null) {
            options = QUERY_OPTIONS_FACTORY.createQueryOptions(
                    URLDecoder.decode(request.getQueryString().replaceAll("\\+", "%2b")));
        } else {
            options = QUERY_OPTIONS_FACTORY.createDummy();
        }
        return serviceRepository.getEntityService(entity)
                                .getEntity(entityId, options);
    }

    /**
     * Matches all requests on Entities referenced directly via id and addressing an association link
     * e.g. /Datastreams(52)/$ref
     *
     * @param entity  name of entity. Automatically set by Spring via @PathVariable
     * @param id      id of entity. Automatically set by Spring via @PathVariable
     * @param request full request
     */
    @GetMapping(
            value = MAPPING_PREFIX + ENTITY_IDENTIFIED_DIRECTLY + SLASHREF,
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readEntityRefDirect(@PathVariable String entity,
                                                          @PathVariable String id,
                                                          HttpServletRequest request) throws Exception {
        String requestURI = request.getRequestURI();
        validateResource(requestURI.substring(request.getContextPath().length(),
                                              requestURI.length() - 5), serviceRepository);

        String entityId = id.substring(1, id.length() - 1);
        HashSet<FilterClause> filters = new HashSet<>();
        // Overwrite select filter with filter only returning id
        filters.add(new SelectFilter(ID));
        return serviceRepository.getEntityService(entity)
                                .getEntity(entityId, QUERY_OPTIONS_FACTORY.createQueryOptions(filters));
    }

    /**
     * Matches all requests on Entities not referenced directly via id but via referenced entity
     * e.g. /Datastreams(52)/Thing
     *
     * @param entity  composite of entity and referenced entity. Automatically set by Spring via @PathVariable
     * @param request full request
     * @return JSON String representing Entity
     */
    @GetMapping(
            value = {
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE
            },
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readRelatedEntity(@PathVariable String entity,
                                                        @PathVariable String target,
                                                        HttpServletRequest request)
            throws Exception {

        validateResource(request.getRequestURI().substring(request.getContextPath().length()), serviceRepository);

        String sourceType = entity.substring(0, entity.indexOf("("));
        String sourceId = entity.substring(sourceType.length() + 1, entity.length() - 1);

        String queryString = request.getQueryString();
        QueryOptions options;
        if (queryString != null) {
            options = QUERY_OPTIONS_FACTORY.createQueryOptions(
                    URLDecoder.decode(request.getQueryString().replaceAll("\\+", "%2b")));
        } else {
            options = QUERY_OPTIONS_FACTORY.createDummy();
        }
        return serviceRepository.getEntityService(target)
                                .getEntityByRelatedEntity(sourceId,
                                                          sourceType,
                                                          null,
                                                          options);
    }

    /**
     * Matches all requests on Entities not referenced directly via id but via referenced entity and addressing an
     * association link
     * e.g. /Datastreams(52)/Thing/$ref
     *
     * @param entity  composite of entity and referenced entity. Automatically set by Spring via @PathVariable
     * @param request full request
     * @return JSON String representing Entity
     */
    @GetMapping(
            value = {
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE + SLASHREF,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE + SLASHREF,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE + SLASHREF
            },
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readRelatedEntityRef(@PathVariable String entity,
                                                           @PathVariable String target,
                                                           HttpServletRequest request)
            throws Exception {
        String requestURI = request.getRequestURI();
        validateResource(requestURI.substring(request.getContextPath().length(), requestURI.length() - 5),
                         serviceRepository);

        String sourceType = entity.substring(0, entity.indexOf("("));
        String sourceId = entity.substring(sourceType.length() + 1, entity.length() - 1);

        HashSet<FilterClause> filters = new HashSet<>();
        // Overwrite select filter with filter only returning id
        filters.add(new SelectFilter(ID));
        return serviceRepository.getEntityService(target)
                                .getEntityByRelatedEntity(sourceId,
                                                          sourceType,
                                                          null,
                                                          QUERY_OPTIONS_FACTORY.createQueryOptions(filters));
    }
}
