package org.n52.sta.service.handler.crud;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<LocationEntity> {
    
    @Autowired
    private LocationMapper mapper;
    
    @Override
    public Entity handleCreateEntityRequest(Entity entity)
            throws ODataApplicationException {
        LocationEntity location = processEntity(entity);
        return location != null ? mapper.createEntity(location) : null;
    }
    
    @Override
    protected LocationEntity processEntity(Entity entity)
            throws ODataApplicationException {
        if (entity != null) {
            LocationEntity location = mapper.createLocation(entity);
            checkLocationEncoding(location);
            Optional<LocationEntity> optionalLocation = getEntityService().create(location);
            return optionalLocation.isPresent() ? optionalLocation.get() : null;
        }
        return null;
    }
    
    @Override
    protected Set<LocationEntity> processEntityCollection(EntityCollection entityCollection)
            throws ODataApplicationException {
        Set<LocationEntity> locations = new LinkedHashSet<>();
        Iterator<Entity> iterator = entityCollection.iterator();
        while (iterator.hasNext()) {
            LocationEntity location = processEntity((Entity) iterator.next());
            if (location != null) {
                locations.add(location);
            }
        }
        return locations;
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
