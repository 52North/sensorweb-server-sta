/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.mapping;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_OBSERVATION_TYPE;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_OBSERVED_AREA;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_PHENOMENON_TIME;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_RESULT_TIME;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_UOM;
import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ES_DATASTREAMS_NAME;
import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ET_DATASTREAM_FQN;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.edm.geo.Point;
import org.apache.olingo.commons.api.edm.geo.Polygon;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.edm.provider.complextypes.UnitOfMeasurementComplexType;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class DatastreamMapper extends AbstractMapper<DatastreamEntity> {

    public Entity createEntity(DatastreamEntity datastream) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, ID_ANNOTATION, ValueType.PRIMITIVE, datastream.getId()));
        addDescription(entity, datastream);
        addNane(entity, datastream);
        entity.addProperty(new Property(null, PROP_OBSERVATION_TYPE, ValueType.PRIMITIVE, datastream.getObservationType().getFormat()));

        entity.addProperty(new Property(null, PROP_PHENOMENON_TIME, ValueType.PRIMITIVE,
                DateTimeHelper.format(createTime(createDateTime(datastream.getSamplingTimeStart()),
                        createDateTime(datastream.getSamplingTimeEnd())))));
        entity.addProperty(new Property(null, PROP_RESULT_TIME, ValueType.PRIMITIVE,
                DateTimeHelper.format(createTime(createDateTime(datastream.getResultTimeStart()),
                        createDateTime(datastream.getResultTimeEnd())))));

        entity.addProperty(new Property(null, PROP_UOM, ValueType.COMPLEX, resolveUnitOfMeasurement(datastream.getUnitOfMeasurement())));
        entity.addProperty(new Property(null, PROP_OBSERVED_AREA, ValueType.GEOSPATIAL, resolveObservedArea(datastream.getGeometryEntity())));

        entity.setType(ET_DATASTREAM_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_DATASTREAMS_NAME, ID_ANNOTATION));

        return entity;
    }

    private ComplexValue resolveUnitOfMeasurement(UnitEntity uom) {
        ComplexValue value = new ComplexValue();
        if (uom != null) {
            value.getValue().add(new Property(null, UnitOfMeasurementComplexType.PROP_NAME, ValueType.PRIMITIVE, uom.getName()));
            value.getValue().add(new Property(null, UnitOfMeasurementComplexType.PROP_SYMBOL, ValueType.PRIMITIVE, uom.getSymbol()));
            value.getValue().add(new Property(null, UnitOfMeasurementComplexType.PROP_DEFINITION, ValueType.PRIMITIVE, uom.getLink()));

        }
        return value;
    }

    private Polygon resolveObservedArea(GeometryEntity geometryEntity) {
        Polygon polygon = null;
        if (geometryEntity != null && geometryEntity.getGeometry().getGeometryType().equals("Polygon")) {
            List<Point> points = Arrays.stream(geometryEntity.getGeometry().getCoordinates()).map(c -> {
                Point p = new Point(Geospatial.Dimension.GEOMETRY, null);
                p.setX(c.x);
                p.setY(c.y);
                return p;
            }).collect(Collectors.toList());
            polygon = new Polygon(Geospatial.Dimension.GEOMETRY, null, null, points);

        }
        return polygon;
    }
}
