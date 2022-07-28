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

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.data.query.specifications.util.SimplePropertyComparator;

public class ThingQuerySpecification extends QuerySpecification<PlatformEntity> {

    public ThingQuerySpecification() {
        super();
        this.filterByMember.put(StaConstants.DATASTREAMS, createDatastreamFilter());
        this.filterByMember.put(StaConstants.HISTORICAL_LOCATIONS, createHistoricalLocationFilter());
        this.filterByMember.put(StaConstants.LOCATIONS, createLocationFilter());

        this.entityPathByProperty.put(StaConstants.PROP_NAME, new SimplePropertyComparator<>(HasName.PROPERTY_NAME));
        this.entityPathByProperty.put(StaConstants.PROP_DESCRIPTION,
                                      new SimplePropertyComparator<>(HasDescription.PROPERTY_DESCRIPTION));
    }

    // TODO discuss: split multiple (tiny) subqueries so that we are able to use
    // kind of a DSL query language

    private MemberFilter<PlatformEntity> createDatastreamFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(AbstractDatasetEntity.PROPERTY_PLATFORM,
                                                  AbstractDatasetEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // 1..n
            return builder.in(root.get(IdEntity.PROPERTY_ID))
                          .value(subquery);

        };
    }

    private MemberFilter<PlatformEntity> createHistoricalLocationFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, HistoricalLocationEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join< ? , ? > join = root.join(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID))
                          .value(subquery);

        };
    }

    private MemberFilter<PlatformEntity> createLocationFilter() {
        return specification -> (root, query, builder) -> {
            EntityQuery memberQuery = createQuery(IdEntity.PROPERTY_ID, LocationEntity.class);
            Subquery< ? > subquery = memberQuery.create(specification, query, builder);
            // m..n
            Join< ? , ? > join = root.join(PlatformEntity.PROPERTY_LOCATIONS, JoinType.INNER);
            return builder.in(join.get(IdEntity.PROPERTY_ID))
                          .value(subquery);

        };
    }

}
