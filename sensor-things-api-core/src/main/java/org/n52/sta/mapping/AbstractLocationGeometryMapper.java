/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ENCODINGTYPE;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_FEATURE;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_LOCATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_OBSERVED_AREA;

import java.util.Locale;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.HibernateRelations.HasGeometry;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractLocationGeometryMapper<T> extends AbstractMapper<T> {

    private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

    @Autowired
    private GeometryMapper geometryMapper;

    protected void checkEncodingType(Entity entity) throws ODataApplicationException {
        checkPropertyValidity(PROP_ENCODINGTYPE, entity);
        if (!getPropertyValue(entity, PROP_ENCODINGTYPE).equals(ENCODINGTYPE_GEOJSON)) {
            throw new ODataApplicationException(String.format("The parameter '%s' is invalid for in entity '%s'!",
                    PROP_ENCODINGTYPE, entity.getType().replace("iot.", "")), HttpStatusCode.BAD_REQUEST.getStatusCode(),
                    Locale.getDefault());
        }
    }

    protected void addGeometry(Entity entity, HasGeometry<?> geometryEntity) {
        addWithEncoding(entity, geometryEntity, PROP_FEATURE);
    }

    protected void addLocation(Entity entity, HasGeometry<?> locationEntity) {
        addWithEncoding(entity, locationEntity, PROP_LOCATION);
    }

    protected void addObservedArea(Entity entity, HasGeometry<?> geometryEntity) {
        add(entity, geometryEntity, PROP_OBSERVED_AREA);
    }

    protected void addWithEncoding(Entity entity, HasGeometry<?> geometryLocationEntity, String property) {
        if (geometryLocationEntity.isSetGeometry()) {
            entity.addProperty(new Property(null, PROP_ENCODINGTYPE, ValueType.PRIMITIVE, ENCODINGTYPE_GEOJSON));
            add(entity, geometryLocationEntity, property);
        }
    }

    protected void add(Entity entity, HasGeometry<?> geometryLocationEntity, String property) {
        if (geometryLocationEntity.isSetGeometry()) {
            entity.addProperty(new Property(null, property, ValueType.GEOSPATIAL,
                    geometryMapper.resolveGeometry(geometryLocationEntity.getGeometryEntity())));
        } else {
            entity.addProperty(new Property(null, property, ValueType.GEOSPATIAL,null));
        }

    }

    protected GeometryEntity parseGeometry(ComplexValue value) {
        return geometryMapper.createGeometryEntity(value);
    }

    protected GeometryEntity parseGeometry(Geospatial geospatial) {
        return geometryMapper.createGeometryEntity(geospatial);
    }

    protected void mergeGeometry(HasGeometry<?> existing, HasGeometry<?> toMerge) {
        if (toMerge.isSetGeometry()) {
            existing.setGeometryEntity(toMerge.getGeometryEntity());
        }
    }

}
