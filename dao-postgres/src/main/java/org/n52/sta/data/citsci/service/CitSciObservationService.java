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

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.sta.plus.RelationEntity;
import org.n52.series.db.beans.sta.plus.StaPlusDataEntity;
import org.n52.series.db.beans.sta.plus.StaPlusQuantityDataEntity;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.vanilla.service.ObservationService;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({ "springApplicationContext" })
@Transactional
@Profile(StaConstants.STAPLUS)
public class CitSciObservationService extends ObservationService {

    @Override
    protected DataEntity castToConcreteObservationType(DataEntity<?> observation,
            DatasetEntity dataset)
            throws STACRUDException {
        DataEntity data = null;
        Object value = observation.getValue();
        switch (dataset.getOMObservationType().getFormat()) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                StaPlusQuantityDataEntity quantityObservationEntity = new StaPlusQuantityDataEntity();
                if (value.equals("NaN") || value.equals("Inf") || value.equals("-Inf")) {
                    quantityObservationEntity.setValue(null);
                } else {
                    quantityObservationEntity.setValue(BigDecimal.valueOf(Double.parseDouble((String) value)));
                }
                data = quantityObservationEntity;
                break;
            default:
                throw new STACRUDException(
                        "Unable to handle OMObservation with type: " + dataset.getOMObservationType().getFormat());
        }
        return fillConcreteObservationType(data, observation, dataset);
    }

    /**
     * Hook to add STAPlus-specific things
     *
     * @param observation the observation to save
     * @param dataset the dataset entity the observation to save relates to
     * @return the saved data entity
     * @throws STACRUDException in case saving fails
     */
    @Override
    protected DataEntity<?> saveObservation(DataEntity<?> observation, DatasetEntity dataset)
            throws STACRUDException {
        StaPlusDataEntity<?> obs = (StaPlusDataEntity<?>) observation;

        if (obs.getSubjects() != null) {
            Set<RelationEntity> subjects = new HashSet<>();
            for (RelationEntity subject : obs.getSubjects()) {
                subjects.add(getObservationRelationService().createOrUpdate(subject));
            }
            obs.setSubjects(subjects);
        }

        return super.saveObservation(observation, dataset);
    }

    @Override
    protected DataEntity<?> fillConcreteObservationType(DataEntity<?> data,
            DataEntity<?> observation,
            DatasetEntity dataset) throws STACRUDException {
        StaPlusDataEntity<?> plusData = (StaPlusDataEntity<?>) super.fillConcreteObservationType(data, observation,
                dataset);
        StaPlusDataEntity<?> plusObservation = (StaPlusDataEntity<?>) observation;
        plusData.setSubjects(plusObservation.getSubjects());
        plusData.setObjects(plusObservation.getObjects());
        plusData.setGroups(plusObservation.getGroups());
        return data;
    }

    private LicenseService getLicenseService() {
        return (LicenseService) serviceRepository
                .getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.License.name());
    }

    private ObservationRelationService getObservationRelationService() {
        return (ObservationRelationService) serviceRepository
                .getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.ObservationRelation.name());
    }
}
