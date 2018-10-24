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

import static org.n52.sta.edm.provider.SensorThingsEdmConstants.ID;
import static org.n52.sta.edm.provider.SensorThingsEdmConstants.ID_ANNOTATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_DEFINITION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_DESCRIPTION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_NAME;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_PHENOMENON_TIME;

import java.util.Date;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.HibernateRelations.HasPhenomenonTime;
import org.n52.series.db.beans.IdEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractMapper<T> {
    
    @Autowired
    EntityCreationHelper entityCreationHelper;
    
    public abstract Entity createEntity(T t);
    
    public abstract T createEntity(Entity entity);
    
    public abstract T merge(T existing, T toMerge) throws ODataApplicationException;
    
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
    
    protected boolean checkProperty(Entity entity, String name) {
        return entity.getProperty(name) != null;
    }
    
    protected boolean checkNavigationLink(Entity entity, String name) {
        return entity.getNavigationLink(name) != null;
    }
    
    protected void addName(Entity entity, HasName describableEntity) {
        addNane(entity, describableEntity.getName());
    }
    
    protected void addNane(Entity entity, String name) {
        entity.addProperty(new Property(null, PROP_NAME, ValueType.PRIMITIVE, name));
    }
    
    protected void addDescription(Entity entity, HasDescription describableEntity) {
        addDescription(entity, describableEntity.getDescription());
    }
    
    protected void addDescription(Entity entity, String description) {
        entity.addProperty(new Property(null, PROP_DESCRIPTION, ValueType.PRIMITIVE, description));
    }
    
    protected void setId(IdEntity idEntity, Entity entity) {
        if (checkProperty(entity, ID)) {
            idEntity.setId(Long.parseLong(getPropertyValue(entity, ID).toString()));
        } else if (checkProperty(entity, ID_ANNOTATION)) {
            idEntity.setId(Long.parseLong(getPropertyValue(entity, ID_ANNOTATION).toString()));
        } 
    }
    
    protected void setNameDescription(DescribableEntity thing, Entity entity) {
        setName(thing, entity);
        setDescription(thing, entity);
    }
    
    protected void setIdentifier(DescribableEntity describableEntity, Entity entity) {
        if (checkProperty(entity, PROP_DEFINITION)) {
            describableEntity.setIdentifier(getPropertyValue(entity, PROP_DEFINITION).toString());
        } else if (checkProperty(entity, PROP_NAME)) {
            describableEntity.setIdentifier(getPropertyValue(entity, PROP_NAME).toString());
        }
        
    }
    
    protected void setName(HasName describableEntity, Entity entity) {
       if (checkProperty(entity, PROP_NAME)) {
           describableEntity.setName(getPropertyValue(entity, PROP_NAME).toString());
       }
    }

    protected void setDescription(HasDescription describableEntity, Entity entity) {
        if (checkProperty(entity, PROP_DESCRIPTION)) {
            describableEntity.setDescription(getPropertyValue(entity, PROP_DESCRIPTION).toString());
        }
    }
    
    protected Object getPropertyValue(Entity entity, String name) {
        if (entity.getProperty(name) != null) {
            return entity.getProperty(name).getValue();
        }
        return "";
    }
    
    protected void addPhenomenonTime(HasPhenomenonTime phenomenonTime , Entity entity) {
        if (checkProperty(entity, PROP_PHENOMENON_TIME)) {
            Time time = parseTime(getPropertyValue(entity, PROP_PHENOMENON_TIME).toString());
            if (time instanceof TimeInstant) {
                phenomenonTime.setSamplingTimeStart(((TimeInstant) time).getValue().toDate());
                phenomenonTime.setSamplingTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                phenomenonTime.setSamplingTimeStart(((TimePeriod) time).getStart().toDate());
                phenomenonTime.setSamplingTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }
    }
    
    protected void mergeIdentifierNameDescription(DescribableEntity existing, DescribableEntity toMerge) {
        if (toMerge.isSetIdentifier()) {
            existing.setIdentifier(toMerge.getIdentifier());
        }
        mergeNameDescription(existing, toMerge);
    }
    
    protected void mergeNameDescription(DescribableEntity existing, DescribableEntity toMerge) {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
    }
    
    protected void mergeName(HasName existing, HasName toMerge) {
        if (toMerge.isSetName()) {
            existing.setName(toMerge.getName());
        }
    }

    protected void mergeDescription(HasDescription existing, HasDescription toMerge) {
        if (toMerge.isSetDescription()) {
            existing.setDescription(toMerge.getDescription());
        }
    }
    
    protected void mergeSamplingTime(HasPhenomenonTime existing, HasPhenomenonTime toMerge) {
        if (toMerge.hasSamplingTimeStart() && toMerge.hasSamplingTimeEnd()) {
            existing.setSamplingTimeStart(toMerge.getSamplingTimeStart());
            existing.setSamplingTimeEnd(toMerge.getSamplingTimeEnd());
        }
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
    
    protected Time parseTime(String timeString) {
        if (timeString.contains("/")) {
            String[] split = timeString.split("/");
            return createTime(DateTimeHelper.parseIsoString2DateTime(split[0]),
                    DateTimeHelper.parseIsoString2DateTime(split[1]));
        }
        return createTime(DateTimeHelper.parseIsoString2DateTime(timeString));
    }
}
