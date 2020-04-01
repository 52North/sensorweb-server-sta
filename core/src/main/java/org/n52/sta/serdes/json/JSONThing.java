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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.series.db.beans.PlatformEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONThing extends JSONBase.JSONwithIdNameDescription<PlatformEntity> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public JsonNode properties;
    @JsonManagedReference
    public JSONLocation[] Locations;
    @JsonManagedReference
    public JSONDatastream[] Datastreams;

    public JSONThing() {
        self = new PlatformEntity();
    }

    @Override
    public PlatformEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
        case FULL:
            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");

            self.setIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);

            //TODO: check if this is correct
            if (properties != null) {
                self.setProperties(properties.toString());
            }

            if (Locations != null) {
                self.setLocations(Arrays.stream(Locations)
                                        .map(loc -> loc.toEntity(JSONBase.EntityType.FULL,
                                                                 JSONBase.EntityType.REFERENCE))
                                        .collect(Collectors.toSet()));
            }

            if (Datastreams != null) {
                self.setDatastreams(Arrays.stream(Datastreams)
                                          .map(ds -> ds.toEntity(JSONBase.EntityType.FULL,
                                                                 JSONBase.EntityType.REFERENCE))
                                          .collect(Collectors.toSet()));
            }

            // Deal with back reference during deep insert
            if (backReference != null) {
                if (backReference instanceof JSONLocation) {
                    self.addLocationEntity(((JSONLocation) backReference).getEntity());
                } else if (backReference instanceof JSONDatastream) {
                    if (self.getDatastreams() != null) {
                        self.getDatastreams().add(((JSONDatastream) backReference).getEntity());
                    } else {
                        self.setDatastreams(Collections.singleton(((JSONDatastream) backReference).getEntity()));
                    }
                } else {
                    self.addHistoricalLocation(((JSONHistoricalLocation) backReference).getEntity());
                }
            }

            return self;
        case PATCH:
            self.setIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);

            //TODO: check if this is correct
            if (properties != null) {
                self.setProperties(properties.toString());
            }

            if (Locations != null) {
                self.setLocations(Arrays.stream(Locations)
                                        .map(loc -> loc.toEntity(JSONBase.EntityType.REFERENCE))
                                        .collect(Collectors.toSet()));
            }

            if (Datastreams != null) {
                self.setDatastreams(Arrays.stream(Datastreams)
                                          .map(ds -> ds.toEntity(JSONBase.EntityType.REFERENCE))
                                          .collect(Collectors.toSet()));
            }

            return self;
        case REFERENCE:
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(properties, INVALID_REFERENCED_ENTITY);

            Assert.isNull(Locations, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Datastreams, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        default:
            return null;
        }
    }
}
