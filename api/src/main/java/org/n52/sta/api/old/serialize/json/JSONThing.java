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
package org.n52.sta.api.old.serialize.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.old.dto.Thing;
import org.n52.sta.api.old.entity.ThingDTO;
import org.n52.sta.api.old.serialize.common.AbstractJSONEntity;
import org.n52.sta.api.old.serialize.common.JSONBase;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONThing extends JSONBase.JSONwithIdNameDescription<ThingDTO> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public ObjectNode properties;

    @JsonManagedReference
    public JSONLocation[] Locations;

    @JsonManagedReference
    public JSONDatastream[] Datastreams;

    @JsonManagedReference
    public JSONHistoricalLocation[] HistoricalLocations;

    public JSONThing() {
        self = new Thing();
    }

    @Override protected void parseReferencedFrom() {
        if (referencedFromType != null) {
            switch (referencedFromType) {
                case StaConstants.LOCATIONS:
                    assertIsNull(Locations, INVALID_DUPLICATE_REFERENCE);
                    this.Locations = new JSONLocation[1];
                    this.Locations[0] = new JSONLocation();
                    this.Locations[0].identifier = referencedFromID;
                    return;
                default:
                    throw new IllegalArgumentException(INVALID_BACKREFERENCE);
            }
        }
    }

    @Override
    public ThingDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                assertNotNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                assertNotNull(description, INVALID_INLINE_ENTITY_MISSING + "description");

                self.setId(identifier);
                self.setName(name);
                self.setDescription(description);

                if (properties != null) {
                    self.setProperties(properties);
                }

                if (Locations != null) {
                    self.setLocations(Arrays.stream(Locations)
                                          .map(loc -> loc.parseToDTO(JSONBase.EntityType.FULL,
                                                                     JSONBase.EntityType.REFERENCE))
                                          .collect(Collectors.toSet()));
                }

                if (Datastreams != null) {
                    self.setDatastreams(Arrays.stream(Datastreams)
                                            .map(ds -> ds.parseToDTO(JSONBase.EntityType.FULL,
                                                                     JSONBase.EntityType.REFERENCE))
                                            .collect(Collectors.toSet()));
                }

                if (HistoricalLocations != null) {
                    self.setHistoricalLocations(Arrays.stream(HistoricalLocations)
                                                    .map(hloc -> hloc.parseToDTO(JSONBase.EntityType.FULL,
                                                                                 JSONBase.EntityType.REFERENCE))
                                                    .collect(Collectors.toSet()));
                }

                // Deal with back reference during deep insert
                if (backReference != null) {
                    if (backReference instanceof JSONLocation) {
                        self.addLocations(((JSONLocation) backReference).getEntity());
                    } else if (backReference instanceof JSONDatastream) {
                        if (self.getDatastream() != null) {
                            self.getDatastream().add(((JSONDatastream) backReference).getEntity());
                        } else {
                            self.setDatastreams(Collections.singleton(((JSONDatastream) backReference).getEntity()));
                        }
                    } else {
                        self.addHistoricalLocation(((JSONHistoricalLocation) backReference).getEntity());
                    }
                }
                return self;
            case PATCH:
                parseReferencedFrom();
                self.setId(identifier);
                self.setName(name);
                self.setDescription(description);

                if (properties != null) {
                    self.setProperties(properties);
                }

                if (Locations != null) {
                    self.setLocations(Arrays.stream(Locations)
                                          .map(loc -> loc.parseToDTO(JSONBase.EntityType.REFERENCE))
                                          .collect(Collectors.toSet()));
                }

                if (Datastreams != null) {
                    self.setDatastreams(Arrays.stream(Datastreams)
                                            .map(ds -> ds.parseToDTO(JSONBase.EntityType.REFERENCE))
                                            .collect(Collectors.toSet()));
                }

                if (HistoricalLocations != null) {
                    self.setHistoricalLocations(Arrays.stream(HistoricalLocations)
                                                    .map(ds -> ds.parseToDTO(JSONBase.EntityType.FULL,
                                                                             JSONBase.EntityType.REFERENCE))
                                                    .collect(Collectors.toSet()));
                }
                return self;
            case REFERENCE:
                assertIsNull(name, INVALID_REFERENCED_ENTITY);
                assertIsNull(description, INVALID_REFERENCED_ENTITY);
                assertIsNull(properties, INVALID_REFERENCED_ENTITY);

                assertIsNull(Locations, INVALID_REFERENCED_ENTITY);
                assertIsNull(Datastreams, INVALID_REFERENCED_ENTITY);

                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }
}
