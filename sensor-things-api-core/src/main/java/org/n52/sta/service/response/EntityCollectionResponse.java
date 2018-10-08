/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.response;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;

/**
 * Represents response data for a EntityCollection request
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class EntityCollectionResponse {

    private EntityCollection entityCollection;

    private EdmEntitySet entitySet;

    public EntityCollection getEntityCollection() {
        return entityCollection;
    }

    public void setEntityCollection(EntityCollection entityCollection) {
        this.entityCollection = entityCollection;
    }

    public EdmEntitySet getEntitySet() {
        return entitySet;
    }

    public void setEntitySet(EdmEntitySet entitySet) {
        this.entitySet = entitySet;
    }

}
