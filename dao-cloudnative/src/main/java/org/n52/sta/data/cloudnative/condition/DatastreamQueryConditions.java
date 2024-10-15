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
import org.n52.sta.data.cloudnative.schema.tables.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class DatastreamQueryConditions extends EntityQueryConditions {

    public Condition withName(final String name) {
        return DATASTREAM.FK_AGGREGATION_ID.isNull()
                .or(DATASTREAM.FK_AGGREGATION_ID.eq(DATASET_AGGREGATION_MARKER))
                .and(DATASTREAM.NAME.eq(name));
    }

    public Condition withStaIdentifier(final String staIdentifier) {
        return DATASTREAM.FK_AGGREGATION_ID.isNull()
                .or(DATASTREAM.FK_AGGREGATION_ID.eq(DATASET_AGGREGATION_MARKER))
                .and(DATASTREAM.STA_IDENTIFIER.eq(staIdentifier));
    }

    public Condition withStaIdentifier(final List<String> identifiers) {
        return DATASTREAM.FK_AGGREGATION_ID.isNull()
                .or(DATASTREAM.FK_AGGREGATION_ID.eq(DATASET_AGGREGATION_MARKER))
                .and(DATASTREAM.STA_IDENTIFIER.in(identifiers));
    }


    @Override
    protected <T extends Comparable<? super T>> Condition handleDirectPropertyFilter(
            String propertyName,
            Field<T> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        try {
            switch (propertyName) {
                case StaConstants.PROP_ID:
                    return handleDirectStringPropertyFilter(
                            DATASTREAM.STA_IDENTIFIER,
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            DATASTREAM.NAME,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            DATASTREAM.DESCRIPTION,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_OBSERVATION_TYPE:
                    Condition subCondition = handleDirectStringPropertyFilter(
                            FORMAT.DEFINITION,
                            propertyValue,
                            operator,
                            switched);
                    return DATASTREAM.FK_FORMAT_ID.in(ctx
                            .select(DATASTREAM.FK_FORMAT_ID)
                            .from(DATASTREAM)
                            .join(FORMAT)
                            .onKey()
                            .where(subCondition));
                default:
                    // We are filtering on variable keys on properties
                    if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                        return handleProperties(
                                propertyName,
                                propertyValue,
                                operator,
                                switched,
                                DatasetParameter.DATASET_PARAMETER.FK_DATASET_ID,
                                ParameterFactory.EntityType.DATASET);
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
            switch (propertyName) {
                case STAEntityDefinition.SENSOR: {
                    Procedure SENSOR = Procedure.PROCEDURE;
                    return DATASTREAM.FK_PROCEDURE_ID.in(ctx
                            .select(SENSOR.PROCEDURE_ID)
                            .from(SENSOR)
                            .where(propertyValue)
                    );
                }
                case STAEntityDefinition.OBSERVED_PROPERTY: {
                    Phenomenon OBSERVED_PROPERTY = Phenomenon.PHENOMENON;
                    return DATASTREAM.FK_PHENOMENON_ID.in(ctx
                            .select(OBSERVED_PROPERTY.PHENOMENON_ID)
                            .from(OBSERVED_PROPERTY)
                            .where(propertyValue)
                    );
                }
                case STAEntityDefinition.THING: {
                    Platform THING = Platform.PLATFORM;
                    return DATASTREAM.FK_PLATFORM_ID.in(ctx
                            .select(THING.PLATFORM_ID)
                            .from(THING)
                            .where(propertyValue)
                    );
                }
                case STAEntityDefinition.OBSERVATIONS: {
                    Observation OBSERVATION = Observation.OBSERVATION;
                    return DATASTREAM.DATASET_ID.in(ctx
                            .select(OBSERVATION.FK_DATASET_ID)
                            .from(OBSERVATION)
                            .where(propertyValue)
                    );
                }
                default:
                    throw new STAInvalidFilterExpressionException(COULD_NOT_FIND_RELATED_PROPERTY + propertyName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Condition withFeatureStaIdentifier(final String featureIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(FEATURE_OF_INTEREST)
                        .where(FEATURE_OF_INTEREST.STA_IDENTIFIER.eq(featureIdentifier))
                        .and(DATASTREAM.FK_FEATURE_ID.eq(FEATURE_OF_INTEREST.FEATURE_ID))
        );
    }

    public Condition withObservedPropertyStaIdentifier(final String observedPropertyIdentifier) {
         return DSL.exists(
                ctx.selectOne()
                        .from(OBSERVED_PROPERTY)
                        .where(OBSERVED_PROPERTY.STA_IDENTIFIER.eq(observedPropertyIdentifier))
                        .and(DATASTREAM.FK_PHENOMENON_ID.eq(OBSERVED_PROPERTY.PHENOMENON_ID))
        );
    }

    public Condition withObservedPropertyName(final String name) {
        return DSL.exists(
                ctx.selectOne()
                        .from(OBSERVED_PROPERTY)
                        .where(OBSERVED_PROPERTY.NAME.eq(name))
                        .and(DATASTREAM.FK_PHENOMENON_ID.eq(OBSERVED_PROPERTY.PHENOMENON_ID))
        );
    }

    public Condition withThingStaIdentifier(final String thingIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(THING)
                        .where(THING.STA_IDENTIFIER.eq(thingIdentifier))
                        .and(DATASTREAM.FK_PLATFORM_ID.eq(THING.PLATFORM_ID))
        );
    }

    public Condition withThingName(final String name) {
        return DSL.exists(
                ctx.selectOne()
                        .from(THING)
                        .where(THING.NAME.eq(name))
                        .and(DATASTREAM.FK_PLATFORM_ID.eq(THING.PLATFORM_ID))
        );
    }

    public Condition withSensorStaIdentifier(final String sensorIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(SENSOR)
                        .where(SENSOR.STA_IDENTIFIER.eq(sensorIdentifier))
                        .and(DATASTREAM.FK_PROCEDURE_ID.eq(SENSOR.PROCEDURE_ID))
        );
    }

    public Condition withSensorName(final String name) {
        return DSL.exists(
                ctx.selectOne()
                        .from(SENSOR)
                        .where(SENSOR.NAME.eq(name))
                        .and(DATASTREAM.FK_PROCEDURE_ID.eq(SENSOR.PROCEDURE_ID))
        );
    }

    public Condition withObservationStaIdentifier(String observationIdentifier) {
        // Subquery to get dataset_id from Observation where sta_identifier matches
        SelectConditionStep<Record1<Long>> sq = ctx
                .select(OBSERVATION.FK_DATASET_ID)
                .from(OBSERVATION)
                .where(OBSERVATION.STA_IDENTIFIER.eq(observationIdentifier));

        // Subquery to get fk_aggregate_id from Datastream where id matches the result from the first subquery
        SelectConditionStep<Record1<Long>> subquery = ctx
                .select(DATASTREAM.FK_AGGREGATION_ID)
                .from(DATASTREAM)
                .where(DATASTREAM.DATASET_ID.in(sq));

        // Either id matches the result from the first subquery or id matches the result from the second subquery
        return DATASTREAM.DATASET_ID.in(sq).or(DATASTREAM.DATASET_ID.in(subquery));
    }

        public Field<?> checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return StaEntity.alias(DATASTREAM, DATASTREAM.STA_IDENTIFIER);
            case StaConstants.PROP_NAME:
                return StaEntity.alias(DATASTREAM, DATASTREAM.NAME);
            case StaConstants.PROP_DESCRIPTION:
                return StaEntity.alias(DATASTREAM, DATASTREAM.DESCRIPTION);
            case StaConstants.PROP_OBSERVED_AREA:
                return StaEntity.alias(DATASTREAM, DATASTREAM.OBSERVED_AREA);
            case StaConstants.PROP_UOM:
                return StaEntity.alias(UNIT, UNIT.NAME);
            case StaConstants.PROP_OBSERVATION_TYPE:
                Format DATASTREAM_FORMAT = StaEntity.FORMAT.as("DATASTREAM_FORMAT");
                return DATASTREAM_FORMAT.DEFINITION.as("DATASTREAM_FORMAT_DEFINITION");
            case StaConstants.PROP_PHENOMENON_TIME:
                return StaEntity.alias(DATASTREAM, DATASTREAM.FIRST_TIME);
            case StaConstants.PROP_RESULT_TIME:
                return StaEntity.alias(DATASTREAM, DATASTREAM.RESULT_TIME_START);
            case StaConstants.PROP_PROPERTIES:
                // TODO:
                return null;
            default:
                return null;
        }
    }

}
