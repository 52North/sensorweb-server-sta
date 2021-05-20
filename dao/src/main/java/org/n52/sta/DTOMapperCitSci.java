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

package org.n52.sta;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.CitSciObservationDTO;
import org.n52.sta.api.dto.CitSciDatastreamDTO;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.dto.LicenseDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.api.dto.ObservationRelationDTO;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.api.dto.PartyDTO;
import org.n52.sta.api.dto.ProjectDTO;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.serdes.DatastreamSerDes;
import org.n52.sta.serdes.FeatureOfInterestSerDes;
import org.n52.sta.serdes.HistoricalLocationSerDes;
import org.n52.sta.serdes.LicenseSerDes;
import org.n52.sta.serdes.LocationSerDes;
import org.n52.sta.serdes.ObservationRelationSerDes;
import org.n52.sta.serdes.ObservationSerDes;
import org.n52.sta.serdes.ObservedPropertySerDes;
import org.n52.sta.serdes.PartySerDes;
import org.n52.sta.serdes.ProjectSerDes;
import org.n52.sta.serdes.SensorSerDes;
import org.n52.sta.serdes.ThingSerDes;
import org.n52.sta.serdes.json.JSONCitSciDatastream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mapper used by the CitSci Extension
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@Profile("citsci")
public class DTOMapperCitSci extends DTOMapper{

    public Class collectionNameToClass(String collectionName) throws STAInvalidUrlException {
        switch (collectionName) {
            case StaConstants.DATASTREAMS:
                // We use custom Datastream DTO to be able to represent link to Party/Project
                return CitSciDatastreamDTO.class;
            case StaConstants.OBSERVATIONS:
                return CitSciObservationDTO.class;
            case StaConstants.OBSERVATION_GROUPS:
                return ObservationGroupDTO.class;
            case StaConstants.OBSERVATION_RELATIONS:
                return ObservationRelationDTO.class;
            case StaConstants.LICENSES:
                return LicenseDTO.class;
            case StaConstants.PROJECTS:
                return ProjectDTO.class;
            case StaConstants.PARTIES:
                return PartyDTO.class;
            default:
                return super.collectionNameToClass(collectionName);
        }
    }

    public Class collectionNameToPatchClass(String collectionName) throws STAInvalidUrlException {
        switch (collectionName) {
            case StaConstants.DATASTREAMS:
            case StaConstants.DATASTREAM:
                return JSONCitSciDatastream.class;
            case StaConstants.OBSERVATIONS:
            case StaConstants.OBSERVATION:
                return .class;
            case StaConstants.OBSERVATION_GROUP:
            case StaConstants.OBSERVATION_GROUPS:
                return .class ;
            case StaConstants.OBSERVATION_RELATIONS:
            case StaConstants.OBSERVATION_RELATION:
                return ObservationRelationSerDes.ObservationRelationPatch.class;
            case StaConstants.LICENSES:
            case StaConstants.LICENSE:
                return LicenseSerDes.LicensePatch.class;
            case StaConstants.PROJECTS:
            case StaConstants.PROJECT:
                return ProjectSerDes.ProjectPatch.class;
            case StaConstants.PARTIES:
            case StaConstants.PARTY:
                return PartySerDes.PartyPatch.class;
            default:
                return super.collectionNameToPatchClass(collectionName);
        }
    }

}
