package org.n52.sta.data.service;

import java.util.Optional;
import java.util.OptionalLong;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.sta.data.repositories.LocationEncodingRepository;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.stereotype.Component;

@Component
public class LocationEncodingService extends AbstractSensorThingsEntityService<LocationEncodingRepository, LocationEncodingEntity> {

    public LocationEncodingService(LocationEncodingRepository repository) {
        super(repository);
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType,
            QueryOptions queryOptions) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity getEntity(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean existsEntity(Long id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<LocationEncodingEntity> create(LocationEncodingEntity locationEncoding) {
        ExampleMatcher createEncodingTypeMatcher = createEncodingTypeMatcher();
        if (!getRepository().exists(createExample(locationEncoding, createEncodingTypeMatcher))) {
            return Optional.of(getRepository().save(locationEncoding));
        }
        return getRepository().findOne(createExample(locationEncoding, createEncodingTypeMatcher));
    }

    @Override
    public Optional<LocationEncodingEntity> update(LocationEncodingEntity locationEncoding) {
        return getRepository().findOne(createExample(locationEncoding, createEncodingTypeMatcher()));
    }

    @Override
    public Optional<LocationEncodingEntity> delete(LocationEncodingEntity locationEncoding) {
        return getRepository().findOne(createExample(locationEncoding, createEncodingTypeMatcher()));
    }

    private Example<LocationEncodingEntity> createExample(LocationEncodingEntity locationEncoding, ExampleMatcher matcher) {
        return Example.<LocationEncodingEntity>of(locationEncoding, matcher);
    }

    private ExampleMatcher createEncodingTypeMatcher() {
        return ExampleMatcher.matching().withMatcher("encodingType", GenericPropertyMatchers.ignoreCase());
    }
}
