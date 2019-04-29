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
package org.n52.sta.edm.provider.entities;

import static org.n52.sta.edm.provider.SensorThingsEdmConstants.NAMESPACE;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ES_OBSERVATIONS_NAME;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ET_OBSERVATION_FQN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class FeatureOfInterestEntityProvider extends AbstractSensorThingsEntityProvider {

    // Entity Type Name
    public static final String ET_FEATURE_OF_INTEREST_NAME = "FeatureOfInterest";
    public static final FullQualifiedName ET_FEATURE_OF_INTEREST_FQN = new FullQualifiedName(NAMESPACE, ET_FEATURE_OF_INTEREST_NAME);

    // Entity Set Name
    public static final String ES_FEATURES_OF_INTEREST_NAME = "FeaturesOfInterest";

    @Override
    protected CsdlEntityType createEntityType() {
        List<CsdlProperty> properties = createCsdlProperties();
        List<CsdlNavigationProperty> navigationProperties = createCsdlNavigationProperties();

        // create CsdlPropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName(PROP_ID);

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_FEATURE_OF_INTEREST_NAME);
        entityType.setProperties(properties);
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(navigationProperties);

        return entityType;
    }

    @Override
    protected CsdlEntitySet createEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_FEATURES_OF_INTEREST_NAME);
        entitySet.setType(ET_FEATURE_OF_INTEREST_FQN);

        CsdlNavigationPropertyBinding navPropFeatureOfInterestBinding = new CsdlNavigationPropertyBinding();
        navPropFeatureOfInterestBinding.setPath(ES_OBSERVATIONS_NAME);
        navPropFeatureOfInterestBinding.setTarget(ES_OBSERVATIONS_NAME);

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
        navPropBindingList.add(navPropFeatureOfInterestBinding);
        entitySet.setNavigationPropertyBindings(navPropBindingList);

        return entitySet;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_FEATURE_OF_INTEREST_FQN;
    }

    private List<CsdlProperty> createCsdlProperties() {
        //create EntityType primitive properties
        CsdlProperty id = new CsdlProperty().setName(PROP_ID)
                .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName())
                .setNullable(false);
        CsdlProperty name = new CsdlProperty().setName(PROP_NAME)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);
        CsdlProperty description = new CsdlProperty().setName(PROP_DESCRIPTION)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);
        CsdlProperty encodingType = new CsdlProperty().setName(PROP_ENCODINGTYPE)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);

        //create EntityType complex properties
//        CsdlProperty feature = new CsdlProperty().setName(PROP_FEATURE)
//                .setType(FeatureComplexType.CT_FEATURE_FQN)
//                .setNullable(false);
        CsdlProperty feature = new CsdlProperty().setName(PROP_FEATURE)
                .setType(EdmPrimitiveTypeKind.Geometry.getFullQualifiedName())
                .setNullable(false);

        return Arrays.asList(
                id,
                name,
                description,
                encodingType,
                feature);
    }

    private List<CsdlNavigationProperty> createCsdlNavigationProperties() {

        // navigation property: one mandatory to many optional
        CsdlNavigationProperty navPropFeatureOfInterest = new CsdlNavigationProperty()
                .setName(ES_OBSERVATIONS_NAME)
                .setType(ET_OBSERVATION_FQN)
                .setCollection(true)
                .setPartner(ET_FEATURE_OF_INTEREST_NAME);

        return Arrays.asList(navPropFeatureOfInterest);
    }
}
