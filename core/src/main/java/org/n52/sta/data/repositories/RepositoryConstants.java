/*
 * Copyright 2015-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.n52.sta.data.repositories;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface RepositoryConstants {

    String DESCRIPTION = "description";
    String NAME = "name";
    String ENCODINGTYPE = "encodingType";
    String LOCATION = "location";
    String PHENOMENONTIME = "phenomenonTime";
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

    String ENTITYNAME_OBSERVATION = "DataEntity";
    String ENTITYNAME_DATASTREAM = "DatastreamEntity";
    String ENTITYNAME_FEATURE_OF_INTEREST = "FeatureEntity";
    String ENTITYNAME_HIST_LOCATION = "HistoricalLocationEntity";
    String ENTITYNAME_LOCATION = "LocationEntity";
    String ENTITYNAME_OBSERVED_PROPERTY = "PhenomenonEntity";
    String ENTITYNAME_SENSOR = "ProcedureEntity";
    String ENTITYNAME_THING = "PlatformEntity";
}
