/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.edm.provider.entities;

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
import static org.n52.sta.edm.provider.SensorThingsEdmConstants.NAMESPACE;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.NAVIGATION_LINK_ANNOTATION;
import static org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider.ES_HISTORICAL_LOCATIONS_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ES_THINGS_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_FQN;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_NAME;
import org.springframework.stereotype.Component;
import static org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_FQN;

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

    // Entity Property Names
    private static final String PROP_ENCODING_TYPE = "encodingType";
    private static final String PROP_LOCATION = "location";

    // Entity Navigation Property Names
    private static final String NAV_LINK_NAME_THINGS = ES_THINGS_NAME + NAVIGATION_LINK_ANNOTATION;
    private static final String NAV_LINK_NAME_HISTORICAL_LOCATIONS = ES_HISTORICAL_LOCATIONS_NAME + NAVIGATION_LINK_ANNOTATION;

    @Override
    protected CsdlEntityType createEntityType() {
        //create EntityType properties
        CsdlProperty id = new CsdlProperty().setName(ID_ANNOTATION).setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty name = new CsdlProperty().setName(PROP_NAME).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty description = new CsdlProperty().setName(PROP_DESCRIPTION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty encodingType = new CsdlProperty().setName(PROP_ENCODING_TYPE).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty location = new CsdlProperty().setName(PROP_LOCATION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

        CsdlProperty selfLink = new CsdlProperty().setName(SELF_LINK_ANNOTATION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkThings = new CsdlProperty().setName(NAV_LINK_NAME_THINGS).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkHistoricalLocations = new CsdlProperty().setName(NAV_LINK_NAME_HISTORICAL_LOCATIONS).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

        // navigation property: one-to-many
        CsdlNavigationProperty navPropThings = new CsdlNavigationProperty()
                .setName(ES_THINGS_NAME)
                .setType(ET_THING_FQN)
                .setCollection(true)
                .setPartner(ES_LOCATIONS_NAME);

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
        entityType.setProperties(Arrays.asList(id, selfLink, name, description, encodingType, location, navLinkThings, navLinkHistoricalLocations));
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(navPropList);

        return entityType;
    }

    @Override
    protected CsdlEntitySet createEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_LOCATIONS_NAME);
        entitySet.setType(ET_LOCATION_FQN);

        CsdlNavigationPropertyBinding navPropLocationBinding = new CsdlNavigationPropertyBinding();
        navPropLocationBinding.setPath(ES_THINGS_NAME); // the path from entity type to navigation property
        navPropLocationBinding.setTarget(ES_THINGS_NAME); //target entitySet, where the nav prop points to

        CsdlNavigationPropertyBinding navPropHistoricalLocationBinding = new CsdlNavigationPropertyBinding();
        navPropHistoricalLocationBinding.setPath(ES_HISTORICAL_LOCATIONS_NAME);
        navPropHistoricalLocationBinding.setTarget(ES_HISTORICAL_LOCATIONS_NAME);

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
        navPropBindingList.addAll(Arrays.asList(navPropLocationBinding, navPropHistoricalLocationBinding));
        entitySet.setNavigationPropertyBindings(navPropBindingList);

        return entitySet;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_LOCATION_FQN;
    }

}
