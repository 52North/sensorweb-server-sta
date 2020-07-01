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

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.sta.AbstractObservationEntity;
import org.n52.series.db.beans.sta.mapped.extension.BooleanCSObservation;
import org.n52.series.db.beans.sta.mapped.extension.CSObservation;
import org.n52.series.db.beans.sta.mapped.extension.CategoryCSObservation;
import org.n52.series.db.beans.sta.mapped.extension.CountCSObservation;
import org.n52.series.db.beans.sta.mapped.extension.ObservationRelation;
import org.n52.series.db.beans.sta.mapped.extension.QuantityCSObservation;
import org.n52.series.db.beans.sta.mapped.extension.TextCSObservation;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.CSObservationQuerySpecifications;
import org.n52.sta.data.repositories.CSObservationRepository;
import org.n52.sta.data.repositories.CategoryRepository;
import org.n52.sta.data.repositories.DataRepository;
import org.n52.sta.data.repositories.DatasetRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.OfferingRepository;
import org.n52.sta.data.repositories.ParameterRepository;
import org.n52.sta.data.service.AbstractObservationService;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile("citSciExtension")
public class CSObservationService
        extends AbstractObservationService<CSObservationRepository<CSObservation<?>>, CSObservation<?>,
        CSObservation<?>> {

    private static final CSObservationQuerySpecifications csoQS = new CSObservationQuerySpecifications();

    @Autowired
    public CSObservationService(CSObservationRepository<CSObservation<?>> repository,
                                DataRepository<DataEntity<?>> dataRepository,
                                CategoryRepository categoryRepository,
                                OfferingRepository offeringRepository,
                                DatastreamRepository datastreamRepository,
                                DatasetRepository datasetRepository,
                                ParameterRepository parameterRepository,
                                @Value("${server.feature.isMobile:false}") boolean isMobileFeatureEnabled) {
        super(repository,
              CSObservation.class,
              csoQS,
              isMobileFeatureEnabled,
              dataRepository,
              categoryRepository,
              offeringRepository,
              datastreamRepository,
              datasetRepository,
              parameterRepository,
              EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS,
              EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASET
        );
    }

    @Override public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.CSObservation, EntityTypes.CSObservations};
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

    @Override public CSObservation createEntity(CSObservation entity) throws STACRUDException {
        CSObservation<?> persisted = super.createEntity(entity);
        for (Object relation : entity.getRelations()) {
            ObservationRelation rel = (ObservationRelation) relation;
            rel.setObservation(persisted);
            getObservationRelationService().createOrUpdate((ObservationRelation) relation);
        }
        return persisted;
    }

    @Override protected CSObservation<?> castToConcreteObservationType(AbstractObservationEntity observation,
                                                                       DatasetEntity dataset) throws STACRUDException {
        CSObservation data = null;
        String value = (String) observation.getValue();
        switch (dataset.getOmObservationType().getFormat()) {
        case OmConstants.OBS_TYPE_MEASUREMENT:
            QuantityCSObservation quantityObservationEntity = new QuantityCSObservation();
            if (observation.hasValue()) {
                if (value.equals("NaN") || value.equals("Inf") || value.equals("-Inf")) {
                    quantityObservationEntity.setValue(null);
                } else {
                    quantityObservationEntity.setValue(BigDecimal.valueOf(Double.parseDouble(value)));
                }
            }
            data = quantityObservationEntity;
            break;
        case OmConstants.OBS_TYPE_CATEGORY_OBSERVATION:
            CategoryCSObservation categoryObservationEntity = new CategoryCSObservation();
            if (observation.hasValue()) {
                categoryObservationEntity.setValue(value);
            }
            data = categoryObservationEntity;
            break;
        case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
            CountCSObservation countObservationEntity = new CountCSObservation();
            if (observation.hasValue()) {
                countObservationEntity.setValue(Integer.parseInt(value));
            }
            data = countObservationEntity;
            break;
        case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
            TextCSObservation textObservationEntity = new TextCSObservation();
            if (observation.hasValue()) {
                textObservationEntity.setValue(value);
            }
            data = textObservationEntity;
            break;
        case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
            BooleanCSObservation booleanObservationEntity = new BooleanCSObservation();
            if (observation.hasValue()) {
                booleanObservationEntity.setValue(Boolean.parseBoolean(value));
            }
            data = booleanObservationEntity;
            break;
        default:
            break;
        }
        return fillConcreteObservationType(data, observation, dataset);
    }
}
