/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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
package org.n52.sta.api.old.serialize.common;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.n52.shetland.ogc.sta.model.DatastreamEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.api.old.entity.DatastreamDTO;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class AbstractDatastreamSerializer<T extends DatastreamDTO> extends AbstractSTASerializer<T> {

    private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();

    public AbstractDatastreamSerializer(Class<T> t, String... activeExtensions) {
        super(t, activeExtensions);
    }

    @Override
    public void serialize(T datastream,
                          JsonGenerator gen,
                          SerializerProvider serializers)
        throws IOException {

        // olingo @iot links
        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_ID)) {
            writeId(gen, datastream.getId());
        }
        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_SELF_LINK)) {
            writeSelfLink(gen, datastream.getId());
        }

        // actual properties
        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_NAME)) {
            gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getName());
        }
        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_DESCRIPTION)) {
            gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, datastream.getDescription());
        }

        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_OBSERVATION_TYPE)) {
            gen.writeObjectField(STAEntityDefinition.PROP_OBSERVATION_TYPE,
                                 datastream.getObservationType());
        }
        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_UOM)) {
            gen.writeObjectFieldStart(STAEntityDefinition.PROP_UOM);
            if (datastream.getUnitOfMeasurement() != null) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getUnitOfMeasurement().getName());
                gen.writeStringField(STAEntityDefinition.PROP_SYMBOL,
                                     datastream.getUnitOfMeasurement().getSymbol());
                gen.writeStringField(STAEntityDefinition.PROP_DEFINITION,
                                     datastream.getUnitOfMeasurement().getDefinition());
            } else {
                gen.writeNullField(STAEntityDefinition.PROP_NAME);
                gen.writeNullField(STAEntityDefinition.PROP_SYMBOL);
                gen.writeNullField(STAEntityDefinition.PROP_DEFINITION);
            }
            gen.writeEndObject();
        }

        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_OBSERVED_AREA)) {
            if (datastream.getObservedArea() != null) {
                gen.writeFieldName(STAEntityDefinition.PROP_OBSERVED_AREA);
                gen.writeRawValue(GEO_JSON_WRITER.write(datastream.getObservedArea()));
            } else {
                gen.writeNullField(STAEntityDefinition.PROP_OBSERVED_AREA);
            }
        }

        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_RESULT_TIME)) {
            if (datastream.getResultTime() != null) {
                gen.writeStringField(STAEntityDefinition.PROP_RESULT_TIME,
                                     DateTimeHelper.format(datastream.getResultTime()));
            } else {
                gen.writeNullField(STAEntityDefinition.PROP_RESULT_TIME);
            }
        }
        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
            if (datastream.getPhenomenonTime() != null) {
                gen.writeStringField(STAEntityDefinition.PROP_PHENOMENON_TIME,
                                     DateTimeHelper.format(datastream.getPhenomenonTime()));
            }
        }

        if (!datastream.hasSelectOption() ||
            datastream.getFieldsToSerialize().contains(STAEntityDefinition.PROP_PROPERTIES)) {
            gen.writeObjectField(STAEntityDefinition.PROP_PROPERTIES, datastream.getProperties());
        }

        // navigation properties
        for (String navigationProperty : DatastreamEntityDefinition.NAVIGATION_PROPERTIES) {
            if (!datastream.hasSelectOption() || datastream.getFieldsToSerialize().contains(navigationProperty)) {
                if (!datastream.hasExpandOption() ||
                    datastream.getFieldsToExpand().get(navigationProperty) == null) {
                    writeNavigationProp(gen, navigationProperty, datastream.getId());
                } else {
                    switch (navigationProperty) {
                        case STAEntityDefinition.OBSERVATIONS:
                            if (datastream.getObservations() == null) {
                                writeNavigationProp(gen, navigationProperty, datastream.getId());
                            } else {
                                gen.writeFieldName(navigationProperty);
                                writeNestedCollection(Collections.unmodifiableSet(datastream.getObservations()),
                                                      gen,
                                                      serializers);
                            }
                            break;
                        case STAEntityDefinition.OBSERVED_PROPERTY:
                            if (datastream.getObservedProperty() == null) {
                                writeNavigationProp(gen, navigationProperty, datastream.getId());
                            } else {
                                gen.writeFieldName(navigationProperty);
                                writeNestedEntity(datastream.getObservedProperty(),
                                                  gen,
                                                  serializers);
                            }
                            break;
                        case STAEntityDefinition.THING:
                            if (datastream.getThing() == null) {
                                writeNavigationProp(gen, navigationProperty, datastream.getId());
                            } else {
                                gen.writeFieldName(navigationProperty);
                                writeNestedEntity(datastream.getThing(),
                                                  gen,
                                                  serializers);
                            }
                            break;
                        case STAEntityDefinition.SENSOR:
                            if (datastream.getSensor() == null) {
                                writeNavigationProp(gen, navigationProperty, datastream.getId());
                            } else {
                                gen.writeFieldName(navigationProperty);
                                writeNestedEntity(datastream.getSensor(),
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
}
