package org.n52.sta.web;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.n52.sta.api.service.DataStreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class DataStreamController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataStreamController.class);

    private final DataStreamService service;
    private final ObjectMapper objectMapper;

    public DataStreamController(DataStreamService service) {
        this.objectMapper = new ObjectMapper();
        this.service = service;
    }

    @GetMapping(path = "/DataStreams", produces = "application/json")
    public StreamingResponseBody getCollection() {
        return outputStream -> {
            JsonFactory jfactory = new JsonFactory();
            try (JsonGenerator jGenerator = jfactory.createGenerator(outputStream, JsonEncoding.UTF8)) {
                jGenerator.setCodec(objectMapper);
                jGenerator.writeStartArray();
                service.<JsonNode>getCollection(objectMapper::valueToTree).forEach(item -> {
                    try {
                        jGenerator.writeTree(item);
                    } catch (IOException e) {
                        LOGGER.error("writing object failed.", e);
                    }
                });
                jGenerator.writeEndArray();
            }
        };
    }

}
