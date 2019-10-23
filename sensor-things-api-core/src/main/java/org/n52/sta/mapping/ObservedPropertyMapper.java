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
package org.n52.sta.mapping;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.core.edm.primitivetype.EdmAny;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.sta.edm.provider.SensorThingsEdmConstants;
import org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider;
import org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class ObservedPropertyMapper extends AbstractMapper<PhenomenonEntity> {

    private final EntityCreationHelper entityCreationHelper;

    @Autowired
    public ObservedPropertyMapper(EntityCreationHelper entityCreationHelper) {
        this.entityCreationHelper = entityCreationHelper;
    }

    @Override
    public Entity createEntity(PhenomenonEntity observedProperty) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null,
                AbstractSensorThingsEntityProvider.PROP_ID,
                ValueType.PRIMITIVE,
                observedProperty.getStaIdentifier()));
        addNameDescriptionProperties(entity, observedProperty);
        entity.addProperty(new Property(null,
                AbstractSensorThingsEntityProvider.PROP_DEFINITION,
                ValueType.PRIMITIVE,
                observedProperty.getIdentifier()));

        entity.setType(ObservedPropertyEntityProvider.ET_OBSERVED_PROPERTY_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(
                entity,
                ObservedPropertyEntityProvider.ES_OBSERVED_PROPERTIES_NAME,
                AbstractSensorThingsEntityProvider.PROP_ID));

        return entity;
    }

    //public Entity createEntity(ObservablePropertyEntity observedProperty) {
    //    return createEntity(observedProperty);
    //}

    @Override
    public ObservablePropertyEntity createEntity(Entity entity) {
        ObservablePropertyEntity phenomenon = new ObservablePropertyEntity();
        setStaIdentifier(phenomenon, entity);
        setIdentifier(phenomenon, entity);
        setName(phenomenon, entity);
        setDescription(phenomenon, entity);
        setDatastreams(phenomenon, entity);
        return phenomenon;
    }

    protected void setStaIdentifier(PhenomenonEntity idEntity, Entity entity) {
        String rawIdentifier = "";
        if (checkProperty(entity, SensorThingsEdmConstants.ID)) {
            try {
                rawIdentifier = EdmAny.getInstance().valueToString(
                        getPropertyValue(entity, AbstractSensorThingsEntityProvider.PROP_ID),
                        false,
                        0,
                        0,
                        0,
                        false);
            } catch (EdmPrimitiveTypeException e) {
                // This should never happen. Value was checked already
            }
        } else if (checkProperty(entity, SensorThingsEdmConstants.ID_ANNOTATION)) {
            rawIdentifier = getPropertyValue(entity, SensorThingsEdmConstants.ID_ANNOTATION).toString();
        } else {
            rawIdentifier = UUID.randomUUID().toString();
        }
        // URLEncode identifier.
        try {
            idEntity.setStaIdentifier(URLEncoder.encode(rawIdentifier, "utf-8"));
        } catch (UnsupportedEncodingException e) {

            // e.printStackTrace();
        }
    }

    @Override
    public PhenomenonEntity merge(PhenomenonEntity existing, PhenomenonEntity toMerge) {
        mergeIdentifierNameDescription(existing, toMerge);
        return existing;
    }

    public ObservablePropertyEntity mergeObservablePropertyEntity(ObservablePropertyEntity existing,
                                                                  ObservablePropertyEntity toMerge) {
        if (toMerge.hasDatastreams()) {
            toMerge.getDatastreams().forEach(d -> existing.addDatastream(d));
        }
        return (ObservablePropertyEntity) merge(existing, toMerge);
    }

    @Override
    public Entity checkEntity(Entity entity) throws ODataApplicationException {
        checkNameAndDescription(entity);
        checkPropertyValidity(AbstractSensorThingsEntityProvider.PROP_DEFINITION, entity);
        return entity;
    }

}
