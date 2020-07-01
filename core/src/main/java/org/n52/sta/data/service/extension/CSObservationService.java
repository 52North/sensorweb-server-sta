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

package org.n52.sta.data.service.extension;

import org.n52.series.db.beans.sta.AbstractObservationEntity;
import org.n52.series.db.beans.sta.mapped.ObservationEntity;
import org.n52.series.db.beans.sta.mapped.extension.CSObservation;
import org.n52.series.db.beans.sta.mapped.extension.ObservationRelation;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.CSObservationQuerySpecifications;
import org.n52.sta.data.repositories.CSObservationRepository;
import org.n52.sta.data.service.AbstractSensorThingsEntityServiceImpl;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile("citSciExtension")
public class CSObservationService
        extends AbstractSensorThingsEntityServiceImpl<CSObservationRepository<CSObservation<?>>, CSObservation<?>,
        CSObservation<?>> {

    private static final CSObservationQuerySpecifications csoQS = new CSObservationQuerySpecifications();

    public CSObservationService(CSObservationRepository repository) {
        super(repository, CSObservation.class);
    }

    @Override public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.CSObservation, EntityTypes.CSObservations};
    }

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        CollectionWrapper observationCollection = getObservationService().getEntityCollection(queryOptions);
        List<ElementWithQueryOptions> recoded = new ArrayList<>();
        for (ElementWithQueryOptions entity : observationCollection.getEntities()) {
            CSObservation obs = new CSObservation((ObservationEntity) entity.getEntity());
            recoded.add(new ElementWithQueryOptions.CSObservationWithQueryOptions(obs, queryOptions));
        }
        return new CollectionWrapper(observationCollection.getTotalEntityCount(),
                                     recoded,
                                     observationCollection.hasNextPage());
    }

    @Override protected CSObservation fetchExpandEntities(CSObservation entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        return null;
    }

    @Override protected Specification<CSObservation<?>> byRelatedEntityFilter(String relatedId,
                                                                              String relatedType,
                                                                              String ownId) {
        Specification<CSObservation<?>> filter;
        switch (relatedType) {
        case STAEntityDefinition.OBSERVATION_RELATIONS:
            filter = csoQS.withRelationStaIdentifier(relatedId);
            break;
        default:
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(csoQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override protected CSObservation createEntity(CSObservation entity) throws STACRUDException {
        AbstractObservationEntity<?> obs = getObservationService().createOrUpdate(entity);
        for (Object relation : entity.getRelations()) {
            ObservationRelation rel = (ObservationRelation) relation;
            rel.setObservation((ObservationEntity) obs);
            getObservationRelationService().createOrUpdate((ObservationRelation) relation);
        }
        return new CSObservation(obs);
    }

    @Override protected CSObservation updateEntity(String id, CSObservation entity, HttpMethod method)
            throws STACRUDException {
        throw new STACRUDException("not implemented yet");
    }

    @Override protected CSObservation updateEntity(CSObservation entity) throws STACRUDException {
        throw new STACRUDException("not implemented yet");
    }

    @Override protected void delete(CSObservation entity) throws STACRUDException {
        throw new STACRUDException("not implemented yet");

    }

    @Override public CSObservation createOrUpdate(CSObservation entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override protected CSObservation merge(CSObservation existing, CSObservation toMerge)
            throws STACRUDException {
        throw new STACRUDException("not implemented yet");
    }

    @Override public void delete(String id) throws STACRUDException {
        getRepository().deleteByIdentifier(id);
    }
}
