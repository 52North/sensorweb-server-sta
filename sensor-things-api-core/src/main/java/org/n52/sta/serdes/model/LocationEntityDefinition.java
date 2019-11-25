package org.n52.sta.serdes.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("VisibilityModifier")
public class LocationEntityDefinition extends STAEntityDefinition {

    public static String entityName = LOCATION;

    public static String entitySetName = LOCATIONS;

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
}
