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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.n52.janmayen.Json;
import org.n52.series.db.beans.sta.AbstractStaEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_DESCRIPTION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_NAME;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_PROPERTIES;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.SELF_LINK_ANNOTATION;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ES_THINGS_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_FQN;
import org.n52.sta.utils.EntityAnnotator;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ThingMapper {

    @Autowired
    EntityCreationHelper entityCreationHelper;

//    @Autowired
//    EntityAnnotator entityAnnotator;
    public Entity createEntity(ThingEntity thing) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, ID_ANNOTATION, ValueType.PRIMITIVE, thing.getId()));
        entity.addProperty(new Property(null, PROP_NAME, ValueType.PRIMITIVE, thing.getName()));
        entity.addProperty(new Property(null, PROP_DESCRIPTION, ValueType.PRIMITIVE, thing.getDescription()));
        entity.addProperty(new Property(null, PROP_PROPERTIES, ValueType.COMPLEX, createProperties(thing.getProperties())));

        entity.setType(ET_THING_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_THINGS_NAME, ID_ANNOTATION));

        return entity;
    }

    public ComplexValue createProperties(String properties) {
        ComplexValue value = new ComplexValue();
        JsonNode propertyNode = Json.loadString(properties);
        Iterator<Entry<String, JsonNode>> iterator = propertyNode.fields();
        while (iterator.hasNext()) {
            Entry<String, JsonNode> entry = iterator.next();
            value.getValue().add(new Property(null, entry.getKey(), ValueType.PRIMITIVE, entry.getValue().asText()));
        }

        return value;

    }

}
