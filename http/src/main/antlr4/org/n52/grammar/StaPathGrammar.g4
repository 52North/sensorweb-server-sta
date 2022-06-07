/*
 * Copyright 2022-2022 52°North Initiative for Geospatial Open Source
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

/* ----------------------------------------------------------------------------
 * Grammar for parsing OGC SensorthingsAPI 1.1 URIs.
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * ----------------------------------------------------------------------------
 */
parser grammar StaPathGrammar;

options { tokenVocab = StaPathLexer; }

path
   : SLASH resource (SLASH REF)? EOF
   ;

resource
   : datastream
   | datastreams
   | thing
   | things
   | location
   | locations
   | historicalLocation
   | historicalLocations
   | sensor
   | sensors
   | observedProperty
   | observedProperties
   | observation
   | observations
   | featureOfInterest
   | featuresOfInterest
   ;

datastream
   : DATASTREAM
   | DATASTREAMS identifier
        (SLASH (observations
                | observation
                | observedProperties
                | observedProperty
                | sensor
                | thing
                | datastreamProperty
               )
        )?
   ;

observation
   : OBSERVATIONS identifier
        (SLASH (datastream | featuresOfInterest | observationProperty ))?
   ;

thing
   : THING
   | THINGS identifier
        (SLASH (datastream
                | datastreams
                | location
                | locations
                | historicalLocation
                | historicalLocations
                | thingProperty
               )
        )?
   ;

location
   : LOCATIONS identifier
        (SLASH (thing | things | historicalLocation | historicalLocations | locationProperty))?
   ;

historicalLocation
   : HISTORICAL_LOCATIONS identifier
        (SLASH (thing | things | location| locations | historicalLocationProperty))?
   ;

sensor
   : SENSOR
   | SENSORS identifier
        (SLASH (datastream | datastreams | sensorProperty))?
   ;

observedProperty
   : OBSERVED_PROPERTY
   | OBSERVED_PROPERTIES identifier
        (SLASH (datastream | datastreams | observedPropertyProperty))?
   ;

featureOfInterest
   : FEATURE_OF_INTEREST
   | FEATURES_OF_INTEREST identifier
        (SLASH (observation | observations | featureOfInterestProperty))?
   ;

////////////////////////////////////////////////////////////////
// properties
////////////////////////////////////////////////////////////////

datastreamProperty
   : PROP_ID
   | PROP_NAME
   | PROP_DESCRIPTION
   | PROP_OBSERVATION_TYPE
   | PROP_UOM
   | PROP_OBSERVED_AREA
   | PROP_PHENOMENON_TIME
   | PROP_RESULT_TIME
   | PROP_PROPERTIES
   ;

observationProperty
   : PROP_ID
   | PROP_PHENOMENON_TIME
   | PROP_RESULT
   | PROP_RESULT_TIME
   | PROP_RESULT_QUALITY
   | PROP_VALID_TIME
   | PROP_PARAMETERS
   ;

thingProperty
   : PROP_ID
   | PROP_NAME
   | PROP_DESCRIPTION
   | PROP_PROPERTIES
   ;

locationProperty
   : PROP_ID
   | PROP_NAME
   | PROP_DESCRIPTION
   | PROP_ENCODINGTYPE
   | PROP_LOCATION
   | PROP_PROPERTIES
   ;

historicalLocationProperty
   : PROP_ID
   | PROP_TIME
   ;

sensorProperty
   : PROP_ID
   | PROP_NAME
   | PROP_DESCRIPTION
   | PROP_ENCODINGTYPE
   | PROP_METADATA
   | PROP_PROPERTIES
   ;

observedPropertyProperty
   : PROP_ID
   | PROP_NAME
   | PROP_DESCRIPTION
   | PROP_DEFINITION
   | PROP_PROPERTIES
   ;

featureOfInterestProperty
   : PROP_ID
   | PROP_NAME
   | PROP_DESCRIPTION
   | PROP_ENCODINGTYPE
   | PROP_FEATURE
   ;

////////////////////////////////////////////////////////////////
// Collections
////////////////////////////////////////////////////////////////

datastreams : DATASTREAMS ;
observations : OBSERVATIONS ;
things : THINGS ;
locations: LOCATIONS ;
historicalLocations: HISTORICAL_LOCATIONS ;
sensors : SENSORS ;
observedProperties : OBSERVED_PROPERTIES ;
featuresOfInterest : FEATURES_OF_INTEREST ;

////////////////////////////////////////////////////////////////
// General
////////////////////////////////////////////////////////////////

identifier
   : IDENTIFIER
   ;