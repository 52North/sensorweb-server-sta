package org.n52.sta.data.cndao.condition;

public interface EntityQueryConstants {

    String SENSOR = "Sensor";
    String OBSERVED_PROPERTY = "ObservedProperty";
    String THING = "Thing";
    String THINGS = "Things";
    String DATASTREAM = "Datastream";
    String DATASTREAMS = "Datastreams";
    String FEATUREOFINTEREST = "FeatureOfInterest";
    String LOCATIONS = "Locations";
    String HISTORICAL_LOCATIONS = "HistoricalLocations";
    String OBSERVATIONS = "Observations";


    String THING_TABLE = "platform";
    String THING_LOCATION_TABLE = "platform_location";
    String LOCATION_TABLE = "location";
    String DATASTREAM_TABLE = "dataset";
    String FEATURE_TABLE = "feature";
    String HISTORICAL_LOCATION_TABLE = "historical_location";
    String OBSERVATION_TABLE = "observation";
    String SENSOR_TABLE = "procedure";
    String FORMAT_TABLE = "format";
    String OBSERVED_PROPERTY_TABLE = "phenomenon";
    String LOCATION_HISTORICAL_LOCATION_TABLE = "location_historical_location";
    
    
    String STA_IDENTIFIER_FIELD = "sta_identifier";
    String STA_NAME_FIELD = "name";
    String STA_DESCRIPTION_FIELD = "description";
    String STA_PROPERTIES_FIELD = "properties";
    String STA_DEFINITION_FIELD = "definition";
    String SENSOR_METADATA_FIELD = "description_file";
    String HISTORICAL_LOCATION_TIME_FIELD = "time";
    
    
    String THING_ID_FIELD = "platform_id";
    String LOCATION_ID_FIELD = "location_id";
    String SENSOR_ID_FIELD = "procedure_id";
    String FORMAT_ID_FIELD = "format_id";
    String OBSERVED_PROPERTY_ID_FIELD = "phenomenon_id";
    String HISTORICAL_LOCATION_ID_FIELD = "historical_location_id";
    String FK_HISTORICAL_LOCATION_ID_FIELD = "fk_historical_location_id";
    String FK_LOCATION_ID_FIELD = "fk_location_id";
    String FK_THING_ID_FIELD = "fk_platform_id";
    String FK_SENSOR_ID_FIELD = "fk_procedure_id";
    String FK_FORMAT_ID_FIELD = "fk_format_id";
    String FK_OBSERVED_PROPERTY_ID_FIELD = "fk_phenomenon_id";

    String COULD_NOT_FIND_RELATED_PROPERTY = "Could not find related property: ";
    String ERROR_GETTING_FILTER_NO_PROP = "Error getting filter for Property: '%s'. No such " +
            "property in Entity.";
    String ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE =
            "Error getting filter for Property: '%s'. No such property with type %s in Entity.";
    String ERROR_TEMPLATE = "Operator \"%s\" is not supported for given arguments.";
    String INVALID_DATATYPE_CANNOT_CAST = "Invalid Datatypes found. Cannot cast ";
    String ERROR_INVALID_PARAMETER_ENTITY_TYPE = "Error getting entity from '%s'. No such parameter entity found";
}
