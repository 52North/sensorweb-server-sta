/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.data.service;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.ObservedPropertyQuerySpecifications;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.PhenomenonRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.ObservedPropertyMapper;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.*;

import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ET_DATASTREAM_NAME;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class ObservedPropertyService extends AbstractSensorThingsEntityService<PhenomenonRepository, PhenomenonEntity> {

    private final static DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();

    private final static ObservedPropertyQuerySpecifications oQS = new ObservedPropertyQuerySpecifications();

    private final static String IDENTIFIER = "staIdentifier";

    @Autowired
    private DatastreamRepository datastreamRepository;

    private ObservedPropertyMapper mapper;

    public ObservedPropertyService(PhenomenonRepository repository, ObservedPropertyMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }

    @Override
    public EntityTypes getType() {
        return EntityTypes.ObservedProperty;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) throws ODataApplicationException {
        EntityCollection retEntitySet = new EntityCollection();
        Specification<PhenomenonEntity> filter = getFilterPredicate(PhenomenonEntity.class, queryOptions);
        getRepository().findAll(filter, createPageableRequest(queryOptions, IDENTIFIER)).forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(String id) {
        Optional<PhenomenonEntity> entity = getRepository().findByStaIdentifier(id);
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(String sourceId, EdmEntityType sourceEntityType, QueryOptions queryOptions) {
        return null;
    }

    @Override
    public boolean existsEntity(String id) {
        return getRepository().existsByStaIdentifier(id);
    }

    @Override
    public boolean existsRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.existsRelatedEntity(sourceId, sourceEntityType, null);
    }

    private Specification<PhenomenonEntity> relatedEntitySpecification(String datastreamId, String obsPropId) {
        return (root, query, builder) -> {
            Subquery<PhenomenonEntity> sq = query.subquery(PhenomenonEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, PhenomenonEntity> join = datastream.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY);
            sq.select(join).where(builder.equal(datastream.get(DescribableEntity.PROPERTY_IDENTIFIER), datastreamId));
            if (obsPropId != null) {
                return builder.and(builder.in(root).value(sq), builder.equal(root.get("staIdentifier"), obsPropId));
            }
            return builder.in(root).value(sq);
        };
    }

    @Override
    public boolean existsRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Datastream": {
//                BooleanExpression filter = oQS.withDatastream(sourceId);
//                if (targetId != null) {
//                    filter = filter.and(oQS.withId(targetId));
//                }
                return getRepository().findOne(relatedEntitySpecification(sourceId, targetId)).isPresent();
            }
            default:
                return false;
        }
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<PhenomenonEntity> sensor = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return sensor.map(phenomenonEntity -> Optional.of(phenomenonEntity.getStaIdentifier())).orElseGet(Optional::empty);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<PhenomenonEntity> entity = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return entity.map(phenomenonEntity -> mapper.createEntity(phenomenonEntity)).orElse(null);
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            case "definition":
                return DataEntity.PROPERTY_IDENTIFIER;
            case "identifier":
                return "staIdentifier";
            default:
                return super.checkPropertyName(property);
        }
    }

    /**
     * Retrieves ObservedPropertyEntity (aka PhenomenonEntity) with Relation to
     * sourceEntity from Database. Returns empty if Entity is not found or
     * Entities are not related.
     *
     * @param sourceId         Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId         Id of the Entity to be retrieved
     * @return Optional<PhenomenonEntity> Requested Entity
     */
    private Optional<PhenomenonEntity> getRelatedEntityRaw(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Datastream": {
                break;
            }
            default:
                return Optional.empty();
        }
        return getRepository().findOne(relatedEntitySpecification(sourceId, targetId));
    }

    @Override
    public long getCount(QueryOptions queryOptions) throws ODataApplicationException {
        return getRepository().count(getFilterPredicate(PhenomenonEntity.class, queryOptions));
    }

    @Override
    public PhenomenonEntity create(PhenomenonEntity observableProperty) throws ODataApplicationException {
        if (observableProperty.getStaIdentifier() != null && !observableProperty.isSetName()) {
            return getRepository().findByStaIdentifier((observableProperty.getStaIdentifier())).get();
        }
        if (observableProperty.getStaIdentifier() == null) {
            if (getRepository().existsByName(observableProperty.getName())) {
                Optional<PhenomenonEntity> optional
                        = getRepository().findOne(oQS.withName(observableProperty.getName()));
                return optional.isPresent() ? optional.get() : null;
            } else {
                // Autogenerate Identifier
                observableProperty.setStaIdentifier(UUID.randomUUID().toString());
            }
        } else if (getRepository().existsByStaIdentifier(observableProperty.getStaIdentifier())) {
            throw new ODataApplicationException("Identifier already exists!",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
        return getRepository().save(getAsPhenomenonEntity(observableProperty));
    }

    @Override
    public PhenomenonEntity update(PhenomenonEntity entity, HttpMethod method) throws ODataApplicationException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            Optional<PhenomenonEntity> existing = getRepository().findByStaIdentifier(entity.getStaIdentifier());
            if (existing.isPresent()) {
                PhenomenonEntity merged = mapper.merge(existing.get(), entity);
                return getRepository().save(getAsPhenomenonEntity(merged));
            }
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        throw new ODataApplicationException("Invalid http method for updating entity!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }

    private void checkUpdate(PhenomenonEntity entity) throws ODataApplicationException {
        if (entity instanceof ObservablePropertyEntity) {
            ObservablePropertyEntity observableProperty = (ObservablePropertyEntity) entity;
            if (observableProperty.hasDatastreams()) {
                for (DatastreamEntity datastream : observableProperty.getDatastreams()) {
                    checkInlineDatastream(datastream);
                }
            }
        }
    }

    @Override
    protected PhenomenonEntity update(PhenomenonEntity entity) {
        return getRepository().save(getAsPhenomenonEntity(entity));
    }

    @Override
    public void delete(String id) throws ODataApplicationException {
        if (getRepository().existsByStaIdentifier(id)) {
            // delete datastreams
            datastreamRepository.findAll(dQS.withObservedPropertyIdentifier(id)).forEach(d -> {
                try {
                    getDatastreamService().delete(d.getIdentifier());
                } catch (ODataApplicationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            getRepository().deleteByStaIdentifier(id);
        } else {
            throw new ODataApplicationException("Entity not found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        }
    }

    @Override
    protected void delete(PhenomenonEntity entity) {
        getRepository().deleteByStaIdentifier(entity.getStaIdentifier());
    }

    @Override
    protected PhenomenonEntity createOrUpdate(PhenomenonEntity entity) throws ODataApplicationException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return update(entity, HttpMethod.PATCH);
        }
        return create(entity);
    }

    private PhenomenonEntity getAsPhenomenonEntity(PhenomenonEntity observableProperty) {
        return observableProperty instanceof ObservablePropertyEntity
                ? ((ObservablePropertyEntity) observableProperty).asPhenomenonEntity()
                : observableProperty;
    }

    private AbstractSensorThingsEntityService<?, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(
                EntityTypes.Datastream);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    @Override
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        Set<String> datastreamIds = new HashSet<>();
        PhenomenonEntity entity = (PhenomenonEntity) rawObject;

        Iterable<DatastreamEntity> observations = datastreamRepository
                .findAll(dQS.withObservedPropertyIdentifier(entity.getStaIdentifier()));
        observations.forEach((o) -> datastreamIds.add(o.getIdentifier()));
        collections.put(ET_DATASTREAM_NAME, datastreamIds);
        return collections;
    }
}
