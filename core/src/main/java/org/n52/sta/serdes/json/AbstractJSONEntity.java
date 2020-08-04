/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.serdes.json;

import org.n52.series.db.beans.sta.AbstractDatastreamEntity;
import org.n52.series.db.beans.sta.StaRelations;
import org.n52.sta.serdes.json.extension.JSONCSDatastream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public interface AbstractJSONEntity {

    String INVALID_REFERENCED_ENTITY =
            "Cannot parse as Reference: Only @iot.id may be present when referencing an existing entity!";

    String INVALID_INLINE_ENTITY_MISSING =
            "Cannot parse Entity: Not all required properties present! Missing: ";

    String INVALID_INLINE_ENTITY_INVALID_VALUE =
            "Invalid Entity. Invalid Value: ";

    String INVALID_BACKREFERENCE =
            "Invalid nesting of Entities!";

    String INVALID_DUPLICATE_REFERENCE =
            "Duplicate references to related Entity provided! Either specify reference to related Entity in JSON " +
                    "Payload OR inside Request URL";

    default void parseCSDatastreams(StaRelations.HasDatastreams self, JSONCSDatastream[] csdatastreams) {
        if (csdatastreams != null) {
            Set<AbstractDatastreamEntity> joinedDatastreams;
            if (self.getDatastreams() != null) {
                joinedDatastreams = self.getDatastreams();
            } else {
                joinedDatastreams = new HashSet<>();
            }
            joinedDatastreams.addAll(Arrays.stream(csdatastreams)
                                           .map(ds -> ds.toEntity(JSONBase.EntityType.FULL,
                                                                  JSONBase.EntityType.REFERENCE))
                                           .collect(Collectors.toSet()));
            self.setDatastreams(joinedDatastreams);
        }
    }

    default void parseDatastreams(StaRelations.HasDatastreams self, JSONDatastream[] datastreams){
        if (datastreams != null) {
            self.setDatastreams(Arrays.stream(datastreams)
                                      .map(ds -> ds.toEntity(JSONBase.EntityType.FULL,
                                                             JSONBase.EntityType.REFERENCE))
                                      .collect(Collectors.toSet()));
        }
    }
}
