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
import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ES_DATASTREAMS_NAME;
import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ET_DATASTREAM_FQN;

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
public class ObservedPropertyEntityProvider extends AbstractSensorThingsEntityProvider {

    // Entity Type Name
    public static final String ET_OBSERVED_PROPERTY_NAME = "ObservedProperty";
    public static final FullQualifiedName ET_OBSERVED_PROPERTY_FQN = new FullQualifiedName(NAMESPACE, ET_OBSERVED_PROPERTY_NAME);

    // Entity Set Name
    public static final String ES_OBSERVED_PROPERTIES_NAME = "ObservedProperties";

    @Override
    protected CsdlEntityType createEntityType() {

        List<CsdlProperty> properties = createCsdlProperties();

        List<CsdlNavigationProperty> navigationProperties = createCsdlNavigationProperties();

        // create CsdlPropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName(PROP_ID);

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_OBSERVED_PROPERTY_NAME);
        entityType.setProperties(properties);
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(navigationProperties);

        return entityType;
    }

    @Override
    protected CsdlEntitySet createEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_OBSERVED_PROPERTIES_NAME);
        entitySet.setType(ET_OBSERVED_PROPERTY_FQN);

        CsdlNavigationPropertyBinding navPropDatastreamBinding = new CsdlNavigationPropertyBinding();
        navPropDatastreamBinding.setPath(ES_DATASTREAMS_NAME);
        navPropDatastreamBinding.setTarget(ES_DATASTREAMS_NAME);

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
        navPropBindingList.add(navPropDatastreamBinding);
        entitySet.setNavigationPropertyBindings(navPropBindingList);

        return entitySet;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_OBSERVED_PROPERTY_FQN;
    }

    private List<CsdlProperty> createCsdlProperties() {
        //create EntityType properties
        CsdlProperty id = new CsdlProperty().setName(PROP_ID)
                .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName())
                .setNullable(false);
        CsdlProperty name = new CsdlProperty().setName(PROP_NAME)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);
        CsdlProperty description = new CsdlProperty().setName(PROP_DESCRIPTION)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);
        CsdlProperty definition = new CsdlProperty().setName(PROP_DEFINITION)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(true);

        return Arrays.asList(
                id,
                name,
                description,
                definition);
    }

    private List<CsdlNavigationProperty> createCsdlNavigationProperties() {
         // navigation property: one mandatory to many optional
        CsdlNavigationProperty navPropDatastreams = new CsdlNavigationProperty()
                .setName(ES_DATASTREAMS_NAME)
                .setType(ET_DATASTREAM_FQN)
                .setCollection(true)
                .setPartner(ET_OBSERVED_PROPERTY_NAME);

        return Arrays.asList(navPropDatastreams);
    }

}
