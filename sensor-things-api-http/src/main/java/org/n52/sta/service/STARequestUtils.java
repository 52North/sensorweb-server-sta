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
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.exception.STAInvalidUrlException;
import org.n52.sta.service.query.QueryOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class STARequestUtils {

    final String IDENTIFIER_REGEX = "\\(['\\-0-9a-zA-Z]+\\)";
    final String COLLECTION_REGEX =
            "Observations|Datastreams|Things|Sensors|Locations|HistoricalLocations|" +
                    "FeaturesOfInterest|ObservedProperties";
    final String IDENTIFIED_BY_DATASTREAM_REGEX =
            "Datastreams" + IDENTIFIER_REGEX + "/(Sensor|ObservedProperty|Thing|Observations)";
    final String IDENTIFIED_BY_OBSERVATION_REGEX =
            "Observations" + IDENTIFIER_REGEX + "/(Datastream|FeatureOfInterest)";
    final String IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX =
            "HistoricalLocations" + IDENTIFIER_REGEX + "/Thing";
    final String IDENTIFIED_BY_THING_REGEX =
            "Things" + IDENTIFIER_REGEX + "/(Datastreams|HistoricalLocations|Locations)";
    final String IDENTIFIED_BY_LOCATION_REGEX =
            "Locations" + IDENTIFIER_REGEX + "/(Things|HistoricalLocations)}";
    final String IDENTIFIED_BY_SENSOR_REGEX =
            "Sensors" + IDENTIFIER_REGEX + "/Datastreams";
    final String IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX =
            "ObservedProperties" + IDENTIFIER_REGEX + "/Datastreams";
    final String IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX =
            "FeaturesOfInterest" + IDENTIFIER_REGEX + "/Observations";

    final String ENTITY_IDENTIFIED_BY_DATASTREAM_PATH =
            "{entity:Datastreams" + IDENTIFIER_REGEX + "}/{target:Sensor|ObservedProperty|Thing}";
    final String ENTITY_IDENTIFIED_BY_OBSERVATION_PATH =
            "{entity:Observations" + IDENTIFIER_REGEX + "}/{target:Datastream|FeatureOfInterest}";
    final String ENTITY_IDENTIFIED_BY_HISTORICAL_LOCATION_PATH =
            "{entity:HistoricalLocations" + IDENTIFIER_REGEX + "}/{target:Thing}";

    final String COLLECTION_IDENTIFIED_BY_DATASTREAM_PATH =
            "{entity:Datastreams" + IDENTIFIER_REGEX + "}/{target:Observations}";
    final String COLLECTION_IDENTIFIED_BY_THING_PATH =
            "{entity:Things" + IDENTIFIER_REGEX + "}/{target:Datastreams|HistoricalLocations|Locations}";
    final String COLLECTION_IDENTIFIED_BY_LOCATION_PATH =
            "{entity:Locations" + IDENTIFIER_REGEX + "}/{target:Things|HistoricalLocations}";
    final String COLLECTION_IDENTIFIED_BY_SENSOR_PATH =
            "{entity:Sensors" + IDENTIFIER_REGEX + "}/{target:Datastreams}";
    final String COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH =
            "{entity:ObservedProperties" + IDENTIFIER_REGEX + "}/{target:Datastreams}";
    final String COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH =
            "{entity:FeaturesOfInterest" + IDENTIFIER_REGEX + "}/{target:Observations}";

    final static Map<String, Class> collectionNameToClass;

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

    final Pattern byIdPattern = Pattern.compile("(" + COLLECTION_REGEX + ")" + IDENTIFIER_REGEX);
    final Pattern byDatastreamPattern = Pattern.compile(IDENTIFIED_BY_DATASTREAM_REGEX);
    final Pattern byObservationPattern = Pattern.compile(IDENTIFIED_BY_OBSERVATION_REGEX);
    final Pattern byHistoricalLocationPattern = Pattern.compile(IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX);
    final Pattern byLocationPattern = Pattern.compile(IDENTIFIED_BY_LOCATION_REGEX);
    final Pattern byThingPattern = Pattern.compile(IDENTIFIED_BY_THING_REGEX);
    final Pattern bySensorsPattern = Pattern.compile(IDENTIFIED_BY_SENSOR_REGEX);
    final Pattern byObservedPropertiesPattern = Pattern.compile(IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX);
    final Pattern byFeaturesOfInterestPattern = Pattern.compile(IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX);

    STAInvalidUrlException validateURL(StringBuffer requestURL,
                       EntityServiceRepository serviceRepository,
                       int rootUrlLength) {
        String[] uriResources = requestURL.substring(rootUrlLength).split("/");

        STAInvalidUrlException ex;
        ex = validateURISyntax(uriResources);
        if (ex != null) {
            return ex;
        }
        ex = validateURISemantic(uriResources, serviceRepository);
        if (ex != null) {
            return ex;
        }
        return null;
    }

    /**
     * Validates a given URI syntactically.
     *
     * @param uriResources URI of the Request split by "/"
     * @return STAInvalidUrlException if URI is malformed
     */
    private STAInvalidUrlException validateURISyntax(String[] uriResources) {
        // Validate URL syntax via Regex
        // Skip validation if no navigationPath is provided as Spring already validated syntax
        if (uriResources.length > 1) {
            // check iteratively and fail-fast
            for (int i = 0; i < uriResources.length; i++) {
                if (byIdPattern.matcher(uriResources[i]).matches()) {
                    // Resource is adressed by Id
                    // e.g. Things(1)
                } else {
                    // Resource is addressed by relation to other entity
                    // e.g. Datastreams(1)/Thing
                    if (i > 0) {
                        // Look back at last resource and check if association is valid
                        String resource = uriResources[i - 1] + "/" + uriResources[i];
                        if (!(byDatastreamPattern.matcher(resource).matches()
                                || byHistoricalLocationPattern.matcher(resource).matches()
                                || byLocationPattern.matcher(resource).matches()
                                || byThingPattern.matcher(resource).matches()
                                || byFeaturesOfInterestPattern.matcher(resource).matches()
                                || byObservationPattern.matcher(resource).matches()
                                || bySensorsPattern.matcher(resource).matches()
                                || byObservedPropertiesPattern.matcher(resource).matches())) {
                            return new STAInvalidUrlException("Url is invalid. "
                                    + uriResources[i - 1]
                                    + "/" + uriResources[i]
                                    + " is not a valid resource path.");

                        }
                    } else {
                        return new STAInvalidUrlException("Url is invalid. "
                                + uriResources[i]
                                + " is not a valid resource.");
                    }
                }
            }
        }
        return null;
    }

    /**
     * This function validates a given URI semantically by checking if all Entities referenced in the navigation
     * exists. As URI is syntactically valid indices can be hard-coded.
     *
     * @param uriResources URI of the Request split by "/"
     * @return STAInvalidUrlException if URI is malformed
     */
    private STAInvalidUrlException validateURISemantic(String[] uriResources,
                                                         EntityServiceRepository serviceRepository) {
        // Check if this is Request to root collection. They are always valid
        if (uriResources.length == 1 && !uriResources[0].contains("(")) {
            return null;
        }
        // Parse first navigation Element
        String[] sourceEntity = uriResources[0].split("\\(");
        String sourceId = sourceEntity[1].replace(")", "");
        String sourceType = sourceEntity[0];

        if (!serviceRepository.getEntityService(sourceType).existsEntity(sourceId)) {
            return new STAInvalidUrlException("No Entity: " + uriResources[0] + " found!");
        }

        // Iterate over the rest of the uri validating each resource
        for (int i = 1, uriResourcesLength = uriResources.length; i < uriResourcesLength; i++) {
            String[] targetEntity = uriResources[i].split("\\(");
            String targetType = targetEntity[0];
            String targetId = null;
            if (targetEntity.length == 1) {
                // Resource is addressed by related Entity
                // e.g. /Datastreams(1)/Thing/
                // Getting id directly as it is needed for next iteration
                targetId = serviceRepository.getEntityService(targetType)
                        .getEntityIdByRelatedEntity(sourceId, sourceType);
                if (targetId == null) {
                    return new STAInvalidUrlException("No Entity: "
                            + uriResources[i]
                            + " associated with "
                            + uriResources[i - 1]
                            + " found!");
                }
            } else {
                // Resource is addressed by Id directly
                // e.g. /Things(1)/
                // Only checking exists as Id is already known
                targetId = targetEntity[1].replace(")", "");
                if (!serviceRepository.getEntityService(sourceType)
                        .existsEntityByRelatedEntity(sourceId, targetType, targetId)) {
                    return new STAInvalidUrlException("No Entity: "
                            + uriResources[i]
                            + " associated with "
                            + uriResources[i - 1]
                            + " found!");
                }
            }

            // Store target as source for next iteration
            sourceId = targetId;
            sourceType = targetType;
        }
        // As no error is thrown the uri is valid
        return null;
    }

    //TODO: actually implement parsing of Query options
    static QueryOptions createQueryOptions(Map<String, String> raw) {

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
