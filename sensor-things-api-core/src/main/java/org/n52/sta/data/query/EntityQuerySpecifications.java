/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.data.query;

import org.n52.series.db.beans.QDataEntity;
import org.n52.series.db.beans.QDatasetEntity;
import org.n52.series.db.beans.QFeatureEntity;
import org.n52.series.db.beans.QProcedureEntity;
import org.n52.series.db.beans.sta.QDatastreamEntity;
import org.n52.series.db.beans.sta.QHistoricalLocationEntity;
import org.n52.series.db.beans.sta.QLocationEntity;
import org.n52.series.db.beans.sta.QThingEntity;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public abstract class EntityQuerySpecifications {
    
    final static DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    final static ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    
    final static QDatastreamEntity qdatastream = QDatastreamEntity.datastreamEntity;
    final static QLocationEntity qlocation = QLocationEntity.locationEntity;
    final static QHistoricalLocationEntity qhistoricallocation = QHistoricalLocationEntity.historicalLocationEntity;
    final static QProcedureEntity qsensor = QProcedureEntity.procedureEntity;
    final static QDataEntity qobservation = QDataEntity.dataEntity;
    final static QFeatureEntity qfeature = QFeatureEntity.featureEntity;
    final static QDatasetEntity qdataset = QDatasetEntity.datasetEntity;
    final static QThingEntity qthing = QThingEntity.thingEntity;
}
