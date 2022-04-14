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
package org.n52.sta.plus.serialize.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.old.entity.DatastreamDTO;
import org.n52.sta.api.old.serialize.json.JSONDatastream;
import org.n52.sta.api.old.serialize.json.JSONObservedProperty;
import org.n52.sta.api.old.serialize.json.JSONSensor;
import org.n52.sta.api.old.serialize.json.JSONThing;

@SuppressWarnings("checkstyle:VisibilityModifier")
public class JSONPlusDatastream extends JSONDatastream {

    @JsonManagedReference
    public JSONParty Party;
    @JsonManagedReference
    public JSONProject Project;

    public JSONPlusDatastream() {
        //self = new JSONPlusDatastream();
    }

    @Override protected void parseReferencedFrom() {
        if (referencedFromType != null) {
            switch (referencedFromType) {
                case StaConstants.SENSORS:
                    assertIsNull(Sensor, INVALID_DUPLICATE_REFERENCE);
                    this.Sensor = new JSONSensor();
                    this.Sensor.identifier = referencedFromID;
                    return;
                case StaConstants.OBSERVED_PROPERTIES:
                    assertIsNull(ObservedProperty, INVALID_DUPLICATE_REFERENCE);
                    this.ObservedProperty = new JSONObservedProperty();
                    this.ObservedProperty.identifier = referencedFromID;
                    return;
                case StaConstants.THINGS:
                    assertIsNull(Thing, INVALID_DUPLICATE_REFERENCE);
                    this.Thing = new JSONThing();
                    this.Thing.identifier = referencedFromID;
                    return;
                default:
                    throw new IllegalArgumentException(INVALID_BACKREFERENCE);
            }
        }
    }

    protected DatastreamDTO createPostEntity() {
        DatastreamDTO base = super.createPostEntity();

        /*
        if (Party != null) {
            self.setParty(org.n52.sta.api.dto.impl.citsci.Party.parseToDTO(JSONBase.EntityType.FULL,
                                                                           JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONParty) {
            self.setParty(((JSONParty) backReference).self);
        }

        if (Project != null) {
            self.setProject(org.n52.sta.api.dto.impl.citsci.Project.parseToDTO(JSONBase.EntityType.FULL,
                                                                               JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONProject) {
            self.setProject(((JSONProject) backReference).self);
        }
         */

        return self;
    }

    protected DatastreamDTO createPatchEntity() {
        super.createPatchEntity();
        /*
        if (Party != null) {
            self.setParty(org.n52.sta.api.dto.impl.citsci.Party.parseToDTO(JSONBase.EntityType.FULL,
                                                                           JSONBase.EntityType.REFERENCE));
        }

        if (Project != null) {
            self.setProject(org.n52.sta.api.dto.impl.citsci.Project.parseToDTO(JSONBase.EntityType.FULL,
                                                                               JSONBase.EntityType.REFERENCE));
        }
        */
        return self;
    }
}
