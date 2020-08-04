/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.AbstractDatastreamEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.ObservedPropertyEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.ObservedPropertyQuerySpecifications;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.PhenomenonRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class ObservedPropertyService
        extends AbstractSensorThingsEntityServiceImpl<PhenomenonRepository,
        PhenomenonEntity,
        ObservablePropertyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ObservedPropertyService.class);

    private static final DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    private static final ObservedPropertyQuerySpecifications oQS = new ObservedPropertyQuerySpecifications();

    private final DatastreamRepository datastreamRepository;

    @Autowired
    public ObservedPropertyService(PhenomenonRepository repository,
                                   DatastreamRepository datastreamRepository) {
        super(repository, PhenomenonEntity.class);
        this.datastreamRepository = datastreamRepository;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.ObservedProperty, EntityTypes.ObservedProperties};
    }

    /**
     * Checks if an Entity with given id exists
     *
     * @param id the id of the Entity
     * @return true if an Entity with given id exists
     */
    @Override
    public boolean existsEntity(String id) {
        return getRepository().existsByStaIdentifier(id);
    }

    @Override
    public ElementWithQueryOptions getEntity(String id, QueryOptions queryOptions) throws STACRUDException {
        try {
            PhenomenonEntity entity = getRepository().findByStaIdentifier(id).get();
            if (queryOptions.hasExpandFilter()) {
                return this.createWrapper(fetchExpandEntities(entity, queryOptions.getExpandFilter()), queryOptions);
            } else {
                return this.createWrapper(entity, queryOptions);
            }
        } catch (RuntimeException | STAInvalidQueryException e) {
            throw new STACRUDException(e.getMessage());
        }
    }

    @Override protected ObservablePropertyEntity fetchExpandEntities(PhenomenonEntity entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (ObservedPropertyEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                Page<DatastreamEntity> obsP = getDatastreamService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                               STAEntityDefinition.OBSERVED_PROPERTIES,
                                                               expandItem.getQueryOptions());
                ObservablePropertyEntity obsProp = new ObservablePropertyEntity(entity);
                obsProp.setDatastreams(obsP.get().collect(Collectors.toSet()));
                return obsProp;
            } else {
                throw new STAInvalidQueryException("Invalid expandOption supplied. Cannot find " + expandProperty +
                                                           " on Entity of type 'ObservableProperty'");
            }
        }
        return new ObservablePropertyEntity(entity);
    }

    @Override
    public boolean existsEntityByRelatedEntity(String relatedId, String relatedType, String ownId) {
        switch (relatedType) {
        case STAEntityDefinition.DATASTREAMS: {
            return getRepository().findOne(byRelatedEntityFilter(relatedId, relatedType, ownId)).isPresent();
        }
        default:
            return false;
        }
    }

    @Override
    public Specification<PhenomenonEntity> byRelatedEntityFilter(String relatedId,
                                                                 String relatedType,
                                                                 String ownId) {
        Specification<PhenomenonEntity> filter;
        switch (relatedType) {
        case STAEntityDefinition.CSDATASTREAMS:
        case STAEntityDefinition.DATASTREAMS: {
            filter = oQS.withDatastreamStaIdentifier(relatedId);
            break;
        }
        default:
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + " not found!");
        }
        if (ownId != null) {
            filter = filter.and(oQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public String checkPropertyName(String property) {
        return oQS.checkPropertyName(property);
    }

    @Override
    public PhenomenonEntity createEntity(PhenomenonEntity observableProperty) throws STACRUDException {
        if (observableProperty.getStaIdentifier() != null && !observableProperty.isSetName()) {
            Optional<PhenomenonEntity> optionalEntity =
                    getRepository().findByStaIdentifier(observableProperty.getStaIdentifier());
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException("No ObservedProperty with id '" + observableProperty.getStaIdentifier() +
                                                   "' found");
            }
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
        }
        synchronized (getLock(observableProperty.getStaIdentifier())) {
            // Check for duplicate definition
            if (getRepository().existsByIdentifier(observableProperty.getIdentifier())) {
                throw new STACRUDException("Observed Property with given Definition already exists!",
                                           HTTPStatus.CONFLICT);
            }
            if (getRepository().existsByStaIdentifier(observableProperty.getStaIdentifier())) {
                throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
            }
            return getRepository().save(getAsPhenomenonEntity(observableProperty));
        }
    }

    @Override
    public PhenomenonEntity updateEntity(String id, PhenomenonEntity entity, HttpMethod method)
            throws STACRUDException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<PhenomenonEntity> existing = getRepository().findByStaIdentifier(id);
                if (existing.isPresent()) {
                    PhenomenonEntity merged = merge(existing.get(), entity);
                    return getRepository().save(getAsPhenomenonEntity(merged));
                }
                throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    @Override
    protected PhenomenonEntity updateEntity(PhenomenonEntity entity) {
        return getRepository().save(getAsPhenomenonEntity(entity));
    }

    private void checkUpdate(PhenomenonEntity entity) throws STACRUDException {
        if (entity instanceof ObservablePropertyEntity) {
            ObservablePropertyEntity observableProperty = (ObservablePropertyEntity) entity;
            if (observableProperty.hasDatastreams()) {
                for (AbstractDatastreamEntity datastream : observableProperty.getDatastreams()) {
                    checkInlineDatastream(datastream);
                }
            }
        }
    }

    @Override
    public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                // delete datastreams
                datastreamRepository.findAll(dQS.withObservedPropertyStaIdentifier(id)).forEach(d -> {
                    try {
                        getDatastreamService().delete(d);
                    } catch (STACRUDException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(
                        "Unable to delete. Entity not found.", HTTPStatus.NOT_FOUND);
            }
        }
    }

    @Override
    protected void delete(PhenomenonEntity entity) throws STACRUDException {
        delete(entity.getStaIdentifier());
    }

    @Override
    public PhenomenonEntity createOrUpdate(PhenomenonEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    private PhenomenonEntity getAsPhenomenonEntity(PhenomenonEntity observableProperty) {
        return observableProperty instanceof ObservablePropertyEntity
                ? ((ObservablePropertyEntity) observableProperty).asPhenomenonEntity()
                : observableProperty;
    }

    @Override
    public PhenomenonEntity merge(PhenomenonEntity existing, PhenomenonEntity toMerge) {
        mergeIdentifierNameDescription(existing, toMerge);
        return existing;
    }

    // TODO: check if this is used somewhere
    public ObservablePropertyEntity mergeObservablePropertyEntity(ObservablePropertyEntity existing,
                                                                  ObservablePropertyEntity toMerge) {
        if (toMerge.hasDatastreams()) {
            toMerge.getDatastreams().forEach(d -> existing.addDatastream(d));
        }
        return (ObservablePropertyEntity) merge(existing, toMerge);
    }
}
