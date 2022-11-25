/*
 * Copyright (C) 2018-2022 52°North Initiative for Geospatial Open Source
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
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.utils.AbstractSTARequestHandler;
import org.n52.sta.utils.RequestUtils;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class CollectionRequestHandler<T extends RequestUtils> extends AbstractSTARequestHandler {

    public CollectionRequestHandler(String rootUrl,
                                    boolean shouldEscapeId,
                                    EntityServiceRepository serviceRepository) {
        super(rootUrl, shouldEscapeId, serviceRepository);
    }

    /**
     * Matches all requests on Collections referenced directly
     * e.g. /Datastreams
     *
     * @param collectionName name of the collection. Automatically set by Spring via @PathVariable
     * @param request        Full request
     * @return CollectionWrapper Requested collection
     */
    public CollectionWrapper readCollectionDirect(@PathVariable String collectionName,
                                                  HttpServletRequest request)
        throws STACRUDException {
        QueryOptions options = decodeQueryString(request);
        return serviceRepository
            .getEntityService(collectionName)
            .getEntityCollection(options)
            .setRequestURL(rootUrl + collectionName);
    }

    /**
     * Matches all requests on Collections referenced directly and addressing an association link
     * e.g. /Datastreams/$ref
     *
     * @param collectionName name of the collection. Automatically set by Spring via @PathVariable
     * @param request        Full request
     * @return CollectionWrapper Requested collection
     */
    public CollectionWrapper readCollectionRefDirect(@PathVariable String collectionName,
                                                     HttpServletRequest request)
        throws STACRUDException {
        HashSet<FilterClause> filters = new HashSet<>();
        String queryString = request.getQueryString();
        if (queryString != null) {
            // Parse QueryString normally and extract relevant Filters
            QueryOptions options = decodeQueryString(request);
            filters.add(options.getSkipFilter());
            filters.add(options.getTopFilter());
            filters.add(options.getCountFilter());
            filters.add(options.getFilterFilter());
        }
        // Overwrite select filter with filter only returning id
        filters.add(new SelectFilter(ID));
        return serviceRepository
            .getEntityService(collectionName)
            .getEntityCollection(QueryOptionsFactory.createQueryOptions(filters))
            .setRequestURL(rootUrl + collectionName);
    }

    /**
     * Matches all requests on Entities not referenced directly via id but via related entity.
     * e.g. /Datastreams(52)/Thing
     *
     * @param entity  requested entity. Automatically set by Spring via @PathVariable
     * @param target  related entity. Automatically set by Spring via @PathVariable
     * @param request full request
     * @return CollectionWrapper Requested collection
     */
    public CollectionWrapper readCollectionRelated(@PathVariable String entity,
                                                   @PathVariable String target,
                                                   HttpServletRequest request)
        throws Exception {
        validateResource((String) request.getAttribute(UrlPathHelper.PATH_ATTRIBUTE), serviceRepository);

        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1];

        QueryOptions options = decodeQueryString(request);
        return serviceRepository.getEntityService(target)
            .getEntityCollectionByRelatedEntity(sourceId,
                                                sourceType,
                                                options)
            .setRequestURL(rootUrl + entity + "/" + target);
    }

    /**
     * Matches all requests on Entities not referenced directly via id but via referenced entity and addressing an
     * association link
     * e.g. /Datastreams(52)/Thing/$ref
     *
     * @param entity  composite of entity and referenced entity. Automatically set by Spring via @PathVariable
     * @param request full request
     * @return CollectionWrapper Requested collection
     */
    public CollectionWrapper readCollectionRelatedRef(@PathVariable String entity,
                                                      @PathVariable String target,
                                                      HttpServletRequest request)
        throws Exception {
        String lookupPath = (String) request.getAttribute(UrlPathHelper.PATH_ATTRIBUTE);
        validateResource(lookupPath.substring(0, lookupPath.length() - 5), serviceRepository);

        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1];

        HashSet<FilterClause> filters = new HashSet<>();
        String queryString = request.getQueryString();
        if (queryString != null) {
            // Parse QueryString normally and extract relevant Filters
            QueryOptions options = decodeQueryString(request);
            filters.add(options.getSkipFilter());
            filters.add(options.getTopFilter());
            filters.add(options.getCountFilter());
            filters.add(options.getFilterFilter());
        }
        // Overwrite select filter with filter only returning id
        filters.add(new SelectFilter(ID));
        return serviceRepository.getEntityService(target)
            .getEntityCollectionByRelatedEntity(sourceId,
                                                sourceType,
                                                QueryOptionsFactory.createQueryOptions(filters))
            .setRequestURL(rootUrl + entity + "/" + target);
    }
}
