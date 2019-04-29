/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.service.handler.crud;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;

import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.n52.series.db.beans.IdEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.edm.provider.SensorThingsEdmConstants;
import org.n52.sta.mapping.AbstractMapper;
import org.n52.sta.service.response.EntityResponse;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractEntityCrudRequestHandler<T extends IdEntity> {

    @Autowired
    private EntityServiceRepository serviceRepository;

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    protected abstract AbstractMapper<T> getMapper();

    @Transactional(rollbackFor=Exception.class)
    public EntityResponse handleCreateEntityRequest(Entity entity, List<UriResource> resourcePaths) throws ODataApplicationException {
        UriResourceEntitySet uriResourceEntitySet = getUriResourceEntitySet(resourcePaths);
        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();
        Entity responseEntity = handleCreateEntityRequest(entity);
        EntityResponse response = new EntityResponse();
        if (resourcePaths.size() > 1) {
            response.setEntitySet(navigationResolver.resolveUriResourceNavigationPaths(resourcePaths).getTargetEntitySet());
        } else {
            response.setEntitySet(responseEntitySet);
        }
        response.setEntity(responseEntity);
        return response;
    }

    protected abstract Entity handleCreateEntityRequest(Entity entity) throws ODataApplicationException;

    @Transactional(rollbackFor=Exception.class)
    public EntityResponse handleUpdateEntityRequest(Entity entity, HttpMethod method,
            List<UriResource> resourcePaths) throws ODataApplicationException {
        UriResourceEntitySet uriResourceEntitySet = getUriResourceEntitySet(resourcePaths);
        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();
        Entity responseEntity = handleUpdateEntityRequest(checkId(entity, uriResourceEntitySet), method);
        EntityResponse response = new EntityResponse();
        response.setEntitySet(responseEntitySet);
        response.setEntity(responseEntity);
        return response;
    }


    protected abstract Entity handleUpdateEntityRequest(Entity entity, HttpMethod httpMethod) throws ODataApplicationException;

    @Transactional(rollbackFor=Exception.class)
    public EntityResponse handleDeleteEntityRequest(List<UriResource> resourcePaths) throws ODataApplicationException {
        UriResourceEntitySet uriResourceEntitySet = getUriResourceEntitySet(resourcePaths);
        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();
        handleDeleteEntityRequest(getId(uriResourceEntitySet));
        EntityResponse response = new EntityResponse();
        response.setEntitySet(responseEntitySet);
//        response.setEntity(responseEntity);
        return response;
    }

    protected abstract void handleDeleteEntityRequest(Long id) throws ODataApplicationException;

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

    protected Entity mapToEntity(T entity) {
        return entity != null ? getMapper().createEntity(entity) : null;
    }

    private Entity checkId(Entity entity, UriResourceEntitySet uriResourceEntitySet) throws ODataApplicationException {
        return entity.addProperty(new Property(null, PROP_ID, ValueType.PRIMITIVE, getId(uriResourceEntitySet)));
    }

    private Long getId(UriResourceEntitySet uriResourceEntitySet) throws ODataApplicationException {
        if (!uriResourceEntitySet.getKeyPredicates().isEmpty()) {
            for (UriParameter uriParameter : uriResourceEntitySet.getKeyPredicates()) {
                if (uriParameter.getName() != null && uriParameter.getName().equals(SensorThingsEdmConstants.ID)
                        && uriParameter.getText() != null && !uriParameter.getText().isEmpty()) {
                    return Long.parseLong(uriParameter.getText());
                }
            }
        }
        throw new ODataApplicationException("The request URL does not contain an required 'ID'!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }

}
