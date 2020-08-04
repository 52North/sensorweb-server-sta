/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterJsonEntity;
import org.n52.series.db.beans.sta.AbstractObservationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sta.model.ObservationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.serdes.util.ElementWithQueryOptions;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class AbstractObservationSerializer<T extends ElementWithQueryOptions<AbstractObservationEntity<?>>>
        extends AbstractSTASerializer<T, AbstractObservationEntity<?>> {

    private static final String VALUE = "value";

    protected AbstractObservationSerializer(Class<T> t) {
        super(t);
    }

    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers, STAEntityDefinition definition)
            throws IOException {
        AbstractObservationEntity<?> observation = unwrap(value);

        // olingo @iot links
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ID)) {
            writeId(gen, observation.getStaIdentifier());
        }
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
            writeSelfLink(gen, observation.getStaIdentifier());
        }

        // actual properties
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT)) {
            gen.writeStringField(STAEntityDefinition.PROP_RESULT, observation.getValue().toString());
        }
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_TIME)) {
            if (observation.hasResultTime()) {
                gen.writeStringField(STAEntityDefinition.PROP_RESULT_TIME,
                                     observation.getResultTime().toInstant().toString());
            } else {
                gen.writeNullField(STAEntityDefinition.PROP_RESULT_TIME);
            }
        }
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
            String phenomenonTime = DateTimeHelper.format(createPhenomenonTime(observation));
            gen.writeStringField(STAEntityDefinition.PROP_PHENOMENON_TIME, phenomenonTime);
        }

        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_QUALITY)) {
            gen.writeNullField(STAEntityDefinition.PROP_RESULT_QUALITY);
        }

        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_VALID_TIME)) {
            if (observation.isSetValidTime()) {
                gen.writeStringField(STAEntityDefinition.PROP_VALID_TIME,
                                     DateTimeHelper.format(createValidTime(observation)));
            } else {
                gen.writeNullField(STAEntityDefinition.PROP_VALID_TIME);
            }
        }

        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PARAMETERS)) {
            gen.writeArrayFieldStart(STAEntityDefinition.PROP_PARAMETERS);
            if (observation.hasParameters()) {
                for (ParameterEntity<?> parameter : observation.getParameters()) {
                    gen.writeStartObject();
                    gen.writeStringField("name", parameter.getName());
                    if (parameter instanceof ParameterJsonEntity) {
                        ObjectMapper mapper = new ObjectMapper();
                        gen.writeObjectField(VALUE, mapper.readTree(parameter.getValueAsString()));
                    } else {
                        gen.writeStringField(VALUE, parameter.getValueAsString());
                    }
                    gen.writeEndObject();
                }
            }
            gen.writeEndArray();
        }



        // navigation properties
        Set<String> navProps = new HashSet<>();
        navProps.addAll(definition.getNavPropsMandatory());
        navProps.addAll(definition.getNavPropsOptional());
        for (String navigationProperty : navProps) {
            if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                if (!hasExpandOption || fieldsToExpand.get(navigationProperty) == null) {
                    writeNavigationProp(gen, navigationProperty, observation.getStaIdentifier());
                } else {
                    switch (navigationProperty) {
                    case ObservationEntityDefinition.DATASTREAM:
                        if (observation.getDatastream() == null) {
                            writeNavigationProp(gen, navigationProperty, observation.getStaIdentifier());
                        } else {
                            gen.writeFieldName(navigationProperty);
                            writeNestedEntity(observation.getDatastream(),
                                              fieldsToExpand.get(navigationProperty),
                                              gen,
                                              serializers);
                        }
                        break;
                    case ObservationEntityDefinition.FEATURE_OF_INTEREST:
                        if (observation.getFeature() == null) {
                            writeNavigationProp(gen, navigationProperty, observation.getStaIdentifier());
                        } else {
                            gen.writeFieldName(navigationProperty);
                            writeNestedEntity(observation.getFeature(),
                                              fieldsToExpand.get(navigationProperty),
                                              gen,
                                              serializers);
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + navigationProperty);
                    }
                }
            }
        }
    }

    private Time createPhenomenonTime(AbstractObservationEntity<?> observation) {
        final DateTime start = new DateTime(observation.getSamplingTimeStart(), DateTimeZone.UTC);
        DateTime end;
        if (observation.getSamplingTimeEnd() != null) {
            end = new DateTime(observation.getSamplingTimeEnd(), DateTimeZone.UTC);
        } else {
            end = start;
        }
        return createTime(start, end);
    }

    private Time createValidTime(AbstractObservationEntity<?> observation) {
        final DateTime start = new DateTime(observation.getValidTimeStart(), DateTimeZone.UTC);
        DateTime end;
        if (observation.getValidTimeEnd() != null) {
            end = new DateTime(observation.getValidTimeEnd(), DateTimeZone.UTC);
        } else {
            end = start;
        }
        return createTime(start, end);
    }

    private Time createTime(DateTime start, DateTime end) {
        if (start.equals(end)) {
            return new TimeInstant(start);
        } else {
            return new TimePeriod(start, end);
        }
    }
}
