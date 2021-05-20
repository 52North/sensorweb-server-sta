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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.api.dto.ObservationRelationDTO;
import org.n52.sta.api.dto.impl.citsci.ObservationGroup;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONObservationGroup extends JSONBase.JSONwithIdNameDescriptionTime<ObservationGroupDTO>
    implements AbstractJSONEntity {

    public String name;
    public String description;
    public String purpose;
    public String runtime;
    public String created;
    public ObjectNode properties;

    @JsonManagedReference
    @JsonProperty(StaConstants.OBSERVATION_RELATIONS)
    public JSONObservationRelation[] relations;

    @JsonManagedReference
    @JsonProperty(StaConstants.OBSERVATIONS)
    public JSONObservation[] observations;

    public JSONObservationGroup() {
        self = new ObservationGroup();
    }

    @Override public ObservationGroupDTO parseToDTO(JSONBase.EntityType type) {
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

    private ObservationGroupDTO createEntity() {
        self.setId(identifier);
        self.setName(name);
        self.setDescription(description);

        if (properties != null) {
            self.setProperties(properties);
        }
        if (runtime != null) {
            self.setRuntime(parseTime(runtime));
        }
        if (created != null) {
            self.setCreated(parseTime(created));
        }

        if (relations != null) {
            Set<ObservationRelationDTO> related = new HashSet<>();
            for (JSONObservationRelation observation : relations) {
                related.add(observation.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setObservationRelations(related);
        } else if (backReference instanceof JSONObservationRelation) {
            Set<ObservationRelationDTO> related = new HashSet<>();
            related.add(((JSONObservationRelation) backReference).getEntity());
            self.setObservationRelations(related);
        }
        return self;
    }
}

