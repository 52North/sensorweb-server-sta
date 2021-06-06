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

package org.n52.sta.data.citsci.service;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetAggregationEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.parameter.observation.ObservationParameterEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.citsci.CitSciEntityServiceRepository;
import org.n52.sta.data.citsci.query.ObservationQuerySpecifications;
import org.n52.sta.data.vanilla.repositories.DatastreamRepository;
import org.n52.sta.data.vanilla.repositories.EntityGraphRepository;
import org.n52.sta.data.vanilla.repositories.ObservationParameterRepository;
import org.n52.sta.data.vanilla.repositories.ObservationRepository;
import org.n52.sta.data.vanilla.service.ObservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class CitSciObservationService extends ObservationService {

    private static final ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    private static final Logger LOGGER = LoggerFactory.getLogger(CitSciObservationService.class);

    private LicenseService licenseService;

    public CitSciObservationService(ObservationRepository<DataEntity<?>> repository,
                                    EntityManager em,
                                    DatastreamRepository datastreamRepository,
                                    ObservationParameterRepository parameterRepository) {
        super(repository, em, datastreamRepository, parameterRepository);
    }

    @Override
    public Specification<DataEntity<?>> byRelatedEntityFilter(String relatedId,
                                                              String relatedType,
                                                              String ownId) {
        Specification<DataEntity<?>> filter;
        switch (relatedType) {
            case STAEntityDefinition.DATASTREAMS: {
                filter = ObservationQuerySpecifications.withDatastreamStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.FEATURES_OF_INTEREST: {
                filter = ObservationQuerySpecifications.withFeatureOfInterestStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.NAV_SUBJECTS: {
                filter = ObservationQuerySpecifications.asSubject(relatedId);
                break;
            }
            case STAEntityDefinition.LICENSES: {
                filter = ObservationQuerySpecifications.withLicenseStaIdentifier(relatedId);
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
    public DataEntity<?> createOrfetch(DataEntity<?> entity) throws STACRUDException {
        synchronized (getLock(entity.getStaIdentifier())) {
            DataEntity<?> observation = entity;
            if (!observation.isProcessed()) {
                if (entity.getStaIdentifier() != null && entity.getSamplingTimeStart() == null) {
                    Optional<DataEntity<?>> optionalEntity =
                        getRepository().findByStaIdentifier(entity.getStaIdentifier());
                    if (optionalEntity.isPresent()) {
                        return optionalEntity.get();
                    } else {
                        throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                                 StaConstants.THING,
                                                                 entity.getStaIdentifier()));
                    }
                }

                observation.setProcessed(true);
                check(observation);

                // Fetch dataset and check if FOI matches to reuse existing dataset
                AbstractDatasetEntity datastream = datastreamRepository
                    .findByStaIdentifier(entity.getDataset().getStaIdentifier(),
                                         EntityGraphRepository.FetchGraph.FETCHGRAPH_FEATURE)
                    .orElseThrow(() -> new STACRUDException("Unable to find Datastream!"));
                AbstractFeatureEntity<?> feature = createOrfetchFeature(observation, datastream.getPlatform().getId());

                // Check all subdatasets for a matching  dataset
                Set<DatasetEntity> datasets;
                if (datastream.getAggregation() == null && !(datastream instanceof DatasetAggregationEntity)) {
                    // We are not an aggregate so there is only one dataset to check for fit
                    datasets = Collections.singleton((DatasetEntity) datastream);
                } else {
                    datasets = datastreamRepository.findAllByAggregationId(datastream.getId())
                        .stream()
                        .map(d -> (DatasetEntity) d)
                        .collect(Collectors.toSet());
                }

                // Check all datasets for a matching FOI
                boolean found = false;
                for (DatasetEntity dataset : datasets) {
                    if (!dataset.hasFeature()) {
                        // We have a dataset without a feature
                        LOGGER.debug("Reusing existing dataset without FOI.");
                        dataset.setFeature(feature);
                        observation.setDataset(datastreamRepository.save(dataset));
                        found = true;
                        break;
                    } else if (feature.getId().equals(dataset.getFeature().getId())) {
                        // We have a dataset with a matching feature
                        observation.setDataset(dataset);
                        LOGGER.debug("Reusing existing dataset with matching FOI.");
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // We have not found a matching dataset so we need to create a new one
                    LOGGER.debug("Creating new dataset as none with matching FOI exists");
                    observation.setDataset(getDatastreamService().createOrExpandAggregation(datastream, feature));
                }

                // Save License
                if (observation.getLicense() != null) {
                    observation.setLicense(((LicenseService)
                        ((CitSciEntityServiceRepository) serviceRepository)
                            .getEntityServiceRaw(CitSciEntityServiceRepository.CitSciEntityTypes.License))
                                               .createOrfetch(observation.getLicense()));
                }

                // Save Observation
                DataEntity<?> data = saveObservation(observation, observation.getDataset());

                // Save parameters
                if (observation.getParameters() != null) {
                    parameterRepository.saveAll(
                        observation
                            .getParameters()
                            .stream()
                            .filter(o -> o instanceof ObservationParameterEntity)
                            .map(o -> {
                                ((ObservationParameterEntity<?>) o).setObservation(data);
                                return (ObservationParameterEntity) o;
                            })
                            .collect(Collectors.toSet())
                    );
                    data.setParameters(observation.getParameters());
                }

                // Update FirstValue/LastValue + FirstObservation/LastObservation of Dataset + Aggregation
                updateDataset(observation.getDataset(), data);
                return data;
            }
            return observation;
        }
    }
}
