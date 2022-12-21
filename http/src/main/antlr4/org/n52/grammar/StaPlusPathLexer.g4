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
lexer grammar StaPlusPathLexer;

import StaPathLexer;

PARTY: 'Party';
PROJECT: 'Project';
LICENSE: 'License';
GROUP: 'Group';
RELATION: 'Relation';

PARTIES: 'Parties';
PROJECTS: 'Projects';
LICENSES: 'Licenses';
GROUPS: 'Groups';
RELATIONS: 'Relations';

PROP_AUTH_ID: 'authId';
PROP_ROLE: 'role';
PROP_DISPLAY_NAME: 'displayName';
PROP_CLASSIFICATION: 'classification';
PROP_TERMS_OF_USE: 'termsOfUse';
PROP_PRIVACY_POLICY: 'privacyPolicy';
PROP_CREATIONTIME: 'creationTime';
PROP_RUNTIME: 'runTime';
PROP_URL: 'url';

PROP_LOGO: 'logo';
PROP_PURPOSE: 'purpose';
PROP_EXTERNAL_OBJECT: 'externalObject';

SUBJECT: 'Subject';
OBJECT: 'Object';
SUBJECTS: 'Subjects';
OBJECTS: 'Objects';
