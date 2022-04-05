/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.data.vanilla.query;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.phenomenon.PhenomenonParameterEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.data.common.query.EntityQuerySpecifications;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservedPropertyQuerySpecifications extends EntityQuerySpecifications<PhenomenonEntity> {

    private static final String IDENTIFIER = "identifier";

    public Specification<PhenomenonEntity> withDatastreamStaIdentifier(final String datastreamStaIdentifier) {
        return (root, query, builder) -> {
            Subquery<PhenomenonEntity> sq = query.subquery(PhenomenonEntity.class);
            Root<AbstractDatasetEntity> datastream = sq.from(AbstractDatasetEntity.class);
            Join<AbstractDatasetEntity, PhenomenonEntity> join =
                datastream.join(AbstractDatasetEntity.PROPERTY_PHENOMENON);
            sq.select(join)
                .where(builder.equal(datastream.get(DescribableEntity.PROPERTY_STA_IDENTIFIER),
                                     datastreamStaIdentifier));
            return builder.in(root).value(sq);
        };
    }

    @Override protected Specification<PhenomenonEntity> handleRelatedPropertyFilter(String propertyName,
                                                                                    Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (StaConstants.DATASTREAMS.equals(propertyName)) {
                Subquery<PhenomenonEntity> sq = query.subquery(PhenomenonEntity.class);
                Root<AbstractDatasetEntity> datastream = sq.from(AbstractDatasetEntity.class);
                final Join<AbstractDatasetEntity, PhenomenonEntity> join =
                    datastream.join(AbstractDatasetEntity.PROPERTY_PHENOMENON, JoinType.INNER);
                sq.select(join)
                    .where(((Specification<AbstractDatasetEntity>) propertyValue).toPredicate(datastream,
                                                                                              query,
                                                                                              builder));
                return builder.in(root).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override protected Specification<PhenomenonEntity> handleDirectPropertyFilter(
        String propertyName,
        Expression<?> propertyValue,
        FilterConstants.ComparisonOperator operator,
        boolean switched) {
        return (Specification<PhenomenonEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        return handleDirectStringPropertyFilter(root.get(PhenomenonEntity.STA_IDENTIFIER),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                false);
                    case StaConstants.PROP_NAME:
                        return handleDirectStringPropertyFilter(root.get(PhenomenonEntity.NAME),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    case StaConstants.PROP_DESCRIPTION:
                        return handleDirectStringPropertyFilter(root.get(PhenomenonEntity.DESCRIPTION),
                                                                propertyValue,
                                                                operator,
                                                                builder,
                                                                switched);
                    case StaConstants.PROP_DEFINITION:
                    case IDENTIFIER:
                        return handleDirectStringPropertyFilter(root.get(PhenomenonEntity.IDENTIFIER),
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
                                                    PhenomenonParameterEntity.PROP_PHENOMENON_ID,
                                                    ParameterFactory.EntityType.PHENOMENON);
                        } else {
                            throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                        }
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_DEFINITION:
                return PhenomenonEntity.PROPERTY_IDENTIFIER;
            case IDENTIFIER:
                return PhenomenonEntity.STA_IDENTIFIER;
            default:
                return super.checkPropertyName(property);
        }
    }

}
