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
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ES_LOCATIONS_NAME;
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ET_LOCATION_FQN;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ES_THINGS_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_FQN;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_NAME;

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
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class HistoricalLocationEntityProvider extends AbstractSensorThingsEntityProvider {

    // Entity Type Name
    public static final String ET_HISTORICAL_LOCATION_NAME = "HistoricalLocation";
    public static final FullQualifiedName ET_HISTORICAL_LOCATION_FQN = new FullQualifiedName(NAMESPACE, ET_HISTORICAL_LOCATION_NAME);

    //Entity Set Name
    public static final String ES_HISTORICAL_LOCATIONS_NAME = "HistoricalLocations";

    @Override
    protected CsdlEntityType createEntityType() {

        List<CsdlProperty> properties = createCsdlProperties();

        List<CsdlNavigationProperty> navigationProperties = createCsdlNavigationProperties();

        // create CsdlPropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName(PROP_ID);

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_HISTORICAL_LOCATION_NAME);
        entityType.setProperties(properties);
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(navigationProperties);

        return entityType;
    }

    @Override
    protected CsdlEntitySet createEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_HISTORICAL_LOCATIONS_NAME);
        entitySet.setType(ET_HISTORICAL_LOCATION_FQN);

        CsdlNavigationPropertyBinding navPropLocationBinding = new CsdlNavigationPropertyBinding();
        navPropLocationBinding.setPath(ES_LOCATIONS_NAME);
        navPropLocationBinding.setTarget(ES_LOCATIONS_NAME);

        CsdlNavigationPropertyBinding navPropThingBinding = new CsdlNavigationPropertyBinding();
        navPropThingBinding.setPath(ET_THING_NAME); // the path from entity type to navigation property
        navPropThingBinding.setTarget(ES_THINGS_NAME); //target entitySet, where the nav prop points to

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
        navPropBindingList.addAll(Arrays.asList(navPropLocationBinding, navPropThingBinding));
        entitySet.setNavigationPropertyBindings(navPropBindingList);

        return entitySet;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_HISTORICAL_LOCATION_FQN;
    }

    private List<CsdlProperty> createCsdlProperties() {
        //create EntityType properties
        CsdlProperty id = new CsdlProperty().setName(PROP_ID)
                .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName())
                .setNullable(false);

        CsdlProperty time = new CsdlProperty().setName(PROP_TIME)
                .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName())
                .setNullable(false);

        return Arrays.asList(
                id,
                time);
    }

    private List<CsdlNavigationProperty> createCsdlNavigationProperties() {
        // navigation property: many optional to many mandatory
        CsdlNavigationProperty navPropLocations = new CsdlNavigationProperty()
                .setName(ES_LOCATIONS_NAME)
                .setType(ET_LOCATION_FQN)
                .setCollection(true)
                .setPartner(ES_HISTORICAL_LOCATIONS_NAME);

        // navigation property: many optional to one mandatory
        CsdlNavigationProperty navPropThings = new CsdlNavigationProperty()
                .setName(ET_THING_NAME)
                .setType(ET_THING_FQN)
                .setNullable(false)
                .setPartner(ES_HISTORICAL_LOCATIONS_NAME);

        return Arrays.asList(navPropLocations, navPropThings);
    }

}
