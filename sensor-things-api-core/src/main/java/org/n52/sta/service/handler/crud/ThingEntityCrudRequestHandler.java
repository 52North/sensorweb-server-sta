package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.AbstractMapper;
import org.n52.sta.mapping.ThingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThingEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<PlatformEntity> {

    @Autowired
    private ThingMapper mapper;
    
    @Override
    protected Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            PlatformEntity thing = getEntityService().create(mapper.createEntity(getMapper().checkEntity(entity)));
            return mapToEntity(thing);
        }
        return null;
    }
    
    @Override
    protected Entity handleUpdateEntityRequest(Entity entity, HttpMethod method)
            throws ODataApplicationException {
        if (entity != null) {
            PlatformEntity thing = getEntityService().update(mapper.createEntity(entity), method);
            return mapToEntity(thing);
        }
        return null;
    }


    @Override
    protected void handleDeleteEntityRequest(Long id) throws ODataApplicationException {
        getEntityService().delete(id);
    }
    
    @Override
    protected AbstractMapper<PlatformEntity> getMapper() {
        return mapper;
    }

    private AbstractSensorThingsEntityService<?, PlatformEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, PlatformEntity>) getEntityService(EntityTypes.Thing);
    }

}
