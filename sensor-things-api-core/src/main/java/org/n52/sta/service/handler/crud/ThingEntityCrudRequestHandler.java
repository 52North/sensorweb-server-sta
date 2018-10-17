package org.n52.sta.service.handler.crud;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.joda.time.DateTime;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.ThingMapper;
import org.n52.sta.service.response.EntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThingEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler<ThingEntity> {

    @Autowired
    private ThingMapper mapper;
    
    @Autowired
    private LocationEntityCrudRequestHandler locationHandler;
    
    @Override
    public Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException {
        ThingEntity thing = processEntity(entity);
        return thing != null ? mapper.createEntity(thing) : null;
    }
    
    @Override
    protected ThingEntity processEntity(Entity entity) throws ODataApplicationException {
        if (entity != null) {
            ThingEntity thing = mapper.createThing(entity);
            Set<LocationEntity> locations = processLocations(entity.getNavigationLink("Locations"));
            thing.setLocationEntities(locations);
            Optional<ThingEntity> optionalThing = getEntityService().create(thing);
            if (optionalThing.isPresent()) {
                processHistoricalLocations(optionalThing.get(), locations);
                return optionalThing.get();
            }
        }
        return null;
    }
    
    private Set<LocationEntity> processLocations(Link link) throws ODataApplicationException {
        LinkedHashSet<LocationEntity> locations = new LinkedHashSet<>();
        if (link != null) {
            if (link.getInlineEntity() != null) {
                locations.add(locationHandler.processEntity(link.getInlineEntity()));
            } else if (link.getInlineEntitySet() != null) {
                locations.addAll(locationHandler.processEntityCollection(link.getInlineEntitySet()));
            }
        }
        return locations;
    }

    private void processHistoricalLocations(ThingEntity thing, Set<LocationEntity> locations) {
        if (thing != null && locations != null && !locations.isEmpty()) {
            Set<HistoricalLocationEntity> historicalLocations = new LinkedHashSet<>();
            HistoricalLocationEntity historicalLocation = new HistoricalLocationEntity();
            historicalLocation.setThingEntity(thing);
            historicalLocation.setLocationEntities(locations);
            historicalLocation.setTime(DateTime.now().toDate());
            Optional<HistoricalLocationEntity> createdHistoricalLocation = getHistoricalLocationService().create(historicalLocation);
            if (createdHistoricalLocation.isPresent()) {
                historicalLocations.add(createdHistoricalLocation.get());
            }
            for (LocationEntity location : locations) {
                location.setHistoricalLocationEntities(historicalLocations);
                getLocationService().update(location);
            }
            thing.setHistoricalLocationEntities(historicalLocations);
        }
    }

    @Override
    public EntityResponse handleUpdateEntityRequest(DeserializerResult deserializerResult, HttpMethod method)
            throws ODataApplicationException {
        EntityResponse response = null;
        Entity entity = deserializerResult.getEntity();
        if (entity != null) {
            getEntityService().update(mapper.createThing(entity));
        }
        // TODO Auto-generated method stub
        return response;
    }


    @Override
    public EntityResponse handleDeleteEntityRequest(DeserializerResult deserializerResult)
            throws ODataApplicationException {
        EntityResponse response = null;
        Entity entity = deserializerResult.getEntity();
        if (entity != null) {
            getEntityService().delete(mapper.createThing(entity));
        }
        // TODO Auto-generated method stub
        return response;
    }
    
    private AbstractSensorThingsEntityService<?, ThingEntity> getEntityService() {
        return (AbstractSensorThingsEntityService<?, ThingEntity>) getEntityService(EntityTypes.Thing);
    }

    private AbstractSensorThingsEntityService<?, LocationEntity> getLocationService() {
        return (AbstractSensorThingsEntityService<?, LocationEntity>) getEntityService(EntityTypes.Location);
    }
    
    private AbstractSensorThingsEntityService<?, HistoricalLocationEntity> getHistoricalLocationService() {
        return (AbstractSensorThingsEntityService<?, HistoricalLocationEntity>) getEntityService(EntityTypes.HistoricalLocation);
    }
}
