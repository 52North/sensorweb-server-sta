package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.AbstractMapper;
import org.n52.sta.mapping.FeatureOfInterestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureOfInterestEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<AbstractFeatureEntity<?>> {

    @Autowired
    private FeatureOfInterestMapper mapper;

    @Override
    protected Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            AbstractFeatureEntity<?> feature = getEntityService().create(mapper.createEntity(entity));
            return mapToEntity(feature);
        }
        return null;
    }
    
    @Override
    protected Entity handleUpdateEntityRequest(Entity entity, HttpMethod method) throws ODataApplicationException {
        if (entity != null) {
            AbstractFeatureEntity<?> feature = getEntityService().update(mapper.createEntity(entity), method);
            return mapToEntity(feature);
        }
        return null;
    }

    @Override
    protected void handleDeleteEntityRequest(Long id) throws ODataApplicationException {
        getEntityService().delete(id);
    }
    
    @Override
    protected AbstractMapper<AbstractFeatureEntity<?>> getMapper() {
        return mapper;
    }

    private AbstractSensorThingsEntityService<?, AbstractFeatureEntity<?>> getEntityService() {
        return (AbstractSensorThingsEntityService<?, AbstractFeatureEntity<?>>) getEntityService(EntityTypes.FeatureOfInterest);
    }
}
