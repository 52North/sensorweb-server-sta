package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.SensorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SensorEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<ProcedureEntity> {
    
    @Autowired
    SensorMapper mapper;

    @Override
    public Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        ProcedureEntity sensor = processEntity(entity);
        return sensor != null ? mapper.createEntity(sensor) : null;
    }
    
    @Override
    protected ProcedureEntity processEntity(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            ProcedureEntity sensor = mapper.createSensor(entity);
            return getEntityService().create(sensor);
        }
        return null;
    }
    
    private AbstractSensorThingsEntityService<?, ProcedureEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, ProcedureEntity>) getEntityService(EntityTypes.Sensor);
    }
}
