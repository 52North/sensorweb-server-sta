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

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.sta.AbstractObservationEntity;
import org.n52.series.db.beans.sta.mapped.BooleanObservationEntity;
import org.n52.series.db.beans.sta.mapped.CategoryObservationEntity;
import org.n52.series.db.beans.sta.mapped.CountObservationEntity;
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.series.db.beans.sta.mapped.ObservationEntity;
import org.n52.series.db.beans.sta.mapped.QuantityObservationEntity;
import org.n52.series.db.beans.sta.mapped.TextObservationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.ObservationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.ObservationQuerySpecifications;
import org.n52.sta.data.repositories.CategoryRepository;
import org.n52.sta.data.repositories.DataRepository;
import org.n52.sta.data.repositories.DatasetRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.ObservationRepository;
import org.n52.sta.data.repositories.OfferingRepository;
import org.n52.sta.data.repositories.ParameterRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
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
public class ObservationService
        extends AbstractObservationService<ObservationRepository<ObservationEntity<?>>, ObservationEntity<?>,
        ObservationEntity<?>> {

    private static final ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();

    @Autowired
    public ObservationService(ObservationRepository<ObservationEntity<?>> repository,
                              DataRepository<DataEntity<?>> dataRepository,
                              CategoryRepository categoryRepository,
                              OfferingRepository offeringRepository,
                              DatastreamRepository datastreamRepository,
                              DatasetRepository datasetRepository,
                              ParameterRepository parameterRepository,
                              @Value("${server.feature.isMobile:false}") boolean isMobileFeatureEnabled) {
        super(repository,
              ObservationEntity.class,
              oQS,
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

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Observation, EntityTypes.Observations};
    }

    @Override
    protected ObservationEntity<?> fetchExpandEntities(ObservationEntity<?> returned,
                                                       ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        // I returned = new ObservationEntity(entity);
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (ObservationEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                switch (expandProperty) {
                case STAEntityDefinition.DATASTREAM:
                    DatastreamEntity datastream = getDatastreamService()
                            .getEntityByRelatedEntityRaw(returned.getStaIdentifier(),
                                                         STAEntityDefinition.OBSERVATIONS,
                                                         null,
                                                         expandItem.getQueryOptions());
                    returned.setDatastream(datastream);
                    break;
                case STAEntityDefinition.FEATURE_OF_INTEREST:
                    AbstractFeatureEntity<?> foi = ((FeatureOfInterestService)
                            getFeatureOfInterestService()).getEntityByDatasetIdRaw(returned.getDataset().getId(),
                                                                                   expandItem.getQueryOptions());
                    returned.setFeature(foi);
                    break;
                default:
                    throw new RuntimeException("This can never happen!");
                }
            } else {
                throw new STAInvalidQueryException("Invalid expandOption supplied. Cannot find " + expandProperty +
                                                           " on Entity of type 'Observations'");
            }
        }
        return returned;
    }

    @Override
    public Specification<ObservationEntity<?>> byRelatedEntityFilter(String relatedId,
                                                                     String relatedType,
                                                                     String ownId) {
        Specification<ObservationEntity<?>> filter;
        switch (relatedType) {
        case STAEntityDefinition.DATASTREAMS: {
            filter = oQS.withDatastreamStaIdentifier(relatedId);
            break;
        }
        case STAEntityDefinition.FEATURES_OF_INTEREST: {
            filter = oQS.withFeatureOfInterestStaIdentifier(relatedId);
            break;
        }
        default:
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }
        if (ownId != null) {
            filter = filter.and(oQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    protected ObservationEntity castToConcreteObservationType(AbstractObservationEntity observation,
                                                              DatasetEntity dataset)
            throws STACRUDException {
        ObservationEntity data = null;
        String value = (String) observation.getValue();
        switch (dataset.getOmObservationType().getFormat()) {
        case OmConstants.OBS_TYPE_MEASUREMENT:
            QuantityObservationEntity quantityObservationEntity = new QuantityObservationEntity();
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
            CategoryObservationEntity categoryObservationEntity = new CategoryObservationEntity();
            if (observation.hasValue()) {
                categoryObservationEntity.setValue(value);
            }
            data = categoryObservationEntity;
            break;
        case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
            CountObservationEntity countObservationEntity = new CountObservationEntity();
            if (observation.hasValue()) {
                countObservationEntity.setValue(Integer.parseInt(value));
            }
            data = countObservationEntity;
            break;
        case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
            TextObservationEntity textObservationEntity = new TextObservationEntity();
            if (observation.hasValue()) {
                textObservationEntity.setValue(value);
            }
            data = textObservationEntity;
            break;
        case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
            BooleanObservationEntity booleanObservationEntity = new BooleanObservationEntity();
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
