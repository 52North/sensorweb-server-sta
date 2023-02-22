/*
 * Copyright (C) 2018-2023 52°North Initiative for Geospatial Open Source
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

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.springframework.util.Assert;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONThing extends JSONBase.JSONwithIdNameDescription<PlatformEntity> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public JsonNode properties;

    @JsonManagedReference
    public JSONLocation[] Locations;

    @JsonManagedReference
    public JSONDatastream[] Datastreams;

    @JsonManagedReference
    public JSONHistoricalLocation[] HistoricalLocations;

    public JSONThing() {
        self = new PlatformEntity();
    }

    @Override protected void parseReferencedFrom() {
        if (referencedFromType != null) {
            switch (referencedFromType) {
                case StaConstants.LOCATIONS:
                    Assert.isNull(Locations, INVALID_DUPLICATE_REFERENCE);
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
    public PlatformEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                Assert.notNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                Assert.notNull(description, INVALID_INLINE_ENTITY_MISSING + "description");

                self.setIdentifier(identifier);
                self.setStaIdentifier(identifier);
                self.setName(name);
                self.setDescription(description);

                if (properties != null) {
                    self.setParameters(convertParameters(properties, self));
                }

                if (Locations != null) {
                    self.setLocations(Arrays.stream(Locations)
                                          .map(loc -> loc.toEntity(JSONBase.EntityType.FULL,
                                                                   JSONBase.EntityType.REFERENCE))
                                          .collect(Collectors.toSet()));
                }

                if (Datastreams != null) {
                    self.setDatasets(Arrays.stream(Datastreams)
                                         .map(ds -> ds.toEntity(JSONBase.EntityType.FULL,
                                                                JSONBase.EntityType.REFERENCE))
                                         .collect(Collectors.toSet()));
                }

                if (HistoricalLocations != null) {
                    self.setHistoricalLocations(Arrays.stream(HistoricalLocations)
                                                    .map(hloc -> hloc.toEntity(JSONBase.EntityType.FULL,
                                                                               JSONBase.EntityType.REFERENCE))
                                                    .collect(Collectors.toSet()));
                }

                // Deal with back reference during deep insert
                if (backReference != null) {
                    if (backReference instanceof JSONLocation) {
                        self.addLocationEntity(((JSONLocation) backReference).getEntity());
                    } else if (backReference instanceof JSONDatastream) {
                        if (self.getDatasets() != null) {
                            self.getDatasets().add(((JSONDatastream) backReference).getEntity());
                        } else {
                            self.setDatasets(Collections.singleton(((JSONDatastream) backReference).getEntity()));
                        }
                    } else {
                        self.addHistoricalLocation(((JSONHistoricalLocation) backReference).getEntity());
                    }
                }
                return self;
            case PATCH:
                parseReferencedFrom();
                self.setIdentifier(identifier);
                self.setStaIdentifier(identifier);
                self.setName(name);
                self.setDescription(description);

                if (properties != null) {
                    self.setParameters(convertParameters(properties, self));
                }

                if (Locations != null) {
                    self.setLocations(Arrays.stream(Locations)
                                          .map(loc -> loc.toEntity(JSONBase.EntityType.REFERENCE))
                                          .collect(Collectors.toSet()));
                }

                if (Datastreams != null) {
                    self.setDatasets(Arrays.stream(Datastreams)
                                         .map(ds -> ds.toEntity(JSONBase.EntityType.REFERENCE))
                                         .collect(Collectors.toSet()));
                }

                if (HistoricalLocations != null) {
                    self.setHistoricalLocations(Arrays.stream(HistoricalLocations)
                                                    .map(ds -> ds.toEntity(JSONBase.EntityType.FULL,
                                                                           JSONBase.EntityType.REFERENCE))
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
                self.setStaIdentifier(identifier);
                return self;
            default:
                return null;
        }
    }
}
