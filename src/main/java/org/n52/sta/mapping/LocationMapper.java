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
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_DESCRIPTION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ENCODINGTYPE;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_LOCATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_NAME;
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ES_LOCATIONS_NAME;
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ET_LOCATION_FQN;

import javax.json.Json;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.n52.series.db.beans.sta.LocationEntity;
//import org.n52.sta.utils.EntityAnnotator;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class LocationMapper {

    @Autowired
    EntityCreationHelper entityCreationHelper;

    public Entity createEntity(LocationEntity location) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, ID_ANNOTATION, ValueType.PRIMITIVE, location.getId()));
        entity.addProperty(new Property(null, PROP_NAME, ValueType.PRIMITIVE, location.getName()));
        entity.addProperty(new Property(null, PROP_DESCRIPTION, ValueType.PRIMITIVE, location.getDescription()));

        String geojson = "";
//        Coordinate coord = location.getGeometry().getCoordinate();
//        String geojson = Json.createObjectBuilder()
//                .add("type", "Feature")
//                .add("geometry", Json.createObjectBuilder()
//                                     .add("type", location.getGeometry().getGeometryType())
//                                     .add("coordinates", "[" + coord.x + "," + coord.y + "]"))
//                .build()
//                .toString();
        
        
        entity.addProperty(new Property(null, PROP_LOCATION, ValueType.PRIMITIVE, geojson));
        entity.addProperty(new Property(null, PROP_ENCODINGTYPE, ValueType.PRIMITIVE, "application/vnd.geo+json"));

        entity.setType(ET_LOCATION_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_LOCATIONS_NAME, ID_ANNOTATION));

        return entity;

    }

}
