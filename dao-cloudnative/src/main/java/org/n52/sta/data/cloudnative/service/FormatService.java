/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.cloudnative.service;

import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.om.features.SfConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Format;
import org.n52.sta.data.cloudnative.dao.impl.FormatDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class FormatService {

    private static final Logger logger = LoggerFactory.getLogger(FormatService.class);
    private final MutexFactory mutexFactory;
    private final FormatDaoImpl formatDao;
    private static final AtomicLong TS = new AtomicLong();

    public FormatService(MutexFactory mutexFactory,
                         FormatDaoImpl formatDao) throws STACRUDException {
        this.mutexFactory = mutexFactory;
        this.formatDao = formatDao;

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
            createOrFetchFormat(common_format);
        }
    }

    public Format createOrFetchFormat(String formatType) throws STACRUDException {
        synchronized (mutexFactory.getLock(formatType)) {
            if (!formatDao.existsByFormat(formatType)) {
                Format formatPOJO = new Format();
                formatPOJO.setFormatId(getUniqueTimestamp());
                formatPOJO.setDefinition(formatType);
                logger.debug("Persisting new formatPOJO: " + formatPOJO.getDefinition());
                formatDao.save(formatPOJO);
                return formatPOJO;
            } else {
                return formatDao.findByFormat(formatType).get();
            }
        }
    }

    public String getFormatDefinitionFromGeometry(Geometry geometry) {
        if (geometry != null) {
            switch (geometry.getGeometryType()) {
                case "Point":
                    return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT;
                case "LineString":
                    return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE;
                case "Polygon":
                    return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE;
                default:
                    return SfConstants.SAMPLING_FEAT_TYPE_SF_SPATIAL_SAMPLING_FEATURE;
            }
        }
        return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_FEATURE;
    }

    public Format createFormatFromGeometry(Geometry geometry) throws STACRUDException {
        Format formatPOJO = new Format();
        formatPOJO.setFormatId(getUniqueTimestamp());
        formatPOJO.setDefinition(getFormatDefinitionFromGeometry(geometry));
        formatDao.save(formatPOJO);
        return formatPOJO;
    }

    public Long getUniqueTimestamp() {
        long micros = System.currentTimeMillis() * 1000;
        for ( ; ; ) {
            long value = TS.get();
            if (micros <= value)
                micros = value + 1;
            if (TS.compareAndSet(value, micros))
                return micros;
        }
    }
}
