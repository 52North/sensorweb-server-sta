package org.n52.sta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.series.db.beans.IdEntity;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.exception.STACRUDException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/v2")
public class STACrudRequestHandler<T extends IdEntity> extends STARequestUtils {

    private final EntityServiceRepository serviceRepository;

    private final ObjectMapper mapper;

    public STACrudRequestHandler(EntityServiceRepository serviceRepository, ObjectMapper mapper) {
        this.serviceRepository = serviceRepository;
        this.mapper = mapper;
    }

    @PostMapping(
            consumes = "application/json",
            value = "/{collectionName: " + COLLECTION_REGEX + "$}",
            produces = "application/json")
    @SuppressWarnings("unchecked")
    public Object handlePost(@PathVariable String collectionName,
                             @RequestBody String body) throws IOException, STACRUDException {

        Class clazz = collectionNameToClass.get(collectionName);
        return ((AbstractSensorThingsEntityService<?, T>) serviceRepository.getEntityService(collectionName))
                .create((T) mapper.readValue(body, clazz));
    }
}
