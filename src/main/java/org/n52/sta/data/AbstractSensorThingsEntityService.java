/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data;

import java.util.List;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.uri.UriParameter;
import org.n52.series.db.beans.sta.AbstractStaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public interface AbstractSensorThingsEntityService {
    
    public EntityCollection getEntityCollection();

    public EntityCollection getRelatedEntityCollection(Entity sourceEntity);

    public Entity getEntity(List<UriParameter> keyPredicates);

    public Entity getRelatedEntity(Entity sourceEntity);

    public Entity getRelatedEntity(Entity sourceEntity, List<UriParameter> keyPredicates);


}
