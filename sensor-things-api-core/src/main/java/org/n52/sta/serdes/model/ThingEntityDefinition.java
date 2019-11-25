package org.n52.sta.serdes.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("VisibilityModifier")
public class ThingEntityDefinition extends STAEntityDefinition {

    public static String entityName = THING;

    public static String entitySetName = THINGS;

    private static String[] navProps = new String[] {
            DATASTREAMS,
            LOCATIONS,
            HISTORICAL_LOCATIONS
    };

    private static String[] entityProps = new String[] {
            PROP_NAME,
            PROP_DESCRIPTION,
            PROP_PROPERTIES
    };

    public static Set<String> navigationProperties = new HashSet<>(Arrays.asList(navProps));

    public static Set<String> entityProperties = new HashSet<>(Arrays.asList(entityProps));
}
