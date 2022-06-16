/*
 * Copyright 2022-2022 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* ----------------------------------------------------------------------------
 * Grammar for parsing OGC SensorthingsAPI 1.1 URIs.
 * @author <a href='mailto:j.speckamp@52north.org'>Jan Speckamp</a>
 * ----------------------------------------------------------------------------
 */
lexer grammar StaPathLexer;
// General

SLASH
   : '/'
   ;

SQ
   : [']
   ;

OP
   : '('
   ;

CP
   : ')'
   ;

IDENTIFIER
   : OP SQ? .*? SQ? CP
   ;

fragment DIGIT
   : [0-9]
   ;

REF
   : '$ref'
   ;

VALUE
   : '$value'
   ;

DIGITPLUS
   : DIGIT+
   ;
   // STA Entities
   
DATASTREAM
   : 'Datastream'
   ;

DATASTREAMS
   : 'Datastreams'
   ;

THING
   : 'Thing'
   ;

THINGS
   : 'Things'
   ;

LOCATION
   : 'Location'
   ;

LOCATIONS
   : 'Locations'
   ;

HISTORICAL_LOCATION
   : 'HistoricalLocation'
   ;

HISTORICAL_LOCATIONS
   : 'HistoricalLocations'
   ;

SENSOR
   : 'Sensor'
   ;

SENSORS
   : 'Sensors'
   ;

OBSERVED_PROPERTY
   : 'ObservedProperty'
   ;

OBSERVED_PROPERTIES
   : 'ObservedProperties'
   ;

OBSERVATION
   : 'Observation'
   ;

OBSERVATIONS
   : 'Observations'
   ;

FEATURE_OF_INTEREST
   : 'FeatureOfInterest'
   ;

FEATURES_OF_INTEREST
   : 'FeaturesOfInterest'
   ;
   // Entity Properties
   
PROP_ID
   : 'id'
   ;

PROP_SELF_LINK
   : 'selfLink'
   ;

PROP_DEFINITION
   : 'definition'
   ;

PROP_DESCRIPTION
   : 'description'
   ;

PROP_ENCODINGTYPE
   : 'encodingType'
   ;

PROP_FEATURE
   : 'feature'
   ;

PROP_LOCATION
   : 'location'
   ;

PROP_NAME
   : 'name'
   ;

PROP_OBSERVATION_TYPE
   : 'observationType'
   ;

PROP_OBSERVED_AREA
   : 'observedArea'
   ;

PROP_PARAMETERS
   : 'parameters'
   ;

PROP_PHENOMENON_TIME
   : 'phenomenonTime'
   ;

PROP_PROPERTIES
   : 'properties'
   ;

PROP_RESULT_TIME
   : 'resultTime'
   ;

PROP_RESULT
   : 'result'
   ;

PROP_RESULT_QUALITY
   : 'resultQuality'
   ;

PROP_TIME
   : 'time'
   ;

PROP_UOM
   : 'unitOfMeasurement'
   ;

PROP_VALID_TIME
   : 'validTime'
   ;

PROP_METADATA
   : 'metadata'
   ;

