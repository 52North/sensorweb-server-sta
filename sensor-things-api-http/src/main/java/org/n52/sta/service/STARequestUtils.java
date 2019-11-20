package org.n52.sta.service;

import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.service.query.QueryOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class STARequestUtils {

    protected final String IDENTIFIER_REGEX = "\\(['\\-0-9a-zA-Z]+\\)";
    protected final String COLLECTION_REGEX =
            "Observations|Datastreams|Things|Sensors|Locations|HistoricalLocations|" +
                    "FeaturesOfInterest|ObservedProperties";
    protected final String IDENTIFIED_BY_DATASTREAM_REGEX =
            "Datastreams" + IDENTIFIER_REGEX + "/(Sensor|ObservedProperty|Thing|Observations)";
    protected final String IDENTIFIED_BY_OBSERVATION_REGEX =
            "Observations" + IDENTIFIER_REGEX + "/(Datastream|FeatureOfInterest)";
    protected final String IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX =
            "HistoricalLocations" + IDENTIFIER_REGEX + "/Thing";
    protected final String IDENTIFIED_BY_THING_REGEX =
            "Things" + IDENTIFIER_REGEX + "/(Datastreams|HistoricalLocations|Locations)";
    protected final String IDENTIFIED_BY_LOCATION_REGEX =
            "Locations" + IDENTIFIER_REGEX + "/(Things|HistoricalLocations)}";
    protected final String IDENTIFIED_BY_SENSOR_REGEX =
            "Sensors" + IDENTIFIER_REGEX + "/Datastreams";
    protected final String IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX =
            "ObservedProperties" + IDENTIFIER_REGEX + "/Datastreams";
    protected final String IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX =
            "FeaturesOfInterest" + IDENTIFIER_REGEX + "/Observations";

    protected final String IDENTIFIED_BY_DATASTREAM_PATH =
            "{entity:Datastreams" + IDENTIFIER_REGEX + "}/{target:(Sensor|ObservedProperty|Thing|Observations)}";
    protected final String IDENTIFIED_BY_OBSERVATION_PATH =
            "{entity:Observations" + IDENTIFIER_REGEX + "}/{target:(Datastream|FeatureOfInterest)}";
    protected final String IDENTIFIED_BY_HISTORICAL_LOCATION_PATH =
            "{entity:HistoricalLocations" + IDENTIFIER_REGEX + "}/{target:Thing}";
    protected final String IDENTIFIED_BY_THING_PATH =
            "{entity:Things" + IDENTIFIER_REGEX + "}/{target:(Datastreams|HistoricalLocations|Locations)}";
    protected final String IDENTIFIED_BY_LOCATION_PATH =
            "{entity:Locations" + IDENTIFIER_REGEX + "}/{target:(Things|HistoricalLocations)}";
    protected final String IDENTIFIED_BY_SENSOR_PATH =
            "{entity:Sensors" + IDENTIFIER_REGEX + "}/{target:Datastreams}";
    protected final String IDENTIFIED_BY_OBSERVED_PROPERTY_PATH =
            "{entity:ObservedProperties" + IDENTIFIER_REGEX + "}/{target:Datastreams}";
    protected final String IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH =
            "{entity:FeaturesOfInterest" + IDENTIFIER_REGEX + "}/{target:Observations}";

    protected final static Map<String, Class> collectionNameToClass;

    static {
        HashMap<String, Class> map = new HashMap<>();
        map.put("Things", PlatformEntity.class);
        map.put("Locations", LocationEntity.class);
        map.put("Datastreams", DatastreamEntity.class);
        map.put("HistoricalLocations", HistoricalLocationEntity.class);
        map.put("Sensors", ProcedureEntity.class);
        map.put("Observations", DataEntity.class);
        map.put("ObservedProperties", PhenomenonEntity.class);
        map.put("FeaturesOfInterest", AbstractFeatureEntity.class);
        collectionNameToClass = Collections.unmodifiableMap(map);
    }

    //TODO: actually implement parsing of Query options
    protected static QueryOptions createQueryOptions(Map<String, String> raw) {

        return new QueryOptions() {
            @Override
            public String getBaseURI() {
                return null;
            }

            @Override
            public boolean hasCountOption() {
                return false;
            }

            @Override
            public boolean getCountOption() {
                return false;
            }

            @Override
            public int getTopOption() {
                return 0;
            }

            @Override
            public boolean hasSkipOption() {
                return false;
            }

            @Override
            public int getSkipOption() {
                return 0;
            }

            @Override
            public boolean hasOrderByOption() {
                return false;
            }

            @Override
            public OrderByOption getOrderByOption() {
                return null;
            }

            @Override
            public boolean hasSelectOption() {
                return false;
            }

            @Override
            public Set<String> getSelectOption() {
                return null;
            }

            @Override
            public boolean hasExpandOption() {
                return false;
            }

            @Override
            public Set<String> getExpandOption() {
                return null;
            }

            @Override
            public boolean hasFilterOption() {
                return false;
            }

            @Override
            public Set<String> getFilterOption() {
                return null;
            }
        };
    }
}
