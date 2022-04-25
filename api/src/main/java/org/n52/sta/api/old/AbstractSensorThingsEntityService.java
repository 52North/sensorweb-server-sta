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
package org.n52.sta.api.old;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.api.old.dto.common.StaDTO;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface AbstractSensorThingsEntityService<R extends StaDTO> {

    /**
     * Checks if an Entity with given id exists
     *
     * @param id the id of the Entity
     * @return true if an Entity with given id exists
     */
    boolean existsEntity(String id) throws STACRUDException;

    /**
     * Gets the Entity with given id
     *
     * @param id           the id of the Entity
     * @param queryOptions query Options
     * @return ElementWithQueryOptions wrapping requested Entity
     * @throws STACRUDException if an error occurred
     */
    R getEntity(String id, QueryOptions queryOptions) throws STACRUDException;

    /**
     * Requests the full EntityCollection
     *
     * @param queryOptions {@link QueryOptions}
     * @return the full EntityCollection
     * @throws STACRUDException if the queryOptions are invalid
     */
    CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException;

    /**
     * Requests the Entity with given ownId that is related to a single Entity with
     * given relatedId and
     * relatedType
     *
     * @param relatedId    ID of the related Entity
     * @param relatedType  EntityType of the related Entity
     * @param ownId        ID of the requested Entity. Can be null.
     * @param queryOptions {@link QueryOptions} used for serialization
     * @return Entity that matches
     * @throws STACRUDException if an error occurred
     */
    R getEntityByRelatedEntity(String relatedId, String relatedType, String ownId,
            QueryOptions queryOptions) throws STACRUDException;

    /**
     * Requests the EntityCollection that is related to a single Entity with the
     * given ID and type
     *
     * @param relatedId    the ID of the Entity the EntityCollection is related to
     * @param relatedType  EntityType of the related Entity
     * @param queryOptions {@link QueryOptions}
     * @return List of Entities that match
     * @throws STACRUDException if the queryOptions are invalid
     */
    CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId, String relatedType,
            QueryOptions queryOptions) throws STACRUDException;

    /**
     * Gets the Id on an Entity that is related to a single Entity with given
     * relatedId and relatedType. May
     * be overwritten by classes that use a different field for storing the
     * identifier.
     *
     * @param relatedId   ID of the related Entity
     * @param relatedType EntityType of the related Entity
     * @return Id of the Entity. Null if no entity is present
     */
    String getEntityIdByRelatedEntity(String relatedId, String relatedType) throws STACRUDException;

    /**
     * Checks if an entity with given ownId exists that relates to an entity with
     * given relatedId and
     * relatedType
     *
     * @param relatedId   ID of the related Entity
     * @param relatedType EntityType of the related Entity
     * @param ownId       ID of the requested Entity. Can be null.
     * @return true if an Entity exists
     */
    boolean existsEntityByRelatedEntity(String relatedId, String relatedType, String ownId) throws STACRUDException;

    StaDTO create(R entity) throws STACRUDException;

    StaDTO update(String id, R entity, String method) throws STACRUDException;

    void delete(String id) throws STACRUDException;
}
