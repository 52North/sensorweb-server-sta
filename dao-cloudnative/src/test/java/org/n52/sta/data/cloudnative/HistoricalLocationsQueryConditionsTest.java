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
package org.n52.sta.data.cloudnative;

import org.jooq.DSLContext;
import org.jooq.Condition;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.n52.series.db.common.Utils;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.condition.HistoricalLocationQueryConditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class HistoricalLocationsQueryConditionsTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private HistoricalLocationQueryConditions historicalLocationQueryConditions;

    @BeforeEach
    public void setUp() {
        historicalLocationQueryConditions = new HistoricalLocationQueryConditions();
        historicalLocationQueryConditions.setDslContext(ctx);
    }

    private String read_parquet(String table) {
        return String.format("read_parquet('s3://52n-sta/%s') %s ", table, table);
    }

    @Test
    public void testWithLocationStaIdentifier() {
        final String locationStaIdentifier = "LOCATION123";

        Condition result = historicalLocationQueryConditions.withLocationStaIdentifier(locationStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select LOCATION.LOCATION_ID " +
                        "from " +
                        read_parquet("location_historical_location".toUpperCase()) +
                        "join " +
                        read_parquet("historical_location".toUpperCase()) +
                        "on " +
                        "location_historical_location.fk_historical_location_id = ".toUpperCase() +
                        "historical_location.historical_location_id ".toUpperCase() +
                        "join " +
                        read_parquet("location".toUpperCase()) +
                        "on " +
                        "location_historical_location.fk_location_id = ".toUpperCase() +
                        "location.location_id ".toUpperCase() +
                        "where " +
                        "location.sta_identifier = '%s')".toUpperCase(),
                locationStaIdentifier
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithThingStaIdentifier() {
        final String thingStaIdentifier = "THING123";

        Condition result = historicalLocationQueryConditions.withThingStaIdentifier(thingStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select PLATFORM.PLATFORM_ID " +
                        "from " +
                        read_parquet("HISTORICAL_LOCATION") +
                        "join " +
                        read_parquet("PLATFORM") +
                        "on " +
                        "historical_location.fk_platform_id = platform.platform_id ".toUpperCase() +
                        "where " +
                        "platform.sta_identifier = '%s')".toUpperCase(),
                thingStaIdentifier
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("historicalLocation123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = historicalLocationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "HISTORICAL_LOCATION.STA_IDENTIFIER = 'historicalLocation123'";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterTime() {
        String propertyName = StaConstants.PROP_TIME;
        Date currentTime = Utils.createUnmutableTimestamp(new Date());
        Field<Date> propertyValue = DSL.val(currentTime);
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = historicalLocationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format("HISTORICAL_LOCATION.time = timestamp '%s'", currentTime.toString());

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Location() {
        String propertyName = STAEntityDefinition.LOCATIONS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try  {
            result = historicalLocationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID in " +
                "(select " +
                "HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID " +
                "from " +
                read_parquet("LOCATION_HISTORICAL_LOCATION") +
                "join " +
                read_parquet("HISTORICAL_LOCATION") +
                "on " +
                "location_historical_location.fk_historical_location_id = ".toUpperCase() +
                "historical_location.historical_location_id ".toUpperCase() +
                "join " +
                read_parquet("LOCATION") +
                "on " +
                "location_historical_location.fk_location_id = ".toUpperCase() +
                "location.location_id ".toUpperCase() +
                "where ())";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithRelatedPropertyFilter_Thing() {
        String propertyName = StaConstants.THING;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try  {
            result = historicalLocationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID in " +
                "(select " +
                "HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID " +
                "from " +
                read_parquet("HISTORICAL_LOCATION") +
                "join " +
                read_parquet("PLATFORM") +
                "on " +
                "historical_location.fk_platform_id = platform.platform_id ".toUpperCase() +
                "where ())";

        Assertions.assertEquals(expectedSQL, sql);

    }

}
