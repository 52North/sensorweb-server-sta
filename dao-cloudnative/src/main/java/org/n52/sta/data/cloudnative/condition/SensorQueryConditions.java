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
import org.n52.sta.data.cloudnative.schema.tables.Format;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class SensorQueryConditions extends EntityQueryConditions {

    public Condition withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return DSL.exists(
                ctx.select(DATASTREAM.DATASET_ID)
                        .from(DATASTREAM)
                        .where(DATASTREAM.STA_IDENTIFIER.eq(datastreamIdentifier))
                        .and(SENSOR.PROCEDURE_ID.eq(DATASTREAM.FK_PROCEDURE_ID))
        );

    }

    @Override
    public Condition withStaIdentifier(String staIdentifier) {
        return SENSOR.STA_IDENTIFIER.eq(staIdentifier);
    }

    @Override
    public Condition withStaIdentifier(List<String> identifiers) {
        return SENSOR.STA_IDENTIFIER.in(identifiers);
    }

    public Condition withName(String name) {
        return SENSOR.NAME.eq(name);
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
                            SENSOR.STA_IDENTIFIER,
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            SENSOR.NAME,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            SENSOR.DESCRIPTION,
                            propertyValue,
                            operator,
                            switched);
                case "format":
                case StaConstants.PROP_ENCODINGTYPE:
                    Condition subCondition = handleDirectStringPropertyFilter(
                            FORMAT.DEFINITION,
                            propertyValue,
                            operator,
                            switched);
                    SelectConditionStep<Record1<Long>> subquery = ctx
                            .select(SENSOR.PROCEDURE_ID)
                            .from(SENSOR)
                            .join(FORMAT)
                            .onKey()
                            .where(subCondition);

                    return SENSOR.PROCEDURE_ID.in(subquery);
                case StaConstants.PROP_METADATA:
                    return handleDirectStringPropertyFilter(
                            SENSOR.DESCRIPTION_FILE,
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
                                SENSOR_PROPERTIES.FK_PROCEDURE_ID,
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
            SelectConditionStep<Record1<Long>> subquery;
            if (propertyName.equals(STAEntityDefinition.DATASTREAMS)) {
                subquery = ctx
                        .select(DATASTREAM.FK_PROCEDURE_ID)
                        .from(DATASTREAM)
                        .where(propertyValue);

                return SENSOR.PROCEDURE_ID.in(subquery);
            }
            throw new STAInvalidFilterExpressionException("Could not find related property: " + propertyName);
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Field<?> checkAliasedPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return StaEntity.alias(SENSOR, SENSOR.STA_IDENTIFIER);
            case StaConstants.PROP_NAME:
                return StaEntity.alias(SENSOR, SENSOR.NAME);
            case StaConstants.PROP_DESCRIPTION:
                return StaEntity.alias(SENSOR, SENSOR.DESCRIPTION);
            case StaConstants.PROP_PROPERTIES:
                // TODO:
                return null;
            case StaConstants.PROP_ENCODINGTYPE:
                Format SENSOR_FORMAT = StaEntity.FORMAT.as("SENSOR_FORMAT");
                return SENSOR_FORMAT.DEFINITION.as("SENSOR_FORMAT_DEFINITION");
            case StaConstants.PROP_METADATA:
                return StaEntity.alias(SENSOR, SENSOR.DESCRIPTION_FILE);
            default:
                return null;
        }
    }

    @Override
    public Field<?> checkOriginalPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return SENSOR.STA_IDENTIFIER;
            case StaConstants.PROP_NAME:
                return SENSOR.NAME;
            case StaConstants.PROP_DESCRIPTION:
                return SENSOR.DESCRIPTION;
            case StaConstants.PROP_PROPERTIES:
                // TODO:
                return null;
            case StaConstants.PROP_ENCODINGTYPE:
                Format SENSOR_FORMAT = StaEntity.FORMAT.as("SENSOR_FORMAT");
                return SENSOR_FORMAT.DEFINITION.as("SENSOR_FORMAT_DEFINITION");
            case StaConstants.PROP_METADATA:
                return SENSOR.DESCRIPTION_FILE;
            default:
                return null;
        }
    }
}
