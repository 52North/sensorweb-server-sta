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

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.ObservedPropertyEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.ObservedPropertyQuerySpecifications;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.PhenomenonParameterRepository;
import org.n52.sta.data.repositories.PhenomenonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class ObservedPropertyService
    extends AbstractSensorThingsEntityServiceImpl<PhenomenonRepository, PhenomenonEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ObservedPropertyService.class);

    private static final DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    private static final ObservedPropertyQuerySpecifications oQS = new ObservedPropertyQuerySpecifications();

    private final DatastreamRepository datastreamRepository;
    private final PhenomenonParameterRepository parameterRepository;

    @Autowired
    public ObservedPropertyService(PhenomenonRepository repository,
                                   DatastreamRepository datastreamRepository,
                                   PhenomenonParameterRepository parameterRepository,
                                   EntityManager em) {
        super(repository, em, PhenomenonEntity.class);
        this.datastreamRepository = datastreamRepository;
        this.parameterRepository = parameterRepository;
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
    public boolean existsEntityByRelatedEntity(String relatedId, String relatedType, String ownId) {
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS: {
                return getRepository().findOne(byRelatedEntityFilter(relatedId, relatedType, ownId)).isPresent();
            }
            default:
                return false;
        }
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
        throws STAInvalidQueryException {
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                if (ObservedPropertyEntityDefinition.DATASTREAMS.equals(expandProperty)) {
                    return new EntityGraphRepository.FetchGraph[] {
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS,
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS,
                    };
                }
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                 expandProperty,
                                                                 StaConstants.OBSERVED_PROPERTY));
            }
        }
        return new EntityGraphRepository.FetchGraph[]{
            EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS
        };
    }

    @Override
    protected PhenomenonEntity fetchExpandEntitiesWithFilter(PhenomenonEntity entity, ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            String expandProperty = expandItem.getPath();
            if (ObservedPropertyEntityDefinition.DATASTREAMS.equals(expandProperty)) {
                Page<AbstractDatasetEntity> datastreams = getDatastreamService()
                    .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                           STAEntityDefinition.OBSERVED_PROPERTIES,
                                                           expandItem.getQueryOptions());
                entity.setDatasets(datastreams.get().collect(Collectors.toSet()));
            } else {
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                 expandProperty,
                                                                 StaConstants.OBSERVED_PROPERTY));
            }
        }
        return entity;
    }

    @Override
    public Specification<PhenomenonEntity> byRelatedEntityFilter(String relatedId,
                                                                 String relatedType,
                                                                 String ownId) {
        Specification<PhenomenonEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS: {
                filter = oQS.withDatastreamStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }
        if (ownId != null) {
            filter = filter.and(oQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public PhenomenonEntity createOrfetch(PhenomenonEntity observableProperty) throws STACRUDException {
        if (observableProperty.getStaIdentifier() != null && !observableProperty.isSetName()) {
            Optional<PhenomenonEntity> optionalEntity =
                getRepository().findByStaIdentifier(observableProperty.getStaIdentifier());
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                         StaConstants.OBSERVED_PROPERTY,
                                                         observableProperty.getStaIdentifier()));
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
        synchronized (getLock(observableProperty.getIdentifier())) {
            synchronized (getLock(observableProperty.getStaIdentifier())) {
                // Check for duplicate definition
                if (getRepository().existsByIdentifier(observableProperty.getIdentifier())) {
                    throw new STACRUDException("Observed Property with given Definition already exists!",
                                               HTTPStatus.CONFLICT);
                }
                if (getRepository().existsByStaIdentifier(observableProperty.getStaIdentifier())) {
                    throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
                }
                return getRepository().save(observableProperty);
            }
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
                    PhenomenonEntity result = getRepository().save(merged);
                    Hibernate.initialize(result.getParameters());
                    return result;
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override
    public PhenomenonEntity createOrUpdate(PhenomenonEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override
    public String checkPropertyName(String property) {
        return oQS.checkPropertyName(property);
    }

    @Override
    public PhenomenonEntity merge(PhenomenonEntity existing, PhenomenonEntity toMerge) {
        mergeIdentifierNameDescription(existing, toMerge);
        if (toMerge.hasParameters()) {
            existing.getParameters().clear();
            toMerge.getParameters().forEach(p -> {
                                                p.setDescribeableEntity(existing);
                                                existing.addParameter(p);
                                            }
            );
        }
        return existing;
    }

    private void checkUpdate(PhenomenonEntity entity) throws STACRUDException {
        if (entity.hasDatastreams()) {
            for (AbstractDatasetEntity datastream : entity.getDatasets()) {
                checkInlineDatastream(datastream);
            }
        }
    }

    @Override
    public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                // delete datastreams
                for (AbstractDatasetEntity datastreamEntity :
                    datastreamRepository.findAll(dQS.withObservedPropertyStaIdentifier(id))) {
                    getDatastreamService().delete(datastreamEntity.getStaIdentifier());
                }
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }
}
