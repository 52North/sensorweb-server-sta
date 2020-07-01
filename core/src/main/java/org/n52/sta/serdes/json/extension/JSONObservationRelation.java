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

package org.n52.sta.serdes.json.extension;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.series.db.beans.sta.mapped.extension.ObservationRelation;
import org.n52.sta.serdes.json.AbstractJSONEntity;
import org.n52.sta.serdes.json.JSONBase;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONObservationRelation extends JSONBase.JSONwithId<ObservationRelation> implements AbstractJSONEntity {

    public String type;

    @JsonManagedReference
    public JSONObservationGroup Group;

    @JsonManagedReference
    public JSONCSObservation Observation;

    public JSONObservationRelation() {
        self = new ObservationRelation();
    }

    @Override public ObservationRelation toEntity(JSONBase.EntityType type) {
        switch (type) {
        case FULL:
            parseReferencedFrom();
            // This might be set via backreference
            // Assert.notNull(Observation, INVALID_INLINE_ENTITY_MISSING + "Observation");
            // Assert.notNull(Group, INVALID_INLINE_ENTITY_MISSING + "Group");
            Assert.notNull(type, INVALID_INLINE_ENTITY_MISSING + "type");

            return createPostEntity();
        case PATCH:
            parseReferencedFrom();
            throw new RuntimeException("PATCH not implemented yet!");
            // return self;

        case REFERENCE:
            Assert.isNull(Group, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Observation, INVALID_REFERENCED_ENTITY);
            Assert.isNull(type, INVALID_REFERENCED_ENTITY);
            self.setStaIdentifier(identifier);
            self.setIdentifier(identifier);
            return self;
        default:
            return null;
        }
    }

    private ObservationRelation createPostEntity() {
        self.setStaIdentifier(identifier);
        self.setIdentifier(identifier);
        self.setType(type);

        if (Observation != null) {
            self.setObservation(Observation.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONCSObservation) {
            self.setObservation(((JSONCSObservation) backReference).getEntity());
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
