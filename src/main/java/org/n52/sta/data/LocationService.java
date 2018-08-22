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
package org.n52.sta.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.mapping.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.HashSet;
import java.util.Set;
import org.apache.olingo.commons.api.data.Entity;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class LocationService implements AbstractSensorThingsEntityService {

    @Autowired
    private LocationMapper locationMapper;

    protected List<LocationEntity> createLocationEntities() {
        List<LocationEntity> locations = new ArrayList<>();

        LocationEntity loc1 = new LocationEntity();
        loc1.setId(42L);
        loc1.setName("Demo Name 1");
        loc1.setDescription("Demo Location 1");
        loc1.setGeometry(new GeometryFactory().createPoint(new Coordinate(Math.random() * 90, Math.random() * 180)));

        loc1.setLocationEncodings(createEncoding());

        locations.add(loc1);
        return locations;
    }

    private LocationEncodingEntity createEncoding() {
        LocationEncodingEntity encoding = new LocationEncodingEntity();
        encoding.setId(43L);
        encoding.setEncodingType("DemoEncoding");
        return encoding;
    }

    @Override
    public EntityCollection getEntityCollection() {
        EntityCollection retEntitySet = new EntityCollection();

        this.createLocationEntities().forEach(t -> retEntitySet.getEntities().add(locationMapper.createLocationEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntityForId(String id) {
        LocationEntity loc1 = new LocationEntity();
        loc1.setId(42L);
        loc1.setName("Demo Name 1");
        loc1.setDescription("Demo Location 1");
        loc1.setGeometry(new GeometryFactory().createPoint(new Coordinate(Math.random() * 90, Math.random() * 180)));

        return locationMapper.createLocationEntity(loc1);
    }
}
