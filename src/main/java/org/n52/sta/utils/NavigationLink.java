/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;

/**
 * Represents a navigation link between an entity set and its soruce entity.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class NavigationLink {

    private Entity sourceEntity;

    private EdmEntitySet targetEntitySet;

    public Entity getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(Entity sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    public EdmEntitySet getTargetEntitySet() {
        return targetEntitySet;
    }

    public void setTargetEntitySet(EdmEntitySet targetEntitySet) {
        this.targetEntitySet = targetEntitySet;
    }

}
