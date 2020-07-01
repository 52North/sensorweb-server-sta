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

import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ParameterQuerySpecifications extends EntityQuerySpecifications<ParameterEntity<?>> {

    @Override protected Specification<ParameterEntity<?>> handleRelatedPropertyFilter(String propertyName,
                                                                                      Specification<?> propertyValue)
            throws STAInvalidFilterExpressionException {
        throw new STAInvalidFilterExpressionException("Parameters do not have related properties");
    }

    @Override protected Specification<ParameterEntity<?>> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<ParameterEntity<?>>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case StaConstants.PROP_NAME: {
                    return handleDirectStringPropertyFilter(
                            root.get(ParameterEntity.NAME),
                            propertyValue,
                            operator,
                            builder,
                            false);
                }
                case "value": {
                    return handleDirectStringPropertyFilter(
                            root.get(ParameterEntity.PROPERTY_VALUE_JSON),
                            propertyValue,
                            operator,
                            builder,
                            false);
                }

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
