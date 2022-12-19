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

package org.n52.sta.api.domain.aggregate;

import java.util.Map;
import java.util.Set;

import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.domain.TargetReference;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.Relation;

public class RelationAggregate extends EntityAggregate<Relation> implements Relation {

    private final Relation relation;

    public RelationAggregate(Relation relation) {
        this(relation, null);
    }

    public RelationAggregate(Relation relation, EntityEditor<Relation> editor) {
        super(relation, editor);
        this.relation = relation;
    }

    public String getId() {
        return relation.getId();
    }


    public String getDescription() {
        return relation.getDescription();
    }

    public Map<String, Object> getProperties() {
        return relation.getProperties();
    }

    @Override
    public String getRole() {
        return relation.getRole();
    }

    @Override
    public Observation getSubject() {
       return relation.getSubject();
    }

    @Override
    public TargetReference getObject() {
        return relation.getObject();
    }

    @Override
    public Set<Group> getGroups() {
        return relation.getGroups();
    }


}
