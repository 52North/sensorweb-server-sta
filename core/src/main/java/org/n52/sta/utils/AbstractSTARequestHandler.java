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

package org.n52.sta.utils;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.shetland.ogc.sta.exception.STANotFoundException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.service.EntityServiceRepository;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class AbstractSTARequestHandler implements STARequestUtils {

    protected final boolean shouldEscapeId;
    protected final EntityServiceRepository serviceRepository;
    protected final String rootUrl;

    public AbstractSTARequestHandler(String rootUrl,
                                     boolean shouldEscapeId,
                                     EntityServiceRepository serviceRepository
    ) {
        this.shouldEscapeId = shouldEscapeId;
        this.serviceRepository = serviceRepository;
        this.rootUrl = rootUrl;
    }

    protected QueryOptions decodeQueryString(HttpServletRequest request) {
        if (request.getQueryString() != null) {
            String decoded = UriUtils.decode(request.getQueryString(), Charset.defaultCharset());
            return QUERY_OPTIONS_FACTORY.createQueryOptions(decoded);
        } else {
            return QUERY_OPTIONS_FACTORY.createDummy();
        }
    }

    /**
     * Validates a given Resource Path. Checks Syntax + Semantics
     *
     * @param requestURI        URL to the Resource.
     * @param serviceRepository Backend Repository Factory
     * @throws Exception if URL is not valid
     */
    protected void validateResource(StringBuffer requestURI,
                                    EntityServiceRepository serviceRepository)
            throws Exception {
        validateResource(requestURI.toString(), serviceRepository);
    }

    /**
     * Validates a given Resource Path. Checks Syntax + Semantics
     *
     * @param requestURI        URL to the Resource.
     * @param serviceRepository Backend Repository Factory
     * @throws Exception if URL is not valid
     */
    protected void validateResource(String requestURI,
                                    EntityServiceRepository serviceRepository)
            throws Exception {
        String[] uriResources;
        if (requestURI.startsWith("/")) {
            uriResources = requestURI.substring(1).split(SLASH);
        } else {
            uriResources = requestURI.split(SLASH);
        }

        Exception ex;
        ex = validateURISyntax(uriResources);
        if (ex != null) {
            throw ex;
        }
        ex = validateURISemantic(uriResources, serviceRepository);
        if (ex != null) {
            throw ex;
        }
    }

    /**
     * Validates whether an entity has given property.
     *
     * @param entity   Name of the Entity to be checked
     * @param property Property of the Entity
     * @throws STAInvalidUrlException when the URL is invalid
     */
    protected void validateProperty(String entity, String property) throws STAInvalidUrlException {
        STAEntityDefinition definition = STAEntityDefinition.definitions.get(entity);

        if (!definition.getEntityPropsMandatory().contains(property) &&
                !definition.getEntityPropsOptional().contains(property)) {
            throw new STAInvalidUrlException("Entity: " + entity + " does not have property: " + property);
        }
    }

    /**
     * Validates a given URI syntactically.
     *
     * @param uriResources URI of the Request split by SLASH
     * @return STAInvalidUrlException if URI is malformed
     */
    protected STAInvalidUrlException validateURISyntax(String[] uriResources) {
        // Validate URL syntax via Regex
        // Skip validation if no navigationPath is provided as Spring already validated syntax
        if (uriResources.length > 1) {
            // check iteratively and fail-fast
            for (int i = 0; i < uriResources.length; i++) {
                if (!BY_ID_PATTERN.matcher(uriResources[i]).matches()) {
                    // Resource is addressed by relation to other entity
                    // e.g. Datastreams(1)/Thing
                    if (i > 0) {
                        // Look back at last resource and check if association is valid
                        String resource = uriResources[i - 1] + SLASH + uriResources[i];
                        if (!(BY_DATASTREAM_PATTERN.matcher(resource).matches()
                                || BY_HIST_LOC_PATTERN.matcher(resource).matches()
                                || BY_LOCATION_PATTERN.matcher(resource).matches()
                                || BY_THING_PATTERN.matcher(resource).matches()
                                || BY_FOI_PATTERN.matcher(resource).matches()
                                || BY_OBSERVATION_PATTERN.matcher(resource).matches()
                                || BY_OBSER_PROP_PATTERN.matcher(resource).matches()
                                || BY_SENSOR_PATTERN.matcher(resource).matches()
                                || BY_OBSER_PROP_PATTERN.matcher(resource).matches())) {
                            return new STAInvalidUrlException(URL_INVALID
                                                                      + uriResources[i - 1]
                                                                      + SLASH + uriResources[i]
                                                                      + " is not a valid resource path.");

                        }
                    } else {
                        return new STAInvalidUrlException(URL_INVALID
                                                                  + uriResources[i]
                                                                  + " is not a valid resource.");
                    }
                }
                // Resource is adressed by Id
                // e.g. Things(1), no processing required
            }
        }
        return null;
    }

    /**
     * This function validates a given URI semantically by checking if all Entities referenced in the navigation
     * exists. As URI is syntactically valid indices can be hard-coded.
     *
     * @param uriResources      URI of the Request split by SLASH
     * @param serviceRepository Repository for EntityServices
     * @return STAInvalidUrlException if URI is malformed
     */
    protected Exception validateURISemantic(String[] uriResources,
                                            EntityServiceRepository serviceRepository) {
        // Check if this is Request to root collection. They are always valid
        if (uriResources.length == 1 && !uriResources[0].contains(ROUND_BRACKET_OPEN)) {
            return null;
        }
        // Parse first navigation Element
        String[] sourceEntity = splitId(uriResources[0]);
        String sourceId = sourceEntity[1].replace(ROUND_BRACKET_CLOSE, "");
        sourceId = unescapeIdIfWanted(sourceId.replaceAll("%2F", "/"));
        String sourceType = sourceEntity[0];

        if (!serviceRepository.getEntityService(sourceType).existsEntity(sourceId)) {
            return createNotFoundExceptionNoEntity(uriResources[0]);
        }

        // Iterate over the rest of the uri validating each resource
        for (int i = 1, uriResourcesLength = uriResources.length; i < uriResourcesLength - 1; i++) {
            String[] targetEntity = splitId(uriResources[i]);
            String targetType = targetEntity[0];
            String targetId = null;
            if (targetEntity.length == 1) {
                // Resource is addressed by related Entity
                // e.g. /Datastreams(1)/Thing/
                // Getting id directly as it is needed for next iteration
                targetId = serviceRepository.getEntityService(targetType)
                                            .getEntityIdByRelatedEntity(sourceId, sourceType);
                if (targetId == null) {
                    return createInvalidUrlExceptionNoEntityAssociated(uriResources[i], uriResources[i - 1]);
                }
            } else {
                // Resource is addressed by Id directly
                // e.g. /Things(1)/
                // Only checking exists as Id is already known
                targetId = targetEntity[1];
                if (!serviceRepository.getEntityService(targetType)
                                      .existsEntityByRelatedEntity(sourceId, sourceType, targetId)) {
                    return createInvalidUrlExceptionNoEntityAssociated(uriResources[i], uriResources[i - 1]);
                }
            }

            // Store target as source for next iteration
            sourceId = targetId;
            sourceType = targetType;
        }
        // As no error is thrown the uri is valid
        return null;
    }

    protected STANotFoundException createNotFoundExceptionNoEntity(String entity) {
        return new STANotFoundException("No Entity: " + entity + " found!");
    }

    protected STAInvalidUrlException createInvalidUrlExceptionNoEntityAssociated(String first, String last) {
        return new STAInvalidUrlException(first + " associated with " + last);
    }

    protected String[] splitId(String entity) {
        String[] split = entity.split("\\(");
        split[1] = unescapeIdIfWanted(split[1].replace(")", ""));
        return split;
    }

    protected String unescapeIdIfWanted(String id) {
        if (shouldEscapeId && id.startsWith("'") && id.endsWith("'")) {
            return id.substring(1, id.length() - 1);
        } else {
            return id;
        }
    }
}
