/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;

/**
 * Contains parameters that are required to determine the repsponse content for
 * a specific request.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class EntityQueryParams {

    private EdmEntityType sourceEntityType;

    private Long sourceId;

    private EdmEntitySet targetEntitySet;

    public EdmEntityType getSourceEntityType() {
        return sourceEntityType;
    }

    public void setSourceEntityType(EdmEntityType sourceEntityType) {
        this.sourceEntityType = sourceEntityType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceKeyPredicates(Long sourceId) {
        this.sourceId = sourceId;
    }

    public EdmEntitySet getTargetEntitySet() {
        return targetEntitySet;
    }

    public void setTargetEntitySet(EdmEntitySet targetEntitySet) {
        this.targetEntitySet = targetEntitySet;
    }

}
