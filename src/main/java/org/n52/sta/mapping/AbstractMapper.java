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

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_DESCRIPTION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_NAME;

import java.util.Date;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractMapper<T> {
    
    @Autowired
    EntityCreationHelper entityCreationHelper;
    
    @Autowired
    GeometryMapper geometryMapper;
    
    public abstract Entity createEntity(T t);
    
    protected void addNameDescriptionProperties(Entity entity, DescribableEntity describableEntity) {
        // add name
        String name =   describableEntity.getIdentifier();
        if (describableEntity.isSetName()) {
            name = describableEntity.getName();
        }
        addNane(entity, name);
        // add description
        String description =  "Description of " + describableEntity.getIdentifier();
        if (describableEntity.isSetDescription()) {
            description = describableEntity.getDescription();
        }
        addDescription(entity, description);
    }
    
    protected void addNane(Entity entity, HasName describableEntity) {
        entity.addProperty(new Property(null, PROP_NAME, ValueType.PRIMITIVE, describableEntity.getName()));
    }
    
    protected void addNane(Entity entity, String name) {
        entity.addProperty(new Property(null, PROP_NAME, ValueType.PRIMITIVE, name));
    }
    
    protected void addDescription(Entity entity, HasDescription describableEntity) {
        entity.addProperty(new Property(null, PROP_DESCRIPTION, ValueType.PRIMITIVE, describableEntity.getDescription()));
    }
    
    protected void addDescription(Entity entity, String description) {
        entity.addProperty(new Property(null, PROP_DESCRIPTION, ValueType.PRIMITIVE, description));
    }
    
    protected DateTime createDateTime(Date date) {
        return new DateTime(date, DateTimeZone.UTC);
    }

    protected Time createTime(DateTime time) {
        return new TimeInstant(time);
    }
    
    /**
     * Create {@link Time} from {@link DateTime}s
     *
     * @param start
     *            Start {@link DateTime}
     * @param end
     *            End {@link DateTime}
     * @return Resulting {@link Time}
     */
    protected Time createTime(DateTime start, DateTime end) {
        if (start.equals(end)) {
            return createTime(start);
        } else {
            return new TimePeriod(start, end);
        }
    }
}
