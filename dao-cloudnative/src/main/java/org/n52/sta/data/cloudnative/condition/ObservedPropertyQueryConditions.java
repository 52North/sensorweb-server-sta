/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.data.cloudnative.condition;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ObservedPropertyQueryConditions extends EntityQueryConditions {

    private static final String IDENTIFIER = "identifier";

    public Condition withDatastreamStaIdentifier(final String datastreamStaIdentifier) {
        return DSL.exists(
                ctx.select(DATASTREAM.DATASET_ID)
                        .from(DATASTREAM)
                        .join(OBSERVED_PROPERTY)
                        .onKey()
                        .where(DATASTREAM.STA_IDENTIFIER.eq(datastreamStaIdentifier))
        );
    }

    @Override
    protected Condition handleRelatedPropertyFilter(String propertyName, Condition propertyValue) {
        try {
            SelectConditionStep<Record1<Long>> subquery;
            switch (propertyName) {
                case StaConstants.DATASTREAMS: {
                    subquery = ctx
                            .select(DATASTREAM.FK_PHENOMENON_ID)
                            .from(DATASTREAM)
                            .where(propertyValue);

                    return OBSERVED_PROPERTY.PHENOMENON_ID.in(subquery);
                }
                default:
                    throw new STAInvalidFilterExpressionException("Could not find related property: " + propertyName);
            }
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Condition withStaIdentifier(String staIdentifier) {
        return OBSERVATION.STA_IDENTIFIER.eq(staIdentifier);
    }

    @Override
    public Condition withStaIdentifier(List<String> identifiers) {
        return OBSERVATION.STA_IDENTIFIER.in(identifiers);
    }

    public Condition withName(String name) {
        return OBSERVED_PROPERTY.NAME.eq(name);
    }

    @Override
    protected  <T extends Comparable<? super T>> Condition handleDirectPropertyFilter(String propertyName,
                                                   Field<T> propertyValue,
                                                   FilterConstants.ComparisonOperator operator,
                                                   boolean switched) {
        try {
            switch (propertyName) {
                case StaConstants.PROP_ID:
                    return handleDirectStringPropertyFilter(
                            OBSERVED_PROPERTY.STA_IDENTIFIER,
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            OBSERVED_PROPERTY.NAME,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            OBSERVED_PROPERTY.DESCRIPTION,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DEFINITION:
                case IDENTIFIER:
                    return handleDirectStringPropertyFilter(
                            OBSERVED_PROPERTY.IDENTIFIER,
                            propertyValue,
                            operator,
                            switched);
                default:
                    // We are filtering on variable keys on properties
                    if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                        return handleProperties(
                                propertyName,
                                propertyValue,
                                operator,
                                switched,
                                OBSERVED_PROPERTY_PROPERTIES.FK_PHENOMENON_ID,
                                ParameterFactory.EntityType.PHENOMENON);
                    } else {
                        throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                    }
            }
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Field<?> checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return OBSERVED_PROPERTY.STA_IDENTIFIER;
            case StaConstants.PROP_DEFINITION:
                return OBSERVED_PROPERTY.IDENTIFIER;
            case StaConstants.PROP_NAME:
                return OBSERVED_PROPERTY.NAME;
            case StaConstants.PROP_DESCRIPTION:
                return OBSERVED_PROPERTY.DESCRIPTION;
            case StaConstants.PROP_PROPERTIES:
                // TODO:
                return null;
            default:
                return null;
        }
    }
}
