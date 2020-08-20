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
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class SensorQuerySpecifications extends EntityQuerySpecifications<ProcedureEntity> {

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
        case StaConstants.PROP_ENCODINGTYPE:
            return ProcedureEntity.PROPERTY_PROCEDURE_DESCRIPTION_FORMAT;
        case StaConstants.PROP_METADATA:
            // TODO: Add sorting by HistoricalLocation that replaces Description if it is not present
            return "descriptionFile";
        default:
            return super.checkPropertyName(property);
        }
    }

    public Specification<ProcedureEntity> withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            Subquery<ProcedureEntity> sq = query.subquery(ProcedureEntity.class);
            Root<AbstractDatasetEntity> datastream = sq.from(AbstractDatasetEntity.class);
            Join<AbstractDatasetEntity, ProcedureEntity> join = datastream.join(AbstractDatasetEntity.PROCEDURE);
            sq.select(join)
              .where(builder.equal(datastream.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), datastreamIdentifier));
            return builder.in(root).value(sq);
        };
    }

    @Override protected Specification<ProcedureEntity> handleRelatedPropertyFilter(String propertyName,
                                                                                   Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (StaConstants.DATASTREAMS.equals(propertyName)) {
                Subquery<ProcedureEntity> sq = query.subquery(ProcedureEntity.class);
                Root<AbstractDatasetEntity> datastream = sq.from(AbstractDatasetEntity.class);
                sq.select(datastream.get(AbstractDatasetEntity.PROCEDURE))
                  .where(((Specification<AbstractDatasetEntity>) propertyValue).toPredicate(datastream,
                                                                                            query,
                                                                                            builder));
                return builder.in(root.get(DescribableEntity.PROPERTY_ID)).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override protected Specification<ProcedureEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<ProcedureEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case StaConstants.PROP_ID:
                    return handleDirectStringPropertyFilter(root.get(ProcedureEntity.STA_IDENTIFIER),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(root.get(ProcedureEntity.NAME),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(root.get(ProcedureEntity.DESCRIPTION),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case "format":
                case StaConstants.PROP_ENCODINGTYPE:
                    Join<ProcedureEntity, FormatEntity> join =
                            root.join(ProcedureEntity.PROPERTY_PROCEDURE_DESCRIPTION_FORMAT);
                    return handleDirectStringPropertyFilter(join.get(FormatEntity.FORMAT),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case StaConstants.PROP_METADATA:
                    return handleDirectStringPropertyFilter(root.get(ProcedureEntity.PROPERTY_DESCRIPTION_FILE),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                default:
                    throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
