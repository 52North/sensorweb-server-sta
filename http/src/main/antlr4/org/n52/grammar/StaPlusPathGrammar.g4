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
 * Grammar for parsing STAPlus URIs
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * ----------------------------------------------------------------------------
 */
parser grammar StaPlusPathGrammar;


import StaPathGrammar;

options { tokenVocab = StaPlusPathLexer; }

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
   | party
   | parties
   | project
   | projects
   | license
   | licenses
   | group
   | groups
   | relation
   | relations
   | subject
   | subjects
   ;

observation
   : OBSERVATIONS identifier (SLASH (datastream | featureOfInterest | observationProperty | group | groups | subjects | objects))?
   ;
   
datastream
   : DATASTREAM
   | DATASTREAMS identifier (SLASH (observations | observation | observedProperties | observedProperty | sensor | thing | datastreamProperty | party | parties | license | licenses | project | projects))?
   ;

thing
   : THING
   | THINGS identifier (SLASH (datastream | datastreams | location | locations | historicalLocation | historicalLocations | thingProperty | party | parties))?
   ;

party
  : PARTY
  | PARTIES identifier (SLASH (group | groups | datastream | datastreams | thing | things | partyProperty))?
  ;

project
  : PROJECT
  | PROJECTS identifier (SLASH (datastream | datastreams | projectProperty))?
  ;

license
  : LICENSE
  | LICENSES identifier (SLASH (group | groups | datastream | datastreams | licenseProperty))?
  ;

group
  : GROUP
  | GROUPS identifier (SLASH (party | parties | license | licenses | relation | relations | observation | observations | groupProperty))?
  ;


relation
  : RELATION
  | RELATIONS identifier (SLASH (group | groups | subject | object | relationProperty))?
  ;
  
subject
  : SUBJECT
  | SUBJECTS
  ;
  
object
  : OBJECT
  | OBJECTS
  ;

////////////////////////////////////////////////////////////////

// properties

////////////////////////////////////////////////////////////////


partyProperty
  : (PROP_ID | PROP_NAME | PROP_DESCRIPTION | PROP_AUTH_ID | PROP_ROLE | PROP_DISPLAY_NAME) (SLASH VALUE)?
  ;

projectProperty
  : (PROP_ID | PROP_NAME | PROP_DESCRIPTION | PROP_CLASSIFICATION | PROP_TERMS_OF_USE | PROP_PRIVACY_POLICY | PROP_CREATIONTIME | PROP_RUNTIME | PROP_URL | PROP_PROPERTIES) (SLASH VALUE)?
  ;

licenseProperty
  : (PROP_ID | PROP_NAME | PROP_DESCRIPTION | PROP_DEFINITION | PROP_LOGO | PROP_PROPERTIES) (SLASH VALUE)?
  ;

groupProperty
  : (PROP_ID | PROP_NAME | PROP_DESCRIPTION | PROP_PURPOSE | PROP_RUNTIME | PROP_CREATIONTIME |PROP_PROPERTIES) (SLASH VALUE)?
  ;

relationProperty
  : (PROP_ID | PROP_DESCRIPTION | PROP_ROLE | PROP_EXTERNAL_OBJECT | PROP_PROPERTIES) (SLASH VALUE)?
  ;

////////////////////////////////////////////////////////////////

// Collections

////////////////////////////////////////////////////////////////


parties
  : PARTIES
  ;

projects
  : PROJECTS
  ;

licenses
  : LICENSES
  ;

groups
  : GROUPS
  ;

relations
  : RELATIONS
  ;

subjects
  : SUBJECTS
  ;

objects
  : OBJECTS
  ;

////////////////////////////////////////////////////////////////

// General

////////////////////////////////////////////////////////////////

identifier
   : IDENTIFIER
   ;
