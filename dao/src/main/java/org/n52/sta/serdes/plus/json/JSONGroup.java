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

package org.n52.sta.serdes.plus.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.dto.vanilla.ObservationDTO;
import org.n52.sta.api.dto.plus.GroupDTO;
import org.n52.sta.api.dto.plus.RelationDTO;
import org.n52.sta.api.dto.impl.citsci.Group;
import org.n52.sta.serdes.AbstractJSONEntity;
import org.n52.sta.serdes.JSONBase;
import org.n52.sta.serdes.vanilla.json.JSONObservation;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONGroup extends JSONBase.JSONwithIdNameDescriptionTime<GroupDTO>
    implements AbstractJSONEntity {

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
    public JSONObservation[] observations;

    public JSONGroup() {
        self = new Group();
    }

    @Override public GroupDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                Assert.notNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                Assert.notNull(description, INVALID_INLINE_ENTITY_MISSING + "description");

                return createEntity();
            case PATCH:
                parseReferencedFrom();
                return createEntity();

            case REFERENCE:
                Assert.isNull(name, INVALID_REFERENCED_ENTITY);
                Assert.isNull(description, INVALID_REFERENCED_ENTITY);
                Assert.isNull(relations, INVALID_REFERENCED_ENTITY);
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
            Set<ObservationDTO> obs = new HashSet<>();
            for (JSONObservation observation : observations) {
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

