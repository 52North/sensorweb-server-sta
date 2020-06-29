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
import org.n52.sta.utils.RequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class AbstractPropertyRequestHandler implements RequestUtils {

    protected final EntityServiceRepository serviceRepository;
    protected final ObjectMapper mapper;

    public AbstractPropertyRequestHandler(
            EntityServiceRepository serviceRepository, ObjectMapper mapper) {
        this.serviceRepository = serviceRepository;
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
    public ElementWithQueryOptions<?> readEntityPropertyDirect(String entity,
                                                               String id,
                                                               String property,
                                                               HttpServletRequest request) throws Exception {

        String url = request.getRequestURI().substring(request.getContextPath().length());
        return readEntityPropertyDirect(entity, id, property, url);
    }

    private ElementWithQueryOptions<?> readEntityPropertyDirect(String entity,
                                                                String id,
                                                                String property,
                                                                String url) throws Exception {
        validateResource(url.substring(0, url.length() - property.length() - 1),
                         serviceRepository);
        validateProperty(entity, property);

        String entityId = id.substring(1, id.length() - 1);
        HashSet<FilterClause> filters = new HashSet<>();

        // Add select filter with filter only returning property
        filters.add(new SelectFilter(property));
        return serviceRepository.getEntityService(entity)
                                .getEntity(entityId, RequestUtils.QUERY_OPTIONS_FACTORY.createQueryOptions(filters));
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
    public ElementWithQueryOptions<?> readRelatedEntityProperty(String entity,
                                                                String target,
                                                                String property,
                                                                HttpServletRequest request)
            throws Exception {
        String url = request.getRequestURI().substring(request.getContextPath().length());
        return readRelatedEntityProperty(entity, target, property, url);
    }

    private ElementWithQueryOptions readRelatedEntityProperty(String entity,
                                                              String target,
                                                              String property,
                                                              String url) throws Exception {
        validateResource(url.substring(0, url.length() - property.length() - 1),
                         serviceRepository);
        String sourceType = entity.substring(0, entity.indexOf("("));
        String sourceId = entity.substring(sourceType.length() + 1, entity.length() - 1);

        validateProperty(sourceType, property);

        HashSet<FilterClause> filters = new HashSet<>();
        // Add select filter with filter only returning property
        filters.add(new SelectFilter(property));

        return serviceRepository.getEntityService(target)
                                .getEntityByRelatedEntity(
                                        sourceId,
                                        sourceType,
                                        null,
                                        RequestUtils.QUERY_OPTIONS_FACTORY.createQueryOptions(filters)
                                );
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
        String url = request.getRequestURI().substring(request.getContextPath().length());
        ElementWithQueryOptions<?> elementWithQueryOptions =
                this.readEntityPropertyDirect(entity, id, property, url.substring(0, url.length() - 7));
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
        String url = request.getRequestURI().substring(request.getContextPath().length());
        ElementWithQueryOptions<?> elementWithQueryOptions =
                this.readRelatedEntityProperty(entity, target, property, url.substring(0, url.length() - 7));
        return mapper.valueToTree(elementWithQueryOptions).fields().next().getValue().toString();
    }
}
