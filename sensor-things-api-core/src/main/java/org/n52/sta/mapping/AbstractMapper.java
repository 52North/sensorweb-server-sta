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

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmAny;
import org.apache.olingo.server.api.ODataApplicationException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations.HasDescription;
import org.n52.series.db.beans.HibernateRelations.HasName;
import org.n52.series.db.beans.HibernateRelations.HasPhenomenonTime;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.StaRelations;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.*;

import static org.n52.sta.edm.provider.SensorThingsEdmConstants.ID;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.*;
import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ES_DATASTREAMS_NAME;

public abstract class AbstractMapper<T> {

    @Autowired
    protected EntityCreationHelper entityCreationHelper;

    @Autowired
    private DatastreamMapper datastreamMapper;

    public abstract Entity createEntity(T t);

    protected abstract T createEntity(Entity entity) throws EdmPrimitiveTypeException;

    public abstract T merge(T existing, T toMerge) throws ODataApplicationException;

    public abstract Entity checkEntity(Entity entity) throws ODataApplicationException;

    protected void checkNavigationLink(Entity entity) throws ODataApplicationException {
        if (entity.getProperties().size() == 1) {
            checkPropertyValidity(ID, entity);
        } else {
            checkEntity(entity);
        }
    }

    protected void checkNameAndDescription(Entity entity) throws ODataApplicationException {
        checkPropertyValidity(PROP_NAME, entity);
        checkPropertyValidity(PROP_DESCRIPTION, entity);
    }

    protected void checkPropertyValidity(String propName, Entity entity) throws ODataApplicationException {
        if (!checkProperty(entity, propName)) {
            throw new ODataApplicationException(getMissingPropertyExceptionString(propName, entity),
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
    }

    protected String getMissingPropertyExceptionString(String propName, Entity entity) {
        return getMissingPropertyExceptionString(propName, entity.getType().replace("iot.", ""));
    }

    protected String getMissingPropertyExceptionString(String propName, ComplexValue complexValue) {
        return getMissingPropertyExceptionString(propName, complexValue.getTypeName());
    }

    private String getMissingPropertyExceptionString(String propName, String entity) {
        return String.format("The parameter '%s' is missing for in entity '%s'!", propName, entity);
    }

    private void checkPropertyValidity(ComplexValue complexValue, String propName) throws ODataApplicationException {
        if (!checkProperty(complexValue, propName)) {
            throw new ODataApplicationException(getMissingPropertyExceptionString(propName, complexValue),
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
    }

    protected void addNameDescriptionProperties(Entity entity, DescribableEntity describableEntity) {
        // add name
        String name = describableEntity.getIdentifier();
        if (describableEntity.isSetName()) {
            name = describableEntity.getName();
        }
        addName(entity, name);
        // add description
        String description = "Description of " + describableEntity.getIdentifier();
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
        addName(entity, describableEntity.getName());
    }

    protected void addName(Entity entity, String name) {
        entity.addProperty(new Property(null, PROP_NAME, ValueType.PRIMITIVE, name));
    }

    protected void addDescription(Entity entity, HasDescription describableEntity) {
        addDescription(entity, describableEntity.getDescription());
    }

    protected void addDescription(Entity entity, String description) {
        entity.addProperty(new Property(null, PROP_DESCRIPTION, ValueType.PRIMITIVE, description));
    }

    protected void setNameDescription(DescribableEntity thing, Entity entity) {
        setName(thing, entity);
        setDescription(thing, entity);
    }

    protected void setIdentifier(DescribableEntity describableEntity, Entity entity) {
        String rawIdentifier = null;
        if (checkProperty(entity, PROP_ID)) {
            try {
                rawIdentifier = EdmAny.getInstance().valueToString(getPropertyValue(entity, PROP_ID), false, 0, 0, 0, false);
            } catch (EdmPrimitiveTypeException e) {
                // This should never happen. Value was checked already
            }
        } else if (checkProperty(entity, PROP_DEFINITION)) {
            rawIdentifier = getPropertyValue(entity, PROP_DEFINITION).toString();
        }

        // URLEncode identifier.
        if (rawIdentifier != null) {
            try {
                describableEntity.setIdentifier(URLEncoder.encode(rawIdentifier.replace("\'", ""), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                // This should never happen.
            }
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

    protected void setDatastreams(StaRelations.Datastreams<?> datastreams, Entity entity) {
        if (checkNavigationLink(entity, ES_DATASTREAMS_NAME)) {
            Set<DatastreamEntity> ds = new LinkedHashSet<>();
            Iterator<Entity> iterator = entity.getNavigationLink(ES_DATASTREAMS_NAME).getInlineEntitySet().iterator();
            while (iterator.hasNext()) {
                ds.add(datastreamMapper.createEntity((Entity) iterator.next()));
            }
            datastreams.setDatastreams(ds);
        }

    }

    protected Object getPropertyValue(Entity entity, String name) {
        if (entity.getProperty(name) != null) {
            return entity.getProperty(name).getValue();
        }
        return "";
    }

    protected boolean checkProperty(ComplexValue complexValue, String name) {
        return getProperty(complexValue, name) != null;
    }

    protected Property getProperty(ComplexValue complexValue, String name) {
        Optional<Property> property = complexValue.getValue().stream().filter(p -> p.getName().equals(name)).findAny();
        return property.isPresent() ? property.get() : null;
    }

    protected Object getPropertyValue(ComplexValue complexValue, String name) {
        Property property = getProperty(complexValue, name);
        return property != null ? property.getValue() : "";
    }

    protected void addPhenomenonTime(HasPhenomenonTime phenomenonTime, Entity entity) {
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
     * @param start Start {@link DateTime}
     * @param end   End {@link DateTime}
     * @return Resulting {@link Time}
     */
    protected Time createTime(DateTime start, DateTime end) {
        if (start.equals(end)) {
            return createTime(start);
        } else {
            return new TimePeriod(start, end);
        }
    }

    protected Time parseTime(Object object) {
        if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            return new TimeInstant(new Instant(timestamp.getTime()));
        } else {
            String obj = object.toString();
            if (obj.contains("/")) {
                String[] split = obj.split("/");
                return createTime(DateTime.parse(split[0]),
                        DateTime.parse(split[1]));
            } else {
                return new TimeInstant(DateTime.parse(obj));
            }
        }
    }
}
