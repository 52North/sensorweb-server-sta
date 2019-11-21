package org.n52.sta.edm.provider.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HistoricalLocationEntityDefinition extends STAEntityDefinition {

    private static String[] navProps = new String[] {
            THING,
            LOCATIONS
    };

    private static String[] entityProps = new String[] {
            PROP_TIME
    };

    public static Set<String> navigationProperties = new HashSet<>(Arrays.asList(navProps));

    public static Set<String> entityProperties = new HashSet<>(Arrays.asList(entityProps));

    public static String entityName = HISTORICAL_LOCATION;

    public static String entitySetName = HISTORICAL_LOCATIONS;

}
