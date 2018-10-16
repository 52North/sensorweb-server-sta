package org.n52.sta.service.handler.crud;

import java.util.List;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.service.response.EntityResponse;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractEntityCrudRequestHandler {
    
    @Autowired
    private EntityServiceRepository serviceRepository;
    
    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    public EntityResponse handleCreateEntityRequest(DeserializerResult deserializerResult) throws ODataApplicationException {
        return null;
    }
    
    public EntityResponse handleUpdateEntityRequest(DeserializerResult deserializerResult, HttpMethod httpMethod) throws ODataApplicationException {
        return null;
    }
    
    public EntityResponse handleDeleteEntityRequest(DeserializerResult deserializerResult) throws ODataApplicationException {
        return null;
    }
    
    protected UriResourceEntitySet getUriResourceEntitySet(List<UriResource> resourcePaths) throws ODataApplicationException {
        return navigationResolver.resolveRootUriResource(resourcePaths.get(0));
    }

    protected AbstractSensorThingsEntityService<?, ?> getEntityService(UriResourceEntitySet uriResourceEntitySet) {
        return getUriResourceEntitySet(uriResourceEntitySet.getEntityType().getName());
    }

    protected AbstractSensorThingsEntityService<?, ?> getUriResourceEntitySet(String type) {
        return serviceRepository.getEntityService(type);
    }
    
    protected AbstractSensorThingsEntityService<?, ?> getEntityService(EntityTypes type) {
        return serviceRepository.getEntityService(type);
    }
}
