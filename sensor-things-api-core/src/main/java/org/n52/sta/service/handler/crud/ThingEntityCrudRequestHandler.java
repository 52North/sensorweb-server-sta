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
    public Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        ThingEntity thing = processEntity(entity);
        return thing != null ? mapper.createEntity(thing) : null;
    }
    
    @Override
    protected ThingEntity processEntity(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            ThingEntity thing = mapper.createThing(entity);
            return getEntityService().create(thing);
        }
        return null;
    }
    

    @Override
    public EntityResponse handleUpdateEntityRequest(DeserializerResult deserializerResult, HttpMethod method)
            throws ODataApplicationException {
        EntityResponse response = null;
        Entity entity = deserializerResult.getEntity();
        if (entity != null) {
            getEntityService().update(mapper.createThing(entity));
        }
        // TODO Auto-generated method stub
        return response;
    }


    @Override
    public EntityResponse handleDeleteEntityRequest(DeserializerResult deserializerResult)
            throws ODataApplicationException {
        EntityResponse response = null;
        Entity entity = deserializerResult.getEntity();
        if (entity != null) {
            getEntityService().delete(mapper.createThing(entity));
        }
        // TODO Auto-generated method stub
        return response;
    }
    
    private AbstractSensorThingsEntityService<?, ThingEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, ThingEntity>) getEntityService(EntityTypes.Thing);
    }

}
