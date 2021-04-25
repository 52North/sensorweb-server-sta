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

package org.n52.sta.service.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@RestController
@ConditionalOnProperty(value = "server.feature.httpReadOnly", havingValue = "false", matchIfMissing = true)
@Profile(StaConstants.CITSCIEXTENSION)
public class CitSciMultipartObservationRequestHandler {

    private final EntityServiceRepository serviceRepository;
    private final ObjectMapper mapper;
    private final String filePath;

    public CitSciMultipartObservationRequestHandler(@Value("${server.feature.uploadDir:/tmp/}") String filePath,
                                                    EntityServiceRepository serviceRepository,
                                                    ObjectMapper mapper) {
        this.serviceRepository = serviceRepository;
        this.mapper = mapper;
        this.filePath = filePath;
    }

    @PostMapping(
        consumes = {"multipart/form-data", "multipart/mixed"},
        value = "/Observations",
        produces = "application/json")
    public ElementWithQueryOptions handleFileUpload(@RequestPart("file") MultipartFile file,
                                                    @RequestPart("body") String body)
        throws IOException, STACRUDException {

        //TODO: error checking, better storage, better filename, refactor to not store if observation persistence fails
        File stored = new File(filePath + System.currentTimeMillis() + "--" + file.getOriginalFilename());
        file.transferTo(stored);

        return ((AbstractSensorThingsEntityService<DataEntity<?>>) serviceRepository
            .getEntityService("Observation"))
            .create(mapper.readValue(body, DataEntity.class));

    }

}
