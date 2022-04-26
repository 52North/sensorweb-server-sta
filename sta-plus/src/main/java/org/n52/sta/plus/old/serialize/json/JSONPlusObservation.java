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

import org.n52.sta.api.old.serialize.common.AbstractJSONEntity;
import org.n52.sta.api.old.serialize.common.JSONBase;
import org.n52.sta.api.old.serialize.common.JSONBase.EntityType;
import org.n52.sta.api.old.serialize.json.JSONObservation;
import org.n52.sta.plus.old.dto.PlusObservation;
import org.n52.sta.plus.old.entity.GroupDTO;
import org.n52.sta.plus.old.entity.PlusObservationDTO;
import org.n52.sta.plus.old.entity.RelationDTO;

@SuppressWarnings("VisibilityModifier")
public class JSONPlusObservation extends JSONBase.JSONwithIdTime<PlusObservationDTO> implements AbstractJSONEntity {

    @JsonManagedReference
    public JSONGroup[] ObservationGroups;

    @JsonManagedReference
    public JSONRelation[] Subjects;

    @JsonManagedReference
    public JSONRelation[] Objects;

    @JsonManagedReference
    public JSONLicense License;

    @Override
    protected void parseReferencedFrom() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public PlusObservationDTO parseToDTO(EntityType type) {
        JSONObservation base = new JSONObservation();
        PlusObservation observation = new PlusObservation(base.parseToDTO(type));

        switch (type) {
            case FULL:
                parseReferencedFrom();
                completePostEntity(observation);
                return observation;
            case PATCH:
                parseReferencedFrom();
                completePatchEntity(observation);
                return observation;
            case REFERENCE:
                // assertIsNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
                // assertIsNull(resultTime, INVALID_REFERENCED_ENTITY);
                // assertIsNull(result, INVALID_REFERENCED_ENTITY);
                // assertIsNull(resultTime, INVALID_REFERENCED_ENTITY);
                // assertIsNull(resultQuality, INVALID_REFERENCED_ENTITY);
                // assertIsNull(parameters, INVALID_REFERENCED_ENTITY);
                // observation.setId(identifier);
                // return observation;
                throw new IllegalArgumentException("REFERENCE not implemented yet");
            default:
                return null;
        }
    }

    private void completePatchEntity(PlusObservation observation) {

        // if (License != null) {
        // self.setLicense(License.parseToDTO(JSONBase.EntityType.REFERENCE));
        // }

    }

    private void completePostEntity(PlusObservation observation) {
        if (Subjects != null) {
            Set<RelationDTO> subjects = new HashSet<>();
            for (JSONRelation sub : Subjects) {
                subjects.add(sub.parseToDTO(JSONBase.EntityType.FULL,
                        JSONBase.EntityType.REFERENCE));
            }
            observation.setSubjects(subjects);
        }

        if (Objects != null) {
            Set<RelationDTO> objects = new HashSet<>();
            for (JSONRelation obj : Objects) {
                objects.add(obj.parseToDTO(JSONBase.EntityType.FULL,
                        JSONBase.EntityType.REFERENCE));
            }
            observation.setObjects(objects);
        }

        if (ObservationGroups != null) {
            Set<GroupDTO> objects = new HashSet<>();
            for (JSONGroup obj : ObservationGroups) {
                objects.add(obj.parseToDTO(JSONBase.EntityType.FULL,
                        JSONBase.EntityType.REFERENCE));
            }
            observation.setObservationGroups(objects);
        }

        if (License != null) {
            observation.setLicense(License.parseToDTO(JSONBase.EntityType.FULL,
                    JSONBase.EntityType.REFERENCE));
        }
    }

}
