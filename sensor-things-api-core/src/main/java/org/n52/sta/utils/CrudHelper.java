/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import java.io.InputStream;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.n52.sta.edm.provider.SensorThingsEdmConstants;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;
import org.n52.sta.service.deserializer.SensorThingsDeserializer;
import org.n52.sta.service.handler.crud.AbstractEntityCrudRequestHandler;
import org.n52.sta.service.handler.crud.EntityCrudRequestHandlerRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class CrudHelper implements InitializingBean {

    @Autowired
    private EntityCrudRequestHandlerRepository crudRequestHandlerReportitory;

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    private SensorThingsDeserializer deserializer;

    public DeserializerResult deserializeRequestBody(InputStream requestBody, UriInfo uriInfo)
            throws DeserializerException, ODataApplicationException {
        if (uriInfo.getUriResourceParts().size() > 1) {
            EntityQueryParams navigationPaths = navigationResolver.resolveUriResourceNavigationPaths(uriInfo.getUriResourceParts());
            DeserializerResult target = deserializer.entity(requestBody, navigationPaths.getTargetEntitySet().getEntityType());
            return addNavigationLink(target, getSourceEntity(navigationPaths));
        }
        return deserializer.entity(requestBody, navigationResolver
                .resolveRootUriResource(uriInfo.getUriResourceParts().get(0)).getEntityType());
    }

    public DeserializerResult addNavigationLink(DeserializerResult target, Entity sourceEntity) {
        Link link = new Link();
        link.setTitle(sourceEntity.getType().replaceAll(SensorThingsEdmConstants.NAMESPACE + ".", ""));
        link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
        link.setInlineEntity(sourceEntity);
        target.getEntity().getNavigationLinks().add(link);
        return target;
    }

    public Entity getSourceEntity(EntityQueryParams navigationPaths) {
        Entity entity = new Entity();
        entity.setType(navigationPaths.getSourceEntityType().getFullQualifiedName().getFullQualifiedNameAsString());
        addId(entity, navigationPaths.getSourceId());
        return entity;
    }

    public Entity addId(Entity entity, Long id) {
        return entity.addProperty(new Property(null, PROP_ID, ValueType.PRIMITIVE, id));
    }

    public AbstractEntityCrudRequestHandler getCrudEntityHanlder(UriInfo uriInfo) throws ODataApplicationException {
        if (uriInfo.getUriResourceParts().size() > 1) {
            return getUriResourceEntitySet(navigationResolver
                    .resolveUriResourceNavigationPaths(uriInfo.getUriResourceParts()).getTargetEntitySet().getEntityType().getName());
        }
        return getCrudEntityHanlder(navigationResolver.resolveRootUriResource(uriInfo.getUriResourceParts().get(0)));
    }

    public AbstractEntityCrudRequestHandler getCrudEntityHanlder(UriResourceEntitySet uriResourceEntitySet) {
        return getUriResourceEntitySet(uriResourceEntitySet.getEntityType().getName());
    }

    public AbstractEntityCrudRequestHandler getUriResourceEntitySet(String type) {
        return crudRequestHandlerReportitory.getEntityCrudRequestHandler(type);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.deserializer = new SensorThingsDeserializer(ContentType.JSON_NO_METADATA);
    }

}
