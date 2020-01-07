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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.n52.series.db.beans.IdEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.EntityPatch;
import org.n52.sta.serdes.model.ElementWithQueryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/v2")
public class STACrudRequestHandler<T extends IdEntity> extends STARequestUtils {

    private static final String COULD_NOT_FIND_RELATED_ENTITY = "Could not find related Entity!";
    private final int rootUrlLength;
    private final EntityServiceRepository serviceRepository;
    private final ObjectMapper mapper;

    public STACrudRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                 EntityServiceRepository serviceRepository,
                                 ObjectMapper mapper) {
        this.rootUrlLength = rootUrl.length();
        this.serviceRepository = serviceRepository;
        this.mapper = mapper;
    }

    @PostMapping(
            consumes = "application/json",
            value = "/{collectionName:" + COLLECTION_REGEX + "$}",
            produces = "application/json")
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions handlePost(@PathVariable String collectionName,
                                              @RequestBody String body) throws IOException, STACRUDException {

        Class<T> clazz = collectionNameToClass.get(collectionName);
        return ((AbstractSensorThingsEntityService<?, T>) serviceRepository.getEntityService(collectionName))
                .create(mapper.readValue(body, clazz));
    }

    /**
     * Matches all PATCH requests on Entities referenced directly via id
     * e.g. ../Datastreams(52)
     *
     * @param collectionName name of entity. Automatically set by Spring via @PathVariable
     * @param id             id of entity. Automatically set by Spring via @PathVariable
     * @param request        full request
     */
    @PatchMapping(
            value = "**/{collectionName:" + COLLECTION_REGEX + "}{id:" + IDENTIFIER_REGEX + "$}",
            produces = "application/json"
    )
    @SuppressWarnings("unchecked")
    public Object handleDirectPatch(@PathVariable String collectionName,
                                    @PathVariable String id,
                                    @RequestBody String body,
                                    HttpServletRequest request)
            throws STACRUDException, IOException, STAInvalidUrlException {
        validateURL(request.getRequestURL(), serviceRepository, rootUrlLength);
        Class<EntityPatch> clazz = collectionNameToPatchClass.get(collectionName);
        ObjectNode jsonBody = (ObjectNode) mapper.readTree(body);
        String strippedId = id.substring(1, id.length() - 1);
        jsonBody.put(StaConstants.AT_IOT_ID, strippedId);
        return ((AbstractSensorThingsEntityService<?, T>) serviceRepository.getEntityService(collectionName))
                .update(strippedId,
                        (T) ((mapper.readValue(jsonBody.toString(), clazz))).getEntity(),
                        HttpMethod.PATCH);
    }

    /**
     * Matches all PATCH requests on Entities referenced via association with different Entity.
     * e.g. /Datastreams(1)/Sensor
     *
     * @param entity  identifier of related. Automatically set by Spring via @PathVariable
     * @param target  name of entity. Automatically set by Spring via @PathVariable
     * @param request full request
     */
    @PatchMapping(
            value = {MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_DATASTREAM_PATH,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_OBSERVATION_PATH,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH
            },
            produces = "application/json"
    )
    @SuppressWarnings("unchecked")
    public Object handleRelatedPatch(@PathVariable String entity,
                                     @PathVariable String target,
                                     @RequestBody String body,
                                     HttpServletRequest request)
            throws STACRUDException, IOException, STAInvalidUrlException {
        validateURL(request.getRequestURL(), serviceRepository, rootUrlLength);

        String sourceType = entity.substring(0, entity.indexOf("("));
        String sourceId = entity.substring(sourceType.length() + 1, entity.length() - 1);
        AbstractSensorThingsEntityService<?, T> entityService =
                (AbstractSensorThingsEntityService<?, T>) serviceRepository.getEntityService(target);

        // Get Id from datastore
        String entityId = entityService.getEntityIdByRelatedEntity(sourceId, sourceType);
        Assert.notNull(entityId, COULD_NOT_FIND_RELATED_ENTITY);

        // Create Patch Entity
        Class<EntityPatch> clazz = collectionNameToPatchClass.get(target);
        Assert.notNull(clazz, "Could not find Patch Class!");

        ObjectNode jsonBody = (ObjectNode) mapper.readTree(body);
        jsonBody.put(StaConstants.AT_IOT_ID, entityId);

        // Do update
        return entityService.update(entityId,
                (T) ((mapper.readValue(jsonBody.toString(), clazz))).getEntity(),
                HttpMethod.PATCH);
    }


    /**
     * Matches all DELETE requests on Entities referenced directly via id
     * e.g. ../Datastreams(52)
     *
     * @param collectionName name of entity. Automatically set by Spring via @PathVariable
     * @param id             id of entity. Automatically set by Spring via @PathVariable
     * @param request        full request
     */
    @DeleteMapping(
            value = "**/{collectionName:" + COLLECTION_REGEX + "}{id:" + IDENTIFIER_REGEX + "$}",
            produces = "application/json"
    )
    public Object handleDelete(@PathVariable String collectionName,
                               @PathVariable String id,
                               HttpServletRequest request)
            throws STACRUDException, STAInvalidUrlException {
        validateURL(request.getRequestURL(), serviceRepository, rootUrlLength);
        serviceRepository.getEntityService(collectionName).delete(id.substring(1, id.length() - 1));
        return null;
    }

    /**
     * Matches all DELETE requests on Entities referenced via association with different Entity.
     * e.g. /Datastreams(1)/Sensor
     *
     * @param entity  identifier of related. Automatically set by Spring via @PathVariable
     * @param target  name of entity. Automatically set by Spring via @PathVariable
     * @param request full request
     */
    @DeleteMapping(
            value = {MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_DATASTREAM_PATH,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_OBSERVATION_PATH,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH
            },
            produces = "application/json"
    )
    @SuppressWarnings("unchecked")
    public Object handleRelatedDelete(@PathVariable String entity,
                                      @PathVariable String target,
                                      @RequestBody String body,
                                      HttpServletRequest request)
            throws STACRUDException, STAInvalidUrlException {
        validateURL(request.getRequestURL(), serviceRepository, rootUrlLength);

        String sourceType = entity.substring(0, entity.indexOf("("));
        String sourceId = entity.substring(sourceType.length() + 1, entity.length() - 1);
        AbstractSensorThingsEntityService<?, T> entityService =
                (AbstractSensorThingsEntityService<?, T>) serviceRepository.getEntityService(target);

        // Get Id from datastore
        String entityId = entityService.getEntityIdByRelatedEntity(sourceId, sourceType);
        Assert.notNull(entityId, COULD_NOT_FIND_RELATED_ENTITY);

        // Do update
        entityService.delete(entityId);
        return null;
    }
}
