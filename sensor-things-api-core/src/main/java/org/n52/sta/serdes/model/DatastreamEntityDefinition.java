package org.n52.sta.serdes.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("VisibilityModifier")
public class DatastreamEntityDefinition extends STAEntityDefinition {

    public static String entityName = DATASTREAM;

    public static String entitySetName = DATASTREAMS;

    private static String[] navProps = new String[] {
            SENSOR,
            THING,
            OBSERVED_PROPERTY,
            OBSERVATIONS
    };

    private static String[] entityProps = new String[] {
            PROP_NAME,
            PROP_DESCRIPTION,
            PROP_OBSERVATION_TYPE,
            PROP_UOM,
            PROP_OBSERVED_AREA,
            PROP_PHENOMENON_TIME,
            PROP_RESULT_TIME
    };

    public static Set<String> navigationProperties = new HashSet<>(Arrays.asList(navProps));

    public static Set<String> entityProperties = new HashSet<>(Arrays.asList(entityProps));
}
