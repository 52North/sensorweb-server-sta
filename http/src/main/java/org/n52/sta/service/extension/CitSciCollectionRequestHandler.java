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

import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.service.AbstractCollectionRequestHandler;
import org.n52.sta.utils.CitSciExtensionRequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles all requests to Entity Collections and Entity Collections association Links defined in the CitizenScience
 * STA Extension
 * e.g. /ObservationGroups
 * e.g. /Observations(52)/ObservationGroups
 * e.g. /ObservationGroups/$ref
 * e.g. /Observations(52)/ObservationGroups/$ref
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@RestController
@Profile("citSciExtension")
public class CitSciCollectionRequestHandler extends AbstractCollectionRequestHandler
        implements CitSciExtensionRequestUtils {

    public CitSciCollectionRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                          EntityServiceRepository serviceRepository) {
        super(serviceRepository, rootUrl);
    }

    @Override
    @GetMapping(
            value = "/{collectionName:" + CitSciExtensionRequestUtils.BASE_COLLECTION_REGEX + "}",
            produces = "application/json"
    )
    public CollectionWrapper readCollectionDirect(@PathVariable String collectionName,
                                                  HttpServletRequest request) throws STACRUDException {
        return super.readCollectionDirect(collectionName, request);
    }

    @Override
    @GetMapping(
            value = "/{collectionName:" + CitSciExtensionRequestUtils.BASE_COLLECTION_REGEX + "}" + SLASHREF,
            produces = "application/json"
    )
    public CollectionWrapper readCollectionRefDirect(@PathVariable String collectionName,
                                                     HttpServletRequest request)
            throws STACRUDException {
        return super.readCollectionRefDirect(collectionName, request);
    }

    @Override
    @GetMapping(
            value = {
                    MAPPING_PREFIX +
                            CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_CSOBSERVATION_PATH_VARIABLE,
                    MAPPING_PREFIX +
                            CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_OBSERVATION_GROUP_PATH_VARIABLE,
                    MAPPING_PREFIX +
                            CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_PROJECT_PATH_VARIABLE,
                    MAPPING_PREFIX +
                            CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_LICENSE_PATH_VARIABLE,
                    MAPPING_PREFIX +
                            CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_PARTY_PATH_VARIABLE,
                    MAPPING_PREFIX +
                            CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_CSDATASTREAM_PATH_VARIABLE,
                    MAPPING_PREFIX +
                            CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH_VARIABLE,
                    MAPPING_PREFIX +
                            CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_SENSOR_PATH_VARIABLE,
                    MAPPING_PREFIX +
                            CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_THING_PATH_VARIABLE,
            },
            produces = "application/json"
    )
    public CollectionWrapper readCollectionRelated(@PathVariable String entity,
                                                   @PathVariable String target,
                                                   HttpServletRequest request)
            throws Exception {
        return super.readCollectionRelated(entity, target, request);
    }

    @Override
    @GetMapping(
            value = {
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_CSOBSERVATION_PATH_VARIABLE
                            + SLASHREF,
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_OBSERVATION_GROUP_PATH_VARIABLE
                            + SLASHREF,
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_PROJECT_PATH_VARIABLE
                            + SLASHREF,
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_LICENSE_PATH_VARIABLE
                            + SLASHREF,
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_PARTY_PATH_VARIABLE
                            + SLASHREF,
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_CSDATASTREAM_PATH_VARIABLE
                            + SLASHREF,
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH_VARIABLE
                            + SLASHREF,
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_SENSOR_PATH_VARIABLE
                            + SLASHREF,
                    MAPPING_PREFIX
                            + CitSciExtensionRequestUtils.COLLECTION_IDENTIFIED_BY_THING_PATH_VARIABLE
                            + SLASHREF,
            },
            produces = "application/json"
    )
    public CollectionWrapper readCollectionRelatedRef(@PathVariable String entity,
                                                      @PathVariable String target,
                                                      HttpServletRequest request)
            throws Exception {
        return super.readCollectionRelatedRef(entity, target, request);
    }
}
