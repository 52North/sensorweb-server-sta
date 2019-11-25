package org.n52.sta.serdes.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("VisibilityModifier")
public class SensorEntityDefinition extends STAEntityDefinition {

    public static String entityName = SENSOR;

    public static String entitySetName = SENSORS;

    private static String[] navProps = new String[] {
            DATASTREAMS
    };

    private static String[] entityProps = new String[] {
            PROP_NAME,
            PROP_DESCRIPTION,
            PROP_ENCODINGTYPE,
            PROP_METADATA
    };

    public static Set<String> navigationProperties = new HashSet<>(Arrays.asList(navProps));

    public static Set<String> entityProperties = new HashSet<>(Arrays.asList(entityProps));
}
