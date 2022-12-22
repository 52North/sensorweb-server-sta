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
package org.n52.sta.data.entity;

import org.n52.series.db.beans.sta.RelationEntity;
import org.n52.sta.api.domain.TargetReference;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.Relation;
import org.n52.sta.config.EntityPropertyMapping;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RelationData extends StaData<RelationEntity> implements Relation {

    public RelationData(RelationEntity dataEntity, Optional<EntityPropertyMapping> propertyMapping) {
        super(dataEntity, propertyMapping);
     // propertyMapping is required for Object
        propertyMapping.orElseThrow(() -> new RuntimeException("no property mapping supplied!"));
    }

    @Override
    public String getRole() {
        return data.getRole();
    }

    @Override
    public String getDescription() {
        return data.getDescription();
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(data.getParameters());
    }

    @Override
    public Observation getSubject() {
        return new ObservationData(data.getSubject(), propertyMapping);
    }

    @Override
    public TargetReference getObject() {
        if (data.isSetExternalObject()) {
            return TargetReference.objectExternal(data.getExternalObject());
        } else {
            return TargetReference.objectInternal(new ObservationData(data.getObject(), propertyMapping));
        }
    }

    @Override
    public Set<Group> getGroups() {
        return toSet(data.getGroups(), groupEntity -> new GroupData(groupEntity, propertyMapping));
    }

}
