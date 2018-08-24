/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mapping;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_DESCRIPTION;
import static org.n52.sta.edm.provider.entities.SensorEntityProvider.ES_SENSORS_NAME;
import static org.n52.sta.edm.provider.entities.SensorEntityProvider.ET_SENSOR_FQN;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DatastreamMapper {

    @Autowired
    EntityCreationHelper entityCreationHelper;

    public Entity createFeatureOfInterestEntity(DatastreamEntity datastream) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, ID_ANNOTATION, ValueType.PRIMITIVE, datastream.getId()));
        entity.addProperty(new Property(null, PROP_DESCRIPTION, ValueType.PRIMITIVE, datastream.getDescription()));
        entity.addProperty(new Property(null, AbstractSensorThingsEntityProvider.PROP_NAME, ValueType.PRIMITIVE, datastream.getName()));

        entity.addProperty(new Property(null, AbstractSensorThingsEntityProvider.PROP_OBSERVATION_TYPE, ValueType.PRIMITIVE, datastream.getObservationType().getFormat()));
        //TODO: construct JSON for UOM
        entity.addProperty(new Property(null, AbstractSensorThingsEntityProvider.PROP_UOM, ValueType.PRIMITIVE, datastream.getUnitOfMeasurement().getSymbol()));
        

        entity.setType(ET_SENSOR_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_SENSORS_NAME, ID_ANNOTATION));

        return entity;
    }

}
