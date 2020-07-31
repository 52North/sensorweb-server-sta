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
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.n52.series.db.beans.sta.AbstractDatastreamEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.model.DatastreamEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.sta.utils.TimeUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public abstract class AbstractDatastreamSerializer<T extends ElementWithQueryOptions>
        extends AbstractSTASerializer<T> {

    private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();
    private static final long serialVersionUID = -6555417490577181829L;

    protected AbstractDatastreamSerializer(Class<T> t) {
        super(t);
    }

    @Override
    public void serialize(T value,
                          JsonGenerator gen,
                          SerializerProvider serializers)
            throws IOException {
        AbstractDatastreamEntity datastream = (AbstractDatastreamEntity) value.getEntity();
        QueryOptions options = value.getQueryOptions();

        Set<String> fieldsToSerialize = null;
        Map<String, QueryOptions> fieldsToExpand = new HashMap<>();
        boolean hasSelectOption = false;
        boolean hasExpandOption = false;
        if (options != null) {
            if (options.hasSelectFilter()) {
                hasSelectOption = true;
                fieldsToSerialize = options.getSelectFilter().getItems();
            }
            if (options.hasExpandFilter()) {
                hasExpandOption = true;
                for (ExpandItem item : options.getExpandFilter().getItems()) {
                    fieldsToExpand.put(item.getPath(), item.getQueryOptions());
                }
            }
        }

        // olingo @iot links
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_ID)) {
            writeId(gen, datastream.getStaIdentifier());
        }
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_SELF_LINK)) {
            writeSelfLink(gen, datastream.getStaIdentifier());
        }

        // actual properties
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_NAME)) {
            gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getName());
        }
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_DESCRIPTION)) {
            gen.writeStringField(STAEntityDefinition.PROP_DESCRIPTION, datastream.getDescription());
        }

        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_OBSERVATION_TYPE)) {
            gen.writeObjectField(STAEntityDefinition.PROP_OBSERVATION_TYPE,
                                 datastream.getObservationType().getFormat());
        }
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_UOM)) {
            gen.writeObjectFieldStart(STAEntityDefinition.PROP_UOM);
            if (datastream.getUnit() != null) {
                gen.writeStringField(STAEntityDefinition.PROP_NAME, datastream.getUnit().getName());
                gen.writeStringField(STAEntityDefinition.PROP_SYMBOL,
                                     datastream.getUnit().getSymbol());
                gen.writeStringField(STAEntityDefinition.PROP_DEFINITION,
                                     datastream.getUnit().getLink());
            } else {
                gen.writeNullField(STAEntityDefinition.PROP_NAME);
                gen.writeNullField(STAEntityDefinition.PROP_SYMBOL);
                gen.writeNullField(STAEntityDefinition.PROP_DEFINITION);
            }
            gen.writeEndObject();
        }

        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_OBSERVED_AREA)) {
            gen.writeFieldName(STAEntityDefinition.PROP_OBSERVED_AREA);
            if (datastream.getGeometryEntity() != null) {
                gen.writeRawValue(GEO_JSON_WRITER.write(datastream.getGeometryEntity().getGeometry()));
            } else {
                gen.writeNull();
            }
        }

        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_RESULT_TIME)) {
            if (datastream.getResultTimeStart() != null) {
                Time time = TimeUtil.createTime(TimeUtil.createDateTime(datastream.getResultTimeStart()),
                                                TimeUtil.createDateTime(datastream.getResultTimeEnd()));
                gen.writeStringField(STAEntityDefinition.PROP_RESULT_TIME,
                                     DateTimeHelper.format(time));
            } else {
                gen.writeNullField(STAEntityDefinition.PROP_RESULT_TIME);
            }
        }
        if (!hasSelectOption || fieldsToSerialize.contains(STAEntityDefinition.PROP_PHENOMENON_TIME)) {
            if (datastream.getSamplingTimeStart() != null) {
                Time time = TimeUtil.createTime(TimeUtil.createDateTime(datastream.getSamplingTimeStart()),
                                                TimeUtil.createDateTime(datastream.getSamplingTimeEnd()));
                gen.writeStringField(STAEntityDefinition.PROP_PHENOMENON_TIME,
                                     DateTimeHelper.format(time));
            } else {
                gen.writeNullField(STAEntityDefinition.PROP_PHENOMENON_TIME);
            }
        }

        // navigation properties
        for (String navigationProperty : DatastreamEntityDefinition.NAVIGATION_PROPERTIES) {
            if (!hasSelectOption || fieldsToSerialize.contains(navigationProperty)) {
                if (!hasExpandOption || fieldsToExpand.get(navigationProperty) == null) {
                    writeNavigationProp(gen, navigationProperty, datastream.getStaIdentifier());
                } else {
                    switch (navigationProperty) {
                    case STAEntityDefinition.OBSERVATIONS:
                        if (datastream.getObservations() == null) {
                            writeNavigationProp(gen, navigationProperty, datastream.getStaIdentifier());
                        } else {
                            gen.writeFieldName(navigationProperty);
                            writeNestedCollection(Collections.unmodifiableSet(datastream.getObservations()),
                                                  fieldsToExpand.get(navigationProperty),
                                                  gen,
                                                  serializers);
                        }
                        break;
                    case STAEntityDefinition.OBSERVED_PROPERTY:
                        if (datastream.getObservableProperty() == null) {
                            writeNavigationProp(gen, navigationProperty, datastream.getStaIdentifier());
                        } else {
                            gen.writeFieldName(navigationProperty);
                            writeNestedEntity(datastream.getObservableProperty(),
                                              fieldsToExpand.get(navigationProperty),
                                              gen,
                                              serializers);
                        }
                        break;
                    case STAEntityDefinition.THING:
                        if (datastream.getThing() == null) {
                            writeNavigationProp(gen, navigationProperty, datastream.getStaIdentifier());
                        } else {
                            gen.writeFieldName(navigationProperty);
                            writeNestedEntity(datastream.getThing(),
                                              fieldsToExpand.get(navigationProperty),
                                              gen,
                                              serializers);
                        }
                        break;
                    case STAEntityDefinition.SENSOR:
                        if (datastream.getProcedure() == null) {
                            writeNavigationProp(gen, navigationProperty, datastream.getStaIdentifier());
                        } else {
                            gen.writeFieldName(navigationProperty);
                            writeNestedEntity(datastream.getProcedure(),
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
}
