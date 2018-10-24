package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.ThingMapper;
import org.n52.sta.service.response.EntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThingEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<ThingEntity> {

    @Autowired
    private ThingMapper mapper;
    
    @Override
    protected Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            ThingEntity thing = getEntityService().create(mapper.createEntity(entity));
            return thing != null ? mapper.createEntity(thing) : null;
        }
        return null;
    }
    
    @Override
    protected Entity handleUpdateEntityRequest(Entity entity, HttpMethod method)
            throws ODataApplicationException {
        if (entity != null) {
            ThingEntity thing = getEntityService().update(mapper.createEntity(entity), method);
            return thing != null ? mapper.createEntity(thing) : null;
        }
        return null;
    }


    @Override
    protected Entity handleDeleteEntityRequest(Entity entity)
            throws ODataApplicationException {
        if (entity != null) {
            ThingEntity thing = getEntityService().delete(mapper.createEntity(entity));
            return thing != null ? mapper.createEntity(thing) : null;
        }
        return null;
    }
    
    private AbstractSensorThingsEntityService<?, ThingEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, ThingEntity>) getEntityService(EntityTypes.Thing);
    }

}
