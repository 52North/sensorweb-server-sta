/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.data.old.service;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.Dataset;
import org.n52.series.db.beans.DatasetAggregationEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.dataset.ValueType;
import org.n52.series.db.beans.parameter.BooleanParameterEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.old.common.CommonDatastreamService;
import org.n52.sta.data.old.repositories.CategoryRepository;
import org.n52.sta.data.old.repositories.DatastreamParameterRepository;
import org.n52.sta.data.old.repositories.DatastreamRepository;
import org.n52.sta.data.old.repositories.ObservationRepository;
import org.n52.sta.data.old.repositories.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

// @Component
// @DependsOn({ "springApplicationContext", "datastreamRepository" })
// @Transactional
public class DatastreamService extends CommonDatastreamService<AbstractDatasetEntity, DatastreamRepository> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamService.class);

    public DatastreamService(
            DatastreamRepository repository,
            @Value("${server.feature.isMobile:false}") boolean isMobileFeatureEnabled,
            @Value("${server.feature.includeDatastreamCategory:false}") boolean includeDatastreamCategory,
            UnitRepository unitRepository,
            CategoryRepository categoryRepository,
            ObservationRepository observationRepository,
            DatastreamParameterRepository parameterRepository,
            OfferingService offeringService,
            FormatService formatService,
            EntityManager em) {
        super(repository,
                isMobileFeatureEnabled,
                includeDatastreamCategory,
                unitRepository,
                categoryRepository,
                observationRepository,
                parameterRepository,
                offeringService,
                formatService,
                em);
    }

    @Override
    protected Dataset createDataset(AbstractDatasetEntity datastream,
            AbstractFeatureEntity<?> feature,
            String staIdentifier)
            throws STACRUDException {
        return (Dataset) fillDataset(new DatasetEntity(), datastream, feature, staIdentifier);
    }

    private AbstractDatasetEntity fillDataset(DatasetEntity shell,
            AbstractDatasetEntity datastream,
            AbstractFeatureEntity<?> feature,
            String staIdentifier)
            throws STACRUDException {
        CategoryEntity category = categoryRepository.findByIdentifier(CategoryService.DEFAULT_CATEGORY)
                .orElseThrow(() -> new STACRUDException("Could not find default SOS Category!"));
        OfferingEntity offering = offeringService.createOrFetchOffering(datastream.getProcedure());
        AbstractDatasetEntity dataset = createDatasetSkeleton(shell,
                datastream.getOMObservationType()
                        .getFormat(),
                isMobileFeatureEnabled
                        && datastream.getThing()
                                .hasParameters()
                        && datastream.getThing()
                                .getParameters()
                                .stream()
                                .filter(p -> p instanceof BooleanParameterEntity)
                                .filter(p -> p.getName()
                                        .equals("isMobile"))
                                .anyMatch(p -> ((ParameterEntity<Boolean>) p).getValue()));
        dataset.setIdentifier(UUID.randomUUID()
                .toString());
        dataset.setStaIdentifier(staIdentifier);
        dataset.setName(datastream.getName());
        dataset.setDescription(datastream.getDescription());
        dataset.setProcedure(datastream.getProcedure());
        dataset.setPhenomenon(datastream.getObservableProperty());
        dataset.setCategory(category);
        dataset.setFeature(feature);
        dataset.setProcedure(datastream.getProcedure());
        dataset.setOffering(offering);
        dataset.setPlatform(datastream.getThing());
        dataset.setGeometryEntity(datastream.getGeometryEntity());
        dataset.setUnit(datastream.getUnit());
        dataset.setOMObservationType(datastream.getOMObservationType());

        // dataset.setParameters(datastream.getParameters());
        if (datastream.getId() != null) {
            dataset.setAggregation(datastream);
        }
        return dataset;
    }

    private DatasetEntity createDatasetSkeleton(DatasetEntity dataset, String observationType, boolean isMobile) {
        dataset.setObservationType(ObservationType.simple);
        if (isMobile) {
            LOGGER.debug("Setting DatasetType to 'trajectory'");
            dataset.setDatasetType(DatasetType.trajectory);
            dataset.setMobile(true);
        } else {
            dataset.setDatasetType(DatasetType.timeseries);
        }
        switch (observationType) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                return dataset.setValueType(ValueType.quantity);
            case OmConstants.OBS_TYPE_CATEGORY_OBSERVATION:
                return dataset.setValueType(ValueType.category);
            case OmConstants.OBS_TYPE_COUNT_OBSERVATION:
                return dataset.setValueType(ValueType.count);
            case OmConstants.OBS_TYPE_TEXT_OBSERVATION:
                return dataset.setValueType(ValueType.text);
            case OmConstants.OBS_TYPE_TRUTH_OBSERVATION:
                return dataset.setValueType(ValueType.bool);
            default:
                return dataset;
        }
    }

    @Override
    protected DatasetAggregationEntity createAggregation() {
        return new DatasetAggregationEntity();
    }
}
