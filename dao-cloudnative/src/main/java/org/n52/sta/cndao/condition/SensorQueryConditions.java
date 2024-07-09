package org.n52.sta.data.cndao.condition;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class SensorQueryConditions extends EntityQueryConditions {

    public Condition withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return DSL.exists(
                dsl.selectOne()
                        .from(table(DATASTREAM_TABLE))
                        .join(table(SENSOR_TABLE))
                        .on(field(DATASTREAM_TABLE + "." + FK_SENSOR_ID_FIELD)
                                .eq(field(SENSOR_TABLE + "." + SENSOR_ID_FIELD)))
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
                case "format":
                case StaConstants.PROP_ENCODINGTYPE:
                    Condition subCondition = handleDirectStringPropertyFilter(
                            field(FORMAT_TABLE + "." + STA_DEFINITION_FIELD, String.class),
                            propertyValue.cast(String.class),
                            operator,
                            switched);
                    SelectConditionStep<Record1<Object>> subquery = dsl
                            .select(field(SENSOR_TABLE + '.' + SENSOR_ID_FIELD))
                            .from(table(SENSOR_TABLE))
                            .join(table(FORMAT_TABLE))
                            .on(field(FORMAT_TABLE + "." + FORMAT_ID_FIELD)
                                    .eq(field(SENSOR_TABLE + "." + FK_FORMAT_ID_FIELD)))
                            .where(subCondition);

                    return field(SENSOR_ID_FIELD).in(subquery);
                case StaConstants.PROP_METADATA:
                    return handleDirectStringPropertyFilter(field(SENSOR_METADATA_FIELD, String.class),
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
                                SENSOR_ID_FIELD,
                                ParameterFactory.EntityType.PROCEDURE);
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
                subquery = dsl.select(field(DATASTREAM_TABLE + "." + FK_SENSOR_ID_FIELD))
                        .from(table(DATASTREAM_TABLE))
                        .where(propertyValue);

                return field(SENSOR_ID_FIELD).in(subquery);
            }
            default:
                throw new STAInvalidFilterExpressionException("Could not find related property: " + propertyName);
        }
    }
    @Override
    public String checkPropertyName(String property) {
        switch (property) {
//            case StaConstants.PROP_ENCODINGTYPE:
//                return STA_DEFINITION_FIELD;
            case StaConstants.PROP_METADATA:
                return SENSOR_METADATA_FIELD;
            default:
                return super.checkPropertyName(property);
        }
    }
}
