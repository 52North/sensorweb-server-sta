package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.DataEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.AbstractMapper;
import org.n52.sta.mapping.ObservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObservationEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<DataEntity<?>> {

    @Autowired
    private ObservationMapper mapper;

    @Override
    protected Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            DataEntity<?> observation = getEntityService().create(mapper.createEntity(getMapper().checkEntity(entity)));
            return mapToEntity(observation);
        }
        return null;
    }
    
    @Override
    protected Entity handleUpdateEntityRequest(Entity entity, HttpMethod method) throws ODataApplicationException {
        if (entity != null) {
            DataEntity<?> datastream = getEntityService().update(mapper.createEntity(entity), method);
            return mapToEntity(datastream);
        }
        return null;
    }
    
    @Override
    protected void handleDeleteEntityRequest(Long id) throws ODataApplicationException {
            getEntityService().delete(id);
    }

    @Override
    protected AbstractMapper<DataEntity<?>> getMapper() {
        return mapper;
    }
    
    private AbstractSensorThingsEntityService<?, DataEntity<?>> getEntityService() {
        return (AbstractSensorThingsEntityService<?, DataEntity<?>>) getEntityService(EntityTypes.Observation);
    }
}
