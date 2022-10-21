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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.n52.series.db.beans.IdEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.sta.serdes.util.EntityPatch;
import org.n52.sta.utils.AbstractSTARequestHandler;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Handles all CUD requests (POST, PUT, DELETE)
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressFBWarnings({"EI_EXPOSE_REP2"})
public abstract class CudRequestHandler<T extends IdEntity> extends AbstractSTARequestHandler {

    private static final String COULD_NOT_FIND_RELATED_ENTITY = "Could not find related Entity!";
    private final ObjectMapper mapper;

    public CudRequestHandler(String rootUrl,
                             boolean shouldEscapeId,
                             EntityServiceRepository serviceRepository,
                             ObjectMapper mapper) {
        super(rootUrl, shouldEscapeId, serviceRepository);
        this.mapper = mapper;
    }

    /**
     * Matches all POST requests on Collections referenced directly
     * e.g. ../Datastreams
     *
     * @param collectionName name of entity. Automatically set by Spring via @PathVariable
     * @param body           request Body. Automatically set by Spring via @RequestBody
     */
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions<?> handlePostDirect(String collectionName,
                                                       String body)
        throws IOException, STACRUDException, STAInvalidUrlException {
        Class<T> clazz = collectionNameToClass(collectionName);
        return ((AbstractSensorThingsEntityService<T>)
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
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions<?> handlePostRelated(String entity,
                                                        String target,
                                                        String body,
                                                        HttpServletRequest request)
        throws Exception {
        String lookupPath = (String) request.getAttribute(UrlPathHelper.PATH_ATTRIBUTE);
        validateResource(lookupPath, serviceRepository);

        // Add information about the related Entity to json payload to be used during deserialization
        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1];
        ObjectNode jsonBody = (ObjectNode) mapper.readTree(body);
        jsonBody.put(REFERENCED_FROM_TYPE, sourceType);
        jsonBody.put(REFERENCED_FROM_ID, sourceId);

        Class<T> clazz = collectionNameToClass(target);
        return ((AbstractSensorThingsEntityService<T>)
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
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions<?> handleDirectPatch(@PathVariable String collectionName,
                                                        @PathVariable String id,
                                                        @RequestBody String body,
                                                        HttpServletRequest request)
        throws Exception {
        String lookupPath = (String) request.getAttribute(UrlPathHelper.PATH_ATTRIBUTE);
        validateResource(lookupPath, serviceRepository);

        Class<EntityPatch> clazz = collectionNameToPatchClass(collectionName);
        ObjectNode jsonBody = (ObjectNode) mapper.readTree(body);
        String strippedId = unescapeIdIfWanted(id.substring(1, id.length() - 1));
        jsonBody.put(StaConstants.AT_IOT_ID, strippedId);
        return ((AbstractSensorThingsEntityService<T>)
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
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions<?> handleRelatedPatch(String entity,
                                                         String target,
                                                         String body,
                                                         HttpServletRequest request)
        throws Exception {
        String lookupPath = (String) request.getAttribute(UrlPathHelper.PATH_ATTRIBUTE);
        validateResource(lookupPath, serviceRepository);

        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1];
        AbstractSensorThingsEntityService<T> entityService =
            (AbstractSensorThingsEntityService<T>) serviceRepository.getEntityService(target);

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
    public Object handleDelete(String collectionName,
                               String id,
                               HttpServletRequest request)
        throws Exception {
        String lookupPath = (String) request.getAttribute(UrlPathHelper.PATH_ATTRIBUTE);
        validateResource(lookupPath, serviceRepository);
        serviceRepository.getEntityService(collectionName).delete(
            unescapeIdIfWanted(id.substring(1, id.length() - 1)));
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
    @SuppressWarnings("unchecked")
    public Object handleRelatedDelete(String entity,
                                      String target,
                                      String body,
                                      HttpServletRequest request)
        throws Exception {
        String lookupPath = (String) request.getAttribute(UrlPathHelper.PATH_ATTRIBUTE);
        validateResource(lookupPath, serviceRepository);

        String[] split = splitId(entity);
        String sourceType = split[0];
        String sourceId = split[1];
        AbstractSensorThingsEntityService<T> entityService =
            (AbstractSensorThingsEntityService<T>) serviceRepository.getEntityService(target);

        // Get Id from datastore
        String entityId = entityService.getEntityIdByRelatedEntity(sourceId, sourceType);
        Assert.notNull(entityId, COULD_NOT_FIND_RELATED_ENTITY);

        // Do update
        entityService.delete(entityId);
        return null;
    }
}
