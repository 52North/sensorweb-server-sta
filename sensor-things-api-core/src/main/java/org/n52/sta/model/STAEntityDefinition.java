package org.n52.sta.edm.provider.entities;

public abstract class STAEntityDefinition {

    public static final String DATASTREAMS = "Datastreams";
    public static final String OBSERVATIONS = "Observations";
    public static final String THINGS = "Things";
    public static final String LOCATIONS = "Locations";
    public static final String HISTORICAL_LOCATIONS = "HistoricalLocations";
    public static final String SENSORS = "Sensors";
    public static final String OBSERVED_PROPERTIES = "ObservedProperties";
    public static final String FEATURES_OF_INTEREST = "FeaturesOfInterest";

    public static final String[] allCollections = new String[] {
            DATASTREAMS,
            OBSERVATIONS,
            THINGS,
            LOCATIONS,
            HISTORICAL_LOCATIONS,
            SENSORS,
            OBSERVED_PROPERTIES,
            FEATURES_OF_INTEREST
    };

    public static final String DATASTREAM = "Datastream";
    public static final String OBSERVATION = "Observation";
    public static final String THING = "Thing";
    public static final String LOCATION = "Location";
    public static final String HISTORICAL_LOCATION = "HistoricalLocation";
    public static final String SENSOR = "Sensor";
    public static final String OBSERVED_PROPERTY = "ObservedProperty";
    public static final String FEATURE_OF_INTEREST = "FeatureofInterest";

    // Entity Property Names
    public static final String PROP_ID = "id";
    public static final String PROP_SELF_LINK = "selfLink";
    public static final String PROP_DEFINITION = "definition";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_ENCODINGTYPE = "encodingType";
    public static final String PROP_FEATURE = "feature";
    public static final String PROP_LOCATION = "location";
    public static final String PROP_NAME = "name";
    public static final String PROP_OBSERVATION_TYPE = "observationType";
    public static final String PROP_OBSERVED_AREA = "observedArea";
    public static final String PROP_PARAMETERS = "parameters";
    public static final String PROP_PHENOMENON_TIME = "phenomenonTime";
    public static final String PROP_PROPERTIES = "properties";
    public static final String PROP_RESULT = "result";
    public static final String PROP_RESULT_QUALITY = "resultQuality";
    public static final String PROP_RESULT_TIME = "resultTime";
    public static final String PROP_TIME = "time";
    public static final String PROP_UOM = "unitOfMeasurement";
    public static final String PROP_VALID_TIME = "validTime";
    public static final String PROP_METADATA = "metadata";
    public static final String PROP_SYMBOL = "symbol";
}
