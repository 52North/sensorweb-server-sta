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
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.sta.GroupEntity;
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.series.db.beans.sta.RelationEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.data.query.specifications.util.SimplePropertyComparator;
import org.n52.sta.data.query.specifications.util.TimePropertyComparator;

public class GroupQuerySpecification extends QuerySpecification<GroupEntity> {

    public GroupQuerySpecification() {
        super();
        this.filterByMember.put(StaConstants.OBSERVATIONS, createObservationFilter());
        this.filterByMember.put(StaConstants.RELATIONS, createRelationFilter());
        this.filterByMember.put(StaConstants.LICENSES, createLicenseFilter());
        this.filterByMember.put(StaConstants.PARTIES, createPartyFilter());

        this.entityPathByProperty.put(StaConstants.PROP_NAME, new SimplePropertyComparator<>(HasName.PROPERTY_NAME));
        this.entityPathByProperty.put(StaConstants.PROP_DESCRIPTION,
                new SimplePropertyComparator<>(HasDescription.PROPERTY_DESCRIPTION));
         this.entityPathByProperty.put(StaConstants.PROP_PURPOSE, new
         SimplePropertyComparator<>(GroupEntity.PROPERTY_PURPOSE));
         this.entityPathByProperty.put(StaConstants.PROP_CREATION_TIME, new
         SimplePropertyComparator<>(GroupEntity.PROPERTY_CREATION_TIME));
         this.entityPathByProperty.put(StaConstants.PROP_RUNTIME,
                 new TimePropertyComparator<>(
                         GroupEntity.PROPERTY_RUN_TIME_START,
                         GroupEntity.PROPERTY_RUN_TIME_ENDT));
    }

    // TODO discuss: split multiple (tiny) subqueries so that we are able to use
    // kind of a DSL query language

    private MemberFilter<GroupEntity> createObservationFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, DataEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join< ? , ? > join = root.join(GroupEntity.PROPERTY_OBSERVATIONS, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID))
                          .value(subquery);
        };
    }

    private MemberFilter<GroupEntity> createRelationFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, RelationEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join< ? , ? > join = root.join(GroupEntity.PROPERTY_RELATIONS, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID))
                          .value(subquery);

        };
    }

    private MemberFilter<GroupEntity> createLicenseFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, LicenseEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join< ? , ? > join = root.join(GroupEntity.PROPERTY_LICENSE, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID))
                          .value(subquery);

        };
    }

    private MemberFilter<GroupEntity> createPartyFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, PartyEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join< ? , ? > join = root.join(GroupEntity.PROPERTY_PARTY, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID))
                          .value(subquery);

        };
    }

}
