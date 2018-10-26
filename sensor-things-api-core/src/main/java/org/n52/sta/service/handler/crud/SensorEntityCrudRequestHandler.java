package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.AbstractMapper;
import org.n52.sta.mapping.SensorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SensorEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<ProcedureEntity> {

    @Autowired
    protected SensorMapper mapper;

    @Override
    protected Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            ProcedureEntity sensor = getEntityService().create(mapper.createEntity(entity));
            return mapToEntity(sensor);
        }
        return null;
    }
    
    @Override
    protected Entity handleUpdateEntityRequest(Entity entity, HttpMethod method) throws ODataApplicationException {
        if (entity != null) {
            ProcedureEntity datastream = getEntityService().update(mapper.createEntity(entity), method);
            return mapToEntity(datastream);
        }
        return null;
    }

    @Override
    protected void handleDeleteEntityRequest(Long id) throws ODataApplicationException {
            getEntityService().delete(id);
    }
    
    @Override
    protected AbstractMapper<ProcedureEntity> getMapper() {
        return mapper;
    }

    private AbstractSensorThingsEntityService<?, ProcedureEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, ProcedureEntity>) getEntityService(EntityTypes.Sensor);
    }
}
