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

import org.n52.sta.api.old.serialize.common.AbstractJSONEntity;
import org.n52.sta.api.old.serialize.common.JSONBase;
import org.n52.sta.api.old.serialize.json.JSONObservation;
import org.n52.sta.plus.dto.Relation;
import org.n52.sta.plus.entity.GroupDTO;
import org.n52.sta.plus.entity.RelationDTO;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONRelation extends JSONBase.JSONwithId<RelationDTO>
    implements AbstractJSONEntity {

    public String name;
    public String description;
    public String role;
    public String externalObject;

    @JsonManagedReference
    public JSONGroup[] ObservationGroups;

    @JsonManagedReference
    public JSONObservation Subject;

    @JsonManagedReference
    public JSONObservation Object;

    public JSONRelation() {
        self = new Relation();
    }

    @Override public RelationDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                assertNotNull(role, INVALID_INLINE_ENTITY_MISSING + "role");
                assertNotNull(Subject, INVALID_INLINE_ENTITY_MISSING + "subject");

                return createEntity();
            case PATCH:
                parseReferencedFrom();
                return createEntity();

            case REFERENCE:
                assertIsNull(ObservationGroups, INVALID_REFERENCED_ENTITY);
                assertIsNull(Object, INVALID_REFERENCED_ENTITY);
                assertIsNull(Subject, INVALID_REFERENCED_ENTITY);
                assertIsNull(type, INVALID_REFERENCED_ENTITY);
                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }

    private RelationDTO createEntity() {
        self.setId(identifier);
        self.setDescription(description);
        self.setExternalObject(externalObject);
        self.setRole(role);

        if (Object != null) {
            self.setObject(Object.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        }

        if (Subject != null) {
            self.setSubject(Subject.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        }

        if (ObservationGroups != null) {
            Set<GroupDTO> groups = new HashSet<>();
            for (JSONGroup jsonGroup : ObservationGroups) {
                groups.add(jsonGroup.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setObservationGroups(groups);
        }
        return self;
    }

}
