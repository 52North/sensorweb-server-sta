package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.AbstractMapper;
import org.n52.sta.mapping.HistoricalLocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HistoricalLocationEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<HistoricalLocationEntity> {

    @Autowired
    private HistoricalLocationMapper mapper;
    
    @Override
    protected Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            HistoricalLocationEntity historicalLocation = getEntityService().create(mapper.createEntity(entity));
            return mapToEntity(historicalLocation);
        }
        return null;
    }
    
    @Override
    protected Entity handleUpdateEntityRequest(Entity entity, HttpMethod method) throws ODataApplicationException {
        if (entity != null) {
            HistoricalLocationEntity historicalLocation = getEntityService().update(mapper.createEntity(entity), method);
            return mapToEntity(historicalLocation);
        }
        return null;
    }

    @Override
    protected void handleDeleteEntityRequest(Long id) throws ODataApplicationException {
        getEntityService().delete(id);
    }
    
    @Override
    protected AbstractMapper<HistoricalLocationEntity> getMapper() {
        return mapper;
    }

    private AbstractSensorThingsEntityService<?, HistoricalLocationEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, HistoricalLocationEntity>) getEntityService(EntityTypes.HistoricalLocation);
    }

}
