/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.sta.AggregationEntity;
import org.n52.series.db.beans.sta.plus.StaPlusDataset;
import org.n52.series.db.beans.sta.plus.StaPlusDatasetAggregationEntity;
import org.n52.series.db.beans.sta.plus.StaPlusDatasetEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.citsci.repositories.StaPlusDatastreamRepository;
import org.n52.sta.data.common.CommonDatastreamService;
import org.n52.sta.data.vanilla.repositories.CategoryRepository;
import org.n52.sta.data.vanilla.repositories.DatastreamParameterRepository;
import org.n52.sta.data.vanilla.repositories.ObservationRepository;
import org.n52.sta.data.vanilla.repositories.UnitRepository;
import org.n52.sta.data.vanilla.service.CategoryService;
import org.n52.sta.data.vanilla.service.FormatService;
import org.n52.sta.data.vanilla.service.OfferingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.UUID;

@Component
@DependsOn({"springApplicationContext", "datastreamRepository"})
@Profile(StaConstants.STAPLUS)
@Transactional
public class CitSciDatastreamService extends CommonDatastreamService<StaPlusDataset, StaPlusDatastreamRepository> {

    public CitSciDatastreamService(
        StaPlusDatastreamRepository repository,
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

    /*
    protected DatasetEntity createandSaveDataset(AbstractDatasetEntity datastream,
                                                 AbstractFeatureEntity<?> feature,
                                                 String staIdentifier) throws STACRUDException {
        if (datastream.getParty() != null) {
            datastream.setParty(getPartyService().createOrfetch(datastream.getParty()));
        }

        if (datastream.getProject() != null) {
            datastream.setProject(getProjectService().createOrfetch(datastream.getProject()));
        }

        DatasetEntity saved = getRepository().save(createDataset(datastream, feature, staIdentifier));
        if (datastream.getParameters() != null) {
            parameterRepository.saveAll(datastream.getParameters()
                                            .stream()
                                            .filter(t -> t instanceof DatasetParameterEntity)
                                            .map(t -> {
                                                ((DatasetParameterEntity) t).setDataset(saved);
                                                return (DatasetParameterEntity) t;
                                            })
                                            .collect(Collectors.toSet()));
        }
        return saved;
    }
    */

    @Override
    protected StaPlusDatasetEntity createDataset(AbstractDatasetEntity datastream,
                                                 AbstractFeatureEntity<?> feature,
                                                 String staIdentifier) throws STACRUDException {
        StaPlusDatasetEntity dataset = new StaPlusDatasetEntity();
        fillDataset(dataset, datastream, feature, staIdentifier);
        dataset.setParty(((StaPlusDatasetEntity) datastream).getParty());
        dataset.setProject(((StaPlusDatasetEntity) datastream).getProject());
        return dataset;
    }

    protected AbstractDatasetEntity fillDataset(StaPlusDatasetEntity dataset,
                                                AbstractDatasetEntity datastream,
                                                AbstractFeatureEntity<?> feature,
                                                String staIdentifier) throws STACRUDException {
        CategoryEntity category = categoryRepository.findByIdentifier(CategoryService.DEFAULT_CATEGORY)
            .orElseThrow(() -> new STACRUDException("Could not find default SOS Category!"));
        OfferingEntity offering = offeringService.createOrFetchOffering(datastream.getProcedure());

        // dataset.setObservationType(ObservationType.simple);
        // dataset.setValueType(ValueType.quantity);
        dataset.setIdentifier(UUID.randomUUID().toString());
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

    @Override
    protected AggregationEntity createAggregation() {
        return new StaPlusDatasetAggregationEntity();
    }
}
