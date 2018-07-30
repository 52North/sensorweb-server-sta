/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mapping;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
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

    @Autowired
    EntityAnnotator entityAnnotator;

    public Entity createThingEntity(ThingEntity thing) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, ID_ANNOTATION, ValueType.PRIMITIVE, thing.getId()));
        entity.addProperty(new Property(null, PROP_NAME, ValueType.PRIMITIVE, thing.getName()));
        entity.addProperty(new Property(null, PROP_DESCRIPTION, ValueType.PRIMITIVE, thing.getDescription()));
        entity.addProperty(new Property(null, PROP_PROPERTIES, ValueType.PRIMITIVE, thing.getProperties()));

        entity.setType(ET_THING_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_THINGS_NAME, ID_ANNOTATION));

        return entity;
    }

}
