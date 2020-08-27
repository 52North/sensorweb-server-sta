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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.sta.utils.AbstractSTARequestHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;

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
public class STAPropertyRequestHandler extends AbstractSTARequestHandler {

    private final ObjectMapper mapper;

    public STAPropertyRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                     @Value("${server.feature.escapeId:true}") boolean shouldEscapeId,
                                     EntityServiceRepository serviceRepository,
                                     ObjectMapper mapper) {
        super(rootUrl, shouldEscapeId, serviceRepository);
        this.mapper = mapper;
    }

    /**
     * Matches all requests for properties on Entities referenced directly via id
     * e.g. /Datastreams(52)/name
     *
     * @param entity   type of entity. Automatically set by Spring via @PathVariable
     * @param id       id of entity. Automatically set by Spring via @PathVariable
     * @param property property to be returned. Automatically set by Spring via @PathVariable
     * @param request  Full request object. Automatically set by Spring
     */
    @GetMapping(
            value = MAPPING_PREFIX + ENTITY_IDENTIFIED_DIRECTLY + SLASH + PATH_PROPERTY,
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readEntityPropertyDirect(@PathVariable String entity,
                                                               @PathVariable String id,
                                                               @PathVariable String property,
                                                               HttpServletRequest request) throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        return readEntityPropertyDirect(entity, id, property, lookupPath);
    }

    private ElementWithQueryOptions<?> readEntityPropertyDirect(String entity,
                                                                String id,
                                                                String property,
                                                                String url) throws Exception {
        validateResource(url.substring(0, url.length() - property.length() - 1),
                         serviceRepository);
        validateProperty(entity, property);

        String entityId = unescapeIdIfWanted(id.substring(1, id.length() - 1));
        HashSet<FilterClause> filters = new HashSet<>();

        // Add select filter with filter only returning property
        filters.add(new SelectFilter(property));
        return serviceRepository.getEntityService(entity)
                                .getEntity(entityId, QUERY_OPTIONS_FACTORY.createQueryOptions(filters));
    }

    /**
     * Matches all requests for properties on Entities not referenced directly via id but via referenced entity
     * e.g. /Datastreams(52)/Thing/name
     *
     * @param entity   Source Entity. Automatically set by Spring via @PathVariable
     * @param target   Referenced Entity. Automatically set by Spring via @PathVariable
     * @param property Property to be returned. Automatically set by Spring via @PathVariable
     * @param request  Full request object. Automatically set by Spring
     * @return JSON Object with serialized property
     */
    @GetMapping(
            value = {
                    MAPPING_PREFIX + ENTITY_PROPERTY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE,
                    MAPPING_PREFIX + ENTITY_PROPERTY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE,
                    MAPPING_PREFIX + ENTITY_PROPERTY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE
            },
            produces = "application/json"
    )
    public ElementWithQueryOptions<?> readRelatedEntityProperty(@PathVariable String entity,
                                                                @PathVariable String target,
                                                                @PathVariable String property,
                                                                HttpServletRequest request)
            throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        return readRelatedEntityProperty(entity, target, property, lookupPath);
    }

    private ElementWithQueryOptions readRelatedEntityProperty(String entity,
                                                              String target,
                                                              String property,
                                                              String url) throws Exception {
        validateResource(url.substring(0, url.length() - property.length() - 1),
                         serviceRepository);

        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1];

        validateProperty(sourceType, property);
        HashSet<FilterClause> filters = new HashSet<>();
        // Add select filter with filter only returning property
        filters.add(new SelectFilter(property));

        return serviceRepository.getEntityService(target)
                                .getEntityByRelatedEntity(sourceId,
                                                          sourceType,
                                                          null,
                                                          QUERY_OPTIONS_FACTORY.createQueryOptions(filters));
    }

    /**
     * Matches all requests for properties on Entities referenced directly via id
     * e.g. /Datastreams(52)/name/$value
     *
     * @param entity   type of entity. Automatically set by Spring via @PathVariable
     * @param id       id of entity. Automatically set by Spring via @PathVariable
     * @param property property to be returned. Automatically set by Spring via @PathVariable
     * @param request  Full request object. Automatically set by Spring
     */
    @GetMapping(
            value = MAPPING_PREFIX + ENTITY_IDENTIFIED_DIRECTLY + SLASH + PATH_PROPERTY + SLASHVALUE,
            produces = "text/plain"
    )
    public String readEntityPropertyValueDirect(@PathVariable String entity,
                                                @PathVariable String id,
                                                @PathVariable String property,
                                                HttpServletRequest request) throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        ElementWithQueryOptions<?> elementWithQueryOptions =
                this.readEntityPropertyDirect(entity, id, property, lookupPath.substring(0, lookupPath.length() - 7));
        return mapper.valueToTree(elementWithQueryOptions).fields().next().getValue().toString();
    }

    /**
     * Matches all requests for properties on Entities not referenced directly via id but via referenced entity
     * e.g. /Datastreams(52)/Thing/name/$value
     *
     * @param entity   Source Entity. Automatically set by Spring via @PathVariable
     * @param target   Referenced Entity. Automatically set by Spring via @PathVariable
     * @param property Property to be returned. Automatically set by Spring via @PathVariable
     * @param request  Full request object. Automatically set by Spring
     * @return JSON Object with serialized property
     */
    @GetMapping(
            value = {
                    MAPPING_PREFIX + ENTITY_PROPERTY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE + SLASHVALUE,
                    MAPPING_PREFIX + ENTITY_PROPERTY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE + SLASHVALUE,
                    MAPPING_PREFIX + ENTITY_PROPERTY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE + SLASHVALUE
            },
            produces = "text/plain"
    )
    public String readRelatedEntityPropertyValue(@PathVariable String entity,
                                                 @PathVariable String target,
                                                 @PathVariable String property,
                                                 HttpServletRequest request) throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        ElementWithQueryOptions<?> elementWithQueryOptions =
                this.readRelatedEntityProperty(entity,
                                               target,
                                               property,
                                               lookupPath.substring(0, lookupPath.length() - 7));
        return mapper.valueToTree(elementWithQueryOptions).fields().next().getValue().toString();
    }
}
