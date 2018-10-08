/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.NAVIGATION_LINK_ANNOTATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.SELF_LINK_ANNOTATION;
import org.springframework.stereotype.Component;

/**
 * Helper class to annotate Entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityAnnotator {

    /**
     * Annotates an entity with it's self link and navigation links
     *
     * @param entity the Entity to annotate
     * @param entityType the type of the entity to annotate
     * @param baseUri the baseUri for the service deployed
     * @return the annotated Entity
     */
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
