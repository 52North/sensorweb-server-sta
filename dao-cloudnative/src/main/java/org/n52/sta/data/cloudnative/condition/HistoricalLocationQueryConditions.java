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
import org.jooq.impl.DSL;

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
public class HistoricalLocationQueryConditions extends EntityQueryConditions {

    public Condition withLocationStaIdentifier(final String locationIdentifier) {
        // Join and condition
        return DSL.exists(
                ctx.select(LOCATION.LOCATION_ID)
                        .from(LOCATION_HISTORICAL_LOCATION)
                        .join(LOCATION)
                        .onKey()
                        .where(LOCATION.STA_IDENTIFIER.eq(locationIdentifier))
                        .and(HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID
                                .eq(LOCATION_HISTORICAL_LOCATION.FK_HISTORICAL_LOCATION_ID))
        );
    }

    public Condition withThingStaIdentifier(final String thingIdentifier) {
        return DSL.exists(
                ctx.select(THING.PLATFORM_ID)
                        .from(THING)
                        .where(THING.STA_IDENTIFIER.eq(thingIdentifier))
                        .and(HISTORICAL_LOCATION.FK_PLATFORM_ID.eq(THING.PLATFORM_ID))
        );
    }

    @Override
    public Condition withStaIdentifier(String staIdentifier) {
        return HISTORICAL_LOCATION.STA_IDENTIFIER.eq(staIdentifier);
    }

    @Override
    public Condition withStaIdentifier(List<String> identifiers) {
        return HISTORICAL_LOCATION.STA_IDENTIFIER.in(identifiers);
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
                            HISTORICAL_LOCATION.STA_IDENTIFIER,
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_TIME:
                    return handleDirectDateTimePropertyFilter(
                            HISTORICAL_LOCATION.TIME,
                            propertyValue,
                            operator
                    );
                default:
                    throw new RuntimeException("Error getting filter for Property: \"" + propertyName
                            + "\". No such property in Entity.");
            }
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Condition handleRelatedPropertyFilter(String propertyName,
                                                    Condition propertyValue) {

        if (STAEntityDefinition.THING.equals(propertyName)) {
            return HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID.in(
                    ctx
                    .select(HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID)
                    .from(HISTORICAL_LOCATION)
                    .join(THING)
                    .onKey()
                    .where(propertyValue)
            );
        } else if (STAEntityDefinition.LOCATIONS.equals(propertyName)) {
            return HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID.in(
                    ctx
                    .select(HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID)
                    .from(LOCATION_HISTORICAL_LOCATION)
                    .join(HISTORICAL_LOCATION)
                    .onKey()
                    .join(LOCATION)
                    .onKey()
                    .where(propertyValue)
            );
        } else {
            throw new RuntimeException("Could not find related property: " + propertyName);
        }
    }

    @Override
    public Field<?> checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return StaEntity.alias(HISTORICAL_LOCATION, HISTORICAL_LOCATION.STA_IDENTIFIER);
            case StaConstants.PROP_TIME:
                return StaEntity.alias(HISTORICAL_LOCATION, HISTORICAL_LOCATION.TIME);
            default:
                // TODO:
                return null;
        }
    }
}
