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

import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.AbstractDatastreamEntity;
import org.n52.series.db.beans.sta.mapped.extension.CSDatastream;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.ogc.sta.model.ThingEntityDefinition;
import org.n52.sta.data.repositories.ThingRepository;
import org.n52.sta.data.service.ThingService;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaption of the ThingService to be used when CitizenScience Extension is active. Realises links to CSDatastream
 * during CRUD operations.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile("citSciExtension")
public class CSThingService extends ThingService {

    public CSThingService(ThingRepository repository) {
        super(repository);
    }

    @Override protected PlatformEntity fetchExpandEntities(PlatformEntity entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (ThingEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                switch (expandProperty) {
                case STAEntityDefinition.CSDATASTREAMS:
                    Page<CSDatastream> datastreams = getCSDatastreamService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                                   STAEntityDefinition.THINGS,
                                                                   expandItem.getQueryOptions());
                    entity.setDatastreams(datastreams.get().collect(Collectors.toSet()));
                    break;
                default:
                    super.fetchExpandEntities(entity, expandOption);
                }
            } else {
                throw new STAInvalidQueryException("Invalid expandOption supplied. Cannot find " + expandProperty +
                                                           " on Entity of type 'Thing'");
            }
        }
        return entity;
    }

    @Override
    protected Specification<PlatformEntity> byRelatedEntityFilter(String relatedId,
                                                                  String relatedType,
                                                                  String ownId) {
        Specification<PlatformEntity> filter;
        switch (relatedType) {
        case STAEntityDefinition.CSDATASTREAMS: {
            filter = tQS.withCSDatastreamStaIdentifier(relatedId);
            if (ownId != null) {
                filter = filter.and(tQS.withStaIdentifier(ownId));
            }
            return filter;
        }
        default:
            return super.byRelatedEntityFilter(relatedId, relatedType, ownId);
        }
    }

    @Override
    protected void processDatastreams(PlatformEntity thing) throws STACRUDException {
        if (thing.hasDatastreams()) {
            Set<AbstractDatastreamEntity> datastreams = new LinkedHashSet<>();
            for (AbstractDatastreamEntity datastream : thing.getDatastreams()) {
                datastream.setThing(thing);
                datastreams.add(getAbstractDatastreamService(datastream).createEntity(datastream));
            }
            thing.setDatastreams(datastreams);
        }
    }
}
