/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Helper class for Entity creation
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityCreationHelper {

    @Autowired
    ApplicationContext ctx;

    /**
     * Creates an ID for an entity
     *
     * @param entity the Entity to create the ID for
     * @param entitySetName the name of the EntitySet the Entity belongs to
     * @param idPropertyName name of the ID property
     * @return the ID as URI
     */
    public URI createId(Entity entity, String entitySetName, String idPropertyName) {
        return createId(entity, entitySetName, idPropertyName, null);
    }

    private URI createId(Entity entity, String entitySetName, String idPropertyName, String navigationName) {
        try {
            StringBuilder sb = new StringBuilder(entitySetName).append("(");
            final Property property = entity.getProperty(idPropertyName);
            sb.append(property.asPrimitive()).append(")");
            if (navigationName != null) {
                sb.append("/").append(navigationName);
            }
            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create (Atom) id for entity: " + entity, e);
        }
    }

}
