/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data;

import java.util.List;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.n52.sta.mapping.ThingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.uri.UriParameter;
import org.n52.sta.edm.provider.entities.ThingEntityProvider;
import org.n52.sta.utils.DummyEntityCreator;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ThingService implements AbstractSensorThingsEntityService {

    @Autowired
    private ThingMapper thingMapper;

    @Autowired
    private DummyEntityCreator entityCreator;

    @Override
    public EntityCollection getEntityCollection() {
        return entityCreator.createEntityCollection(ThingEntityProvider.ET_THING_NAME);
    }

    @Override
    public Entity getEntity(List<UriParameter> keyPredicates) {
        return getEntityForId(keyPredicates.get(0).getText());
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity) {
        return getEntityForId(String.valueOf(ThreadLocalRandom.current().nextInt()));
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity, List<UriParameter> keyPredicates) {
        return getEntityForId(keyPredicates.get(0).getText());
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Entity sourceEntity) {
        return getEntityCollection();
    }

    private Entity getEntityForId(String id) {
        return entityCreator.createEntity(ThingEntityProvider.ET_THING_NAME, id);
    }
}
