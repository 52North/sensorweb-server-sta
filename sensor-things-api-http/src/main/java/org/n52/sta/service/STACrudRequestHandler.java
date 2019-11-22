package org.n52.sta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.series.db.beans.IdEntity;
import org.n52.sta.serdes.model.ElementWithQueryOptions;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.exception.STACRUDException;
import org.n52.sta.exception.STAInvalidUrlException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/v2")
public class STACrudRequestHandler<T extends IdEntity> extends STARequestUtils {

    private final int rootUrlLength;
    private final EntityServiceRepository serviceRepository;
    private final ObjectMapper mapper;

    public STACrudRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                 EntityServiceRepository serviceRepository,
                                 ObjectMapper mapper) {
        this.rootUrlLength = rootUrl.length();
        this.serviceRepository = serviceRepository;
        this.mapper = mapper;
    }

    @PostMapping(
            consumes = "application/json",
            value = "/{collectionName: " + COLLECTION_REGEX + "$}",
            produces = "application/json")
    @SuppressWarnings("unchecked")
    public ElementWithQueryOptions handlePost(@PathVariable String collectionName,
                                              @RequestBody String body) throws IOException, STACRUDException {

        Class clazz = collectionNameToClass.get(collectionName);
        return ((AbstractSensorThingsEntityService<?, T>) serviceRepository.getEntityService(collectionName))
                .create((T) mapper.readValue(body, clazz));
    }

    /**
     * Matches all requests on Entities referenced directly via id
     * e.g. /Datastreams(52)
     *
     * @param collectionName  name of entity. Automatically set by Spring via @PathVariable
     * @param id  id of entity. Automatically set by Spring via @PathVariable
     * @param request full request
     */
    @PatchMapping(
            value = "**/{collectionName:" + COLLECTION_REGEX + "}{id:" + IDENTIFIER_REGEX + "$}",
            produces = "application/json"
    )
    public Object readEntityDirect(@PathVariable String collectionName,
                                   @PathVariable String id,
                                   @RequestBody String body,
                                   HttpServletRequest request) throws STACRUDException, IOException {
        STAInvalidUrlException ex = validateURL(request.getRequestURL(), serviceRepository, rootUrlLength);
        if (ex != null) {
            return ex;
        } else {
            Class clazz = collectionNameToClass.get(collectionName);
            return ((AbstractSensorThingsEntityService<?, T>) serviceRepository.getEntityService(collectionName))
                    .update(id, (T) mapper.readValue(body, clazz), HttpMethod.PATCH);
        }
    }
}
