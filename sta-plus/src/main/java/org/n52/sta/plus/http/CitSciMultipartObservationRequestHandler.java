/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.plus.http;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STANotFoundException;
import org.n52.sta.api.AbstractSensorThingsEntityService;
import org.n52.sta.api.EntityServiceFactory;
import org.n52.sta.api.old.dto.common.StaDTO;
import org.n52.sta.api.old.entity.ObservationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@RestController
@ConditionalOnProperty(value = "server.feature.httpReadOnly", havingValue = "false", matchIfMissing = true)
@Profile(StaConstants.STAPLUS)
public class CitSciMultipartObservationRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CitSciMultipartObservationRequestHandler.class);
    private static final String SQ = "\"";
    private static final String FILES_ENDPOINT = "/files";

    private final EntityServiceFactory serviceRepository;
    private final ObjectMapper mapper;
    private final Path uploadDirectory;
    private final String rootUrl;

    public CitSciMultipartObservationRequestHandler(
                                     @Value("${server.rootUrl}") String rootUrl,
                                     @Value("${server.feature.blobUploadDir:/tmp/sta_blobs}") String uploadDirectory,
                                     EntityServiceFactory serviceRepository,
                                     ObjectMapper mapper) {
        this.serviceRepository = serviceRepository;
        this.rootUrl = rootUrl;
        this.mapper = mapper;

        this.uploadDirectory = Paths.get(uploadDirectory);
        if (!Files.exists(this.uploadDirectory)) {
            try {
                Files.createDirectories(Paths.get(uploadDirectory));
            } catch (IOException e) {
                LOGGER.error("Could not create missing upload directory: " + uploadDirectory);
            }
        }
    }

    @PostMapping(
        consumes = {
            "multipart/form-data",
            "multipart/mixed"
        },
        value = "/Observations",
        produces = "application/json")
    public StaDTO handleFileUpload(@RequestPart("file") MultipartFile file,
                                   @RequestPart("body") String body)
        throws IOException, STACRUDException {

        // TODO: error checking, better storage, better filename, refactor to not store if observation
        // persistence fails
        long now = System.currentTimeMillis();
        Path filename = uploadDirectory.resolve(now + "--" + file.getOriginalFilename());
        File stored = filename.toFile();
        file.transferTo(stored);
        LOGGER.info("Stored uploaded file as: {}", filename);

        ObservationDTO observation = mapper.readValue(body, ObservationDTO.class);
        observation.setResult(createFileLocation(stored));
        return getObservationRepository().create(observation);
    }

    private AbstractSensorThingsEntityService<ObservationDTO> getObservationRepository() {
        return (AbstractSensorThingsEntityService<ObservationDTO>) serviceRepository.getEntityService(
            "Observation");
    }

    @GetMapping(FILES_ENDPOINT + "/{filename:.+}")
    @ResponseBody
    public ResponseEntity<?> serveFile(@PathVariable String filename) throws STANotFoundException, IOException {
        Path pathToFile = uploadDirectory.resolve(filename);
        File fileToGet = pathToFile.toFile();
        if (!fileToGet.exists()) {
            LOGGER.debug("File {} does not exist.", fileToGet);
            throw new STANotFoundException("File not found: " + filename);
        }
        String mediaType = Files.probeContentType(fileToGet.toPath());
        MediaType parsedMediaType = mediaType == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(mediaType);
        LOGGER.debug("Serving file (MediaType: {}): {}", mediaType, fileToGet.getAbsoluteFile());
        return ResponseEntity.ok()
            .contentType(parsedMediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + SQ + filename + SQ)
            .body(new FileSystemResource(fileToGet));
    }

    @GetMapping(
        value = FILES_ENDPOINT,
        produces = "application/json")
    public String listUploadedFiles() throws IOException {
        File dir = uploadDirectory.toFile();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (File file : dir.listFiles()) {
            sb.append(SQ);
            sb.append(file.getName());
            sb.append("\",");
        }
        sb.replace(sb.length() - 1, sb.length(), "]");
        return sb.toString();
    }

    @PostMapping(FILES_ENDPOINT)
    public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile multipartFile)
        throws IOException, URISyntaxException {

        Path fileToUpload = uploadDirectory.resolve(multipartFile.getOriginalFilename());
        File file = fileToUpload.toFile();
        if (file.exists()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            multipartFile.transferTo(file);
            String location = createFileLocation(file);
            return ResponseEntity.created(new URI(location))
                .build();
        }
    }

    private String createFileLocation(File file) {
        // server.rootUrl has to be configured with a trailing slash
        String endpoint = FILES_ENDPOINT.substring(1);
        String filesEndpoint = ensureTrailingSlash(rootUrl.concat(endpoint));
        return filesEndpoint.concat(file.getName());
    }

    private String ensureTrailingSlash(String withOrWithoutTrailingSlash) {
        if (withOrWithoutTrailingSlash == null) {
            return "/";
        }
        return !withOrWithoutTrailingSlash.endsWith("/")
            ? withOrWithoutTrailingSlash.concat("/")
            : withOrWithoutTrailingSlash;
    }

}
