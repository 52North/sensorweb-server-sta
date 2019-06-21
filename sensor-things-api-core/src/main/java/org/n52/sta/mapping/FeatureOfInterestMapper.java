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
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.server.api.ODataApplicationException;
import org.locationtech.jts.geom.Geometry;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.om.features.SfConstants;
import org.n52.shetland.util.JavaHelper;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_FEATURE;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;
import static org.n52.sta.edm.provider.entities.FeatureOfInterestEntityProvider.ES_FEATURES_OF_INTEREST_NAME;
import static org.n52.sta.edm.provider.entities.FeatureOfInterestEntityProvider.ET_FEATURE_OF_INTEREST_FQN;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 *
 */
@Component
public class FeatureOfInterestMapper extends AbstractLocationGeometryMapper<AbstractFeatureEntity<?>> {

    @Autowired
    private EntityCreationHelper entityCreationHelper;

    @Override
    public Entity createEntity(AbstractFeatureEntity<?> feature) {
        Entity entity = new Entity();
        entity.addProperty(new Property(null, PROP_ID, ValueType.PRIMITIVE, feature.getIdentifier()));
        addNameDescriptionProperties(entity, feature);
        addGeometry(entity, feature);
        entity.setType(ET_FEATURE_OF_INTEREST_FQN.getFullQualifiedNameAsString());
        entity.setId(entityCreationHelper.createId(entity, ES_FEATURES_OF_INTEREST_NAME, PROP_ID));
        return entity;
    }

    @Override
    public AbstractFeatureEntity<?> createEntity(Entity entity) {
        FeatureEntity featureOfInterest = new FeatureEntity();
        setIdentifier(featureOfInterest, entity);
        featureOfInterest.setIdentifier(JavaHelper.generateID(featureOfInterest.getIdentifier()));
        setName(featureOfInterest, entity);
        setDescription(featureOfInterest, entity);
        if (checkProperty(entity, PROP_FEATURE)) {
            Property featureProperty = entity.getProperty(PROP_FEATURE);
            if (featureProperty.getValueType().equals(ValueType.PRIMITIVE) && featureProperty.getValue() instanceof Geospatial) {
                featureOfInterest.setGeometryEntity(parseGeometry((Geospatial) featureProperty.getValue()));
            }
        }
        featureOfInterest.setFeatureType(createFeatureType(featureOfInterest.getGeometry()));
        return featureOfInterest;
    }

    public AbstractFeatureEntity<?> createFeatureOfInterest(LocationEntity location) {
        FeatureEntity featureOfInterest = new FeatureEntity();
        featureOfInterest.setIdentifier(location.getName());
        featureOfInterest.setName(location.getName());
        featureOfInterest.setDescription(location.getDescription());
        featureOfInterest.setGeometryEntity(location.getGeometryEntity());
        featureOfInterest.setFeatureType(createFeatureType(location.getGeometry()));
        return featureOfInterest;
    }

    @Override
    public AbstractFeatureEntity<?> merge(AbstractFeatureEntity<?> existing, AbstractFeatureEntity<?> toMerge) {
        mergeIdentifierNameDescription(existing, toMerge);
        mergeGeometry(existing, toMerge);
        mergeFeatureType(existing);
        return existing;
    }

    private void mergeFeatureType(AbstractFeatureEntity<?> existing) {
        FormatEntity featureType = createFeatureType(existing.getGeometry());
        if (!featureType.getFormat().equals(existing.getFeatureType().getFormat())) {
            existing.setFeatureType(featureType);
        }
    }

    private FormatEntity createFeatureType(Geometry geometry) {
        FormatEntity formatEntity = new FormatEntity();
        if (geometry != null) {
            switch (geometry.getGeometryType()) {
                case "Point":
                    formatEntity.setFormat(SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT);
                    break;
                case "LineString":
                    formatEntity.setFormat(SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE);
                    break;
                case "Polygon":
                    formatEntity.setFormat(SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE);
                    break;
                default:
                    formatEntity.setFormat(SfConstants.SAMPLING_FEAT_TYPE_SF_SPATIAL_SAMPLING_FEATURE);
                    break;
            }
            return formatEntity;
        }
        return formatEntity.setFormat(SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_FEATURE);
    }

    @Override
    public Entity checkEntity(Entity entity) throws ODataApplicationException {
        checkNameAndDescription(entity);
        checkPropertyValidity(PROP_FEATURE, entity);
        checkEncodingType(entity);
        return entity;
    }

}
