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
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ES_LOCATIONS_NAME;
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ET_LOCATION_FQN;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ES_THINGS_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_FQN;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_NAME;
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

    // Entity Property Names
    private static final String PROP_TIME = "time";

    // Entity Navigation Property Names
    private static final String NAV_LINK_NAME_THING = ET_THING_NAME + NAVIGATION_LINK_ANNOTATION;
    private static final String NAV_LINK_NAME_LOCATIONS = ES_LOCATIONS_NAME + NAVIGATION_LINK_ANNOTATION;

    @Override
    protected CsdlEntityType createEntityType() {
        //create EntityType properties
        CsdlProperty id = new CsdlProperty().setName(ID_ANNOTATION).setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty time = new CsdlProperty().setName(PROP_TIME).setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());

        CsdlProperty selfLink = new CsdlProperty().setName(SELF_LINK_ANNOTATION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkThing = new CsdlProperty().setName(NAV_LINK_NAME_THING).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkLocations = new CsdlProperty().setName(NAV_LINK_NAME_LOCATIONS).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

        // navigation property: one-to-many
        CsdlNavigationProperty navPropThings = new CsdlNavigationProperty()
                .setName(ET_THING_NAME)
                .setType(ET_THING_FQN)
                .setNullable(false)
                .setPartner(ES_HISTORICAL_LOCATIONS_NAME);

        CsdlNavigationProperty navPropHistoricalLocations = new CsdlNavigationProperty()
                .setName(ES_LOCATIONS_NAME)
                .setType(ET_LOCATION_FQN)
                .setCollection(true)
                .setPartner(ES_HISTORICAL_LOCATIONS_NAME);

        List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
        navPropList.addAll(Arrays.asList(navPropThings, navPropHistoricalLocations));

        // create CsdlPropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName(ID_ANNOTATION);

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_HISTORICAL_LOCATION_NAME);
        entityType.setProperties(Arrays.asList(id, selfLink, time, navLinkThing, navLinkLocations));
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(navPropList);

        return entityType;
    }

    @Override
    protected CsdlEntitySet createEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_HISTORICAL_LOCATIONS_NAME);
        entitySet.setType(ET_HISTORICAL_LOCATION_FQN);

        CsdlNavigationPropertyBinding navPropLocationBinding = new CsdlNavigationPropertyBinding();
        navPropLocationBinding.setPath(ET_THING_NAME); // the path from entity type to navigation property
        navPropLocationBinding.setTarget(ES_THINGS_NAME); //target entitySet, where the nav prop points to

        CsdlNavigationPropertyBinding navPropHistoricalLocationBinding = new CsdlNavigationPropertyBinding();
        navPropHistoricalLocationBinding.setPath(ES_LOCATIONS_NAME);
        navPropHistoricalLocationBinding.setTarget(ES_LOCATIONS_NAME);

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
        navPropBindingList.addAll(Arrays.asList(navPropLocationBinding, navPropHistoricalLocationBinding));
        entitySet.setNavigationPropertyBindings(navPropBindingList);

        return entitySet;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_HISTORICAL_LOCATION_FQN;
    }

}
