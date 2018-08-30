/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data;

import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.n52.io.response.OfferingOutput;
import org.n52.series.db.DatasetRepository;
import org.n52.series.db.OfferingRepository;
import org.n52.series.db.assembler.OfferingAssembler;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.old.dao.DefaultDbQueryFactory;
import org.n52.sta.edm.provider.entities.ThingEntityProvider;
import org.n52.sta.utils.DummyEntityCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ThingService implements AbstractSensorThingsEntityService {

    @Autowired
    private DummyEntityCreator entityCreator;

    @Override
    public EntityCollection getEntityCollection() {
        return entityCreator.createEntityCollection(ThingEntityProvider.ET_THING_NAME);
    }

    @Override
    public Entity getEntity(Long id) {
        return getEntityForId(String.valueOf(id));
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

    @Override
    public boolean existsEntity(Long id) {
        return true;
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return true;
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        return true;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId) {
        return getEntityCollection();
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId) {
        return entityCreator.createId(sourceId);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, Long targetId) {
        return entityCreator.createId(targetId);
    }
}
