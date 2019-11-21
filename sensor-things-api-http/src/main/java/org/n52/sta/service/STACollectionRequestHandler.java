package org.n52.sta.service;

import org.n52.sta.data.serialization.ElementWithQueryOptions;
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
import java.util.List;
import java.util.Map;

@RequestMapping("/v2")
@RestController
public class STACollectionRequestHandler extends STARequestUtils {

    private final EntityServiceRepository serviceRepository;
    private final String mappingPrefix = "**/";
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
    public List<ElementWithQueryOptions> readCollectionDirect(@PathVariable String collectionName,
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

        STAInvalidUrlException ex = validateURL(request.getRequestURL(), serviceRepository, rootUrlLength);
        if (ex != null) {
            return ex;
        }

        String sourceType = entity.split("\\(")[0];
        String sourceId = entity.split("\\(")[1].replace(")", "");

        return serviceRepository.getEntityService(target)
                .getEntityCollectionByRelatedEntity(sourceId, sourceType, createQueryOptions(queryOptions));
    }
}