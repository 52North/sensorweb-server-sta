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

import org.jooq.*;
import org.jooq.impl.DSL;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ThingQueryConditions extends EntityQueryConditions {

    public Condition withLocationStaIdentifier(final String locationIdentifier) {
        // Join and condition
        return DSL.exists(
                ctx.select(LOCATION.LOCATION_ID)
                        .from(THING_LOCATION)
                        .join(LOCATION)
                        .onKey()
                        .where(LOCATION.STA_IDENTIFIER.eq(locationIdentifier))
                        .and(THING.PLATFORM_ID.eq(THING_LOCATION.FK_PLATFORM_ID))
        );
    }

    public Condition withHistoricalLocationStaIdentifier(final String historicalIdentifier) {
        return DSL.exists(
                ctx.select(HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID)
                        .from(HISTORICAL_LOCATION)
                        .where(HISTORICAL_LOCATION.STA_IDENTIFIER.eq(historicalIdentifier))
                        .and(THING.PLATFORM_ID.eq(HISTORICAL_LOCATION.FK_PLATFORM_ID))
        );
    }

    public Condition withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return DSL.exists(
                ctx.select(DATASTREAM.DATASET_ID)
                        .from(DATASTREAM)
                        .where(DATASTREAM.STA_IDENTIFIER.eq(datastreamIdentifier))
                        .and(THING.PLATFORM_ID.eq(DATASTREAM.FK_PLATFORM_ID))
        );
    }

    @Override
    public Condition withStaIdentifier(String staIdentifier) {
        return THING.STA_IDENTIFIER.eq(staIdentifier);
    }

    @Override
    public Condition withStaIdentifier(List<String> identifiers) {
        return THING.STA_IDENTIFIER.in(identifiers);
    }

    @Override
    protected  <T extends Comparable<? super T>> Condition handleDirectPropertyFilter(
            String propertyName,
            Field<T> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        try {
            switch (propertyName) {
                case StaConstants.PROP_ID:
                    // check if propertyValue is of type String
                    return handleDirectStringPropertyFilter(
                            THING.STA_IDENTIFIER,
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            THING.NAME,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            THING.DESCRIPTION,
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
                                THING_PROPERTIES.FK_PLATFORM_ID,
                                ParameterFactory.EntityType.PLATFORM);
                    } else {
                        throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                    }
            }
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Condition handleRelatedPropertyFilter(String propertyName, Condition propertyValue) {
        try {
            SelectConditionStep<Record1<Long>> subquery;
            switch (propertyName) {
                case STAEntityDefinition.DATASTREAMS: {
                    subquery = ctx
                            .select(DATASTREAM.FK_PLATFORM_ID)
                            .from(DATASTREAM)
                            .where(propertyValue);

                    return THING.PLATFORM_ID.in(subquery);
                }
                case STAEntityDefinition.LOCATIONS: {
                    subquery = ctx
                            .select(THING_LOCATION.FK_PLATFORM_ID)
                            .from(THING_LOCATION)
                            .join(LOCATION)
                            .onKey()
                            .where(propertyValue);

                    return THING.PLATFORM_ID.in(subquery);
                }
                case STAEntityDefinition.HISTORICAL_LOCATIONS:
                    subquery = ctx
                            .select(HISTORICAL_LOCATION.FK_PLATFORM_ID)
                            .from(HISTORICAL_LOCATION)
                            .join(THING)
                            .onKey()
                            .where(propertyValue);

                    return THING.PLATFORM_ID.in(subquery);
                default:
                    throw new STAInvalidFilterExpressionException(
                            "Could not find related property: " + propertyName);
            }
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Field<?> checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return StaEntity.alias(THING, THING.STA_IDENTIFIER);
            case StaConstants.PROP_NAME:
                return StaEntity.alias(THING, THING.NAME);
            case StaConstants.PROP_DESCRIPTION:
                return StaEntity.alias(THING, THING.DESCRIPTION);
            case StaConstants.PROP_PROPERTIES:
                // TODO:
                return null;
            default:
                return null;
        }
    }


}
