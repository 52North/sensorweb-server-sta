package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.FeatureOfInterestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureOfInterestEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<FeatureEntity> {

    @Autowired
    private FeatureOfInterestMapper mapper;

    @Override
    protected Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            FeatureEntity feature = getEntityService().create(mapper.createEntity(entity));
            return feature != null ? mapper.createEntity(feature) : null;
        }
        return null;
    }

    private AbstractSensorThingsEntityService<?, FeatureEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, FeatureEntity>) getEntityService(EntityTypes.FeatureOfInterest);
    }
}
