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

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.shetland.ogc.sta.StaConstants;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Subquery;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ThingQuerySpecification implements BaseQuerySpecifications<PlatformEntity> {

    private final Map<String, MemberFilter<PlatformEntity>> filterByMember;

    private final Map<String, PropertyComparator<PlatformEntity, ?>> entityPathByProperty;

    public ThingQuerySpecification() {
        this.filterByMember = new HashMap<>();
        this.filterByMember.put(StaConstants.DATASTREAMS, new DatastreamFilter());
        this.filterByMember.put(StaConstants.HISTORICAL_LOCATIONS, new HistoricalLocationsFilter());
        this.filterByMember.put(StaConstants.LOCATIONS, new LocationsFilter());

        this.entityPathByProperty = new HashMap<>();
        this.entityPathByProperty.put(StaConstants.PROP_ID,
                createStringComparator(DescribableEntity.PROPERTY_STA_IDENTIFIER));
        this.entityPathByProperty.put(StaConstants.PROP_NAME, createStringComparator(HasName.PROPERTY_NAME));
        this.entityPathByProperty.put(StaConstants.PROP_DESCRIPTION,
                createStringComparator(HasDescription.PROPERTY_DESCRIPTION));
    }

    public Specification<PlatformEntity> withMemberStaIdentifier(String member, String staIdentifier) {
        return (root, query, builder) -> {
            String property = DescribableEntity.STA_IDENTIFIER;
            return builder.equal(root.join(member, JoinType.INNER).get(property), staIdentifier);
        };
    }

    @Override
    public Specification<PlatformEntity> compareProperty(String property, ComparisonOperator operator,
            Expression<?> rightExpr) throws SpecificationsException {
        assertAvailableProperty(property);
        PropertyComparator<PlatformEntity, ?> comparator = entityPathByProperty.get(property);
        return comparator.compareToRight(rightExpr, operator);
    }

    @Override
    public Specification<PlatformEntity> compareProperty(Expression<?> leftExpr, ComparisonOperator operator,
            String property) throws SpecificationsException {
        assertAvailableProperty(property);
        PropertyComparator<PlatformEntity, ?> comparator = entityPathByProperty.get(property);
        return comparator.compareToLeft(leftExpr, operator);
    }

    @Override
    public Specification<PlatformEntity> applyOnMember(String member, Specification<?> specification)
            throws SpecificationsException {
        assertAvailableMember(member);
        MemberFilter<PlatformEntity> filter = filterByMember.get(member);
        return filter.apply(specification);
    }

    private void assertAvailableMember(String member) throws SpecificationsException {
        if (!filterByMember.containsKey(member)) {
            throw new SpecificationsException("Thing has no member '" + member + "'");
        }
    }

    private void assertAvailableProperty(String property) throws SpecificationsException {
        if (!entityPathByProperty.containsKey(property)) {
            throw new SpecificationsException("Thing has no property '" + property + "'");
        }
    }

    private PropertyComparator<PlatformEntity, String> createStringComparator(String entityPath) {
        return new PropertyComparator<>(entityPath);
    }

    // TODO discuss: split multiple (tiny) subqueries so that we are able to use
    // kind of a DSL query language

    private final class DatastreamFilter implements MemberFilter<PlatformEntity> {

        private final Function<Specification<?>, Specification<PlatformEntity>> queryApplier;

        private DatastreamFilter() {
            this.queryApplier = this::prepareQuery;
        }

        public Specification<PlatformEntity> apply(Specification<?> datastreamSpecification) {
            return queryApplier.apply(datastreamSpecification);
        }

        private Specification<PlatformEntity> prepareQuery(Specification<?> specification) {
            return (root, query, builder) -> {
                MemberQuery memberQuery = createMemberQuery(AbstractDatasetEntity.PROPERTY_PLATFORM,
                        AbstractDatasetEntity.class);
                Subquery<?> subquery = memberQuery.create(specification, query, builder);
                // 1..n
                return builder.in(root.get(IdEntity.PROPERTY_ID)).value(subquery);
            };
        }

    }

    private final class HistoricalLocationsFilter implements MemberFilter<PlatformEntity> {

        private final Function<Specification<?>, Specification<PlatformEntity>> queryApplier;

        private HistoricalLocationsFilter() {
            this.queryApplier = this::prepareQuery;
        }

        public Specification<PlatformEntity> apply(Specification<?> locationsSpecification) {
            return queryApplier.apply(locationsSpecification);
        }

        private Specification<PlatformEntity> prepareQuery(Specification<?> specification) {
            return (root, query, builder) -> {
                MemberQuery memberQuery = createMemberQuery(IdEntity.PROPERTY_ID, HistoricalLocationEntity.class);
                Subquery<?> subquery = memberQuery.create(specification, query, builder);
                // m..n
                Join<?, ?> join = root.join(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS, JoinType.INNER);
                return builder.in(join.get(IdEntity.PROPERTY_ID)).value(subquery);
            };
        }

    }

    private final class LocationsFilter implements MemberFilter<PlatformEntity> {

        private final Function<Specification<?>, Specification<PlatformEntity>> queryApplier;

        private LocationsFilter() {
            this.queryApplier = this::prepareQuery;
        }

        public Specification<PlatformEntity> apply(Specification<?> locationsSpecification) {
            return queryApplier.apply(locationsSpecification);
        }

        private Specification<PlatformEntity> prepareQuery(Specification<?> specification) {
            return (root, query, builder) -> {
                MemberQuery memberQuery = createMemberQuery(IdEntity.PROPERTY_ID, LocationEntity.class);
                Subquery<?> subquery = memberQuery.create(specification, query, builder);
                // m..n
                Join<?, ?> join = root.join(PlatformEntity.PROPERTY_LOCATIONS, JoinType.INNER);
                return builder.in(join.get(IdEntity.PROPERTY_ID)).value(subquery);
            };
        }

    }

}