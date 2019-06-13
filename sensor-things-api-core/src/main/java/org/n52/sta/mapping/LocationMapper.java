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
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_LOCATION;
import static org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider.ES_HISTORICAL_LOCATIONS_NAME;
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ES_LOCATIONS_NAME;
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ET_LOCATION_FQN;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ES_THINGS_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_NAME;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import static org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_NAME;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class LocationMapper extends AbstractLocationGeometryMapper<LocationEntity> {

    @Autowired
    private ThingMapper thingMapper;

    public Entity createEntity(LocationEntity location) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, PROP_ID, ValueType.PRIMITIVE, location.getIdentifier()));
        addDescription(entity, location);
        addName(entity, location);
        addLocation(entity, location);

        entity.setType(ET_LOCATION_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_LOCATIONS_NAME, PROP_ID));

        return entity;
    }

    @Override
    public LocationEntity createEntity(Entity entity) {
        LocationEntity location = new LocationEntity();
        setId(location, entity);
        setName(location, entity);
        setDescription(location, entity);
        Property locationProperty = entity.getProperty(PROP_LOCATION);
        if (locationProperty != null) {
            if (locationProperty.getValueType().equals(ValueType.PRIMITIVE) && locationProperty.getValue() instanceof Geospatial) {
                location.setGeometryEntity(parseGeometry((Geospatial) locationProperty.getValue()));
            } else {
                location.setLocation(locationProperty.getValue().toString());
            }
        }
        if (entity.getProperty(PROP_ENCODINGTYPE) != null) {
            location.setLocationEncoding(createLocationEncodingEntity(entity.getProperty(PROP_ENCODINGTYPE)));
        }
        addThings(location, entity);
        return location;
    }

    private LocationEncodingEntity createLocationEncodingEntity(Property property) {
        LocationEncodingEntity locationEncodingEntity = new LocationEncodingEntity();
        locationEncodingEntity.setEncodingType(property.getValue().toString());
        return locationEncodingEntity;
    }

    @Override
    public LocationEntity merge(LocationEntity existing, LocationEntity toMerge) {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        if (toMerge.hasLocation()) {
            existing.setLocation(toMerge.getLocation());
        }
        mergeGeometry(existing, toMerge);
        return existing;
    }

    private void addThings(LocationEntity location, Entity entity) {
        if (checkNavigationLink(entity, ES_THINGS_NAME)) {
            Set<PlatformEntity> things = new LinkedHashSet<>();
            Iterator<Entity> iterator = entity.getNavigationLink(ES_THINGS_NAME).getInlineEntitySet().iterator();
            while (iterator.hasNext()) {
                things.add(thingMapper.createEntity(iterator.next()));
            }
            location.setThings(things);
        }
    }

    @Override
    public Entity  checkEntity(Entity entity) throws ODataApplicationException {
        checkNameAndDescription(entity);
        checkPropertyValidity(PROP_LOCATION, entity);
        checkEncodingType(entity);
        if (checkNavigationLink(entity, ES_THINGS_NAME)) {
            Iterator<Entity> iterator = entity.getNavigationLink(ES_THINGS_NAME).getInlineEntitySet().iterator();
            while (iterator.hasNext()) {
                thingMapper.checkNavigationLink(iterator.next());
            }
        }
        return entity;
    }

}
