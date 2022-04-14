/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.old.entity.DatastreamDTO;
import org.n52.sta.api.old.serialize.common.AbstractJSONEntity;
import org.n52.sta.api.old.serialize.common.JSONBase;
import org.n52.sta.api.old.serialize.json.JSONDatastream;
import org.n52.sta.plus.dto.Party;
import org.n52.sta.plus.entity.PartyDTO;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONParty extends JSONBase.JSONwithId<PartyDTO> implements AbstractJSONEntity {

    public String authId;
    public String displayName;
    public String description;
    public PartyDTO.Role role;
    public ObjectNode properties;

    @JsonManagedReference
    @JsonProperty(StaConstants.DATASTREAMS)
    public JSONDatastream[] datastreams;

    public JSONParty() {
        self = new Party();
    }

    @Override public PartyDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                assertNotNull(authId, INVALID_INLINE_ENTITY_MISSING + "authId");
                assertNotNull(role, INVALID_INLINE_ENTITY_MISSING + "role");

                return createEntity();
            case PATCH:
                parseReferencedFrom();
                return createEntity();

            case REFERENCE:
                assertIsNull(displayName, INVALID_REFERENCED_ENTITY);
                assertIsNull(role, INVALID_REFERENCED_ENTITY);
                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }

    private PartyDTO createEntity() {
        self.setId(identifier);
        self.setRole(role);
        self.setAuthId(authId);
        self.setDescription(description);

        if (displayName != null) {
            self.setDisplayName(displayName);
        }

        Set<DatastreamDTO> related = new HashSet<>();
        if (datastreams != null) {
            for (JSONDatastream datastream : datastreams) {
                related.add(datastream.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setDatastreams(related);
        } else if (backReference instanceof JSONDatastream) {
            related.add(((JSONDatastream) backReference).getEntity());
            self.setDatastreams(related);
        }

        return self;
    }
}
