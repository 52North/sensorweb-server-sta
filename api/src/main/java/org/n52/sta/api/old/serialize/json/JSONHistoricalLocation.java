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

import org.n52.sta.api.old.dto.HistoricalLocation;
import org.n52.sta.api.old.entity.HistoricalLocationDTO;
import org.n52.sta.api.old.serialize.common.AbstractJSONEntity;
import org.n52.sta.api.old.serialize.common.JSONBase;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({
    "NM_FIELD_NAMING_CONVENTION",
    "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
})
public class JSONHistoricalLocation extends JSONBase.JSONwithIdTime<HistoricalLocationDTO>
        implements
        AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String time;
    public JSONThing Thing;

    @JsonManagedReference
    public JSONLocation[] Locations;

    public JSONHistoricalLocation() {
        self = new HistoricalLocation();
    }

    @Override
    protected void parseReferencedFrom() {
        if (referencedFromType != null) {
            switch (referencedFromType) {
            case "Locations":
                assertIsNull(Locations, INVALID_DUPLICATE_REFERENCE);
                this.Locations = new JSONLocation[1];
                this.Locations[0] = new JSONLocation();
                this.Locations[0].identifier = referencedFromID;
                return;
            case "Things":
                assertIsNull(Thing, INVALID_DUPLICATE_REFERENCE);
                this.Thing = new JSONThing();
                this.Thing.identifier = referencedFromID;
                return;
            default:
                throw new IllegalArgumentException(INVALID_BACKREFERENCE);
            }
        }
    }

    @Override
    public HistoricalLocationDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
        case FULL:
            parseReferencedFrom();
            assertNotNull(time, INVALID_INLINE_ENTITY_MISSING + "time");

            self.setId(identifier);
            self.setTime(parseTime(time));

            if (Thing != null) {
                self.setThing(Thing.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            } else if (backReference instanceof JSONThing) {
                self.setThing(((JSONThing) backReference).getEntity());
                this.Thing = (JSONThing) backReference;
            } else {
                assertNotNull(null, INVALID_INLINE_ENTITY_MISSING + "Thing");
            }

            if (Locations != null) {
                self.setLocations(Arrays.stream(Locations)
                                        .map(loc -> loc.parseToDTO(JSONBase.EntityType.FULL,
                                                                   JSONBase.EntityType.REFERENCE))
                                        .collect(Collectors.toSet()));
            } else if (backReference instanceof JSONLocation) {
                self.setLocations(Collections.singleton(((JSONLocation) backReference).getEntity()));
            } else {
                assertNotNull(null, INVALID_INLINE_ENTITY_MISSING + "Location");
            }

            return self;
        case PATCH:
            parseReferencedFrom();
            self.setId(identifier);
            self.setTime(parseTime(time));

            if (Thing != null) {
                self.setThing(Thing.parseToDTO(JSONBase.EntityType.REFERENCE));
            }

            if (Locations != null) {
                self.setLocations(Arrays.stream(Locations)
                                        .map(loc -> loc.parseToDTO(JSONBase.EntityType.REFERENCE))
                                        .collect(Collectors.toSet()));
            }

            return self;
        case REFERENCE:
            assertIsNull(time, INVALID_REFERENCED_ENTITY);
            assertIsNull(Thing, INVALID_REFERENCED_ENTITY);
            assertIsNull(Locations, INVALID_REFERENCED_ENTITY);

            self.setId(identifier);
            return self;
        default:
            return null;
        }
    }
}
