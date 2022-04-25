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
package org.n52.sta.data.old.repositories;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface RepositoryConstants {

    String DESCRIPTION = "description";
    String NAME = "name";
    String ENCODINGTYPE = "encodingType";
    String LOCATION = "location";
    String PHENOMENONTIME = "phenomenonTime";
    String SAMPLINGTIMESTART = "samplingTimeStart";
    String SAMPLINGTIMEEND = "samplingTimeEnd";
    String RESULTTIMESTART = "resultTimeStart";
    String RESULTTIMEEND = "resultTimeEnd";
    String VALIDTIMESTART = "validTimeStart";
    String VALIDTIMEEND = "validTimeEnd";
    String RESULTTIME = "resultTime";
    String VALIDTIME = "validTime";
    String METADATA = "metadata";
    String PROPERTIES = "properties";
    String OBSERVATIONTYPE = "observationType";
    String UOM = "unitOfMeasurement";
    String OBSERVEDAREA = "observedArea";
    String TIME = "time";
    String FEATURE = "feature";
    String DEFINITION = "definition";
    String PARAMETERS = "parameters";
    String RESULT = "result";

    String ENTITYNAME_OBSERVATION = "DataEntity";
    String ENTITYNAME_ABSTRACT_DATASET = "AbstractDatasetEntity";
    String ENTITYNAME_DATASET = "DatasetEntity";
    String ENTITYNAME_DATASET_AGGREGATION = "DatasetAggregationEntity";
    String ENTITYNAME_AFEATURE_OF_INTEREST = "AbstractFeatureEntity";
    String ENTITYNAME_FEATURE_OF_INTEREST = "FeatureEntity";
    String ENTITYNAME_HIST_LOCATION = "HistoricalLocationEntity";
    String ENTITYNAME_LOCATION = "LocationEntity";
    String ENTITYNAME_OBSERVED_PROPERTY = "PhenomenonEntity";
    String ENTITYNAME_SENSOR = "ProcedureEntity";
    String ENTITYNAME_THING = "PlatformEntity";
}
