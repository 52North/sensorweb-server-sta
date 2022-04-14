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
package org.n52.sta.http;

import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.api.CoreRequestUtils;
import org.n52.sta.api.EntityServiceFactory;
import org.n52.sta.http.common.CollectionRequestHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
public class CoreCollectionRequestHandler extends CollectionRequestHandler implements CoreRequestUtils {

    public CoreCollectionRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                        @Value("${server.feature.escapeId:true}") boolean shouldEscapeId,
                                        EntityServiceFactory serviceRepository) {
        super(rootUrl, shouldEscapeId, serviceRepository);
    }

    @GetMapping(
        value = "/{collectionName:" + BASE_COLLECTION_REGEX + "}",
        produces = "application/json"
    )
    public CollectionWrapper readCollectionDirect(@PathVariable String collectionName,
                                                  HttpServletRequest request) throws STACRUDException {
        return super.readCollectionDirect(collectionName, request);
    }

    @GetMapping(
        value = "/{collectionName:" + BASE_COLLECTION_REGEX + "}" + SLASHREF,
        produces = "application/json"
    )
    public CollectionWrapper readCollectionRefDirect(@PathVariable String collectionName,
                                                     HttpServletRequest request) throws STACRUDException {
        return super.readCollectionRefDirect(collectionName, request);
    }

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
                                                   HttpServletRequest request) throws Exception {
        return super.readCollectionRelated(entity, target, request);
    }

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
                                                      HttpServletRequest request) throws Exception {
        return super.readCollectionRelatedRef(entity, target, request);
    }

}
