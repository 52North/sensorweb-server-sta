/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.n52.sta.edm.provider.SensorThingsEdmConstants;
import org.n52.sta.edm.provider.complextypes.OpenComplexType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ThingEntityProvider extends AbstractSensorThingsEntityProvider {

    // Entity Type Name
    public static final String ET_THING_NAME = "Thing";
    public static final FullQualifiedName ET_THING_FQN =
            new FullQualifiedName(SensorThingsEdmConstants.NAMESPACE, ET_THING_NAME);

    // Entity Set Name
    public static final String ES_THINGS_NAME = "Things";

    @Override
    protected CsdlEntityType createEntityType() {
        List<CsdlProperty> properties = createCsdlProperties();

        List<CsdlNavigationProperty> navigationProperties = createCsdlNavigationProperties();

        // create CsdlPropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName(PROP_ID);

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_THING_NAME);
        entityType.setProperties(properties);
        entityType.setOpenType(true);
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(navigationProperties);

        return entityType;
    }

    @Override
    protected CsdlEntitySet createEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_THINGS_NAME);
        entitySet.setType(ET_THING_FQN);

        CsdlNavigationPropertyBinding navPropLocationBinding = new CsdlNavigationPropertyBinding();
        // the path from entity type to navigation property
        navPropLocationBinding.setPath(LocationEntityProvider.ES_LOCATIONS_NAME);
        //target entitySet, where the nav prop points to
        navPropLocationBinding.setTarget(LocationEntityProvider.ES_LOCATIONS_NAME);

        CsdlNavigationPropertyBinding navPropDatastreamBinding = new CsdlNavigationPropertyBinding();
        navPropDatastreamBinding.setPath(DatastreamEntityProvider.ES_DATASTREAMS_NAME);
        navPropDatastreamBinding.setTarget(DatastreamEntityProvider.ES_DATASTREAMS_NAME);

        CsdlNavigationPropertyBinding navPropHistoricalLocationBinding = new CsdlNavigationPropertyBinding();
        navPropHistoricalLocationBinding.setPath(HistoricalLocationEntityProvider.ES_HISTORICAL_LOCATIONS_NAME);
        navPropHistoricalLocationBinding.setTarget(HistoricalLocationEntityProvider.ES_HISTORICAL_LOCATIONS_NAME);

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
        navPropBindingList.addAll(Arrays.asList(
                navPropLocationBinding,
                navPropDatastreamBinding,
                navPropHistoricalLocationBinding));
        entitySet.setNavigationPropertyBindings(navPropBindingList);

        return entitySet;
    }

    private List<CsdlProperty> createCsdlProperties() {
        //create EntityType primitive properties
        CsdlProperty id = new CsdlProperty().setName(PROP_ID)
                .setType(EdmPrimitiveTypeKind.Any.getFullQualifiedName())
                .setNullable(true);
        CsdlProperty name = new CsdlProperty().setName(PROP_NAME)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);
        CsdlProperty description = new CsdlProperty().setName(PROP_DESCRIPTION)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false);

        //create EntityType complex properties
        CsdlProperty properties = new CsdlProperty().setName(PROP_PROPERTIES)
                .setType(OpenComplexType.CT_OPEN_TYPE_FQN)
                .setNullable(true);

        return Arrays.asList(
                id,
                name,
                description,
                properties);
    }

    private List<CsdlNavigationProperty> createCsdlNavigationProperties() {
        // navigation property: many optional to many optional
        CsdlNavigationProperty navPropLocations = new CsdlNavigationProperty()
                .setName(LocationEntityProvider.ES_LOCATIONS_NAME)
                .setType(LocationEntityProvider.ET_LOCATION_FQN)
                .setCollection(true)
                .setPartner(ES_THINGS_NAME);

        // navigation property: one mandatory to many optional
        CsdlNavigationProperty navPropDatastreams = new CsdlNavigationProperty()
                .setName(DatastreamEntityProvider.ES_DATASTREAMS_NAME)
                .setType(DatastreamEntityProvider.ET_DATASTREAM_FQN)
                .setCollection(true)
                .setPartner(ET_THING_NAME);

        // navigation property: one mandatory to many optional
        CsdlNavigationProperty navPropHistoricalLocations = new CsdlNavigationProperty()
                .setName(HistoricalLocationEntityProvider.ES_HISTORICAL_LOCATIONS_NAME)
                .setType(HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_FQN)
                .setCollection(true)
                .setPartner(ET_THING_NAME);

        return Arrays.asList(navPropLocations, navPropDatastreams, navPropHistoricalLocations);
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_THING_FQN;
    }

}
