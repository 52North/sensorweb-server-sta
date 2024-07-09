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

import org.jooq.*;
import org.jooq.impl.DSL;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;

import static org.jooq.impl.DSL.*;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class SensorQueryConditions extends EntityQueryConditions {

    public Condition withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return DSL.exists(
                dsl.selectOne()
                        .from(table(DATASTREAM_TABLE))
                        .innerJoin(table(SENSOR_TABLE))
                        .onKey()
                        .where(field(name(DATASTREAM_TABLE, STA_IDENTIFIER_FIELD))
                                .eq(datastreamIdentifier))
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
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            field(STA_NAME_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            field(STA_DESCRIPTION_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                case "format":
                case StaConstants.PROP_ENCODINGTYPE:
                    Condition subCondition = handleDirectStringPropertyFilter(
                            field(name(FORMAT_TABLE, STA_DEFINITION_FIELD), String.class),
                            propertyValue,
                            operator,
                            switched);
                    SelectConditionStep<Record1<Object>> subquery = dsl
                            .select(field(name(SENSOR_TABLE, SENSOR_ID_FIELD)))
                            .from(table(SENSOR_TABLE))
                            .innerJoin(table(FORMAT_TABLE)) // join redundant?
                            .onKey()
                            .where(subCondition);

                    return field(SENSOR_ID_FIELD).in(subquery);
                case StaConstants.PROP_METADATA:
                    return handleDirectStringPropertyFilter(
                            field(SENSOR_METADATA_FIELD, String.class),
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
                                FK_SENSOR_ID_FIELD,
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
    protected Condition handleRelatedPropertyFilter(String propertyName, Condition propertyValue) {
        try {
            SelectConditionStep<Record1<Object>> subquery;
            switch (propertyName) {
                case DATASTREAMS: {
                    subquery = dsl
                            .select(field(name(DATASTREAM_TABLE, FK_SENSOR_ID_FIELD)))
                            .from(table(DATASTREAM_TABLE))
                            .where(propertyValue);

                    return field(SENSOR_ID_FIELD).in(subquery);
                }
                default:
                    throw new STAInvalidFilterExpressionException("Could not find related property: " + propertyName);
            }
        } catch(STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ENCODINGTYPE:
                return FK_FORMAT_ID_FIELD; // encodingType => Format::definition
            case StaConstants.PROP_METADATA:
                // TODO: Add sorting by HistoricalLocation that replaces Description if it is not present
                return SENSOR_METADATA_FIELD;
            default:
                return super.checkPropertyName(property);
        }
    }
}
