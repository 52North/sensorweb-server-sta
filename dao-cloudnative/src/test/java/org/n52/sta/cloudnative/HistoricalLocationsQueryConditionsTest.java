package org.n52.sta.cloudnative;

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
import org.n52.sta.cloudnative.condition.EntityQueryConstants;
import org.n52.sta.cloudnative.condition.HistoricalLocationQueryConditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnativedao")
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

    @Test
    public void testWithLocationStaIdentifier() {
        final String locationStaIdentifier = "location123";

        Condition result = historicalLocationQueryConditions.withLocationStaIdentifier(locationStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select 1 one " +
                        "from location_historical_location " +
                        "join historical_location " +
                        "on " +
                        "location_historical_location.fk_historical_location_id = " +
                        "historical_location.historical_location_id " +
                        "join location " +
                        "on " +
                        "location_historical_location.fk_location_id = " +
                        "location.location_id " +
                        "where location.sta_identifier = '%s')",
                locationStaIdentifier
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithThingStaIdentifier() {
        final String thingStaIdentifier = "thing123";

        Condition result = historicalLocationQueryConditions.withThingStaIdentifier(thingStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select 1 one " +
                        "from historical_location " +
                        "join platform " +
                        "on " +
                        "historical_location.fk_platform_id = platform.platform_id " +
                        "where platform.sta_identifier = '%s')",
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
        String expectedSQL = "sta_identifier = cast('historicalLocation123' as varchar)";

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
        String expectedSQL = String.format("time = cast(timestamp '%s' as timestamp)", currentTime.toString());

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Location() {
        String propertyName = EntityQueryConstants.LOCATIONS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try  {
            result = historicalLocationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "historical_location_id in " +
                "(select " +
                "historical_location.historical_location_id " +
                "from location_historical_location " +
                "join historical_location " +
                "on " +
                "location_historical_location.fk_historical_location_id = " +
                "historical_location.historical_location_id " +
                "join location " +
                "on " +
                "location_historical_location.fk_location_id = " +
                "location.location_id " +
                "where ())";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithRelatedPropertyFilter_Thing() {
        String propertyName = EntityQueryConstants.THING;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try  {
            result = historicalLocationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "historical_location_id in " +
                "(select " +
                "historical_location.historical_location_id " +
                "from historical_location " +
                "join platform " +
                "on " +
                "historical_location.fk_platform_id = platform.platform_id " +
                "where ())";

        Assertions.assertEquals(expectedSQL, sql);

    }

}
