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

package org.n52.sta.data.citsci;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryError;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.LicenseDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.api.dto.impl.citsci.License;
import org.n52.sta.data.vanilla.DTOTransformer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class CitSciDTOTransformer<R extends StaDTO, S extends HibernateRelations.HasId> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private DTOTransformer vanillaTransformer;
    private Map<String, Object> serialized;

    @SuppressWarnings("unchecked")
    public R toDTO(Object raw, QueryOptions queryOptions) throws STAInvalidQueryError {
        switch (raw.getClass().getSimpleName()) {
            case "AbstractDatasetEntity":
            case "DatasetEntity":
            case "DatasetAggregationEntity": {
                return (R) toDatastreamDTO((AbstractDatasetEntity) raw, queryOptions);
            }
            case "LicenseEntity": {
                return (R) toLicenseDTO((LicenseEntity) raw, queryOptions);
            }
            default:
                // As we have many different types we unwrap them all here
                if (raw instanceof DataEntity) {
                    return (R) toObservationDTO((DataEntity<?>) raw, queryOptions);
                } else {
                    return (R) vanillaTransformer.toDTO(raw, queryOptions);
                }
        }
    }

    public S fromDTO(R type) {
        serialized = new HashMap<>();
        if (type instanceof DatastreamDTO) {
            return (S) toDatasetEntity((DatastreamDTO) type);
        } else if (type instanceof ObservationDTO) {
            return (S) toDataEntity((ObservationDTO) type);
        } else if (type instanceof LicenseDTO) {
            return (S) toLicenseEntity((LicenseDTO) type);
        } else {
            throw new STAInvalidQueryError(String.format("Could not parse entity %s to Database Entity!",
                                                         type.getClass().getName()));
        }
    }

    private LicenseEntity toLicenseEntity(LicenseDTO type) {
        LicenseEntity licenseEntity = new LicenseEntity();
        licenseEntity.setStaIdentifier(type.getId());
        licenseEntity.setName(type.getName());
        licenseEntity.setDefinition(type.getDefinition());
        licenseEntity.setDescription(type.getDescription());
        licenseEntity.setLogo(type.getLogo());
        return licenseEntity;
    }

    private LicenseDTO toLicenseDTO(LicenseEntity raw, QueryOptions queryOptions) {
        License license = new License();

        license.setId(raw.getStaIdentifier());
        license.setName(raw.getName());
        license.setDescription(raw.getDescription());
        license.setDefinition(raw.getDefinition());
        license.setLogo(raw.getLogo());
        //TODO: implement
        // license.setProperties();
        //license.setDescription(raw.getDescription);
        return license;
    }

    private Object toObservationDTO(DataEntity<?> raw, QueryOptions queryOptions) {
        return null;
    }

    private Object toDatastreamDTO(AbstractDatasetEntity raw, QueryOptions queryOptions) {
        return null;
    }

    private Object toDataEntity(ObservationDTO type) {
        return null;
    }

    private Object toDatasetEntity(DatastreamDTO type) {
        return null;
    }

}
