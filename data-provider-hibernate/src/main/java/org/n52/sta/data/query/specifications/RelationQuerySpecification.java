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
package org.n52.sta.data.query.specifications;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.sta.GroupEntity;
import org.n52.series.db.beans.sta.RelationEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.data.query.specifications.BaseQuerySpecifications.EntityQuery;
import org.n52.sta.data.query.specifications.BaseQuerySpecifications.MemberFilter;
import org.n52.sta.data.query.specifications.util.SimplePropertyComparator;

public class RelationQuerySpecification extends QuerySpecification<RelationEntity> {

    public RelationQuerySpecification() {
        super();
        this.filterByMember.put(StaConstants.GROUPS, createGroupsFilter());
        this.filterByMember.put(StaConstants.SUBJECTS, createSubjectFilter());
        this.filterByMember.put(StaConstants.OBJECTS, createObjectFilter());

        this.entityPathByProperty.put(StaConstants.PROP_NAME, new SimplePropertyComparator<>(HasName.PROPERTY_NAME));
        this.entityPathByProperty.put(StaConstants.PROP_DESCRIPTION,
                new SimplePropertyComparator<>(HasDescription.PROPERTY_DESCRIPTION));
        this.entityPathByProperty.put(StaConstants.PROP_EXTERNAL_OBJECT,
                new SimplePropertyComparator<>(RelationEntity.PROPERTY_EXTERNAL_OBJECT));
        this.entityPathByProperty.put(StaConstants.PROP_ROLE,
                new SimplePropertyComparator<>(RelationEntity.PROPERTY_ROLE));
    }

    private MemberFilter<RelationEntity> createGroupsFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, GroupEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join< ? , ? > join = root.join(RelationEntity.PROPERTY_GROUPS, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID))
                          .value(subquery);

        };
    }

    private MemberFilter<RelationEntity> createSubjectFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID,
                                                  DataEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // n..1
            return builder.in(subquery)
                          .value(root.get(RelationEntity.PROPERTY_SUBJECT));

        };
    }

    private MemberFilter<RelationEntity> createObjectFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID,
                    DataEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // n..1
            return builder.in(subquery)
                          .value(root.get(RelationEntity.PROPERTY_OBJECT));

        };
    }


    // TODO discuss: split multiple (tiny) subqueries so that we are able to use
    // kind of a DSL query language



}
