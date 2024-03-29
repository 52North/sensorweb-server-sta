/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.http.vanilla;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.CoreRequestUtils;
import org.n52.sta.api.EntityServiceFactory;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.http.CudRequestHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Handles all CUD requests (POST, PUT, DELETE)
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@RestController
@ConditionalOnProperty(value = "server.feature.httpReadOnly", havingValue = "false", matchIfMissing = true)
public class CoreCudRequestHandler<T extends StaDTO> extends CudRequestHandler<T> implements CoreRequestUtils {

    public CoreCudRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                 @Value("${server.feature.escapeId:true}") boolean shouldEscapeId,
                                 EntityServiceFactory serviceRepository,
                                 ObjectMapper mapper) {
        super(rootUrl, shouldEscapeId, serviceRepository, mapper);
    }

    @PostMapping(
        consumes = "application/json",
        value = "/{collectionName:" + CoreRequestUtils.BASE_COLLECTION_REGEX + "$}",
        produces = "application/json")
    public StaDTO handlePostDirect(@PathVariable String collectionName,
                                   @RequestBody String body)
        throws IOException, STACRUDException, STAInvalidUrlException {
        return super.handlePostDirect(collectionName, body);
    }

    @PostMapping(
        value = {
            MAPPING_PREFIX + CoreRequestUtils.COLLECTION_IDENTIFIED_BY_THING_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.COLLECTION_IDENTIFIED_BY_LOCATION_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.COLLECTION_IDENTIFIED_BY_SENSOR_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.COLLECTION_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.COLLECTION_IDENTIFIED_BY_HIST_LOCATION_PATH_VARIABLE
        },
        produces = "application/json"
    )
    public StaDTO handlePostRelated(@PathVariable String entity,
                                    @PathVariable String target,
                                    @RequestBody String body,
                                    HttpServletRequest request)
        throws Exception {
        return super.handlePostRelated(entity, target, body, request);
    }

    @PatchMapping(
        value = "**/{collectionName:" + CoreRequestUtils.BASE_COLLECTION_REGEX + "}{id:" + IDENTIFIER_REGEX + "$}",
        produces = "application/json"
    )
    public StaDTO handleDirectPatch(@PathVariable String collectionName,
                                    @PathVariable String id,
                                    @RequestBody String body,
                                    HttpServletRequest request)
        throws Exception {
        return super.handleDirectPatch(collectionName, id, body, request);
    }

    @PatchMapping(
        value = {
            MAPPING_PREFIX + CoreRequestUtils.ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE
        },
        produces = "application/json"
    )
    public StaDTO handleRelatedPatch(@PathVariable String entity,
                                     @PathVariable String target,
                                     @RequestBody String body,
                                     HttpServletRequest request)
        throws Exception {
        return super.handleRelatedPatch(entity, target, body, request);
    }

    @DeleteMapping(
        value = "**/{collectionName:" + CoreRequestUtils.BASE_COLLECTION_REGEX + "}{id:" + IDENTIFIER_REGEX + "$}",
        produces = "application/json"
    )
    public Object handleDelete(@PathVariable String collectionName,
                               @PathVariable String id,
                               HttpServletRequest request)
        throws Exception {
        return super.handleDelete(collectionName, id, request);
    }

    @DeleteMapping(
        value = {
            MAPPING_PREFIX + CoreRequestUtils.ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE,
            MAPPING_PREFIX + CoreRequestUtils.ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE
        },
        produces = "application/json"
    )
    public Object handleRelatedDelete(@PathVariable String entity,
                                      @PathVariable String target,
                                      @RequestBody String body,
                                      HttpServletRequest request)
        throws Exception {
        return super.handleRelatedDelete(entity, target, body, request);
    }
}
