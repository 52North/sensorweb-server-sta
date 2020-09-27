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
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.platform.PlatformParameterEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
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
public class ThingQuerySpecifications extends EntityQuerySpecifications<PlatformEntity> {

    public Specification<PlatformEntity> withLocationStaIdentifier(final String locationIdentifier) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, LocationEntity> join =
                root.join(PlatformEntity.PROPERTY_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), locationIdentifier);
        };
    }

    public Specification<PlatformEntity> withHistoricalLocationStaIdentifier(final String historicalIdentifier) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, HistoricalLocationEntity> join =
                root.join(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), historicalIdentifier);
        };
    }

    public Specification<PlatformEntity> withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, AbstractDatasetEntity> join =
                root.join(PlatformEntity.PROPERTY_DATASETS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), datastreamIdentifier);
        };
    }

    @Override protected Specification<PlatformEntity> handleRelatedPropertyFilter(String propertyName,
                                                                                  Specification<?> propertyValue) {
        return (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.DATASTREAMS: {
                        Subquery<AbstractDatasetEntity> subquery = query.subquery(AbstractDatasetEntity.class);
                        Root<AbstractDatasetEntity> datastream = subquery.from(AbstractDatasetEntity.class);
                        subquery.select(datastream.get(AbstractDatasetEntity.PROPERTY_PLATFORM))
                            .where(((Specification<AbstractDatasetEntity>) propertyValue).toPredicate(datastream,
                                                                                                      query,
                                                                                                      builder));
                        return builder.in(root.get(IdEntity.PROPERTY_ID)).value(subquery);
                    }
                    case StaConstants.LOCATIONS: {
                        Subquery<LocationEntity> subquery = query.subquery(LocationEntity.class);
                        Root<LocationEntity> location = subquery.from(LocationEntity.class);
                        subquery.select(location.get(LocationEntity.PROPERTY_ID))
                            .where(((Specification<LocationEntity>) propertyValue).toPredicate(location,
                                                                                               query,
                                                                                               builder));
                        final Join<PlatformEntity, LocationEntity> join =
                            root.join(PlatformEntity.PROPERTY_LOCATIONS, JoinType.INNER);
                        return builder.in(join.get(DescribableEntity.PROPERTY_ID)).value(subquery);
                    }
                    case StaConstants.HISTORICAL_LOCATIONS:
                        Subquery<HistoricalLocationEntity> subquery = query.subquery(HistoricalLocationEntity.class);
                        Root<HistoricalLocationEntity> historicalLocation =
                            subquery.from(HistoricalLocationEntity.class);
                        subquery.select(historicalLocation.get(HistoricalLocationEntity.PROPERTY_ID))
                            .where(((Specification<HistoricalLocationEntity>) propertyValue).toPredicate(
                                historicalLocation,
                                query,
                                builder));
                        final Join<PlatformEntity, HistoricalLocationEntity> join =
                            root.join(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS);
                        return builder.in(join.get(DescribableEntity.PROPERTY_ID)).value(subquery);
                    default:
                        throw new STAInvalidFilterExpressionException(
                            "Could not find related property: " + propertyName);
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override protected Specification<PlatformEntity> handleDirectPropertyFilter(
        String propertyName,
        Expression<?> propertyValue,
        FilterConstants.ComparisonOperator operator,
        boolean switched) {
        return (Specification<PlatformEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        return handleDirectStringPropertyFilter(root.get(PlatformEntity.STA_IDENTIFIER),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                false);
                    case StaConstants.PROP_NAME:
                        return handleDirectStringPropertyFilter(root.get(PlatformEntity.NAME),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    case StaConstants.PROP_DESCRIPTION:
                        return handleDirectStringPropertyFilter(root.get(PlatformEntity.DESCRIPTION),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    default:
                        // We are filtering on variable keys on properties
                        if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                            return handleProperties(root,
                                                    query,
                                                    builder,
                                                    propertyName,
                                                    propertyValue,
                                                    operator,
                                                    switched,
                                                    PlatformParameterEntity.PROP_PLATFORM_ID,
                                                    ParameterFactory.EntityType.PLATFORM);
                        } else {
                            throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                        }
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
