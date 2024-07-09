package org.n52.sta.data.cndao.condition;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ThingQueryConditions extends EntityQueryConditions {

    public Condition withLocationStaIdentifier(final String locationIdentifier) {
        // Join and condition
        return DSL.exists(
                dsl.selectOne()
                        .from(table(THING_LOCATION_TABLE))
                        .join(table(THING_TABLE))
                        .on(field(THING_LOCATION_TABLE + "." + FK_THING_ID_FIELD)
                                .eq(field(THING_TABLE + "." + THING_ID_FIELD)))
                        .join(table(LOCATION_TABLE))
                        .on(field(THING_LOCATION_TABLE + "." + FK_LOCATION_ID_FIELD)
                                .eq(field(LOCATION_TABLE + "." + LOCATION_ID_FIELD)))
                        .where(field(LOCATION_TABLE + "." + STA_IDENTIFIER_FIELD).eq(locationIdentifier))
        );
    }

    public Condition withHistoricalLocationStaIdentifier(final String historicalIdentifier) {
        return DSL.exists(
                dsl.selectOne()
                        .from(table(THING_TABLE))
                        .join(table(HISTORICAL_LOCATION_TABLE))
                        .on(field(THING_TABLE + "." + THING_ID_FIELD)
                                .eq(field(HISTORICAL_LOCATION_TABLE + "." + FK_THING_ID_FIELD)))
                        .where(field(HISTORICAL_LOCATION_TABLE + "." + STA_IDENTIFIER_FIELD).eq(historicalIdentifier))
        );
    }

    public Condition withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return DSL.exists(
                dsl.selectOne()
                        .from(table(THING_TABLE))
                        .join(table(DATASTREAM_TABLE))
                        .on(field(THING_TABLE + "." + THING_ID_FIELD)
                                .eq(field(DATASTREAM_TABLE + "." + FK_THING_ID_FIELD)))
                        .where(field(DATASTREAM_TABLE + "." + STA_IDENTIFIER_FIELD).eq(datastreamIdentifier))
        );
    }

    @Override
    protected Condition handleDirectPropertyFilter(String propertyName,
                                                   Field<? extends Comparable> propertyValue,
                                                   FilterConstants.ComparisonOperator operator,
                                                   boolean switched) {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        // check if propertyValue is of type String
                        return handleDirectStringPropertyFilter(field(STA_IDENTIFIER_FIELD, String.class),
                                propertyValue.cast(String.class),
                                operator,
                                false);
                    case StaConstants.PROP_NAME:
                        return handleDirectStringPropertyFilter(field(STA_NAME_FIELD, String.class),
                                propertyValue.cast(String.class),
                                operator,
                                switched);
                    case StaConstants.PROP_DESCRIPTION:
                        return handleDirectStringPropertyFilter(field(STA_DESCRIPTION_FIELD, String.class),
                                propertyValue.cast(String.class),
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
    protected Condition handleRelatedPropertyFilter(String propertyName, Condition propertyValue)
            throws STAInvalidFilterExpressionException {

        SelectConditionStep<Record1<Object>> subquery;

        switch (propertyName) {
            case StaConstants.DATASTREAMS: {

                subquery = dsl.select(field(DATASTREAM_TABLE + "." + FK_THING_ID_FIELD))
                        .from(table(DATASTREAM_TABLE))
                        .where(propertyValue);

                return field(THING_ID_FIELD).in(subquery);
            }
            case StaConstants.LOCATIONS: {
                subquery = dsl.select(field(THING_LOCATION_TABLE + '.' + FK_THING_ID_FIELD))
                        .from(table(THING_LOCATION_TABLE))
                        .join(table(LOCATION_TABLE))
                        .on(field(THING_LOCATION_TABLE + "." + FK_LOCATION_ID_FIELD)
                                .eq(field(LOCATION_TABLE + "." + LOCATION_ID_FIELD)))
                        .where(propertyValue);

                return  field(THING_ID_FIELD).in(subquery);
            }
            case StaConstants.HISTORICAL_LOCATIONS:
                subquery = dsl.select(field(HISTORICAL_LOCATION_TABLE + '.' + FK_THING_ID_FIELD))
                        .from(table(HISTORICAL_LOCATION_TABLE))
                        .join(table(THING_TABLE))
                        .on(field(THING_TABLE + "." + THING_ID_FIELD)
                                .eq(field(HISTORICAL_LOCATION_TABLE + "." + FK_THING_ID_FIELD)))
                        .where(propertyValue);

                return field(THING_ID_FIELD).in(subquery);
            default:
                throw new STAInvalidFilterExpressionException(
                        "Could not find related property: " + propertyName);
        }
    }
}
