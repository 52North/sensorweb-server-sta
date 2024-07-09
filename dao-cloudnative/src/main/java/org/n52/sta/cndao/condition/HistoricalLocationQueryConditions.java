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
package org.n52.sta.cndao.condition;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import java.util.Date;
import static org.jooq.impl.DSL.*;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class HistoricalLocationQueryConditions extends EntityQueryConditions{

    public Condition withLocationStaIdentifier(final String locationIdentifier) {
        // Join and condition
        return DSL.exists(
                dsl.selectOne()
                        .from(table(LOCATION_HISTORICAL_LOCATION_TABLE))
                        .innerJoin(table(HISTORICAL_LOCATION_TABLE))
                        .onKey()
                        .innerJoin(table(LOCATION_TABLE))
                        .onKey()
                        .where(field(name(LOCATION_TABLE, STA_IDENTIFIER_FIELD)).eq(locationIdentifier))
        );
    }

    public Condition withThingStaIdentifier(final String thingIdentifier) {
        return DSL.exists(
                dsl.selectOne()
                        .from(table(HISTORICAL_LOCATION_TABLE))
                        .innerJoin(table(THING_TABLE))
                        .onKey()
                        .where(field(name(THING_TABLE, THING_ID_FIELD))
                                .eq(thingIdentifier))
        );
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
                            field(STA_IDENTIFIER_FIELD, String.class),
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_TIME:
                    return handleDirectDateTimePropertyFilter(
                            field(HISTORICAL_LOCATION_TIME_FIELD, Date.class),
                            propertyValue,
                            operator);
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

        SelectConditionStep<Record1<Object>> subquery;
        if (THING.equals(propertyName)) {
            subquery = dsl
                    .select(field(name(HISTORICAL_LOCATION_TABLE, HISTORICAL_LOCATION_ID_FIELD)))
                    .from(table(HISTORICAL_LOCATION_TABLE))
                    .innerJoin(table(THING_TABLE))
                    .onKey()
                    .where(propertyValue);

            return field(HISTORICAL_LOCATION_ID_FIELD).in(subquery);
        } else if (LOCATIONS.equals(propertyName)) {
            subquery = dsl
                    .select(field(name(LOCATION_HISTORICAL_LOCATION_TABLE, FK_HISTORICAL_LOCATION_ID_FIELD)))
                    .from(table(LOCATION_HISTORICAL_LOCATION_TABLE))
                    .innerJoin(table(LOCATION_TABLE))
                    .onKey()
                    .where(propertyValue);

            return field(HISTORICAL_LOCATION_ID_FIELD).in(subquery);
        } else {
            throw new RuntimeException("Could not find related property: " + propertyName);
        }
    }
}
