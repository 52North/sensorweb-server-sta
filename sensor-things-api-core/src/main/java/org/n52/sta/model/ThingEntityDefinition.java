package org.n52.sta.edm.provider.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ThingEntityDefinition extends STAEntityDefinition {

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

    public static String entityName = THING;

    public static String entitySetName = THINGS;
}
