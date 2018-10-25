package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.ObservedPropertyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObservedPropertyEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<PhenomenonEntity> {

    @Autowired
    ObservedPropertyMapper mapper;

    @Override
    protected Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            PhenomenonEntity observableProperty = getEntityService().create(mapper.createEntity(entity));
            return observableProperty != null ? mapper.createEntity(observableProperty) : null;
        }
        return null;
    }

    private AbstractSensorThingsEntityService<?, PhenomenonEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, PhenomenonEntity>) getEntityService(EntityTypes.ObservedProperty);
    }
}