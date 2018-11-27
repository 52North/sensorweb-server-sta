/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import static org.n52.sta.edm.provider.SensorThingsEdmConstants.SELF_LINK_ANNOTATION;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
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
        // Do not annotate if there is nothing to annotate
        if (entity == null) {
            return null;
        }

        String selfLinkValue = String.join("/", baseUri, entity.getId().getPath());

        Link selfLink = new Link();
        selfLink.setTitle(SELF_LINK_ANNOTATION);
        selfLink.setHref(selfLinkValue);
        entity.setSelfLink(selfLink);

        entityType.getNavigationPropertyNames().forEach(np -> {
            EdmNavigationProperty navProp = entityType.getNavigationProperty(np);

            String navigationAnnotationValue = String.join("/", baseUri, entity.getId().getPath(), navProp.getName());

            Link link = new Link();
            link.setTitle(np);
            link.setHref(navigationAnnotationValue);
            entity.getNavigationLinks().add(link);
        });

        return entity;
    }

}
