package org.n52.sta.serdes.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ObservationEntityDefinition extends STAEntityDefinition {

    private static String[] navProps = new String[] {
            DATASTREAM,
            FEATURE_OF_INTEREST
    };

    private static String[] entityProps = new String[] {
            PROP_PHENOMENON_TIME,
            PROP_RESULT_TIME,
            PROP_RESULT_QUALITY,
            PROP_RESULT,
            PROP_VALID_TIME,
            PROP_PARAMETERS
    };

    public static Set<String> navigationProperties = new HashSet<>(Arrays.asList(navProps));

    public static Set<String> entityProperties = new HashSet<>(Arrays.asList(entityProps));

    public static String entityName = OBSERVATION;

    public static String entitySetName = OBSERVATIONS;

}
