/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data.service;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;

import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.uri.UriParameter;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.QThingEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.sta.data.repositories.ThingRepository;
import org.n52.sta.mapping.ThingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ThingService implements AbstractSensorThingsEntityService {
    
    @Autowired
    private ThingMapper mapper;
    
    @Autowired
    private ThingRepository repository;
    
    @Autowired
    private LocationService locationService;

    @Autowired
    private HistoricalLocationService historicalLocationService;

//    @Autowired
//    private DatastreamService datastreamService;
    
    private static QThingEntity qthing = QThingEntity.thingEntity;
    
    @Override
    public EntityCollection getEntityCollection() {
        EntityCollection retEntitySet = new EntityCollection();
        repository.findAll().forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(List<UriParameter> keyPredicates) {
        return getEntityForId(keyPredicates.get(0).getText());
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity) {
        //TODO: Implement

//    case "iot.Datastream": {
//        
//        // source Entity (datastream) should always exists (checked beforehand in Request Handler)
//        Optional<DatastreamEntity> datastream = datastreamService.getRawEntityForId(sourceId);
//        things = Arrays.asList(datastream.get().getThing());
//        break;
//    }
        return null;        
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity, List<UriParameter> keyPredicates) {
        //TODO: Implement
        return null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Entity sourceEntity) {
        Iterable<ThingEntity> things;
        Long sourceId = (Long)sourceEntity.getProperty(ID_ANNOTATION).getValue();
        
        switch (sourceEntity.getType()) {
            case "iot.Location": {
                
                // source Entity (loc) should always exists (checked beforehand in Request Handler)
                Optional<LocationEntity> loc = locationService.getRawEntityForId(sourceId);
                things = repository.findAll(qthing.locationEntities.contains(loc.get()));
                break;
            }
            case "iot.HistoricalLocation": {
                
                // source Entity (loc) should always exists (checked beforehand in Request Handler)
                Optional<HistoricalLocationEntity> loc = historicalLocationService.getRawEntityForId(sourceId);
                things = repository.findAll(qthing.historicalLocationEntities.contains(loc.get()));
                break;
            }
            default: return null;
        }
        EntityCollection retEntitySet = new EntityCollection();
        things.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    private Entity getEntityForId(String id) {
        Optional<ThingEntity> entity = getRawEntityForId(Long.valueOf(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }
    
    protected Optional<ThingEntity> getRawEntityForId(Long id) {
        return repository.findById(id);
    }
}
