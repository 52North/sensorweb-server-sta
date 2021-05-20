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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.api.dto.ObservationRelationDTO;
import org.n52.sta.api.dto.impl.citsci.ObservationRelation;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONObservationRelation extends JSONBase.JSONwithId<ObservationRelationDTO>
    implements AbstractJSONEntity {

    public String name;
    public String description;
    public String role;
    public String namespace;

    @JsonManagedReference
    public JSONObservationGroup[] group;

    @JsonManagedReference
    public JSONObservation subject;

    @JsonManagedReference
    public JSONObservation object;

    public JSONObservationRelation() {
        self = new ObservationRelation();
    }

    @Override public ObservationRelationDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                Assert.notNull(role, INVALID_INLINE_ENTITY_MISSING + "role");
                Assert.notNull(namespace, INVALID_INLINE_ENTITY_MISSING + "namespace");
                Assert.isNull(object, INVALID_INLINE_ENTITY_MISSING + "object");
                Assert.isNull(subject, INVALID_INLINE_ENTITY_MISSING + "subject");

                return createEntity();
            case PATCH:
                parseReferencedFrom();
                return createEntity();

            case REFERENCE:
                Assert.isNull(group, INVALID_REFERENCED_ENTITY);
                Assert.isNull(object, INVALID_REFERENCED_ENTITY);
                Assert.isNull(subject, INVALID_REFERENCED_ENTITY);
                Assert.isNull(type, INVALID_REFERENCED_ENTITY);
                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }

    private ObservationRelationDTO createEntity() {
        self.setId(identifier);
        self.setDescription(description);
        self.setNamespace(namespace);
        self.setRole(role);

        if (object != null) {
            self.setObject(object.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONObservation) {
            self.setObject(((JSONObservation) backReference).getEntity());
        }

        if (subject != null) {
            self.setSubject(subject.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        }

        if (group != null) {
            Set<ObservationGroupDTO> groups = new HashSet<>();
            for (JSONObservationGroup jsonObservationGroup : group) {
                groups.add(jsonObservationGroup.parseToDTO(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setObservationGroups(groups);
        }
        return self;
    }

}
