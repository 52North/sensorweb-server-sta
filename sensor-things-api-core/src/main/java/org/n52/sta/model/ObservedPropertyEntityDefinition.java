package org.n52.sta.edm.provider.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ObservedPropertyEntityDefinition extends STAEntityDefinition {

    private static String[] navProps = new String[] {
            DATASTREAMS
    };

    private static String[] entityProps = new String[] {
            PROP_NAME,
            PROP_DESCRIPTION,
            PROP_DEFINITION,
    };

    public static Set<String> navigationProperties = new HashSet<>(Arrays.asList(navProps));

    public static Set<String> entityProperties = new HashSet<>(Arrays.asList(entityProps));

    public static String entityName = OBSERVED_PROPERTY;

    public static String entitySetName = OBSERVED_PROPERTIES;

}
