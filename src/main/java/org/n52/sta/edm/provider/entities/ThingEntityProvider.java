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
import static org.n52.sta.edm.provider.SensorThingsEdmConstants.*;
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ES_LOCATIONS_NAME;
import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ET_LOCATION_FQN;
import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ES_DATASTREAMS_NAME;
import static org.n52.sta.edm.provider.entities.DatastreamEntityProvider.ET_DATASTREAM_FQN;
import static org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider.ES_HISTORICAL_LOCATIONS_NAME;
import org.springframework.stereotype.Component;
import static org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_FQN;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ThingEntityProvider extends AbstractSensorThingsEntityProvider {

    // Entity Type Name
    public static final String ET_THING_NAME = "Thing";
    public static final FullQualifiedName ET_THING_FQN = new FullQualifiedName(NAMESPACE, ET_THING_NAME);

    // Entity Set Name
    public static final String ES_THINGS_NAME = "Things";

    // Entity Property Names
    private static final String PROP_PROPERTIES = "properties";

    // Entity Navigation Property Names
    private static final String NAV_LINK_NAME_DATASTREAMS = ES_DATASTREAMS_NAME + NAVIGATION_LINK_ANNOTATION;
    private static final String NAV_LINK_NAME_LOCATIONS = ES_LOCATIONS_NAME + NAVIGATION_LINK_ANNOTATION;
    private static final String NAV_LINK_NAME_HISTORICAL_LOCATIONS = ES_HISTORICAL_LOCATIONS_NAME + NAVIGATION_LINK_ANNOTATION;

    @Override
    protected CsdlEntityType createEntityType() {
        //create EntityType properties
        CsdlProperty id = new CsdlProperty().setName(ID_ANNOTATION).setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty name = new CsdlProperty().setName(PROP_NAME).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty description = new CsdlProperty().setName(PROP_DESCRIPTION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty properties = new CsdlProperty().setName(PROP_PROPERTIES).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

        CsdlProperty selfLink = new CsdlProperty().setName(SELF_LINK_ANNOTATION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkDatastreams = new CsdlProperty().setName(NAV_LINK_NAME_DATASTREAMS).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkLocations = new CsdlProperty().setName(NAV_LINK_NAME_LOCATIONS).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkHistoricalLocations = new CsdlProperty().setName(NAV_LINK_NAME_HISTORICAL_LOCATIONS).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

        // navigation property: one-to-many
        CsdlNavigationProperty navPropLocations = new CsdlNavigationProperty()
                .setName(ES_LOCATIONS_NAME)
                .setType(ET_LOCATION_FQN)
                .setCollection(true)
                .setPartner(ES_THINGS_NAME);

        CsdlNavigationProperty navPropDatastreams = new CsdlNavigationProperty()
                .setName(ES_DATASTREAMS_NAME)
                .setType(ET_DATASTREAM_FQN)
                .setCollection(true)
                .setPartner(ET_THING_NAME);

        CsdlNavigationProperty navPropHistoricalLocations = new CsdlNavigationProperty()
                .setName(ES_HISTORICAL_LOCATIONS_NAME)
                .setType(ET_HISTORICAL_LOCATION_FQN)
                .setCollection(true)
                .setPartner(ET_THING_NAME);

        List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
        navPropList.addAll(Arrays.asList(navPropLocations, navPropDatastreams, navPropHistoricalLocations));
        navPropList.addAll(Arrays.asList(navPropLocations, navPropHistoricalLocations));

        // create CsdlPropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName(ID_ANNOTATION);

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_THING_NAME);
        entityType.setProperties(Arrays.asList(id, selfLink, name, description, properties, navLinkDatastreams, navLinkLocations, navLinkHistoricalLocations));
        entityType.setProperties(Arrays.asList(id, selfLink, name, description, properties, navLinkLocations, navLinkHistoricalLocations));
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(navPropList);

        return entityType;
    }

    @Override
    protected CsdlEntitySet createEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_THINGS_NAME);
        entitySet.setType(ET_THING_FQN);

        CsdlNavigationPropertyBinding navPropLocationBinding = new CsdlNavigationPropertyBinding();
        navPropLocationBinding.setPath(ES_LOCATIONS_NAME); // the path from entity type to navigation property
        navPropLocationBinding.setTarget(ES_LOCATIONS_NAME); //target entitySet, where the nav prop points to

        CsdlNavigationPropertyBinding navPropDatastreamBinding = new CsdlNavigationPropertyBinding();
        navPropDatastreamBinding.setPath(ES_DATASTREAMS_NAME);
        navPropDatastreamBinding.setTarget(ES_DATASTREAMS_NAME);

        CsdlNavigationPropertyBinding navPropHistoricalLocationBinding = new CsdlNavigationPropertyBinding();
        navPropHistoricalLocationBinding.setPath(ES_HISTORICAL_LOCATIONS_NAME);
        navPropHistoricalLocationBinding.setTarget(ES_HISTORICAL_LOCATIONS_NAME);

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
        navPropBindingList.addAll(Arrays.asList(navPropLocationBinding, navPropDatastreamBinding, navPropHistoricalLocationBinding));
        navPropBindingList.addAll(Arrays.asList(navPropLocationBinding, navPropHistoricalLocationBinding));
        entitySet.setNavigationPropertyBindings(navPropBindingList);

        return entitySet;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_THING_FQN;
    }

}
