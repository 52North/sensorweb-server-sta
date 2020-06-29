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
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.shetland.ogc.sta.exception.STANotFoundException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface RequestUtils extends StaConstants {

    QueryOptionsFactory QUERY_OPTIONS_FACTORY = new QueryOptionsFactory();

    String INTERNAL_CLIENT_ID = "POC";

    // Used to store information about referenced entity during related POST
    String REFERENCED_FROM_TYPE = "referencedFromType";

    // Used to store information about referenced entity during related POST
    String REFERENCED_FROM_ID = "referencedFromID";

    // Used for identifying/referencing source Entity Type
    // e.g. "Datastreams" in /Datastreams(52)/Thing
    String GROUPNAME_SOURCE_NAME = "sourceName";

    // Used for identifying/referencing source Entity Id
    // e.g. "52" in /Datastreams(52)/Thing
    String GROUPNAME_SOURCE_IDENTIFIER = "sourceId";

    // Used for identifying/referencing requested Entity Type
    // e.g. "Thing" in /Datastreams(52)/Thing
    String GROUPNAME_WANTED_NAME = "wantedName";

    // Used for identifying/referencing requested Entity Type
    // e.g. "Thing" in /Datastreams(52)/Thing
    String GROUPNAME_WANTED_IDENTIFIER = "wantedId";

    // Used for identifying/referencing $select option
    // e.g. "name" in /Datastreams(52)/Thing$select=name
    String GROUPNAME_SELECT = "select";

    // Used for identifying/referencing source Entity Type
    // e.g. "name" in /Datastreams(52)/Thing/name
    String GROUPNAME_PROPERTY = "property";

    String MAPPING_PREFIX = "**/";
    String ID = "id";

    // Note: This is duplicated in LocationService to allow for non-standard 'updateFOI'-feature.
    String IDENTIFIER_REGEX = "(?:\\()[^)]+(?:\\))";

    String URL_INVALID = "Url is invalid. ";

    String PATH_ENTITY = "{entity:";
    String PATH_TARGET = "/{target:";
    String PATH_ID = "{id:";
    String PATH_PROPERTY = "{property:\\w+}";
    String SLASH = "/";
    String BACKSLASH = "\\";
    String OR = "|";
    String DOLLAR = "$";
    String QUESTIONMARK = "?";
    String PLUS = "+";
    String LESS_THAN = "<";
    String GREATER_THAN = ">";
    String ROUND_BRACKET_CLOSE = ")";
    String ROUND_BRACKET_OPEN = "(";
    String SQUARE_BRACKET_CLOSE = "]";
    String SQUARE_BRACKET_OPEN = "[";
    String CURLY_BRACKET_OPEN = "{";
    String CURLY_BRACKET_CLOSE = "}";
    String SLASHREF = SLASH + "$ref";
    String SLASHVALUE = SLASH + "$value";

    // Used to mark start and end of named capturing groups
    String SOURCE_NAME_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_SOURCE_NAME + GREATER_THAN;
    String SOURCE_NAME_GROUP_END = ROUND_BRACKET_CLOSE;
    String SOURCE_ID_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_SOURCE_IDENTIFIER + GREATER_THAN;
    String SOURCE_ID_GROUP_END = ROUND_BRACKET_CLOSE;
    String WANTED_NAME_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_WANTED_NAME + GREATER_THAN;
    String WANTED_NAME_GROUP_END = ROUND_BRACKET_CLOSE;
    String WANTED_ID_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_WANTED_IDENTIFIER + GREATER_THAN;
    String WANTED_ID_GROUP_END = ROUND_BRACKET_CLOSE;
    String SELECT_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_SELECT + GREATER_THAN;
    String SELECT_GROUP_END = ROUND_BRACKET_CLOSE;
    String PROPERTY_GROUP_START =
            ROUND_BRACKET_OPEN + QUESTIONMARK + LESS_THAN + GROUPNAME_PROPERTY + GREATER_THAN;
    String PROPERTY_GROUP_END = ROUND_BRACKET_CLOSE;

    String SELECT_REGEX_NAMED_GROUPS =
            BACKSLASH + QUESTIONMARK + BACKSLASH + DOLLAR + "select=" +
                    SELECT_GROUP_START + SQUARE_BRACKET_OPEN + BACKSLASH + "w" + BACKSLASH + "," +
                    SQUARE_BRACKET_CLOSE + PLUS + SELECT_GROUP_END;

    String PROPERTY_REGEX_NAMED_GROUPS =
            SLASH + PROPERTY_GROUP_START + SQUARE_BRACKET_OPEN + "A-z," + SQUARE_BRACKET_CLOSE + PLUS +
                    PROPERTY_GROUP_END;


    default QueryOptions decodeQueryString(HttpServletRequest request) {
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
    default void validateResource(StringBuffer requestURI,
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
    default void validateResource(String requestURI,
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

    Exception validateURISyntax(String[] uriResources);

    Class collectionNameToClass(String collectionName) throws STAInvalidUrlException;

    Class collectionNameToPatchClass(String collectionName);

    /**
     * This function validates a given URI semantically by checking if all Entities referenced in the navigation
     * exists. As URI is syntactically valid indices can be hard-coded.
     *
     * @param uriResources      URI of the Request split by SLASH
     * @param serviceRepository Repository for EntityServices
     * @return STAInvalidUrlException if URI is malformed
     */
    default Exception validateURISemantic(String[] uriResources,
                                          EntityServiceRepository serviceRepository) {
        // Check if this is Request to root collection. They are always valid
        if (uriResources.length == 1 && !uriResources[0].contains(ROUND_BRACKET_OPEN)) {
            return null;
        }
        // Parse first navigation Element
        String[] sourceEntity = splitId(uriResources[0]);
        String sourceId = sourceEntity[1].replace(ROUND_BRACKET_CLOSE, "");
        sourceId = sourceId.replaceAll("%2F", "/");
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
                targetId = targetEntity[1].replace(ROUND_BRACKET_CLOSE, "");
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

    default STANotFoundException createNotFoundExceptionNoEntity(String entity) {
        return new STANotFoundException("No Entity: " + entity + " found!");
    }

    default STAInvalidUrlException createInvalidUrlExceptionNoEntityAssociated(String first, String last) {
        return new STAInvalidUrlException(first + " associated with " + last);
    }

    default String[] splitId(String entity) {
        return entity.split("\\(");
    }

    /**
     * Validates whether an entity has given property.
     *
     * @param entity   Name of the Entity to be checked
     * @param property Property of the Entity
     * @throws STAInvalidUrlException when the URL is invalid
     */
    default void validateProperty(String entity, String property) throws STAInvalidUrlException {
        STAEntityDefinition definition = STAEntityDefinition.definitions.get(entity);

        if (!definition.getEntityPropsMandatory().contains(property) &&
                !definition.getEntityPropsOptional().contains(property)) {
            throw new STAInvalidUrlException("Entity: " + entity + " does not have property: " + property);
        }
    }
}
