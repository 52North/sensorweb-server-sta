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
   ;

party: PARTY;
project: PROJECT;
license: LICENSE;
group: GROUP;
relation: RELATION;

parties: PARTIES;
projects: PROJECTS;
licenses: LICENSES;
groups: GROUPS;
relations: RELATIONS;
