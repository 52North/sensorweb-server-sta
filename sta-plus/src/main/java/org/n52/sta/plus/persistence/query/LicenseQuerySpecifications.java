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
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.sta.plus.GroupEntity;
import org.n52.series.db.beans.sta.plus.LicenseEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.data.old.query.EntityQuerySpecifications;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class LicenseQuerySpecifications extends EntityQuerySpecifications<LicenseEntity> {

    public Specification<LicenseEntity> withObservationGroupStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            Subquery<GroupEntity> subquery = query.subquery(GroupEntity.class);
            Root<GroupEntity> obs = subquery.from(GroupEntity.class);
            subquery.select(obs.get(GroupEntity.PROPERTY_LICENSE)
                               .get(DescribableEntity.PROPERTY_ID))
                    .where(builder.equal(obs.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), identifier));
            return builder.equal(root.get(DescribableEntity.PROPERTY_ID), subquery);
        };
    }

    @Override
    protected Specification<LicenseEntity> handleRelatedPropertyFilter(String propertyName,
            Specification< ? > propertyValue) {
        return (root, query, builder) -> {
            /*
             * if (StaConstants.DATASTREAMS.equals(propertyName)) { Subquery<LicenseEntity> sq =
             * query.subquery(LicenseEntity.class); Root<AbstractDatasetEntity> datastream =
             * sq.from(AbstractDatasetEntity.class);
             * sq.select(datastream.get(StaPlusDataEntity.PROPERTY_LICENSE))
             * .where(((Specification<AbstractDatasetEntity>) propertyValue).toPredicate(datastream, query,
             * builder)); return builder.in(root.get(AbstractDatasetEntity.PROPERTY_ID)).value(sq); } else {
             * throw new RuntimeException("Could not find related property: " + propertyName); }
             */
            throw new RuntimeException("Could not find related property: " + propertyName);
        };
    }

    @Override
    protected Specification<LicenseEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression< ? > propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<LicenseEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        return handleDirectStringPropertyFilter(root.get(LicenseEntity.STA_IDENTIFIER),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                false);
                    case StaConstants.PROP_NAME:
                        return handleDirectStringPropertyFilter(root.get(LicenseEntity.NAME),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    case StaConstants.PROP_DEFINITION:
                        return handleDirectStringPropertyFilter(root.get(LicenseEntity.PROPERTY_DEFINITION),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    case StaConstants.PROP_LOGO:
                        return handleDirectStringPropertyFilter(root.get(LicenseEntity.PROPERTY_LOGO),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    default:
                        throw new RuntimeException("Error getting filter for Property: \""
                                + propertyName
                                + "\". No such property in Entity.");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
