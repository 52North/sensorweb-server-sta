/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data;

import java.util.List;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.uri.UriParameter;

/**
 * Interface for requesting Sensor Things entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public interface AbstractSensorThingsEntityService {

    /**
     * Requests the full EntityCollection
     *
     * @return the full EntityCollection
     */
    public EntityCollection getEntityCollection();

    /**
     * Requests the EntityCollection that is related to a single Entity
     *
     * @param sourceEntity the Entity the EntityCollection is related to
     * @return the EntityCollection that is related to the given Entity
     */
    public EntityCollection getRelatedEntityCollection(Entity sourceEntity);

    /**
     * Requests the Entity in accordance to a given list of key predicates
     *
     * @param keyPredicates the key predicates to determine the Entity for
     * @return the Entity that is conform to the given key predicates
     */
    public Entity getEntity(List<UriParameter> keyPredicates);

    /**
     * Requests the Entity that is related to a single Entity
     *
     * @param sourceEntity the Entity the Entity is related to
     * @return the Entity that is related to the given Entity
     */
    public Entity getRelatedEntity(Entity sourceEntity);

    /**
     * Requests the Entity that is related to a single Entity and in accordance
     * to a given list of key predicates
     *
     * @param sourceEntity he Entity the Entity is related to
     * @param keyPredicates the key predicates to determine the Entity for
     * @return the Entity that is related to the given Entity and is conform to
     * the given key predicates
     */
    public Entity getRelatedEntity(Entity sourceEntity, List<UriParameter> keyPredicates);

    /**
     * Checks if an Entity exists in accordance to a given list of key
     * predicates
     *
     * @param id the ID to check the existance of an Entity for
     *
     * @return true if an Entity that is conform to the given key predicates
     * exists
     */
    public boolean existsEntity(Long id);

    /**
     * Checks if an Entity exists that is related to a single Entity of the
     * given EntityType and with the given KeyPredicates
     *
     * @param sourceId ID of the related Entity
     * @param sourceEntityType EntityType of the related Entity
     * @return true if an Entity exists that is related to a single Entity of
     * the given EntityType and with the given KeyPredicates
     */
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType);

    /**
     * Checks if an Entity exists that is conform to given KeyPredicates and is
     * related to a single Entity of the given EntityType and with the given
     * KeyPredicates in accordance
     *
     * @param sourceId ID of the related Entity
     * @param sourceEntityType EntityType of the related Entity
     * @param targetId the ID to check the existance of an Entity for
     * @return true if an Entity exists that is conform to the given
     * KeyPredicates and is related to a single Entity of the given EntityType
     * and with the given KeyPredicates
     */
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId);

}
