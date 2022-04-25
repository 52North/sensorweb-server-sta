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
package org.n52.sta.http.old.common;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.sta.api.old.EntityServiceFactory;
import org.n52.sta.api.old.dto.common.StaDTO;
import org.n52.sta.old.utils.AbstractSTARequestHandler;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Handles all requests to Entity Properties
 * e.g. /Things(52)/name
 * e.g. /Things(52)/name/$value
 * e.g. /Datastreams(52)/Thing/name
 * e.g. /Datastreams(52)/Thing/name/$value
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class PropertyRequestHandler extends AbstractSTARequestHandler {

    private final ObjectMapper mapper;

    public PropertyRequestHandler(String rootUrl,
                                  boolean shouldEscapeId,
                                  EntityServiceFactory serviceRepository,
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
    public StaDTO readEntityPropertyDirect(String entity,
                                           String id,
                                           String property,
                                           HttpServletRequest request) throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        return readEntityPropertyDirect(entity, id, property, lookupPath);
    }

    private StaDTO readEntityPropertyDirect(String entity,
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
    public StaDTO readRelatedEntityProperty(String entity,
                                            String target,
                                            String property,
                                            HttpServletRequest request)
        throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        return readRelatedEntityProperty(entity, target, property, lookupPath);
    }

    private StaDTO readRelatedEntityProperty(String entity,
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
    public String readEntityPropertyValueDirect(String entity,
                                                String id,
                                                String property,
                                                HttpServletRequest request) throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        StaDTO elementWithQueryOptions =
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
    public String readRelatedEntityPropertyValue(String entity,
                                                 String target,
                                                 String property,
                                                 HttpServletRequest request) throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        StaDTO elementWithQueryOptions =
            this.readRelatedEntityProperty(entity,
                                           target,
                                           property,
                                           lookupPath.substring(0, lookupPath.length() - 7));
        return mapper.valueToTree(elementWithQueryOptions).fields().next().getValue().toString();
    }
}
