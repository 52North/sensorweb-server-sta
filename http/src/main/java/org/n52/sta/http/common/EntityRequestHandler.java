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
package org.n52.sta.http.common;

import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.sta.api.EntityServiceFactory;
import org.n52.sta.api.old.dto.common.StaDTO;
import org.n52.sta.utils.AbstractSTARequestHandler;
import org.springframework.web.servlet.HandlerMapping;

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
public abstract class EntityRequestHandler extends AbstractSTARequestHandler {

    public EntityRequestHandler(String rootUrl,
                                boolean shouldEscapeId,
                                EntityServiceFactory serviceRepository) {
        super(rootUrl, shouldEscapeId, serviceRepository);
    }

    protected QueryOptions decodeQueryString(HttpServletRequest request) {
        if (request.getQueryString() != null) {
            String decoded = URLDecoder.decode(request.getQueryString());
            return QUERY_OPTIONS_FACTORY.createQueryOptions(decoded);
        } else {
            return QUERY_OPTIONS_FACTORY.createDummy();
        }
    }

    /**
     * Matches all requests on Entities referenced directly via id
     * e.g. /Datastreams(52)
     *
     * @param entity  name of entity. Automatically set by Spring via @PathVariable
     * @param id      id of entity. Automatically set by Spring via @PathVariable
     * @param request full request
     */
    public StaDTO readEntityDirect(String entity,
                                   String id,
                                   HttpServletRequest request) throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        validateResource(lookupPath, serviceRepository);

        String entityId = unescapeIdIfWanted(id.substring(1, id.length() - 1));
        QueryOptions options = decodeQueryString(request);
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
    public StaDTO readEntityRefDirect(String entity,
                                      String id,
                                      HttpServletRequest request) throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        validateResource(lookupPath.substring(0, lookupPath.length() - 5), serviceRepository);

        String entityId = unescapeIdIfWanted(id.substring(1, id.length() - 1));
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
    public StaDTO readRelatedEntity(String entity,
                                    String target,
                                    HttpServletRequest request)
        throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        validateResource(lookupPath, serviceRepository);

        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1];

        QueryOptions options = decodeQueryString(request);
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
    public StaDTO readRelatedEntityRef(String entity,
                                       String target,
                                       HttpServletRequest request)
        throws Exception {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        validateResource(lookupPath.substring(0, lookupPath.length() - 5), serviceRepository);

        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1];

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
