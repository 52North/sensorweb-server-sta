package org.n52.sta.service.handler.crud;

import java.util.Optional;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.DatastreamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatastreamEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<DatastreamEntity> {

    @Autowired
    DatastreamMapper mapper;

    @Override
    public Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        DatastreamEntity datastream = processEntity(entity);
        return datastream != null ? mapper.createEntity(datastream) : null;
    }
    
    @Override
    protected DatastreamEntity processEntity(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            DatastreamEntity datastream = mapper.createDatastream(entity);
            return  getEntityService().create(datastream);
        }
        return null;
    }
    
    private AbstractSensorThingsEntityService<?, DatastreamEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(EntityTypes.Datastream);
    }
}
