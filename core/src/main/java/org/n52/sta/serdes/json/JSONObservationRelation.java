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
import org.n52.series.db.beans.sta.ObservationRelationEntity;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONObservationRelation extends JSONBase.JSONwithId<ObservationRelationEntity>
    implements AbstractJSONEntity {

    public String type;

    @JsonManagedReference
    public JSONObservationGroup Group;

    @JsonManagedReference
    public JSONObservation Observation;

    public JSONObservationRelation() {
        self = new ObservationRelationEntity();
    }

    @Override public ObservationRelationEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                Assert.notNull(type, INVALID_INLINE_ENTITY_MISSING + "type");

                return createPostEntity();
            case PATCH:
                parseReferencedFrom();
                return createPatchEntity();

            case REFERENCE:
                Assert.isNull(Group, INVALID_REFERENCED_ENTITY);
                Assert.isNull(Observation, INVALID_REFERENCED_ENTITY);
                Assert.isNull(type, INVALID_REFERENCED_ENTITY);
                self.setStaIdentifier(identifier);
                return self;
            default:
                return null;
        }
    }

    private ObservationRelationEntity createPatchEntity() {
        self.setStaIdentifier(identifier);
        self.setType(type);

        if (Observation != null) {
            self.setObservation(Observation.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONObservation) {
            self.setObservation(((JSONObservation) backReference).getEntity());
        }

        if (Group != null) {
            self.setGroup(Group.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONObservationGroup) {
            self.setGroup(((JSONObservationGroup) backReference).getEntity());
        }
        return self;
    }

    private ObservationRelationEntity createPostEntity() {
        self.setStaIdentifier(identifier);
        self.setType(type);

        if (Observation != null) {
            self.setObservation(Observation.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONObservation) {
            self.setObservation(((JSONObservation) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "Observation");
        }

        if (Group != null) {
            self.setGroup(Group.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONObservationGroup) {
            self.setGroup(((JSONObservationGroup) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "Group");
        }

        return self;
    }

}