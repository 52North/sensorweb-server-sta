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

package org.n52.sta.plus.persistence.query;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.sta.GroupEntity;
import org.n52.series.db.beans.sta.RelationEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.sta.data.old.query.EntityQuerySpecifications;
import org.n52.sta.data.old.query.ObservationQuerySpecifications;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings("checkstyle:linelength")
public class StaPlusObservationQuerySpecifications extends EntityQuerySpecifications<DataEntity< ? >> {

    private ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();

    public static Specification<DataEntity< ? >> withObservationGroupStaIdentifier(
            final String obsGroupIdentifier) {
        return (root, query, builder) -> {
            final Join<DataEntity, GroupEntity> join = root.join(DataEntity.PROPERTY_GROUPS,
                                                                        JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), obsGroupIdentifier);
        };
    }

    public static Specification<DataEntity< ? >> withObservationRelationStaIdentifierAsSubject(
            final String relationIdentifier) {
        return (root, query, builder) -> {
            final Join<DataEntity, RelationEntity> join = root.join(DataEntity.PROPERTY_SUBJECTS,
                                                                           JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), relationIdentifier);
        };
    }

    public static Specification<DataEntity< ? >> withObservationRelationStaIdentifierAsObject(
            final String relationIdentifier) {
        return (root, query, builder) -> {
            final Join<DataEntity, RelationEntity> join = root.join(DataEntity.PROPERTY_OBJECTS,
                                                                           JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), relationIdentifier);
        };
    }

    @Override
    protected Specification<DataEntity< ? >> handleRelatedPropertyFilter(String propertyName,
            Specification< ? > propertyValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Specification<DataEntity< ? >> handleDirectPropertyFilter(String propertyName,
            Expression< ? > propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        throw new UnsupportedOperationException();
    }
}
