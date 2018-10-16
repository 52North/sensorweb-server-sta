package org.n52.sta.service.handler.crud;

import java.util.Optional;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.LocationMapper;
import org.n52.sta.service.response.EntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler {
    
    @Autowired
    private LocationMapper mapper;
    
    @Override
    public EntityResponse handleCreateEntityRequest(DeserializerResult deserializerResult)
            throws ODataApplicationException {
        EntityResponse response = null;
        Entity entity = deserializerResult.getEntity();
        if (entity != null) {
            LocationEntity location = mapper.createLocation(entity);
            checkLocationEncoding(location);
            Optional<LocationEntity> optionalLocation = getEntityService().create(location);
            entity = optionalLocation.isPresent() ? mapper.createEntity(optionalLocation.get()) : null;
        }
        
        // TODO Auto-generated method stub
        return response;
    }
    
    private void checkLocationEncoding(LocationEntity location) {
        if (location.getLocationEncoding() != null) {
            Optional<LocationEncodingEntity> optionalLocationEncoding = getLocationEncodingEntityService().create(location.getLocationEncoding());
            location.setLocationEncoding(optionalLocationEncoding.isPresent() ? optionalLocationEncoding.get() : null);
        }
    }

    private AbstractSensorThingsEntityService<?, LocationEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, LocationEntity>) getEntityService(EntityTypes.Location);
    }
    
    private AbstractSensorThingsEntityService<?, LocationEncodingEntity> getLocationEncodingEntityService() {
        return (AbstractSensorThingsEntityService<?, LocationEncodingEntity>) getEntityService(EntityTypes.LocationEncoding);
    }
    
}
