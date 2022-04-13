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

package org.n52.sta.plus.serialize.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.serdes.common.AbstractJSONEntity;
import org.n52.sta.api.serdes.common.JSONBase;
import org.n52.sta.api.serdes.json.JSONObservation;

import java.util.HashSet;
import java.util.Set;
import org.n52.sta.plus.dto.GroupDTO;
import org.n52.sta.plus.dto.impl.License;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONLicense extends JSONBase.JSONwithId<License> implements AbstractJSONEntity {

    public String name;
    public String description;
    public String definition;
    public String logo;

    @JsonManagedReference
    @JsonProperty(StaConstants.OBSERVATIONS)
    public JSONObservation[] Observations;

    @JsonManagedReference
    @JsonProperty(StaConstants.GROUPS)
    public JSONGroup[] Groups;

    public JSONLicense() {
        self = new License();
    }

    @Override public License parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                assertNotNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                assertNotNull(definition, INVALID_INLINE_ENTITY_MISSING + "definition");

                return createEntity();
            case PATCH:
                parseReferencedFrom();
                return createEntity();
            case REFERENCE:
                assertIsNull(name, INVALID_REFERENCED_ENTITY);
                assertIsNull(definition, INVALID_REFERENCED_ENTITY);
                assertIsNull(logo, INVALID_REFERENCED_ENTITY);
                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }

    private License createEntity() {
        self.setId(identifier);
        self.setName(name);
        self.setDefinition(definition);
        self.setDescription(description);

        if (logo != null) {
            self.setLogo(logo);
        }

        Set<ObservationDTO> datastreams = new HashSet<>();
        if (Observations != null) {
            for (JSONObservation datastream : Observations) {
                datastreams.add(datastream.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setObservations(datastreams);
        }

        Set<GroupDTO> group = new HashSet<>();
        if (Observations != null) {
            for (JSONGroup g : Groups) {
                group.add(g.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setObservationGroups(group);
        }

        return self;
    }
}

