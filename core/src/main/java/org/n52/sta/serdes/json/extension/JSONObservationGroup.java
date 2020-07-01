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
import org.n52.series.db.beans.sta.mapped.extension.ObservationGroup;
import org.n52.series.db.beans.sta.mapped.extension.ObservationRelation;
import org.n52.sta.serdes.json.AbstractJSONEntity;
import org.n52.sta.serdes.json.JSONBase;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class JSONObservationGroup extends JSONBase.JSONwithIdNameDescription<ObservationGroup>
        implements AbstractJSONEntity {

    @JsonManagedReference
    public JSONObservationRelation[] Relations;

    public JSONObservationGroup() {
        self = new ObservationGroup();
    }

    @Override public ObservationGroup toEntity(JSONBase.EntityType type) {
        switch (type) {
        case FULL:
            parseReferencedFrom();
            Assert.notNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
            // This might be set via backreference
            // Assert.notNull(Relations, INVALID_INLINE_ENTITY_MISSING + "observations");

            return createPostEntity();
        case PATCH:
            parseReferencedFrom();
            throw new RuntimeException("PATCH not implemented yet!");
            // return self;

        case REFERENCE:
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Relations, INVALID_REFERENCED_ENTITY);
            self.setStaIdentifier(identifier);
            self.setIdentifier(identifier);
            return self;
        default:
            return null;
        }
    }

    private ObservationGroup createPostEntity() {
        self.setStaIdentifier(identifier);
        self.setIdentifier(identifier);
        self.setName(name);
        self.setDescription(description);

        if (Relations != null) {
            Set<ObservationRelation> related = new HashSet<>();
            for (JSONObservationRelation observation : Relations) {
                related.add(observation.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setEntities(related);
        } else if (backReference instanceof JSONObservationRelation) {
            Set<ObservationRelation> related = new HashSet<>();
            related.add(((JSONObservationRelation) backReference).getEntity());
            self.setEntities(related);
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "Relations");
        }

        return self;
    }
}

