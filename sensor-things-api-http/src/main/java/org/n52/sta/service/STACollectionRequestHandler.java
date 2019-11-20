package org.n52.sta.service;

import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.exception.STACRUDException;
import org.n52.sta.exception.STAInvalidUrlException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Pattern;

@RequestMapping("/v2")
@RestController
public class STACollectionRequestHandler extends STARequestUtils {

    private final EntityServiceRepository serviceRepository;

    private final Pattern byIdPattern = Pattern.compile(COLLECTION_REGEX);

    private final String mappingPrefix = "**/";

    private final Pattern byDatastreamPattern = Pattern.compile(IDENTIFIED_BY_DATASTREAM_REGEX);
    private final Pattern byObservationPattern = Pattern.compile(IDENTIFIED_BY_OBSERVATION_REGEX);
    private final Pattern byHistoricalLocationPattern = Pattern.compile(IDENTIFIED_BY_HISTORICAL_LOCATION_REGEX);
    private final Pattern byLocationPattern = Pattern.compile(IDENTIFIED_BY_LOCATION_REGEX);
    private final Pattern byThingPattern = Pattern.compile(IDENTIFIED_BY_THING_REGEX);
    private final Pattern bySensorsPattern = Pattern.compile(IDENTIFIED_BY_SENSOR_REGEX);
    private final Pattern byObservedPropertiesPattern = Pattern.compile(IDENTIFIED_BY_OBSERVED_PROPERTY_REGEX);
    private final Pattern byFeaturesOfInterestPattern = Pattern.compile(IDENTIFIED_BY_FEATURE_OF_INTEREST_REGEX);

    private final int rootUrlLength;

    public STACollectionRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                       EntityServiceRepository serviceRepository) {
        rootUrlLength = rootUrl.length();
        this.serviceRepository = serviceRepository;
    }

    /**
     * Matches all requests on Collections referenced directly
     * e.g. /Datastreams
     *
     * @param collectionName name of the collection. Automatically set by Spring via @PathVariable
     * @param queryOptions   query options. Automatically set by Spring via @RequestParam
     */
    @GetMapping(
            value = "/{collectionName:" + COLLECTION_REGEX + "}",
            produces = "application/json"
    )
    public Object readCollectionDirect(@PathVariable String collectionName,
                                       @RequestParam Map<String, String> queryOptions) throws STACRUDException {
        return serviceRepository
                .getEntityService(collectionName)
                .getEntityCollection(createQueryOptions(queryOptions));
    }

    /**
     * Matches all requests on Entities not referenced directly via id but via referenced entity.
     * e.g. /Datastreams(52)/Thing
     *
     * @param entity  composite of entity and referenced entity. Automatically set by Spring via @PathVariable
     * @param request full request
     * @return JSON String representing Entity
     */
    @GetMapping(
            value = {mappingPrefix + IDENTIFIED_BY_THING_PATH,
                     mappingPrefix + IDENTIFIED_BY_LOCATION_PATH,
                     mappingPrefix + IDENTIFIED_BY_OBSERVED_PROPERTY_PATH,
                     mappingPrefix + IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH,
                     mappingPrefix + IDENTIFIED_BY_SENSOR_PATH
            },
            produces = "application/json"
    )
    public Object readRelatedCollection(@PathVariable String entity,
                                        @PathVariable String target,
                                        @RequestParam Map<String, String> queryOptions,
                                        HttpServletRequest request) throws STACRUDException {

        // TODO(specki): check if something needs to be cut from the front like rootUrl
        // TODO(specki): short-circuit if url is only one element as spring already validated that when the path matched
        // TODO(specki): Error serialization for nice output

        String[] uriResources = request.getServletPath().split("/");

        STAInvalidUrlException ex;
        ex = validateURISyntax(uriResources);
        if (ex != null) {
            return ex.getMessage();
        }
        ex = validateURISemantic(uriResources);
        if (ex != null) {
            return ex.getMessage();
        }

        String sourceType = entity.split("\\(")[0];
        String sourceId = entity.split("\\(")[1].replace(")", "");

        return serviceRepository.getEntityService(target)
                .getEntityCollectionByRelatedEntity(sourceId, sourceType, createQueryOptions(queryOptions));
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
                            return new STAInvalidUrlException("Url is invalid. " + uriResources[i - 1]
                                    + "/" + uriResources[i] + "is not a valid resource path.");

                        }
                    } else {
                        return new STAInvalidUrlException("Url is invalid. " + uriResources[i] + "is not a valid resource.");
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
    private STAInvalidUrlException validateURISemantic(String[] uriResources) {
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
                targetId = serviceRepository.getEntityService(sourceType)
                        .getEntityIdByRelatedEntity(sourceId, sourceType);
                if (targetId == null) {
                    return new STAInvalidUrlException("No Entity: " + uriResources[i] +
                            " associated with " + uriResources[i - 1] + "found!");
                }
            } else {
                // Resource is addressed by Id directly
                // e.g. /Things(1)/
                // Only checking exists as Id is already known
                targetId = targetEntity[1].replace(")", "");
                if (!serviceRepository.getEntityService(sourceType)
                        .existsEntityByRelatedEntity(sourceId, targetType, targetId)) {
                    return new STAInvalidUrlException("No Entity: " + uriResources[i] +
                            " associated with " + uriResources[i - 1] + "found!");
                }
            }

            // Store target as source for next iteration
            sourceId = targetId;
            sourceType = targetType;
        }
        // As no error is thrown the uri is valid
        return null;
    }
}