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

import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.sta.mapped.extension.License;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.LicenseQuerySpecifications;
import org.n52.sta.data.repositories.LicenseRepository;
import org.n52.sta.data.service.AbstractSensorThingsEntityServiceImpl;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile("citSciExtension")
public class LicenseService
        extends AbstractSensorThingsEntityServiceImpl<LicenseRepository, License, License> {

    private static final LicenseQuerySpecifications lQS = new LicenseQuerySpecifications();
    private static final String NOT_IMPLEMENTED = "not implemented yet!";

    public LicenseService(LicenseRepository repository) {
        super(repository, License.class);
    }

    @Override public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.License, EntityTypes.Licenses};
    }

    @Override protected License fetchExpandEntities(License entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        return null;
    }

    @Override protected Specification<License> byRelatedEntityFilter(String relatedId,
                                                                     String relatedType,
                                                                     String ownId) {
        Specification<License> filter;
        switch (relatedType) {
        case STAEntityDefinition.CSDATASTREAMS:
            filter = lQS.withRelationStaIdentifier(relatedId);
            break;
        default:
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(lQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override protected License createEntity(License entity) throws STACRUDException {
        License license = entity;
        //if (!license.isProcessed()) {
        if (license.getStaIdentifier() != null && !license.isSetName()) {
            Optional<License> optionalEntity =
                    getRepository().findByStaIdentifier(license.getStaIdentifier());
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException("No License with id '"
                                                   + license.getStaIdentifier() + "' "
                                                   + "found");
            }
        } else if (license.getStaIdentifier() == null) {
            // Autogenerate Identifier
            String uuid = UUID.randomUUID().toString();
            license.setStaIdentifier(uuid);
        }
        synchronized (getLock(license.getStaIdentifier())) {
            if (getRepository().existsByStaIdentifier(license.getStaIdentifier())) {
                throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
            } else {
                getRepository().save(license);
            }
        }
        //}
        return license;
    }

    @Override protected License updateEntity(String id, License entity, HttpMethod method)
            throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override protected License updateEntity(License entity) throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override public License createOrUpdate(License entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    @Override public String checkPropertyName(String property) {
        return property;
    }

    @Override protected License merge(License existing, License toMerge)
            throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override protected void delete(License entity) throws STACRUDException {
        throw new STACRUDException(NOT_IMPLEMENTED);
    }

    @Override public void delete(String id) throws STACRUDException {
        getRepository().deleteByStaIdentifier(id);
    }
}
