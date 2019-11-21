package org.n52.sta.edm.provider.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LocationEntityDefinition extends STAEntityDefinition {

    private static String[] navProps = new String[] {
            THINGS,
            HISTORICAL_LOCATIONS
    };

    private static String[] entityProps = new String[] {
            PROP_NAME,
            PROP_DESCRIPTION,
            PROP_ENCODINGTYPE,
            PROP_LOCATION
    };

    public static Set<String> navigationProperties = new HashSet<>(Arrays.asList(navProps));

    public static Set<String> entityProperties = new HashSet<>(Arrays.asList(entityProps));

    public static String entityName = LOCATION;

    public static String entitySetName = LOCATIONS;

}
