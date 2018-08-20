/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import java.util.List;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.NAVIGATION_LINK_ANNOTATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.SELF_LINK_ANNOTATION;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityAnnotator {

    public Entity annotateEntity(Entity entity, EdmEntityType entityType, String baseUri) {
        String selfLink = String.join("/", baseUri, entity.getId().getPath());
        entity.addProperty(new Property(null, SELF_LINK_ANNOTATION, ValueType.PRIMITIVE, selfLink));

        entityType.getNavigationPropertyNames().forEach(np -> {
            String navigationAnnotationName = np + NAVIGATION_LINK_ANNOTATION;
            EdmNavigationProperty navProp = entityType.getNavigationProperty(np);

            String navigationAnnotationValue = String.join("/", baseUri, entity.getId().getPath(), navProp.getName());
            entity.addProperty(new Property(null, navigationAnnotationName, ValueType.PRIMITIVE, navigationAnnotationValue));
        });

        return entity;
    }

}
