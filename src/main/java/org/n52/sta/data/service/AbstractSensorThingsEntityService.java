/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data.service;

import java.util.OptionalLong;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interface for requesting Sensor Things entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractSensorThingsEntityService<T extends JpaRepository<?, ?>> {

    private T repository;
    
    public AbstractSensorThingsEntityService(T repository) {
        this.repository = repository;
    }

    /**
     * Requests the full EntityCollection
     * @param queryOptions 
     *
     * @return the full EntityCollection
     */
    public abstract EntityCollection getEntityCollection(QueryOptions queryOptions);

    /**
     * Requests the EntityCollection that is related to a single Entity with the
     * given ID and type
     *
     * @param sourceId the ID of the Entity the EntityCollection is related to
     * @param sourceEntityType EntityType of the related Entity
     * @param queryOptions
     * @return the EntityCollection that is related to the given Entity
     */
    public abstract EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType, QueryOptions queryOptions);
    
    /**
     * Request the count for the EntityCollection that is related to a single Entity with the
     * given ID and type
     * 
     * @param sourceId the ID of the Entity the EntityCollection is related to
     * @param sourceEntityType EntityType of the related Entity
     * @return the count of related entities
     */
    public long getRelatedEntityCollectionCount(Long sourceId, EdmEntityType sourceEntityType) {
        return 0;
    }

    /**
     * Requests the Entity in accordance to a given ID
     *
     * @param id the ID to determine the Entity for
     * @return the Entity that is conform to the given key predicates
     */
    public abstract Entity getEntity(Long id);

    /**
     * Requests the ID for an Entity that is related to a single Entity with the
     * given ID
     *
     * @param sourceId the ID for the Entity the requested Entity is related to
     * @param sourceEntityType EntityType of the related Entity
     * @return the ID for the Entity that is related to the Entity with the
     * given Id
     */
    public abstract OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType);

    /**
     * Requests the ID for the Entity that is related to a single Entity with a
     * given ID and in accordance to a given ID
     *
     * @param sourceId the ID for the Entity the requested Entity is related to
     * @param sourceEntityType EntityType of the related Entity
     * @param targetId the ID for the requested Entity
     * @return the Entity that is related to the given Entity and is conform to
     * the given ID
     */
    public abstract OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId);

    /**
     * Checks if an Entity exists in accordance to a given list of key
     * predicates
     *
     * @param id the ID to check the existence of an Entity for
     *
     * @return true if an Entity that is conform to the given key predicates
     * exists
     */
    public abstract boolean existsEntity(Long id);

    /**
     * Checks if an Entity exists that is related to a single Entity of the
     * given EntityType
     *
     * @param sourceId ID of the related Entity
     * @param sourceEntityType EntityType of the related Entity
     * @return true if an Entity exists that is related to a single Entity of
     * the given EntityType and with the given KeyPredicates
     */
    public abstract boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType);

    /**
     * Checks if an Entity exists that is conform to given KeyPredicates and is
     * related to a single Entity of the given EntityType and with the given
     * KeyPredicates in accordance
     *
     * @param sourceId ID of the related Entity
     * @param sourceEntityType EntityType of the related Entity
     * @param targetId ID of the requested Entity
     * @return true if an Entity exists that is conform to the given
     * KeyPredicates and is related to a single Entity of the given EntityType
     * and with the given KeyPredicates
     */
    public abstract boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId);

    /**
     * Requests the Entity that is related to a single Entity with a given ID
     * and type
     *
     * @param sourceId ID of the related Entity
     * @param sourceEntityType EntityType of the related Entity
     * @return the Entity that is related to the Entity with given ID and type
     */
    public abstract Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType);

    /**
     * Requests the Entity that is related to a single Entity with a given ID
     * and type and that is conform to a given ID
     *
     * @param sourceId ID of the related Entity
     * @param sourceEntityType EntityType of the related Entity
     * @param targetId ID of the requested Entity
     * @return the Entity that is related to the given Entity and is conform to
     * the given ID
     */
    public abstract Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId);
    
    public T getRepository() {
        return this.repository;
    }
    
    /**
     * Query for the number of element.
     * 
     * @return the existing elements
     */
    public long getCount() {
       return getRepository().count();
    }
    

    protected OffsetLimitBasedPageRequest createPageableRequest(QueryOptions queryOptions) {
        int offset = queryOptions.hasSkipOption() ? queryOptions.getSkipOption().getValue() : 0;
        return new OffsetLimitBasedPageRequest(offset, queryOptions.getTopOption().getValue());
    }

}
