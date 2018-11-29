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
import org.apache.olingo.server.api.uri.queryoption.QueryOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    public Entity annotateEntity(Entity entity, EdmEntityType entityType, String baseUri, SelectOption selectOption) {
        // Do not annotate if there is nothing to annotate
        if (entity == null) {
            return null;
        }

        // Only annotate with selected Annotations if present
        List<String> selector = new ArrayList<>();
        boolean hasSelectOption = selectOption != null;
        if (hasSelectOption) {
            selectOption.getSelectItems().forEach(
                    elem -> selector.add(elem.getResourcePath().getUriResourceParts().get(0).getSegmentValue())
            );
        }

        if (!hasSelectOption) {
            String selfLinkValue = String.join("/", baseUri, entity.getId().getPath());
            Link selfLink = new Link();
            selfLink.setTitle(SELF_LINK_ANNOTATION);
            selfLink.setHref(selfLinkValue);
            entity.setSelfLink(selfLink);
        }

        entityType.getNavigationPropertyNames().forEach(np -> {
            if (!hasSelectOption || selector.contains(np)) {
                EdmNavigationProperty navProp = entityType.getNavigationProperty(np);

                String navigationAnnotationValue = String.join("/", baseUri, entity.getId().getPath(), navProp.getName());

                Link link = new Link();
                link.setTitle(np);
                link.setHref(navigationAnnotationValue);
                entity.getNavigationLinks().add(link);
            }
        });

        return entity;
    }

}
