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
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.n52.sta.utils.STARequestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
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
public class STACrudRequestHandler<T extends IdEntity> implements STARequestUtils {

    private static final String COULD_NOT_FIND_RELATED_ENTITY = "Could not find related Entity!";
    private final EntityServiceRepository serviceRepository;
    private final ObjectMapper mapper;

    public STACrudRequestHandler(EntityServiceRepository serviceRepository,
                                 ObjectMapper mapper) {
        this.serviceRepository = serviceRepository;
        this.mapper = mapper;
    }

    /**
     * Matches all POST requests on Collections referenced directly
     * e.g. ../Datastreams
     *
     * @param collectionName name of entity. Automatically set by Spring via @PathVariable
     * @param body           request Body. Automatically set by Spring via @RequestBody
     */
    @PostMapping(
            consumes = "application/json",
            value = "/{collectionName:" + BASE_COLLECTION_REGEX + "$}",
            produces = "application/json")
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions<?> handlePostDirect(@PathVariable String collectionName,
                                                       @RequestBody String body)
            throws IOException, STACRUDException {

        Class<T> clazz = collectionNameToClass(collectionName);
        return ((AbstractSensorThingsEntityService<?, T, ? extends T>)
                serviceRepository.getEntityService(collectionName)).create(mapper.readValue(body, clazz));
    }

    /**
     * Matches all POST requests on Collections not referenced directly via id but via referenced entity.
     * e.g. ../Datastreams(52)/Observations
     *
     * @param entity  name and id of related entity. Automatically set by Spring via @PathVariable
     * @param target  type of entity POSTed. Automatically set by Spring via @PathVariable
     * @param body    request Body. Automatically set by Spring via @RequestBody
     * @param request full request
     */
    @PostMapping(
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
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions<?> handlePostRelated(@PathVariable String entity,
                                                        @PathVariable String target,
                                                        @RequestBody String body,
                                                        HttpServletRequest request)
            throws Exception {
        String url = request.getRequestURI().substring(request.getContextPath().length());
        validateResource(url, serviceRepository);

        // Add information about the related Entity to json payload to be used during deserialization
        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1].replace(")", "");
        ObjectNode jsonBody = (ObjectNode) mapper.readTree(body);
        jsonBody.put(REFERENCED_FROM_TYPE, sourceType);
        jsonBody.put(REFERENCED_FROM_ID, sourceId);

        Class<T> clazz = collectionNameToClass(target);
        return ((AbstractSensorThingsEntityService<?, T, ? extends T>)
                serviceRepository.getEntityService(target)).create(mapper.readValue(jsonBody.toString(), clazz));
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
            value = "**/{collectionName:" + BASE_COLLECTION_REGEX + "}{id:" + IDENTIFIER_REGEX + "$}",
            produces = "application/json"
    )
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions<?> handleDirectPatch(@PathVariable String collectionName,
                                                        @PathVariable String id,
                                                        @RequestBody String body,
                                                        HttpServletRequest request)
            throws Exception {
        String url = request.getRequestURI().substring(request.getContextPath().length());
        validateResource(url, serviceRepository);
        Class<EntityPatch> clazz = collectionNameToPatchClass(collectionName);
        ObjectNode jsonBody = (ObjectNode) mapper.readTree(body);
        String strippedId = id.substring(1, id.length() - 1);
        jsonBody.put(StaConstants.AT_IOT_ID, strippedId);
        return ((AbstractSensorThingsEntityService<?, T, ? extends T>)
                serviceRepository.getEntityService(collectionName)).update(strippedId,
                                                                           (T) ((mapper.readValue(jsonBody.toString(),
                                                                                                  clazz))).getEntity(),
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
            value = {
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE
            },
            produces = "application/json"
    )
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions<?> handleRelatedPatch(@PathVariable String entity,
                                                         @PathVariable String target,
                                                         @RequestBody String body,
                                                         HttpServletRequest request)
            throws Exception {
        String url = request.getRequestURI().substring(request.getContextPath().length());
        validateResource(url, serviceRepository);

        String sourceType = entity.substring(0, entity.indexOf("("));
        String sourceId = entity.substring(sourceType.length() + 1, entity.length() - 1);
        AbstractSensorThingsEntityService<?, T, ? extends T> entityService =
                (AbstractSensorThingsEntityService<?, T, ? extends T>) serviceRepository.getEntityService(target);

        // Get Id from datastore
        String entityId = entityService.getEntityIdByRelatedEntity(sourceId, sourceType);
        Assert.notNull(entityId, COULD_NOT_FIND_RELATED_ENTITY);

        // Create Patch Entity
        Class<EntityPatch> clazz = collectionNameToPatchClass(target);
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
            value = "**/{collectionName:" + BASE_COLLECTION_REGEX + "}{id:" + IDENTIFIER_REGEX + "$}",
            produces = "application/json"
    )
    public Object handleDelete(@PathVariable String collectionName,
                               @PathVariable String id,
                               HttpServletRequest request)
            throws Exception {
        String url = request.getRequestURI().substring(request.getContextPath().length());
        validateResource(url, serviceRepository);
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
            value = {
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_DATASTREAM_PATH_VARIABLE,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_OBSERVATION_PATH_VARIABLE,
                    MAPPING_PREFIX + ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH_VARIABLE
            },
            produces = "application/json"
    )
    @SuppressWarnings("unchecked")
    public Object handleRelatedDelete(@PathVariable String entity,
                                      @PathVariable String target,
                                      @RequestBody String body,
                                      HttpServletRequest request)
            throws Exception {

        String url = request.getRequestURI().substring(request.getContextPath().length());
        validateResource(url, serviceRepository);

        String sourceType = entity.substring(0, entity.indexOf("("));
        String sourceId = entity.substring(sourceType.length() + 1, entity.length() - 1);
        AbstractSensorThingsEntityService<?, T, ? extends T> entityService =
                (AbstractSensorThingsEntityService<?, T, ? extends T>) serviceRepository.getEntityService(target);

        // Get Id from datastore
        String entityId = entityService.getEntityIdByRelatedEntity(sourceId, sourceType);
        Assert.notNull(entityId, COULD_NOT_FIND_RELATED_ENTITY);

        // Do update
        entityService.delete(entityId);
        return null;
    }
}
