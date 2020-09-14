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

package org.n52.sta.data.query;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class PartyQuerySpecifications extends EntityQuerySpecifications<PartyEntity> {

    public Specification<PartyEntity> withDatastreamStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<PartyEntity, AbstractDatasetEntity> join =
                root.join(PartyEntity.PROPERTY_DATASTREAMS, JoinType.INNER);
            return builder.equal(join.get(PartyEntity.STA_IDENTIFIER), identifier);
        };
    }

    @Override
    protected Specification<PartyEntity> handleRelatedPropertyFilter(String propertyName,
                                                                     Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (StaConstants.DATASTREAMS.equals(propertyName)) {
                Subquery<PartyEntity> sq = query.subquery(PartyEntity.class);
                Root<AbstractDatasetEntity> datastream = sq.from(AbstractDatasetEntity.class);
                sq.select(datastream.get(AbstractDatasetEntity.PROPERTY_PARTY))
                    .where(((Specification<AbstractDatasetEntity>) propertyValue).toPredicate(datastream,
                                                                                              query,
                                                                                              builder));
                return builder.in(root.get(PartyEntity.ID)).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override protected Specification<PartyEntity> handleDirectPropertyFilter(
        String propertyName,
        Expression<?> propertyValue,
        FilterConstants.ComparisonOperator operator,
        boolean switched) {
        return (Specification<PartyEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        return handleDirectStringPropertyFilter(root.get(PartyEntity.STA_IDENTIFIER),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                false);
                    case StaConstants.PROP_NICKNAME:
                        return handleDirectStringPropertyFilter(root.get(PartyEntity.PROPERTY_NICKNAME),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    case StaConstants.PROP_ROLE:
                        return handleDirectStringPropertyFilter(root.get(PartyEntity.PROPERTY_ROLE),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    default:
                        throw new RuntimeException("Error getting filter for Property: \"" + propertyName
                                                       + "\". No such property in Entity.");

                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
