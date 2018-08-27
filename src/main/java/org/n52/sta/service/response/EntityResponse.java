/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.response;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;

/**
 * Represents response data for a Entity request
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class EntityResponse {

    private Entity entity;

    private EdmEntitySet entitySet;

    public Entity getEntityCollection() {
        return entity;
    }

    public void setEntityCollection(Entity entityCollection) {
        this.entity = entityCollection;
    }

    public EdmEntitySet getEntitySet() {
        return entitySet;
    }

    public void setEntitySet(EdmEntitySet entitySet) {
        this.entitySet = entitySet;
    }

}
