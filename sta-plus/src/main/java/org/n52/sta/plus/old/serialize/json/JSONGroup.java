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

package org.n52.sta.plus.old.serialize.json;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.old.serialize.common.AbstractJSONEntity;
import org.n52.sta.api.old.serialize.common.JSONBase;
import org.n52.sta.plus.old.dto.Group;
import org.n52.sta.plus.old.entity.GroupDTO;
import org.n52.sta.plus.old.entity.PlusObservationDTO;
import org.n52.sta.plus.old.entity.RelationDTO;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({
    "NM_FIELD_NAMING_CONVENTION",
    "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
})
public class JSONGroup extends JSONBase.JSONwithIdNameDescriptionTime<GroupDTO>
        implements
        AbstractJSONEntity {

    public String name;
    public String description;
    public String purpose;
    public String creationTime;
    public String endTime;
    public ObjectNode properties;

    @JsonManagedReference
    @JsonProperty(StaConstants.LICENSE)
    public JSONLicense license;

    @JsonManagedReference
    @JsonProperty(StaConstants.RELATIONS)
    public JSONRelation[] relations;

    @JsonManagedReference
    @JsonProperty(StaConstants.OBSERVATIONS)
    public JSONPlusObservation[] observations;

    public JSONGroup() {
        self = new Group();
    }

    @Override
    public GroupDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
        case FULL:
            parseReferencedFrom();
            assertNotNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
            assertNotNull(description, INVALID_INLINE_ENTITY_MISSING + "description");

            return createEntity();
        case PATCH:
            parseReferencedFrom();
            return createEntity();

        case REFERENCE:
            assertIsNull(name, INVALID_REFERENCED_ENTITY);
            assertIsNull(description, INVALID_REFERENCED_ENTITY);
            assertIsNull(relations, INVALID_REFERENCED_ENTITY);
            self.setId(identifier);
            return self;
        default:
            return null;
        }
    }

    private GroupDTO createEntity() {
        self.setId(identifier);
        self.setName(name);
        self.setDescription(description);

        if (properties != null) {
            self.setProperties(properties);
        }
        if (creationTime != null) {
            self.setCreationTime(parseTime(creationTime));
        }
        if (endTime != null) {
            self.setEndTime(parseTime(endTime));
        }

        if (relations != null) {
            Set<RelationDTO> related = new HashSet<>();
            for (JSONRelation observation : relations) {
                related.add(observation.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setRelations(related);
        } else if (backReference instanceof JSONRelation) {
            Set<RelationDTO> related = new HashSet<>();
            related.add(((JSONRelation) backReference).getEntity());
            self.setRelations(related);
        }

        if (observations != null) {
            Set<PlusObservationDTO> obs = new HashSet<>();
            for (JSONPlusObservation observation : observations) {
                obs.add(observation.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setObservations(obs);
        }

        if (license != null) {
            self.setLicense(license.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        }

        return self;
    }
}
