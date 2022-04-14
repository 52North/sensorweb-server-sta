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
package org.n52.sta;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.old.entity.DatastreamDTO;
import org.n52.sta.api.old.entity.FeatureOfInterestDTO;
import org.n52.sta.api.old.entity.HistoricalLocationDTO;
import org.n52.sta.api.old.entity.LocationDTO;
import org.n52.sta.api.old.entity.ObservationDTO;
import org.n52.sta.api.old.entity.ObservedPropertyDTO;
import org.n52.sta.api.old.entity.SensorDTO;
import org.n52.sta.api.old.entity.ThingDTO;
import org.n52.sta.api.old.serialize.DatastreamSerDes;
import org.n52.sta.api.old.serialize.FeatureOfInterestSerDes;
import org.n52.sta.api.old.serialize.HistoricalLocationSerDes;
import org.n52.sta.api.old.serialize.LocationSerDes;
import org.n52.sta.api.old.serialize.ObservationSerDes;
import org.n52.sta.api.old.serialize.ObservedPropertySerDes;
import org.n52.sta.api.old.serialize.SensorSerDes;
import org.n52.sta.api.old.serialize.ThingSerDes;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class DTOMapper {

    public Class collectionNameToClass(String collectionName) throws STAInvalidUrlException {
        switch (collectionName) {
            case StaConstants.THINGS:
                return ThingDTO.class;
            case StaConstants.LOCATIONS:
                return LocationDTO.class;
            case StaConstants.DATASTREAMS:
                return DatastreamDTO.class;
            case StaConstants.HISTORICAL_LOCATIONS:
                return HistoricalLocationDTO.class;
            case StaConstants.SENSORS:
                return SensorDTO.class;
            case StaConstants.OBSERVATIONS:
                return ObservationDTO.class;
            case StaConstants.OBSERVED_PROPERTIES:
                return ObservedPropertyDTO.class;
            case StaConstants.FEATURES_OF_INTEREST:
                return FeatureOfInterestDTO.class;
//            case StaConstants.GROUPS:
//                return GroupDTO.class;
//            case StaConstants.RELATIONS:
//                return RelationDTO.class;
//            case StaConstants.LICENSES:
//                return LicenseDTO.class;
//            case StaConstants.PROJECTS:
//                return ProjectDTO.class;
//            case StaConstants.PARTIES:
//                return PartyDTO.class;
            default:
                throw new STAInvalidUrlException("could not resolve collectionName to entity");
        }
    }

    public Class collectionNameToPatchClass(String collectionName) throws STAInvalidUrlException {
        switch (collectionName) {
            case StaConstants.THINGS:
            case StaConstants.THING:
                return ThingSerDes.ThingDTOPatch.class;
            case StaConstants.LOCATIONS:
            case StaConstants.LOCATION:
                return LocationSerDes.LocationDTOPatch.class;
            case StaConstants.DATASTREAMS:
            case StaConstants.DATASTREAM:
                return DatastreamSerDes.DatastreamDTOPatch.class;
            case StaConstants.HISTORICAL_LOCATIONS:
            case StaConstants.HISTORICAL_LOCATION:
                return HistoricalLocationSerDes.HistoricalLocationDTOPatch.class;
            case StaConstants.SENSORS:
            case StaConstants.SENSOR:
                return SensorSerDes.SensorDTOPatch.class;
            case StaConstants.OBSERVATIONS:
            case StaConstants.OBSERVATION:
                return ObservationSerDes.ObservationDTOPatch.class;
            case StaConstants.OBSERVED_PROPERTIES:
            case StaConstants.OBSERVED_PROPERTY:
                return ObservedPropertySerDes.ObservedPropertyDTOPatch.class;
            case StaConstants.FEATURES_OF_INTEREST:
            case StaConstants.FEATURE_OF_INTEREST:
                return FeatureOfInterestSerDes.FeatureOfInterestDTOPatch.class;
//            case StaConstants.GROUP:
//            case StaConstants.GROUPS:
//                return ObservationGroupSerDes.ObservationGroupDTOPatch.class;
//            case StaConstants.RELATIONS:
//            case StaConstants.RELATION:
//                return ObservationRelationSerDes.ObservationRelationDTOPatch.class;
//            case StaConstants.LICENSES:
//            case StaConstants.LICENSE:
//                return LicenseSerDes.LicenseDTOPatch.class;
//            case StaConstants.PROJECTS:
//            case StaConstants.PROJECT:
//                return ProjectSerDes.ProjectDTOPatch.class;
//            case StaConstants.PARTIES:
//            case StaConstants.PARTY:
//                return PartySerDes.PartyDTOPatch.class;
            default:
                throw new STAInvalidUrlException("Could not resolve CollectionName to PatchEntity class!");
        }
    }
}
