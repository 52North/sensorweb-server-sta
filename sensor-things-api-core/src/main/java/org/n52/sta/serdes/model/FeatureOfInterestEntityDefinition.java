package org.n52.sta.serdes.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("VisibilityModifier")
public class FeatureOfInterestEntityDefinition extends STAEntityDefinition {

    public static String entityName = FEATURE_OF_INTEREST;

    public static String entitySetName = FEATURES_OF_INTEREST;

    private static String[] navProps = new String[] {
            OBSERVATIONS
    };

    private static String[] entityProps = new String[] {
            PROP_NAME,
            PROP_DESCRIPTION,
            PROP_ENCODINGTYPE,
            PROP_FEATURE
    };

    public static Set<String> navigationProperties = new HashSet<>(Arrays.asList(navProps));

    public static Set<String> entityProperties = new HashSet<>(Arrays.asList(entityProps));
}
