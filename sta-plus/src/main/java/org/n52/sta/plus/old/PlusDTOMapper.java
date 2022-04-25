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
package org.n52.sta.plus.old;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.old.utils.DefaultDTOMapper;
import org.n52.sta.plus.old.entity.GroupDTO;
import org.n52.sta.plus.old.entity.LicenseDTO;
import org.n52.sta.plus.old.entity.PartyDTO;
import org.n52.sta.plus.old.entity.ProjectDTO;
import org.n52.sta.plus.old.entity.RelationDTO;
import org.n52.sta.plus.old.serialize.LicenseSerDes;
import org.n52.sta.plus.old.serialize.ObservationGroupSerDes;
import org.n52.sta.plus.old.serialize.ObservationRelationSerDes;
import org.n52.sta.plus.old.serialize.PartySerDes;
import org.n52.sta.plus.old.serialize.ProjectSerDes;

public class PlusDTOMapper extends DefaultDTOMapper {

    @Override
    public Class collectionNameToClass(String collectionName) throws STAInvalidUrlException {
        switch (collectionName) {
            case StaConstants.GROUPS:
                return GroupDTO.class;
            case StaConstants.RELATIONS:
                return RelationDTO.class;
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

    @Override
    public Class collectionNameToPatchClass(String collectionName) throws STAInvalidUrlException {
        switch (collectionName) {
            case StaConstants.GROUP:
            case StaConstants.GROUPS:
                return ObservationGroupSerDes.ObservationGroupDTOPatch.class;
            case StaConstants.RELATIONS:
            case StaConstants.RELATION:
                return ObservationRelationSerDes.ObservationRelationDTOPatch.class;
            case StaConstants.LICENSES:
            case StaConstants.LICENSE:
                return LicenseSerDes.LicenseDTOPatch.class;
            case StaConstants.PROJECTS:
            case StaConstants.PROJECT:
                return ProjectSerDes.ProjectDTOPatch.class;
            case StaConstants.PARTIES:
            case StaConstants.PARTY:
                return PartySerDes.PartyDTOPatch.class;
            default:
                return super.collectionNameToPatchClass(collectionName);
        }
    }

}
