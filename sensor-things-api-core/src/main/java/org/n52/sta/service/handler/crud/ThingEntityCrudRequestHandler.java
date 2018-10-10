package org.n52.sta.service.handler.crud;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.service.response.EntityResponse;

public class ThingEntityCrudRequestHandler extends AbstractEntityCrudRequestHandler {

    @Override
    public EntityResponse handleCreateEntityRequest(DeserializerResult deserializerResult)
            throws ODataApplicationException {
        EntityResponse response = null;
        Entity entity = deserializerResult.getEntity();
        if (entity != null) {
            AbstractSensorThingsEntityService<?> entityService =
                    getUriResourceEntitySet(entity.getType().replace("iot.", ""));
            entityService.create(entity);
        }
        // TODO Auto-generated method stub
        return response;
    }


    @Override
    public EntityResponse handleUpdateEntityRequest(DeserializerResult deserializerResult)
            throws ODataApplicationException {
        EntityResponse response = null;
        Entity entity = deserializerResult.getEntity();
        if (entity != null) {
            AbstractSensorThingsEntityService<?> entityService =
                    getUriResourceEntitySet(entity.getType().replace("iot.", ""));
            entityService.update(entity);
        }
        // TODO Auto-generated method stub
        return response;
    }


    @Override
    public EntityResponse handleDeleteEntityRequest(DeserializerResult deserializerResult)
            throws ODataApplicationException {
        EntityResponse response = null;
        Entity entity = deserializerResult.getEntity();
        if (entity != null) {
            AbstractSensorThingsEntityService<?> entityService =
                    getUriResourceEntitySet(entity.getType().replace("iot.", ""));
            entityService.delete(entity);
        }
        // TODO Auto-generated method stub
        return response;
    }
}
