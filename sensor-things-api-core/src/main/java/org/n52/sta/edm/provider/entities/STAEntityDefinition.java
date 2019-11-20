package org.n52.sta.edm.provider.entities;

public abstract class STAEntityDefinition {

    public static String DATASTREAMS = "Datastreams";
    public static String OBSERVATIONS = "Observations";
    public static String THINGS = "Things";
    public static String LOCATIONS = "Locations";
    public static String HISTORICAL_LOCATIONS = "HistoricalLocations";
    public static String SENSORS = "Sensors";
    public static String OBSERVED_PROPERTIES = "ObservedProperties";
    public static String FEATURES_OF_INTEREST = "FeaturesOfInterest";

    public static String[] allCollections = new String[] {
            DATASTREAMS,
            OBSERVATIONS,
            THINGS,
            LOCATIONS,
            HISTORICAL_LOCATIONS,
            SENSORS,
            OBSERVED_PROPERTIES,
            FEATURES_OF_INTEREST
    };

    public static String DATASTREAM = "Datastream";
    public static String OBSERVATION = "Observation";
    public static String THING = "Thing";
    public static String LOCATION = "Location";
    public static String HISTORICAL_LOCATION = "HistoricalLocation";
    public static String SENSOR = "Sensor";
    public static String OBSERVED_PROPERTY = "ObservedProperty";
    public static String FEATURE_OF_INTEREST = "FeatureofInterest";

    // Entity Property Names
    public static String PROP_ID = "id";
    public static String PROP_SELF_LINK = "selfLink";
    public static String PROP_DEFINITION = "definition";
    public static String PROP_DESCRIPTION = "description";
    public static String PROP_ENCODINGTYPE = "encodingType";
    public static String PROP_FEATURE = "feature";
    public static String PROP_LOCATION = "location";
    public static String PROP_NAME = "name";
    public static String PROP_OBSERVATION_TYPE = "observationType";
    public static String PROP_OBSERVED_AREA = "observedArea";
    public static String PROP_PARAMETERS = "parameters";
    public static String PROP_PHENOMENON_TIME = "phenomenonTime";
    public static String PROP_PROPERTIES = "properties";
    public static String PROP_RESULT = "result";
    public static String PROP_RESULT_QUALITY = "resultQuality";
    public static String PROP_RESULT_TIME = "resultTime";
    public static String PROP_TIME = "time";
    public static String PROP_UOM = "unitOfMeasurement";
    public static String PROP_VALID_TIME = "validTime";
    public static String PROP_METADATA = "metadata";
    public static String PROP_SYMBOL = "symbol";
}
