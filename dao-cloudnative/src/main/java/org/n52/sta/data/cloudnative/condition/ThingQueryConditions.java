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

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class ThingQueryConditions extends EntityQueryConditions {

    public Condition withLocationStaIdentifier(final String locationIdentifier) {
        // Join and condition
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(THING_LOCATION_TABLE))
                        .join(DSL.table(THING_TABLE))
                        .on(DSL.field(DSL.name(THING_TABLE, THING_ID_FIELD))
                                .eq(DSL.field(DSL.name(THING_LOCATION_TABLE, FK_THING_ID_FIELD))))
                        .join(DSL.table(LOCATION_TABLE))
                        .on(DSL.field(DSL.name(LOCATION_TABLE, LOCATION_ID_FIELD))
                                .eq(DSL.field(DSL.name(THING_LOCATION_TABLE, FK_LOCATION_ID_FIELD))))
                        .where(DSL.field(DSL.name(LOCATION_TABLE, STA_IDENTIFIER_FIELD))
                                .eq(locationIdentifier))
        );
    }

    public Condition withHistoricalLocationStaIdentifier(final String historicalIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(THING_TABLE))
                        .join(DSL.table(HISTORICAL_LOCATION_TABLE))
                        .on(DSL.field(DSL.name(THING_TABLE, THING_ID_FIELD))
                                .eq(DSL.field(DSL.name(HISTORICAL_LOCATION_TABLE, FK_THING_ID_FIELD))))
                        .where(DSL.field(DSL.name(HISTORICAL_LOCATION_TABLE, STA_IDENTIFIER_FIELD))
                                .eq(historicalIdentifier))
        );
    }

    public Condition withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(THING_TABLE))
                        .join(DSL.table(DATASTREAM_TABLE))
                        .on(DSL.field(DSL.name(THING_TABLE, THING_ID_FIELD))
                                .eq(DSL.field(DSL.name(DATASTREAM_TABLE, FK_THING_ID_FIELD))))
                        .where(DSL.field(DSL.name(DATASTREAM_TABLE, STA_IDENTIFIER_FIELD)).eq(datastreamIdentifier))
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
                    // check if propertyValue is of type String
                    return handleDirectStringPropertyFilter(DSL.field(STA_IDENTIFIER_FIELD, String.class),
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(DSL.field(STA_NAME_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(DSL.field(STA_DESCRIPTION_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                default:
                    // We are filtering on variable keys on properties
                    if (propertyName.startsWith(STA_PROPERTIES_FIELD)) {
                        return handleProperties(
                                propertyName,
                                propertyValue,
                                operator,
                                switched,
                                FK_THING_ID_FIELD,
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
            SelectConditionStep<Record1<Object>> subquery;
            switch (propertyName) {
                case DATASTREAMS: {

                    subquery = ctx.select(DSL.field(DSL.name(DATASTREAM_TABLE, FK_THING_ID_FIELD)))
                            .from(DSL.table(DATASTREAM_TABLE))
                            .where(propertyValue);

                    return DSL.field(THING_ID_FIELD).in(subquery);
                }
                case LOCATIONS: {
                    subquery = ctx.select(DSL.field(DSL.name(THING_LOCATION_TABLE, FK_THING_ID_FIELD)))
                            .from(DSL.table(THING_LOCATION_TABLE))
                            .join(DSL.table(LOCATION_TABLE))
                            .on(DSL.field(DSL.name(THING_LOCATION_TABLE, FK_LOCATION_ID_FIELD))
                                    .eq(DSL.field(DSL.name(LOCATION_TABLE, LOCATION_ID_FIELD))))
                            .where(propertyValue);

                    return DSL.field(THING_ID_FIELD).in(subquery);
                }
                case HISTORICAL_LOCATIONS:
                    subquery = ctx.select(DSL.field(DSL.name(HISTORICAL_LOCATION_TABLE, FK_THING_ID_FIELD)))
                            .from(DSL.table(HISTORICAL_LOCATION_TABLE))
                            .join(DSL.table(THING_TABLE))
                            .on(DSL.field(DSL.name(THING_TABLE, THING_ID_FIELD))
                                    .eq(DSL.field(DSL.name(HISTORICAL_LOCATION_TABLE, FK_THING_ID_FIELD))))
                            .where(propertyValue);

                    return DSL.field(THING_ID_FIELD).in(subquery);
                default:
                    throw new STAInvalidFilterExpressionException(
                            "Could not find related property: " + propertyName);
            }
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }


}
