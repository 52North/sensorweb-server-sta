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

import org.n52.series.db.beans.FormatEntity;
import org.n52.shetland.ogc.om.features.SfConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.old.MutexFactory;
import org.n52.sta.data.repositories.value.FormatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
// @Component
// @DependsOn({"springApplicationContext"})
// @Transactional
public class FormatService {

    private static final Logger logger = LoggerFactory.getLogger(FormatService.class);
    private final MutexFactory mutexFactory;
    private final FormatRepository formatRepository;

    public FormatService(MutexFactory mutexFactory,
                         FormatRepository formatRepository) throws STACRUDException {
        this.mutexFactory = mutexFactory;
        this.formatRepository = formatRepository;

        // persist common formats
        String[] COMMON_FORMATS = {
            "application/pdf",
            "application/vnd.geo+json",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation",
            SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_FEATURE,
            SfConstants.SAMPLING_FEAT_TYPE_SF_SPATIAL_SAMPLING_FEATURE,
            SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT,
            SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE,
            SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE,
            SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SOLID,
            SfConstants.SAMPLING_FEAT_TYPE_SF_SPECIMEN,
        };

        for (String common_format : COMMON_FORMATS) {
            FormatEntity formatEntity = new FormatEntity();
            formatEntity.setFormat(common_format);
            createOrFetchFormat(formatEntity);
        }
    }

    @Transactional
    public FormatEntity createOrFetchFormat(FormatEntity formatEntity) throws STACRUDException {
        synchronized (mutexFactory.getLock(formatEntity.getFormat())) {
            if (!formatRepository.existsByFormat(formatEntity.getFormat())) {
                logger.debug("Persisting new FormatEntity: " + formatEntity.getFormat());
                return formatRepository.save(formatEntity);
            } else {
                return formatRepository.findByFormat(formatEntity.getFormat());
            }
        }
    }
}
