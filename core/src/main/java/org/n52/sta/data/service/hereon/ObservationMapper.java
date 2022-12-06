/*
 * Copyright (C) 2018-2022 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.data.service.hereon;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.Geometry;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.MetadataFeature;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.observation.ObservationTextParameterEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.svalbard.odata.core.QueryOptionsFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ObservationMapper {

    //TODO: do we need to set the EPSG dynamically?
    private static final GeometryFactory geometryFactory =
        new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    static DataEntity<?> toDataEntity(MetadataFeature esriFeature) {
        //TODO: make this configurable via mapping file
        TextDataEntity dataEntity = new TextDataEntity();

        dataEntity.setId(Long.valueOf(esriFeature.getAttributes().getValue("objectid")));
        dataEntity.setIdentifier(esriFeature.getAttributes().getValue("globalid"));
        dataEntity.setStaIdentifier(esriFeature.getAttributes().getValue("globalid"));
        dataEntity.setName(esriFeature.getAttributes().getValue("globalId"));

        Date samplingTime = getDate(esriFeature.getAttributes().getValue("date_time"));
        dataEntity.setSamplingTimeStart(samplingTime);
        dataEntity.setSamplingTimeEnd(samplingTime);

        String measure_val = esriFeature.getAttributes().getValue("measure_val");
        dataEntity.setValue(measure_val);

        double lat = esriFeature.getGeometry().getX();
        double lon = esriFeature.getGeometry().getY();
        GeometryEntity geometryEntity = new GeometryEntity();
        geometryEntity.setGeometry(geometryFactory.createPoint(new Coordinate(lat, lon)));
        dataEntity.setGeometryEntity(geometryEntity);

        //TODO: read mapping and only add requested fields
        dataEntity.setParameters(esriFeature.getAttributes().getAdditionalProperties().entrySet().stream().map(
                        (entry) -> {
                            ObservationTextParameterEntity param = new ObservationTextParameterEntity();
                            param.setName(entry.getKey());
                            param.setValue(String.valueOf(entry.getValue()));
                            return param;
                        }
                ).collect(Collectors.toSet())
        );

        return dataEntity;
    }

    public static List<ElementWithQueryOptions> toDataEntities(List<MetadataFeature> features) {
        QueryOptions qo = new QueryOptionsFactory().createDummy();
        return features.stream()
                .map(ObservationMapper::toDataEntity)
                .map(entity -> ElementWithQueryOptions.from(entity, qo))
                .collect(Collectors.toList());
    }

    private static Date getDate(String time) {
        if (time != null && !time.isEmpty()) {
            try {
                return new DateTime(Long.parseLong(time)).toDate();
            } catch (NumberFormatException nfe) {
                return DateTimeHelper.parseIsoString2DateTime(time).toDate();
            }
        }
        return null;
    }
}
