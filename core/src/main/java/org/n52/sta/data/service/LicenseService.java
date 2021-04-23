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
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.LicenseEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.LicenseQuerySpecifications;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.LicenseRepository;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile(StaConstants.CITSCIEXTENSION)
public class LicenseService
    extends AbstractSensorThingsEntityServiceImpl<LicenseRepository, LicenseEntity> {

    private static final LicenseQuerySpecifications lQS = new LicenseQuerySpecifications();
    private final EntityManager em;

    public LicenseService(LicenseRepository repository,
                          EntityManager em) {
        super(repository, em, LicenseEntity.class);
        this.em = em;
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
                if (LicenseEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                    return new EntityGraphRepository.FetchGraph[] {
                        EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS,
                    };
                } else {
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                     expandProperty,
                                                                     StaConstants.LICENSE));
                }
            }
        }
        return new EntityGraphRepository.FetchGraph[0];
    }

    @Override protected LicenseEntity fetchExpandEntitiesWithFilter(LicenseEntity entity, ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        em.detach(entity);
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }

            String expandProperty = expandItem.getPath();
            if (LicenseEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                Page<AbstractDatasetEntity> datastreams = getDatastreamService()
                    .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                           STAEntityDefinition.LICENSES,
                                                           expandItem.getQueryOptions());
                entity.setDatasets(datastreams.get().collect(Collectors.toSet()));
                return entity;
            } else {
                throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                                                 expandProperty,
                                                                 StaConstants.LICENSE));
            }
        }
        return entity;
    }

    @Override protected Specification<LicenseEntity> byRelatedEntityFilter(String relatedId,
                                                                           String relatedType,
                                                                           String ownId) {
        Specification<LicenseEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS:
                filter = lQS.withDatastreamStaIdentifier(relatedId);
                break;
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(lQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override public LicenseEntity createOrfetch(LicenseEntity entity) throws STACRUDException {
        LicenseEntity license = entity;
        //if (!license.isProcessed()) {
        if (license.getStaIdentifier() != null && !license.isSetName()) {
            Optional<LicenseEntity> optionalEntity =
                getRepository().findByStaIdentifier(license.getStaIdentifier());
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                         StaConstants.LICENSE,
                                                         license.getStaIdentifier()));
            }
        } else if (license.getStaIdentifier() == null) {
            // Autogenerate Identifier
            String uuid = UUID.randomUUID().toString();
            license.setStaIdentifier(uuid);
        }
        synchronized (getLock(license.getStaIdentifier())) {
            if (getRepository().existsByStaIdentifier(license.getStaIdentifier())) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            } else {
                for (AbstractDatasetEntity datastream : license.getDatasets()) {
                    getDatastreamService().create(datastream);
                }
                getRepository().save(license);
            }
        }
        //}
        return license;
    }

    @Override protected LicenseEntity updateEntity(String id, LicenseEntity entity, HttpMethod method)
        throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<LicenseEntity> existing = getRepository().findByStaIdentifier(id);
                if (existing.isPresent()) {
                    LicenseEntity merged = merge(existing.get(), entity);
                    return getRepository().save(merged);
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override public LicenseEntity createOrUpdate(LicenseEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override protected LicenseEntity merge(LicenseEntity existing, LicenseEntity toMerge)
        throws STACRUDException {

        if (toMerge.getStaIdentifier() != null) {
            existing.setStaIdentifier(toMerge.getStaIdentifier());
        }
        if (toMerge.getName() != null) {
            existing.setName(toMerge.getName());
        }
        if (toMerge.getDefinition() != null) {
            existing.setDefinition(toMerge.getDefinition());
        }
        if (toMerge.getLogo() != null) {
            existing.setLogo(toMerge.getLogo());
        }

        mergeDatastreams(existing, toMerge);
        return existing;
    }

    @Override public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                LicenseEntity license = getRepository().findByStaIdentifier(id).get();
                // Delete related Datastreams
                for (AbstractDatasetEntity ds : license.getDatasets()) {
                    getDatastreamService().delete(ds.getStaIdentifier());
                }
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }
}
