/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
import static org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider.ES_HISTORICAL_LOCATIONS_NAME;
import static org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_FQN;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ES_THINGS_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_FQN;

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
import org.n52.sta.edm.provider.complextypes.FeatureComplexType;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class LocationEntityProvider extends AbstractSensorThingsEntityProvider {

    // Entity Type Name
    public static final String ET_LOCATION_NAME = "Location";
    public static final FullQualifiedName ET_LOCATION_FQN = new FullQualifiedName(NAMESPACE, ET_LOCATION_NAME);

    // Entity Set Name
    public static final String ES_LOCATIONS_NAME = "Locations";

    // Entity Navigation Property Names
    private static final String NAV_LINK_NAME_THINGS = ES_THINGS_NAME + NAVIGATION_LINK_ANNOTATION;
    private static final String NAV_LINK_NAME_HISTORICAL_LOCATIONS = ES_HISTORICAL_LOCATIONS_NAME + NAVIGATION_LINK_ANNOTATION;

    @Override
    protected CsdlEntityType createEntityType() {
        //create EntityType primitive properties
        CsdlProperty id = new CsdlProperty().setName(ID_ANNOTATION)
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
        CsdlProperty location = new CsdlProperty().setName(PROP_LOCATION)
                .setType(FeatureComplexType.CT_FEATURE_FQN)
                .setNullable(false);

        //create EntityType navigation links
        CsdlProperty selfLink = new CsdlProperty().setName(SELF_LINK_ANNOTATION)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);
        CsdlProperty navLinkThings = new CsdlProperty().setName(NAV_LINK_NAME_THINGS)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);
        CsdlProperty navLinkHistoricalLocations = new CsdlProperty().setName(NAV_LINK_NAME_HISTORICAL_LOCATIONS)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);

        // navigation property: Many optional to many optional
        CsdlNavigationProperty navPropThings = new CsdlNavigationProperty()
                .setName(ES_THINGS_NAME)
                .setType(ET_THING_FQN)
                .setCollection(true)
                .setPartner(ES_LOCATIONS_NAME);

        // navigation property: Many mandatory to many optional
        CsdlNavigationProperty navPropHistoricalLocations = new CsdlNavigationProperty()
                .setName(ES_HISTORICAL_LOCATIONS_NAME)
                .setType(ET_HISTORICAL_LOCATION_FQN)
                .setCollection(true)
                .setPartner(ES_LOCATIONS_NAME);

        List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
        navPropList.addAll(Arrays.asList(navPropThings, navPropHistoricalLocations));

        // create CsdlPropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName(ID_ANNOTATION);

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_LOCATION_NAME);
        entityType.setProperties(Arrays.asList(id,
                selfLink,
                name,
                description,
                encodingType,
                location,
                navLinkThings,
                navLinkHistoricalLocations));
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(navPropList);

        return entityType;
    }

    @Override
    protected CsdlEntitySet createEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_LOCATIONS_NAME);
        entitySet.setType(ET_LOCATION_FQN);

        CsdlNavigationPropertyBinding navPropThingBinding = new CsdlNavigationPropertyBinding();
        navPropThingBinding.setPath(ES_THINGS_NAME);
        navPropThingBinding.setTarget(ES_THINGS_NAME);

        CsdlNavigationPropertyBinding navPropHistoricalLocationBinding = new CsdlNavigationPropertyBinding();
        navPropHistoricalLocationBinding.setPath(ES_HISTORICAL_LOCATIONS_NAME);
        navPropHistoricalLocationBinding.setTarget(ES_HISTORICAL_LOCATIONS_NAME);

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
        navPropBindingList.addAll(Arrays.asList(navPropThingBinding, navPropHistoricalLocationBinding));
        entitySet.setNavigationPropertyBindings(navPropBindingList);

        return entitySet;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_LOCATION_FQN;
    }

}
