package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.AbstractMapper;
import org.n52.sta.mapping.ObservedPropertyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObservedPropertyEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<PhenomenonEntity> {

    @Autowired
    protected ObservedPropertyMapper mapper;

    @Override
    protected Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            PhenomenonEntity observableProperty = getEntityService().create(mapper.createEntity(getMapper().checkEntity(entity)));
            return mapToEntity(observableProperty);
        }
        return null;
    }

    @Override
    protected Entity handleUpdateEntityRequest(Entity entity, HttpMethod method) throws ODataApplicationException {
        if (entity != null) {
            PhenomenonEntity datastream = getEntityService().update(mapper.createEntity(entity), method);
            return mapToEntity(datastream);
        }
        return null;
    }

    @Override
    protected void handleDeleteEntityRequest(Long id) throws ODataApplicationException {
            getEntityService().delete(id);
    }
    
    @Override
    protected AbstractMapper<PhenomenonEntity> getMapper() {
        return mapper;
    }

    private AbstractSensorThingsEntityService<?, ObservablePropertyEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, ObservablePropertyEntity>) getEntityService(EntityTypes.ObservedProperty);
    }
}
