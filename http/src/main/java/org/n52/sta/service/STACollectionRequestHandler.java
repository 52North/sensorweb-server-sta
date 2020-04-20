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
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.utils.STARequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;

/**
 * Handles all requests to Entity Collections and Entity Collections association Links
 * e.g. /Things
 * e.g. /Datastreams(52)/Observations
 * e.g. /Things/$ref
 * e.g. /Datastreams(52)/Observations/$ref
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@RestController
public class STACollectionRequestHandler implements STARequestUtils {

    private final EntityServiceRepository serviceRepository;
    private final String rootUrl;

    public STACollectionRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                       EntityServiceRepository serviceRepository) {
        this.rootUrl = rootUrl;
        this.serviceRepository = serviceRepository;
    }

    /**
     * Matches all requests on Collections referenced directly
     * e.g. /Datastreams
     *
     * @param collectionName name of the collection. Automatically set by Spring via @PathVariable
     * @param request        Full request
     * @return CollectionWrapper Requested collection
     */
    @GetMapping(
            value = "/{collectionName:" + BASE_COLLECTION_REGEX + "}",
            produces = "application/json"
    )
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
    @GetMapping(
            value = "/{collectionName:" + BASE_COLLECTION_REGEX + "}" + SLASHREF,
            produces = "application/json"
    )
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
                .getEntityCollection(QUERY_OPTIONS_FACTORY.createQueryOptions(filters))
                .setRequestURL(rootUrl + collectionName);
    }

    /**
     * Matches all requests on Entities not referenced directly via id but via referenced entity.
     * e.g. /Datastreams(52)/Thing
     *
     * @param entity  composite of entity and referenced entity. Automatically set by Spring via @PathVariable
     * @param request full request
     * @return CollectionWrapper Requested collection
     */
    @GetMapping(
            value = {
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_THING_PATH_VARIABLE,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_LOCATION_PATH_VARIABLE,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH_VARIABLE,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH_VARIABLE,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_SENSOR_PATH_VARIABLE,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_HIST_LOCATION_PATH_VARIABLE
            },
            produces = "application/json"
    )
    public CollectionWrapper readCollectionRelated(@PathVariable String entity,
                                                   @PathVariable String target,
                                                   HttpServletRequest request)
            throws Exception {

        validateResource(request.getRequestURI().substring(request.getContextPath().length()), serviceRepository);

        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1].replace(")", "");

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
    @GetMapping(
            value = {
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_THING_PATH_VARIABLE + SLASHREF,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_LOCATION_PATH_VARIABLE + SLASHREF,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH_VARIABLE + SLASHREF,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH_VARIABLE + SLASHREF,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_SENSOR_PATH_VARIABLE + SLASHREF,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE + SLASHREF,
                    MAPPING_PREFIX + COLLECTION_IDENTIFIED_BY_HIST_LOCATION_PATH_VARIABLE + SLASHREF
            },
            produces = "application/json"
    )
    public CollectionWrapper readCollectionRelatedRef(@PathVariable String entity,
                                                      @PathVariable String target,
                                                      HttpServletRequest request)
            throws Exception {

        String requestURI = request.getRequestURI();
        validateResource(requestURI.substring(request.getContextPath().length(), requestURI.length() - 5),
                         serviceRepository);

        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1].replace(")", "");

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
                                                                    QUERY_OPTIONS_FACTORY.createQueryOptions(filters))
                                .setRequestURL(rootUrl + entity + "/" + target);
    }
}
